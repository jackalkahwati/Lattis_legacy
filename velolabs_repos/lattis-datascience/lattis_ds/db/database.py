from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from lattis_ds.environment import env_var
import sqlalchemy
import logging
import itertools
import os
import sqlparse
import pandas as pd
from lattis_ds.db import utils
import psycopg2

try:
    # Python  compatibility
    basestring
except NameError:
    basestring = str



docker_credentials = dict(
    dialect="postgresql",
    user='postgres',
    password='docker',
    host='127.0.0.1',
    port='5433',
    database='postgres')

# Use environment vars to customize postgresql credentials
pg_credentials = dict(
    dialect="postgresql",
    user=os.environ.get("PG_USERNAME", docker_credentials['user']),
    password=os.environ.get("PG_PASSWORD", docker_credentials['password']),
    host=os.environ.get("PG_HOST", docker_credentials['host']),
    port=os.environ.get("PG_PORT", docker_credentials['port']),
    database=os.environ.get("DB", docker_credentials['database'])
)

main_mysql_credentials = dict(
    dialect="mysql",
    user=env_var['MAIN_DB_USERNAME'],
    password=env_var['MAIN_DB_PASS'],
    host=env_var['MAIN_DB_HOST'],
    port=3306,
    database=env_var['MAIN_DB']
)

user_mysql_credentials = dict(
    dialect="mysql",
    user=env_var['USER_DB_USERNAME'],
    password=env_var['USER_DB_PASS'],
    host=env_var['USER_DB_HOST'],
    port=3306,
    database=env_var['USER_DB']
)

# TODO postgres DB cleanups
# SQLALCHEMY_DATABASE_URL = '{dialect}+psycopg2://{user}:{password}@{host}:{port}/{database}'.format(**pg_credentials)


# engine = create_engine(SQLALCHEMY_DATABASE_URL)
# SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base = declarative_base()


class MYSQL:
    """ class for running queries on a MySQL db
    See query function for more info"""
    def __init__(self, credentials=main_mysql_credentials):
        self.con = self.get_con(credentials=credentials)

    def get_con(self, style='sqlalchemy', credentials=main_mysql_credentials):
        """open a connection to a database by passing credentials"""
        if style == 'sqlalchemy':
            con = sqlalchemy.create_engine(
                '{dialect}+mysqldb://{user}:{password}@{host}:{port}/{database}?charset=utf8mb4'.format(**credentials))
        elif style == 'psycopg2':
            fields = ['user', 'password', 'host', 'port', 'database']
            con = psycopg2.connect(**{k: v for k, v in credentials.items() if k in fields})
        else:
            raise NotImplementedError()
        return con

    @staticmethod
    def _sqlfile_to_statements(sql):
        """
        Takes a SQL string containing 0 or more statements and returns a
        list of individual statements as strings. Comments and
        empty statements are ignored.
        """
        statements = (sqlparse.format(stmt, strip_comments=True).strip() for stmt in sqlparse.split(sql))
        return [stmt for stmt in statements if stmt]

    def query(
            self,
            query_val,
            local_filename=None,
            local_write_options=None,
            session=None,
            verbose=False,
            apply_macros=True,
            **kwds_read_sql):
        """
        query a SQL database and return a pandas.DataFrame (if selecting data). examples:

        db.query('select * from tablename limit 100')
        db.query('~/code_path/query_filename.sql')
        db.query([
            'CREATE TEMPORARY TABLE temp_table AS SELECT some_complex_logic_here',
            'SELECT * FROM temp_table WHERE filter_logic'
            ])
        db.query([
            '~/code_path/query_create_staging_table.sql',
            'SELECT * FROM temp_table WHERE filter_logic'
            ])

        Parameters
        -----------
        query_val :
            query to send to db. can be parameterized by kwds_read_sql['params'].
            can also be a filename ending in .sql
            or a list of query strings or filenames
        local_filename :
            if specified, will write data to this file
        local_write_options :
            for use with csv files, passed to DataFrame.to_csv()
        session :
            MYSQL session
        verbose :
            verbose
        **kwds_read_sql :
            will be passed to pd.read_sql()

        """

        if local_write_options is None:
            local_write_options = {}

        # get a flat list of query statements
        if isinstance(query_val, basestring):
            query_val = [query_val]
        query_parts = itertools.chain.from_iterable(map(utils.get_query_string, query_val))
        #if apply_macros:
        #    query_parts = map(pre_process_query, query_parts)

        query_parts = filter(utils.has_sql_content, map(utils.clean_sql, query_parts))

        write_data = local_filename is not None
        file_format = None
        if write_data:
            file_format = local_filename.split('.')[-1]

            if file_format not in ['csv']:
                raise NotImplementedError('dont recognize file format, maybe you can implement it ?')

        if local_filename:
            logging.info('running query to create {}'.format(local_filename))

        # handle parameters
        params = kwds_read_sql.pop('params', {})

        if session is None:
            # wrap everything in session to support temporary tables
            with self.con.begin() as session:
                df = self._query(
                    query_parts,
                    session,
                    params,
                    verbose,
                    **kwds_read_sql
                )
        else:
            df = self._query(
                query_parts,
                session,
                params,
                verbose,
                **kwds_read_sql
            )

        if write_data and file_format == 'csv':
            df.to_csv(local_filename, index='index_col' in kwds_read_sql, **local_write_options)
        return df

    def _query(self, query_parts, session, params, verbose, **kwds_read_sql):
        df = None
        for query_str in query_parts:
            is_tbl_transaction = utils.is_table_transaction(query_str)
            if verbose:
                logging.info('running:\n{}\n'.format(query_str))
            try:
                if is_tbl_transaction:
                    if verbose:
                        logging.info('running transaction: {}'.format(query_str.splitlines()[0]))
                    for statement in self._sqlfile_to_statements(query_str):
                        session.execute(statement, params)
                else:
                    df = pd.read_sql(
                        sql=query_str,
                        con=session,
                        params=params,
                        **kwds_read_sql)
            except (psycopg2.ProgrammingError, sqlalchemy.exc.ProgrammingError) as e:
                raise ValueError(e)
            except psycopg2.OperationalError:
                logging.error('Is your VPN on?')
                raise
        return df
