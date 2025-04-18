'use strict';

let _ = require('underscore');
let mysql = require('mysql');

let querySubString = function(values, deliminator, terminator, shouldEscape) {
    let subQuery = '';
    let counter = 0;
    if (_.isArray(values)) {
        _.each(values, function(value) {
            value = shouldEscape ? mysql.escape(value) : value;
            _.contains(value, '`') ? subQuery += value + (counter === values.length - 1 ? terminator : deliminator) :
                subQuery += '`' + value + '`' + (counter === values.length - 1 ? terminator : deliminator)
            counter++;
        });
    } else {
        _.each(values, function(value, key) {
            subQuery += '`' + key + '`=' + mysql.escape(value) +
                + (counter === values.length - 1 ? terminator : deliminator);
            counter++;
        });
    }

    return subQuery;
};

let aggregateQuerySubString = function(aggregateColumns, values,
                                       deliminator, terminator, shouldEscape) {
    let subQuery = '';
    let counter = 0;
    if (_.isArray(values)) {
        _.each(values, function(value) {
            let column = value.split(':');
            let oldColumn = column[0];
            let newColumn = column[1];
            subQuery += aggregateColumns + '('+ (shouldEscape ? mysql.escape(oldColumn): (oldColumn)?
                    oldColumn: ' ') + ')' +
                (newColumn ? ' AS ' + newColumn : ' ') +
                (counter === values.length - 1 ? terminator : deliminator);
            counter++;
        });
    } else {
        let keysCount = (_.keys(values)).length - 1;
        _.each(values, function(value, key) {
            let column = value.split(':');
            let oldColumn = column[0];
            let newColumn = column[1];
            subQuery += aggregateColumns + '('+ (shouldEscape ? mysql.escape(oldColumn): (oldColumn)?
                    oldColumn: ' ') + ')'+
                (newColumn ? ' AS ' + newColumn : ' ') +
                (counter === keysCount ? terminator : deliminator);
            counter++;
        });
    }

    return subQuery;
};

let valuesQuerySubString = function(values, deliminator, terminator) {
    let query = '';
    let counter = 0;
    _.each(values, function(value) {
        if(_.isArray(value)) {
            query = valuesQuerySubString(value, deliminator, terminator)
                + (counter === values.length - 1 ? '' : ',');
        } else {
            query += mysql.escape(value) + (counter === values.length - 1 ? terminator : deliminator);
        }

        counter++;
    });

    return query;
};

let whereEqualQuery = function(columnsAndValues, deliminator) {
    let query = '';
    let counter = 0;
    let length = _.keys(columnsAndValues).length;
    _.each(columnsAndValues, function(value, column) {
        query += '`' + column + '`' + '=' + mysql.escape(value) + (counter === length - 1 ? '' : deliminator);
        counter++;
    });

    return query;
};

let groupByColumnsQuery = function(columnsAndValues, deliminator) {
    let query = '';
    let counter = 0;
    let length = _.keys(columnsAndValues).length;
    _.each(columnsAndValues, function(value, column) {
        query +=  value ;
        query +=  (counter === length - 1 ? '' : ' , ');
        counter++;
    });

    return query;
};

let tablesQuerySubString = function(columnsAndValues, deliminator, table, comparisonColumn, separator) {
    let query = '';
    _.each(columnsAndValues, function(value) {
        query += separator + value + deliminator + table +
            '.' + comparisonColumn + '=' + value + '.' + comparisonColumn;
    });
    return query;
};

let whereRangeQuery = function(columnsAndValues, greaterThanEqualTo, lesserThanEqualTo, clause) {
    let query = '';
    let counter = 0;
    let length = _.keys(columnsAndValues).length;
    let index = 0;
    _.each(columnsAndValues, function (value, key) {
        let table = addBackTick(value.table);
        _.each(value.columns, function(value, key) {
            query += (counter === 0 ? ' ' : clause) + table + '.' + value.column + greaterThanEqualTo + value.start + clause +
                table + '.' + value.column + lesserThanEqualTo + value.end;
            counter++;
        });

    });
    return query;
};


