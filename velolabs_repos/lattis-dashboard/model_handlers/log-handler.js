'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const dbConstants = platform.dbConstants

/**
 * Used to insert ticket log
 */

const insertTicketLog = (ticketId, logData, callback) => {
  const formData = logData.data
  const columnsAndValues = {
    ticket_id: ticketId,
    fleet_id: formData.fleet_id,
    operator_id: formData.operator_id,
    raw_data: JSON.stringify(formData),
    message: logData.message,
    log_type: logData.log_type,
    log_section: logData.log_section,
    logged_user: formData.operator_id
  }
  let ticketsQuery = queryCreator.insertSingle(dbConstants.tables.tickets_log, columnsAndValues)
  sqlPool.makeQuery(ticketsQuery, callback)
}

module.exports = Object.assign(module.exports, {
  insertTicketLog: insertTicketLog
})
