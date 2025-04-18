import pymysql.cursors
from config import DatabaseConfig


class DatabaseHandler:
    def __init__(self):
        self.db_config = DatabaseConfig()
        self.connection = None

    # Public Methods
    def close_connection(self):
        if self.connection:
            self.connection.close()
            self.connection = None
        return None

    def select(self, table, columns=None, where_params=None, where_values=None, filter_with_and=True):
        self._setup_connection()
        query = self._select_query(
            table=table,
            columns=columns,
            where_params=where_params,
            where_values=where_values,
            filter_with_and=filter_with_and
        )
        return self._execute_query(query, 'select')

    def insert(self, table, columns, values):
        self._setup_connection()
        query = self._insert_query(table=table, columns=columns, values=values)
        return self._execute_query(query, 'insert')

    def update(self, table, update_dict, where_params, where_values, filter_with_and=True):
        self._setup_connection()
        query = self._update_query(
            table=table,
            update_dict=update_dict,
            where_params=where_params,
            where_values=where_values,
            filter_with_and=filter_with_and
        )
        return self._execute_query(query, 'update')

    def join(self, table1, table2, table1_column, table2_column, join_value):
        self._setup_connection()
        query = self._join_query(table1, table2, table1_column, table2_column, join_value)
        return self._execute_query(query, 'join')

    def convert_dictionary_to_columns_and_values(self, dictionary):
        columns = list(dictionary.keys())
        return {'columns': columns, 'values': [dictionary[column] for column in columns]}

    def convert_list_of_dictionaries_to_columns_and_values(self, list_of_dictionaries):
        return {
            'columns': self.convert_dictionary_to_columns_and_values(list_of_dictionaries[0])['columns'],
            'values': [self.convert_dictionary_to_columns_and_values(dictionary)['values']
                       for dictionary in list_of_dictionaries]
        }

    # Private Methods
    def _execute_query(self, query, query_type):
        # TODO: The following 'with' statement should be wrapped in a try-exepct block
        with self.connection.cursor() as cursor:
            cursor.execute(query)
            self.connection.commit()
            if query_type == 'select' or query_type == 'join':
                result = cursor.fetchall()
            elif query_type == 'insert':
                result = True
            else:
                result = True
        return result

    def _value_query(self, values):
        query = '('
        counter = 0
        for value in values:
            query += self.connection.escape(value)
            if counter == len(values) - 1:
                query += ')'
            else:
                query += ','
            counter += 1
        return query

    def _setup_connection(self):
        if not self.connection:
            self.connection = pymysql.connect(
            host=self.db_config.host,
            user=self.db_config.user,
            password=self.db_config.password,
            db=self.db_config.database,
            charset=self.db_config.charset,
            cursorclass=pymysql.cursors.DictCursor
        )
        return None

    def _insert_query(self, table, columns, values):
        query = 'INSERT INTO `' + table + '` ('
        counter = 0
        for column in columns:
            query += '`' + column + '`'
            if counter == len(columns) - 1:
                query += ') VALUES'
            else:
                query += ','
            counter += 1
        is_single = True
        try:
            # Handles the case where multiple objects are inserted.
            if isinstance(values[0], list):
                is_single = False
                counter = 0
                for value in values:
                    query += self._value_query(value)
                    if counter != len(values) - 1:
                        query += ','
                    counter += 1
        except IndexError:
            pass
        if is_single:
            query += self._value_query(values)
        return query

    def _select_query(self, table, columns, where_params, where_values, filter_with_and):
        query = 'SELECT '
        counter = 0
        if columns:
            for column in columns:
                query += '`' + column +'`' + ('' if counter == len(columns) - 1 else ',')
                counter += 1
        else:
            query += '*'
        query += ' FROM `' + table + '` '
        if where_params and where_values:
            query += self._where_clause(params=where_params, values=where_values, filter_with_and=filter_with_and)
        return query

    def _update_query(self, table, update_dict, where_params, where_values, filter_with_and):
        query = 'UPDATE `' + table + '` SET '
        counter = 0
        for key, value in update_dict.items():
            query += '`' + key + '`=' + self.connection.escape(value)
            if counter < len(update_dict) - 1:
                query += ','
            counter += 1
        query += ' ' + self._where_clause(params=where_params, values=where_values, filter_with_and=filter_with_and)
        return query

    def _join_query(self, table1, table2, table1_column, table2_column, join_value):
        return 'SELECT * FROM `%s` JOIN `%s` ON `%s`.`%s`=`%s`.`%s` AND `%s`.`%s`=%s' % (
            table1,
            table2,
            table1,
            table1_column,
            table1,
            table1_column,
            self.connection.escape(join_value)
        )

    def _where_clause(self, params, values, filter_with_and):
        counter = 0
        query = 'WHERE '
        for i in range(0, len(params)):
            key = params[i]
            value = values[i]
            query += '`' + key + '`=' + self.connection.escape(value)
            if counter < len(params) - 1:
                query += ' AND ' if filter_with_and else ' OR '
            counter += 1
        return query