let addAggregateFunctions = function(query, aggregateColumns) {
    let lastChar;
    if (aggregateColumns) {
        if (aggregateColumns.sum) {
            lastChar = query.charAt(query.length - 1);
            query += lastChar === ',' ? '' : ',';
            query += !aggregateColumns.sum.columns ? '' : aggregateQuerySubString('SUM', aggregateColumns.sum.columns, ',', ' ', false);
        }
        if (aggregateColumns.min) {
            lastChar = query.charAt(query.length - 1);
            query += lastChar === ',' ? '' : ',';
            query += !aggregateColumns.min.columns ? '' : aggregateQuerySubString('MIN', aggregateColumns.min.columns, ',', ' ', false);
        }
        if (aggregateColumns.max) {
            lastChar = query.charAt(query.length - 1);
            query += lastChar === ',' ? '' : ',';
            query += !aggregateColumns.max.columns  ? '' : aggregateQuerySubString('MAX', aggregateColumns.max.columns, ',', ' ', false);
        }
        if (aggregateColumns.avg) {
            lastChar = query.charAt(query.length - 1);
            query += lastChar === ',' ? '' : ',';
            query += !aggregateColumns.avg.columns ? '' : aggregateQuerySubString('AVG', aggregateColumns.avg.columns, ',', ' ', false);
        }
        if (aggregateColumns.count) {
            lastChar = query.charAt(query.length - 1);
            query += lastChar === ',' ? '' : ',';
            query += !_.isArray(aggregateColumns.count.columns) ? ' COUNT(*) '+ ' AS ' + aggregateColumns.count.columns + ' ' : aggregateQuerySubString(' COUNT ', aggregateColumns.count.columns, ',', ' ', false);
        }
    }
    return query;
};

let addBackTick = function(value) {
    return '`' + value + '`';
};

