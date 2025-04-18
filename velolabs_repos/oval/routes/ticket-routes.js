'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const errors = platform.errors
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const ticketHandler = require('./../handlers/ticket-handler')
const commentsHandler = require('../handlers/tickets/comments-handler')
const requestHelper = require('./../helpers/request-helper')
const ticketLogHandler = require('./../handlers/tickets/log-handler')
const ticketConstants = require('../constants/ticket-constants')
/**
 * Used to get all the tickets with the operator_id
 *
 * The request body should include operator_id
 */
router.post('/get-tickets', (req, res) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    req.body.operator_id = operator.operator_id
    ticketHandler.getTickets(req.body, (error, tickets) => {
      if (error) {
        logger('Error: could not get tickets with the operator id', req.body.operator_id, 'Failed with error', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), tickets)
    })
  })
})

router.post('/create-ticket', (req, res) => {
  if (!ticketHandler.validateTicket(req.body)) {
    logger('Some Parameters not passed to server')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    req.body.operator_id = operator.operator_id
    req.body.customer_id = operator.customer_id
    req.body.reported_by_operator_id = operator.operator_id
    ticketHandler.createTicket(req.body, (error) => {
      const logData = {
        log_section: ticketConstants.log_section.create,
        data: req.body,
        log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
        message: error ? ('Error: could not create tickets with the operator id ' + req.body.operator_id + ' Failed with error') : ''
      }
      ticketLogHandler.insertTicketLog(null, logData, () => {
        if (error) {
          logger('Error: could not create tickets with the operator id', req.body.operator_id, 'Failed with error', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
        }
        ovalResponse(res, errors.noError(), null)
      })
    })
  })
})

/**
 * Used to resolve a ticket.
 *
 * The request body should include ticket_id
 */
router.post('/resolve-ticket', (req, res) => {
  if (!_.has(req.body, 'ticket_id')) {
    logger('Error: Some Parameters are missing in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    ticketHandler.resolveTicket(req.body, (error) => {
      const logData = {
        log_section: ticketConstants.log_section.resolve,
        data: req.body,
        log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
        message: error ? 'Error: failed to resolve ticket with error' : ''
      }
      ticketLogHandler.insertTicketLog(req.body.ticket_id, logData, () => {
        if (error) {
          logger('Error: failed to resolve ticket with error', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        ovalResponse(res, errors.noError(), null)
      })
    })
  })
})

/**
 * Used to assign a ticket.
 *
 * The request body should include ticket_id
 */
router.post('/assign-ticket', (req, res) => {
  if (!_.has(req.body, 'ticket_id') || !_.has(req.body, 'operator_id')) {
    logger('Error: Some Parameters are missing in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    ticketHandler.assignTicket(req.body, (error, status) => {
      const logData = {
        log_section: ticketConstants.log_section.assign,
        data: req.body,
        log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
        message: error ? 'Error: failed to assign the ticket with error' : ''
      }
      ticketLogHandler.insertTicketLog(req.body.ticket_id, logData, () => {
        if (error) {
          logger('Error: failed to assign the ticket with error', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        ovalResponse(res, errors.noError(), status)
      })
    })
  })
})

/**
 * Used to update a ticket status (open / closed / inprogress).
 *
 * The request body should include ticket_id, fleet_id, operator_id, ticket_status
 */
router.post('/status-update', (req, res) => {
  if (!_.has(req.body, 'ticket_id') || !_.has(req.body, 'operator_id')) {
    logger('Error: Some Parameters are missing in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    ticketHandler.statusUpdate(req.body, (error, status) => {
      const logData = {
        log_section: ticketConstants.log_section.status_update,
        data: req.body,
        log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
        message: error ? 'Error: failed to update the ticket status with error' : ''
      }
      ticketLogHandler.insertTicketLog(req.body.ticket_id, logData, () => {
        if (error) {
          logger('Error: failed to update the ticket status with error', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        ovalResponse(res, errors.noError(), status)
      })
    })
  })
})

/**
 * Used to insert/update a ticket comment.
 *
 * The request body should include ticket_id, operator_id, comment_id, message
 */
router.post('/save-comment', (req, res) => {
  if (!_.has(req.body, 'ticket_id') || !_.has(req.body, 'operator_id')) {
    logger('Error: Some Parameters are missing in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    req.body['user_type'] = ticketConstants.user_types.operator
    req.body['created_by'] = req.body.operator_id
    commentsHandler.saveComment(req.body, (error, status) => {
      if (error) {
        logger('Error: failed to Add/update the ticket comment with error', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), status)
    })
  })
})

/**
 * Used to delete/deactivate ticket comment.
 *
 * The request body should include ticket_id, operator_id, comment_id
 */
router.post('/delete-comment', (req, res) => {
  if (!_.has(req.body, 'ticket_id') || !_.has(req.body, 'operator_id')) {
    logger('Error: Some Parameters are missing in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    req.body['user_type'] = ticketConstants.user_types.operator
    req.body['created_by'] = req.body.operator_id
    commentsHandler.deleteComment(req.body, (error, status) => {
      if (error) {
        logger('Error: failed to delete/deactivate the ticket comment with error', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), status)
    })
  })
})

module.exports = router
