'use strict'

const platform = require('@velo-labs/platform')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const logger = platform.logger
const errors = platform.errors
const _ = require('underscore')
const dbConstants = platform.dbConstants
const ticketConstants = require('./../../constants/ticket-constants')
const {configureSentry} = require('../../utils/configureSentry')

const Sentry = configureSentry()

/**
 * This methods will create insert/update with given columnAndValues.
 *
 * @param {Object} requestParam - Parameters to insert/update ticket comment.
 * @param {Function} callback
 */
const saveComment = (requestParam, callback) => {
  if (validateFormData(requestParam)) {
    if (_.has(requestParam, 'comment_id') && requestParam.comment_id !== '') {
      const conditionColumns = {
        ticket_id: requestParam.ticket_id,
        comment_id: requestParam.comment_id,
        created_by: requestParam.created_by,
        user_type: requestParam.user_type
      }
      _updateTicketComments({message: requestParam.message}, conditionColumns, function (error) {
        if (error) {
          Sentry.captureException(error, {requestParam})
          logger('Error: Updating ticket comment with ticket_id and comment_id:', _.uniq(_.pluck(requestParam, 'comment_id')), error)
          callback(errors.internalServer(false))
        } else {
          callback(errors.noError())
        }
      })
    } else {
      const commentColumns = {
        ticket_id: requestParam.ticket_id,
        message: requestParam.message,
        created_by: requestParam.created_by,
        user_type: requestParam.user_type,
        status: ticketConstants.table_status.activated
      }
      _insertComment(commentColumns, function (error) {
        if (error) {
          Sentry.captureException(error, {commentColumns})
          logger('Error: Adding ticket comment. Unable to insert ticket comment record:', _.uniq(_.pluck(requestParam, 'ticket_id')), error)
          callback(errors.internalServer(false))
        } else {
          callback(errors.noError())
        }
      })
    }
  } else {
    callback(errors.internalServer(false))
  }
}

/**
 * This methods will delete/deactivate ticket comment with given columnAndValues.
 *
 * @param {Object} requestParam - Parameters to update the ticket.
 * @param {Function} callback
 */
const deleteComment = (requestParam, callback) => {
  if (_.has(requestParam, 'ticket_id') && _.has(requestParam, 'comment_id')) {
    const conditionColumns = {
      ticket_id: requestParam.ticket_id,
      comment_id: requestParam.comment_id,
      created_by: requestParam.created_by,
      user_type: requestParam.user_type
    }
    _updateTicketComments({status: ticketConstants.table_status.deactivated}, conditionColumns, function (error) {
      if (error) {
        Sentry.captureException(error, {requestParam})
        logger('Error: Deactivate(Delete) ticket comment status with ticket_id and comment_id:', _.uniq(_.pluck(requestParam, 'comment_id')), error)
        callback(errors.internalServer(false))
      } else {
        callback(errors.noError())
      }
    })
  } else {
    callback(errors.internalServer(false))
  }
}

/**
 * This methods validates a new ticket comment form object.
 *
 * @param {Object} formData
 * @returns {boolean}
 */
const validateFormData = (formData) => {
  return (
    (_.has(formData, 'ticket_id') && _.has(formData, 'message')) ||
    (_.has(formData, 'ticket_id') && _.has(formData, 'message') && _.has(formData, 'comment_id'))
  )
}

/**
 * This methods will insert ticket with given columnAndValues.
 *
 * @param {Object} columnsAndValues - Parameters to insert ticket comment.
 * @param {Function} callback
 */
const _insertComment = (columnsAndValues, callback) => {
  let ticketsQuery = queryCreator.insertSingle(dbConstants.tables.tickets_comments, columnsAndValues)
  sqlPool.makeQuery(ticketsQuery, callback)
}

/**
 * This methods will update ticket comment with given columnAndValues and comparisonColumnAndValue.
 *
 * @param {Object} columnsAndValues - Parameters to update ticket comment details.
 * @param {Function} callback
 */
const _updateTicketComments = (columnsAndValues, comparisonColumnAndValue, callback) => {
  const commentsQueryForTicket = queryCreator.updateSingle(dbConstants.tables.tickets_comments, columnsAndValues, comparisonColumnAndValue)
  sqlPool.makeQuery(commentsQueryForTicket, callback)
}

module.exports = Object.assign(module.exports, {
  saveComment,
  deleteComment
})