module.exports = {
    /**
     * Select query where comparison is done by unique columns and values.
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     *  @param {object} betweenColumnValues Values and column names
     *  to be used in comparison
     *    For Example:-    [
     *    {
     *          table:'bikes',
                    columns:
                    [{
                        column:  'date_created',
                    start:16952770,
                    end: 16982770
                    }]
            }]
     *  @param {array} groupByColumns list of column names to be used in comparison
     * @param {object} aggregateColumns Values and aggregateColumns
     *     names to filter query by
     *
     *     For example :
     *     { avg:{columns: ['bike_id : bikes','fleet_id : fleets']},sum:
     *     {
               columns:['admin_id : admin']},
                 min:{
                columns:['fleet_id : fleet']},
                 max:{
                columns:['admin_id: admin']},
                 count:{
                columns:['admin_id: admin1']
     *           }
     *           }
     *
     * @returns {string}
     */
    selectWithAnd: function(table, columnsToSelect, comparisonColumnsAndValues, betweenColumnValues,
                            groupByColumns, aggregateColumns) {
        let query = 'SELECT ';

        query += (!columnsToSelect || columnsToSelect.length === 0) ? '* ' :
            querySubString(columnsToSelect, ',', ' ', false);

        query = addAggregateFunctions(query, aggregateColumns);

        query += 'FROM `' + table + '`';

        let lastString = ' AND ';

        query += ((comparisonColumnsAndValues)? (' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' AND ')) : (lastString = ' WHERE ', ' ') );

        query += ((betweenColumnValues)? lastString + whereRangeQuery(betweenColumnValues, ' >= ', ' <= ', ' AND '): ' ');

        query += ((groupByColumns)? ' GROUP BY '+ groupByColumnsQuery(groupByColumns): ' ');

        return query;
    },


    /**
     * Aggregate Sum Query for Select.
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     * @returns {string}
     */
    queryBySum: function(table, columnsToSelect, comparisonColumnsAndValues) {
        let query = 'SELECT ';
        query += aggregateQuerySubString('SUM', columnsToSelect, ',', ' ', false);
        query += 'FROM `' + table + '`';

        if (comparisonColumnsAndValues) {
            query += ' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' AND ');
        }
        return query;
    },

    /**
     * Aggregate Average Query for Select.
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     * @returns {string}
     */
    queryByAverage: function(table, columnsToSelect, comparisonColumnsAndValues) {
        let query = 'SELECT ';
        query += aggregateQuerySubString('AVG', columnsToSelect, ',', ' ', false);
        query += 'FROM `' + table + '`';

        if (comparisonColumnsAndValues) {
            query += ' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' AND ');
        }
        return query;
    },

    /**
     * Aggregate Count Query for Select.
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     * @returns {string}
     */
    queryByCount: function(table, columnsToSelect, comparisonColumnsAndValues) {
        let query = 'SELECT ';
        query += (!columnsToSelect || columnsToSelect.length === 0) ? 'COUNT(*)':aggregateQuerySubString('COUNT', columnsToSelect, ',', ' ', false);
        query += 'FROM `' + table + '`';

        if (comparisonColumnsAndValues) {
            query += ' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' AND ');
        }
        return query;
    },

    /**
     * Aggregate Min Query for Select.
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     * @returns {string}
     */
    queryByMin: function(table, columnsToSelect, comparisonColumnsAndValues) {
        let query = 'SELECT ';
        query += aggregateQuerySubString('MIN', columnsToSelect, ',', ' ', false);
        query += 'FROM `' + table + '`';

        if (comparisonColumnsAndValues) {
            query += ' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' AND ');
        }
        return query;
    },


    /**
     * Aggregate Max Query for Select.
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     * @returns {string}
     */
    queryByMax: function(table, columnsToSelect, comparisonColumnsAndValues) {
        let query = 'SELECT ';
        query += aggregateQuerySubString('MAX', columnsToSelect, ',', ' ', false);
        query += 'FROM `' + table + '`';

        if (comparisonColumnsAndValues) {
            query += ' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' AND ');
        }
        return query;
    },

    /**
     * Select query where comparison is done by a single column name
     * that can have multiple values
     *
     * @param {string} table Name of table to select from
     * @param {array} columnsToSelect Columns to select
     * @param {object} comparisonColumnsAndValues Values and column
     *     names to filter query by
     *  @param {object} betweenColumnValues Values and column names
     *  to be used in comparison
     *    For Example:-    [
     *    {
     *          table:'bikes',
                    columns:
                    [{
                        column:  'date_created',
                    start:16952770,
                    end: 16982770
                    }]
            }]
     *  @param {array} groupByColumns list of column names to be used in comparison
     * @param {object} aggregateColumns Values and aggregateColumns
     *     names to filter query by
     *
     *     For example :
     *     { avg:{columns: ['bike_id : bikes','fleet_id : fleets']},sum:
     *     {
               columns:['admin_id : admin']},
                 min:{
                columns:['fleet_id : fleet']},
                 max:{
                columns:['admin_id: admin']},
                 count:{
                columns:['admin_id: admin1']
     *           }
     *           }
     *
     * @returns {string}
     */
    selectWithOr: function(table, columnsToSelect, comparisonColumnsAndValues,
                           betweenColumnValues, groupByColumns, aggregateColumns) {
        let query = 'SELECT '
        query += (!columnsToSelect || columnsToSelect.length === 0) ? '* ' :
            querySubString(columnsToSelect, ',', ' ', false);
        query = addAggregateFunctions(query, aggregateColumns);
        query += 'FROM `' + table + '`';

        let lastString = ' AND ';

        query += ((comparisonColumnsAndValues)? (' WHERE ' + whereEqualQuery(comparisonColumnsAndValues, ' OR ')) : (lastString = ' WHERE ', ' ') );

        query += ((betweenColumnValues)? lastString + whereRangeQuery(betweenColumnValues, ' >= ', ' <= ', ' AND '): ' ');

        query += ((groupByColumns)? ' GROUP BY '+ groupByColumnsQuery(groupByColumns): ' ');

        return query;
    },

    /*
     * @param {String} Name of table to insert into
     * @param {object} Column and Values to select
     *
     * @return {String} Insert query
     */
    insertSingle: function(table, columnsAndValues) {
        let columns = _.keys(columnsAndValues);
        let values = [];
        _.each(columns, function(column) {
            values.push(columnsAndValues[column]);
        });

        return 'INSERT INTO `' + table + '`(' + querySubString(columns, ',', ')', false) +
            ' VALUES (' + valuesQuerySubString(values, ',', '') + ')';
    },

    /*
     * @param {String} Name of table to insert into
     * @param {Array} Names of columns to insert
     * @param {Array} Array of arrays. Each sub-array holds
     * the column values of one of the insert objects
     *
     * @return {String} Insert query
     */
    insertMultiple: function(table, columns, values) {
        let query = 'INSERT INTO `' + table + '`(' + querySubString(columns, ',', ')', false) + ' VALUES ';
        let counter = 0;
        _.each (values, function(subValue) {
            query += '(' + valuesQuerySubString(subValue, ',', '') + ')' +
                (counter === values.length - 1 ? '' : ',');
            counter++;
        });

        return query;
    },

    /**
     * Updates a single record
     *
     * @param {string} table Table to update
     * @param {Object} columnsAndValues Columns and values to update
     * @param {Object} targetColumnsAndValues ColumnsAndValues to identify the update record
     *
     * @returns {string}
     */
    updateSingle: function(table, columnsAndValues, targetColumnsAndValues) {
        let columns = _.keys(columnsAndValues);
        let values = [];
        _.each(columns, function(column) {
            values.push(columnsAndValues[column]);
        });

        return 'UPDATE `' + table + '` SET ' + whereEqualQuery(columnsAndValues, ',') +
            ' WHERE ' + whereEqualQuery(targetColumnsAndValues, ' AND ');
    },

    /**
     * Create a query string for a join statement
     *
     * @param {string} table1 Name of the first table to join
     * @param {array} table2 Name of the second table to join
     * @param {array} table1ColumnsToSelect Names of the colums for the select statement
     * @param {string} comparisonColumn Column name to run the comparison on
     * @param {array} betweenColumnValues for list of in range columns for the select statement
     * The Format is array { table : 'tableToSelect',columns : [{column:'columnToSelect',start:'Betweenfrom',end:'BetweenTo'}]
     *    For Example:-    [{
                    table:'bikes',
                    columns:[{
                    column:  'date_created',
                    start:16952770,
                    end: 16982770
                    }]
            }]
     * @param {array} groupByColumns are columns to be grouped for the select statement
     * @param {object} aggregateColumns Values are aggregateColumns names to filter query with AS clause
     * The Format is object : aggregatefunction:{columns : ['columnToSelect : ASClauseName' ]};
     *     For example :
     {
     avg:{columns: ['bike_id : bikes','fleet_id : fleets']},
     sum:{columns:['admin_id : admin']},
     min:{columns:['fleet_id : fleet']},
     max:{columns:['admin_id: admin']},
     count:{columns:['admin_id: admin1']},
     }
     * @returns {string}
     */
    join: function(table1, table2, table1ColumnsToSelect, comparisonColumn, betweenColumnValues, groupByColumns,
                   aggregateColumns) {

        _.each(table1ColumnsToSelect, function(filterValue,index) {
            table1ColumnsToSelect[index] = addBackTick(table1)+'.'+filterValue;
        });

        return this.selectWithAnd(table1, table1ColumnsToSelect, null, null, null, aggregateColumns) +
            tablesQuerySubString(table2, ' ON ', table1, comparisonColumn, ' JOIN ') + ' ' +
            ((betweenColumnValues)? ' WHERE '+ whereRangeQuery(betweenColumnValues, ' >= ', ' <= ', ' AND ') : ' ') +
            ((groupByColumns)?' GROUP BY '+ groupByColumnsQuery(groupByColumns) : ' ');

    },

    /*
     * Creates a join statement with a where clause for a single
     * column in a table.
     *
     * @param {string} table1 Name of the first table to join
     * @param {string} table2 Name of the second table to join
     * @param {array} table1ColumnsToSelect Names of the columns for the select statement
     * @param {string} comparisonColumn Column name to run the comparison on
     * @param {string} filterColumn Column to filter with in where clause
     * @param {array} filterValues Values for filtering in where clause
     * @param {array} betweenColumnValues for list of in range columns for the select statement
     * The Format is array { table : 'tableToSelect',columns : [{column:'columnToSelect',start:'Betweenfrom',end:'BetweenTo'}]
     *    For Example:-    [{
     table:'bikes',
     columns:[{
     column:  'date_created',
     start:16952770,
     end: 16982770
     }]
     }]
     * @param {array} groupByColumns are columns to be grouped for the select statement
     * @param {object} aggregateColumns Values are aggregateColumns names to filter query with AS clause
     * The Format is object : aggregatefunction:{columns : ['columnToSelect : ASClauseName' ]};
     *     For example :
     {
     avg:{columns: ['bike_id : bikes','fleet_id : fleets']},
     sum:{columns:['admin_id : admin']},
     min:{columns:['fleet_id : fleet']},
     max:{columns:['admin_id: admin']},
     count:{columns:['admin_id: admin1']},
     }
     * @returns {string}
     */
    joinWithOr: function(table1, table2, table1ColumnsToSelect, comparisonColumn, filterColumn, filterValues,
                         betweenColumnValues, groupByColumns, aggregateColumns) {
        let counter = 0;
        let equalClause = addBackTick(table1) + '.' + addBackTick(filterColumn) + '=';
        let query = this.join(table1, table2, table1ColumnsToSelect, comparisonColumn, betweenColumnValues, null, aggregateColumns) +
            ((aggregateColumns && filterValues)? ' OR ': (filterValues)? ' WHERE ' : ' ');
        _.each(filterValues, function(filterValue) {
            query += equalClause + mysql.escape(filterValue) +
                (counter === filterValues.length - 1 ? '' : ' OR ');
            counter++;
        });
        query +=((groupByColumns)?' GROUP BY '+ groupByColumnsQuery(groupByColumns) : ' ');
        return query;
    },

    /**
     * Creates a join statement with a where and 'AND' clause for multiple
     * columns in a table.
     *
     * @param {string} table1 Name of the first table to select during join
     * @param {array}  list of tables to join
     * @param {array} table1ColumnsToSelect Names of the colums for the select statement
     * @param {array} betweenColumnValues for list of in range columns for the select statement
     * The Format is array { table : 'tableToSelect',columns : [{column:'columnToSelect',start:'Betweenfrom',end:'BetweenTo'}]
     *    For Example:-    [{
                    table:'bikes',
                    columns:[{
                    column:  'date_created',
                    start:16952770,
                    end: 16982770
                    }]
            }]
     * @param {array} groupByColumns are columns to be grouped for the select statement
     * @param {object} aggregateColumns Values are aggregateColumns names to filter query with AS clause
     * The Format is object : aggregatefunction:{columns : ['columnToSelect : ASClauseName' ]};
     *     For example :
     {
     avg:{columns: ['bike_id : bikes','fleet_id : fleets']},
     sum:{columns:['admin_id : admin']},
     min:{columns:['fleet_id : fleet']},
     max:{columns:['admin_id: admin']},
     count:{columns:['admin_id: admin1']},
     }
     * @param {string} comparisonColumn Column name to run the comparison on
     * @param {string} filterColumn Column to filter with in where clause
     * @param {array} filterValues Values for filtering in where clause
     * @returns {string}
     */

    joinWithAnd: function(table1, table2, table1ColumnsToSelect, betweenColumnValues, groupByColumns, aggregateColumns, comparisonColumn, filterColumn, filterValues) {
        let counter = 0;
        let equalClause = addBackTick(table1) + '.' + addBackTick(filterColumn) + '=';
        let query = this.join(table1, table2, table1ColumnsToSelect, comparisonColumn, betweenColumnValues, null, aggregateColumns) +
            ((aggregateColumns && filterValues)? ' AND ': (filterValues)? ' WHERE '
                : ' ');

        if(filterValues) {
            
            _.each(filterValues, function(filterValue) {
                query += equalClause + mysql.escape(filterValue) +
                    (counter === filterValues.length - 1 ? '' : ' AND ');
                counter++;
            });

        }
        query +=((groupByColumns)?' GROUP BY '+ groupByColumnsQuery(groupByColumns) : ' ');
        return query;
    }
};
