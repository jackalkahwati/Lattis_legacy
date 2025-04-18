'use strict'

const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const config = platform.config
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const ticketHandler = require('./../model_handlers/ticket-handler')
// const ticketLogHandler = require('./../model_handlers/log-handler')
// const ticketConstants = require('../constants/ticket-constants')
const { sendUpdateTicketRequestToGPSService } = require('../utils/gpsService')
const uploadFile = require('./../utils/file-upload')

/**
 * Used to create a Ticket .
 *
 * The request body should include the name,photo,category,notes
 *  that the operator has specified.
 */
router.post('/create-ticket', uploadFile.upload.single('photo'), (req, res) => {
  if (!ticketHandler.validateTicket(req.body)) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  const url = config.mailer.baseUrl ? config.mailer.baseUrl : ''

  ticketHandler.createTicket(req.body, req.file, url, (error, ticket) => {
    // const logData = {
    //   log_section: ticketConstants.log_section.create,
    //   data: req.body,
    //   log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
    //   message: error ? ('Error: could not create tickets with the operator id ' + req.body.operator_id + ' Failed with error') : ''
    // }
    // console.log("laaaa", logData)
    // ticketLogHandler.insertTicketLog(null, logData, () => {
    //   if (error) {
    //     logger('Error: failed to create Ticket with error', error)
    //     jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
    //     return
    //   }
    //   jsonResponse(res, responseCodes.OK, errors.noError(), ticket)
    // })
    if (error) {
      logger('Error: failed to create Ticket with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), ticket)
  })
})

/**
 * Used to create a Ticket .
 *
 * The request body should include the name,photo,category,notes
 *  that the operator has specified.
 */
router.post('/get-ticket', (req, res) => {
  if (!_.has(req.body, 'fleet_id') && !_.has(req.body, 'operator_id') && !_.has(req.body, 'customer_id')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  ticketHandler.getTickets(req.body, (error, tickets) => {
    if (error) {
      logger('Error: failed to get Ticket with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), tickets)
  })
})

/**
 * Used to assign a Ticket to a operator.
 *
 * The request body should include the ticket_id, assignee
 *  to whom the ticket needs to be assigned.
 */
router.post('/assign-ticket', (req, res) => {
  if (!_.has(req.body, 'ticket_id') || !_.has(req.body, 'assignee')) {
    logger('Error: Some Parameters are missing in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  // const logData = {
  //   log_section: ticketConstants.log_section.assign,
  //   data: req.body,
  //   log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
  //   message: error ? 'Error: failed to assign the ticket with error' : ''
  // }
  ticketHandler.assignTicket(req.body, (error) => {
    // ticketLogHandler.insertTicketLog(req.body.ticket_id, logData, () => {
    //   if (error) {
    //     logger('Error: failed to assign Ticket with error', error)
    //     jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
    //     return
    //   }
    //   jsonResponse(res, responseCodes.OK, errors.noError(), null)
    // })
    if (error) {
      logger('Error: failed to assign Ticket with error', error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
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
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  ticketHandler.resolveTicket(req.body, async (error) => {
    // const logData = {
    //   log_section: ticketConstants.log_section.resolve,
    //   data: req.body,
    //   log_type: error ? ticketConstants.log_type.error : ticketConstants.log_type.info,
    //   message: error ? 'Error: failed to resolve ticket with error' : ''
    // }
    // ticketLogHandler.insertTicketLog(req.body.ticket_id, logData, async () => {
    //   if (error) {
    //     logger('Error: failed to resolve ticket with error', error)
    //     jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
    //     return
    //   }
    //   try {
    //     await sendUpdateTicketRequestToGPSService(req.body.ticket_id)
    //     jsonResponse(res, responseCodes.OK, errors.noError(), null)
    //   } catch (error) {
    //     jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
    //   }
    // })
    if (error) {
      logger('Error: failed to resolve ticket with error', error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    try {
      await sendUpdateTicketRequestToGPSService(req.body.ticket_id)
      jsonResponse(res, responseCodes.OK, errors.noError(), null)
    } catch (error) {
      jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
    }
  })
})

module.exports = router
