const { logger } = require('./error-handler')
const { errors } = require('./response-utils')
const db = require('../db')

/**
 * Database utility functions for common operations
 */
class DatabaseUtils {
  /**
   * Execute a select query with AND conditions
   * @param {string} table - Table name
   * @param {Array} columns - Columns to select (null for all)
   * @param {Object} where - Where conditions
   * @returns {Promise<Array>}
   */
  static async selectWithAnd(table, columns = null, where = {}) {
    try {
      let query = db.main(table)
      if (columns) {
        query = query.select(columns)
      }
      if (Object.keys(where).length > 0) {
        query = query.where(where)
      }
      return await query
    } catch (error) {
      logger.error('Database select error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Execute an insert query
   * @param {string} table - Table name
   * @param {Object} data - Data to insert
   * @returns {Promise<Array>} - Array of inserted IDs
   */
  static async insert(table, data) {
    try {
      return await db.main(table).insert(data)
    } catch (error) {
      logger.error('Database insert error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Execute an update query
   * @param {string} table - Table name
   * @param {Object} data - Data to update
   * @param {Object} where - Where conditions
   * @returns {Promise<number>} - Number of affected rows
   */
  static async update(table, data, where) {
    try {
      return await db.main(table).where(where).update(data)
    } catch (error) {
      logger.error('Database update error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Execute a delete query
   * @param {string} table - Table name
   * @param {Object} where - Where conditions
   * @returns {Promise<number>} - Number of affected rows
   */
  static async delete(table, where) {
    try {
      return await db.main(table).where(where).delete()
    } catch (error) {
      logger.error('Database delete error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Execute a join query
   * @param {string} table1 - First table
   * @param {string} table2 - Second table
   * @param {Array} columns - Columns to select (null for all)
   * @param {string} joinColumn1 - Join column from first table
   * @param {string} joinColumn2 - Join column from second table
   * @param {Object} where - Where conditions
   * @returns {Promise<Array>}
   */
  static async join(table1, table2, columns = null, joinColumn1, joinColumn2, where = {}) {
    try {
      let query = db.main(table1)
        .join(table2, `${table1}.${joinColumn1}`, '=', `${table2}.${joinColumn2}`)
      
      if (columns) {
        query = query.select(columns)
      }
      
      if (Object.keys(where).length > 0) {
        query = query.where(where)
      }

      return await query
    } catch (error) {
      logger.error('Database join error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Execute a batch insert query
   * @param {string} table - Table name
   * @param {Array<string>} columns - Column names
   * @param {Array<Array>} values - Array of value arrays
   * @returns {Promise<Array>} - Array of inserted IDs
   */
  static async batchInsert(table, columns, values) {
    try {
      const data = values.map(valueArray => {
        const obj = {}
        columns.forEach((col, index) => {
          obj[col] = valueArray[index]
        })
        return obj
      })

      return await db.main(table).insert(data)
    } catch (error) {
      logger.error('Database batch insert error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Execute a raw query
   * @param {string} query - Raw SQL query
   * @param {Array} bindings - Query bindings
   * @returns {Promise<Array>}
   */
  static async raw(query, bindings = []) {
    try {
      const result = await db.main.raw(query, bindings)
      return result[0]
    } catch (error) {
      logger.error('Database raw query error:', error)
      throw errors.internalServer(true)
    }
  }

  /**
   * Begin a transaction
   * @returns {Promise<Transaction>}
   */
  static async beginTransaction() {
    return await db.main.transaction()
  }

  /**
   * Execute a query within a transaction
   * @param {Function} callback - Callback function that receives the transaction object
   * @returns {Promise<any>}
   */
  static async transaction(callback) {
    try {
      return await db.main.transaction(callback)
    } catch (error) {
      logger.error('Database transaction error:', error)
      throw errors.internalServer(true)
    }
  }
}

module.exports = DatabaseUtils
