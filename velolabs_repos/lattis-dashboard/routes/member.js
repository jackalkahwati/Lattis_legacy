'use strict'

const platform = require('@velo-labs/platform')
const express = require('express')
const _ = require('underscore')
const { promotions } = require('../model_handlers/promotions')
const { statusCodeFromError } = require('../utils/responseCodes')
const { schema, validator } = require('../validators')
const router = express.Router()
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const memberHandler = require('./../model_handlers/member-handler')
const usersHandler = require('./../model_handlers/users-handler')
const uploadFile = require('./../utils/file-upload')

/**
 * Used to get all the member list
 *
 * The request body should include fleet_id
 **/
router.post('/get-member-list', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.getMemberList(req.body, (error, status) => {
    if (error) {
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get all the users
 *
 * The request body should include user_id
 **/
router.post('/get-users', (req, res) => {
  if (!_.has(req.body, 'user_id')) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  usersHandler.getUsers(req.body, (error, status) => {
    if (error) {
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Used to get trip history
 *
 * The request body should include fleet_id, user_id
 **/
router.post('/get-trip-history', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'user_id')) {
    logger('Some Parameters not passed to server')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.getTripDetails(req.body, (error, status) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), status)
  })
})

/**
 * Update Member profile.
 *
 * The request body should include the user_id, country_code, first_name, last_name, phone_number
 *  and email
 */
router.post('/get-csv-file', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: no fleet id sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.getCSV(req.body, (error, updateStatus) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), updateStatus)
  })
})

/**
 * Update Member profile.
 *
 * The request body should include the user_id, country_code, first_name, last_name, phone_number
 *  and email
 */
router.post('/update-member-profile', (req, res) => {
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'first_name') || !_.has(req.body, 'last_name')) {
    logger('Error: no user id sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.updateMemberProfile(req.body, (error, updateStatus) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), updateStatus)
  })
})

/**
 * Grant/Revoke Access to Member to the fleet.
 *
 * The request body should include the user_id
 */
router.post('/grant-or-revoke-member-access', (req, res) => {
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'fleet_id') || !_.has(req.body, 'access')) {
    logger('Error: Required parameter not sent sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  if (_.has(req.body, 'new_record')) {
    if (_.has(req.body, 'usersToAdd')) {
      let usersToAdd = req.body.usersToAdd
      let usersObject = []
      usersToAdd.forEach(function (user) {
        let userData = {
          user_id: user.user_id,
          fleet_id: req.body.fleet_id,
          email: user.email,
          access: user.access
        }
        usersObject.push(userData)
      })
      memberHandler.grantOrRevokeMemberAccess({
        user_id: req.body.user_id,
        fleet_id: req.body.fleet_id,
        email: req.body.email,
        verified: 1,
        add_new_users: usersObject
      }, req.body.access, 'csv', (error) => {
        if (error) {
          logger('Error: Unable to change access to private fleet user: ', req.body.user_id)
          jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
          return
        }
        jsonResponse(res, responseCodes.OK, errors.noError(), null)
      })
    } else {
      memberHandler.grantOrRevokeMemberAccess({
        user_id: req.body.user_id,
        fleet_id: req.body.fleet_id,
        email: req.body.email,
        verified: 0
      }, req.body.access, 'csv', (error) => {
        if (error) {
          logger('Error: Unable to change access to private fleet user: ', req.body.user_id)
          jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
          return
        }
        jsonResponse(res, responseCodes.OK, errors.noError(), null)
      })
    }
  } else if (_.has(req.body, 'multiple_records')) {
    memberHandler.grantOrRevokeMemberAccess({
      fleet_id: req.body.fleet_id, publicFleet: req.body.verified || 0
    }, req.body.access, req.body.access_mode || 'csv', (error) => {
      if (error) {
        logger('Error: Unable to change access to private fleet user: ', req.body.user_id)
        jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), null)
    })
  } else {
    memberHandler.grantOrRevokeMemberAccess({user_id: req.body.user_id, fleet_id: req.body.fleet_id}, req.body.access, 'csv', (error) => {
      if (error) {
        logger('Error: Unable to change access to private fleet user: ', req.body.user_id)
        jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
        return
      }
      jsonResponse(res, responseCodes.OK, errors.noError(), null)
    })
  }
})
router.post('/toggle-access', async (req, res) => {
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'fleet_id')) {
    logger('Error: some parameters are missing in he request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const memberAccessResponse = await memberHandler.toggleIndividualAccess(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), memberAccessResponse)
  } catch (error) {
    logger(`Failed to update member access: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})
router.post('/revoke-fleet-access', async (req, res) => {
  if (!_.has(req.body, 'fleetId')) {
    logger('Error: some parameters are missing in the request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const memberAccessResponse = await memberHandler.revokeAccessForAllFleetMembers(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), memberAccessResponse)
  } catch (error) {
    logger(`Failed to update member access: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})
router.post('/get-user', async (req, res) => {
  if (!_.has(req.body, 'user_id') || !_.has(req.body, 'fleet_id')) {
    logger('Error: no member csv file sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  try {
    const user = await memberHandler.getLattisUser(req.body)
    jsonResponse(res, responseCodes.OK, errors.noError(), user)
  } catch (error) {
    logger(`Failed to fetch user: ${error.message}`)
    jsonResponse(
      res,
      responseCodes.InternalServer,
      errors.internalServer(true),
      null
    )
  }
})

/**
 * Used to get member email addresses from csv file and save it to DB.
 *
 * The request body should include the csv file for member upload
 */

router.post('/upload-member-csv', uploadFile.upload.single('member_csv'), (req, res) => {
  if (
    !req.file ||
    // !_.has(req.files, 'member_csv') ||
    !_.has(req.body, 'fleet_id')) {
    logger('Error: no member csv file sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.updateMembers({member_csv: req.file}, req.body.fleet_id, (error) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), null)
  })
})

router.post('/get-member-csv', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: no member csv file sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }

  memberHandler.getMemberCsv(req.body.fleet_id, (error, csv) => {
    if (error) {
      logger(error)
      jsonResponse(res, error.code, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), csv)
  })
})

/**
 * Revoke Access to Members from old csv file
 *
 * The request body should include the fleet_id
 */
router.post('/revoke-old-member-csv', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: no user id sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.revokeMembersFromOldCSV(req.body, (error, updateStatus) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), updateStatus)
  })
})

/**
 * Get the list of members who are currently available as private fleet users
 *
 * The request body should include the fleet_id
 */
router.post('/get-private-fleet-users', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: no user fleet id sent in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.getPrivateAccounts(req.body, (error, response) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), response)
  })
})
/**
 * Used to get reports of member for an operator
 *
 * The request body should include the fleet_id and month that an operator has specified
 */
router.post('/member-reports', (req, res) => {
  if (!_.has(req.body, 'fleet_id') || !_.has(req.body, 'month') || !_.has(req.body, 'year')) {
    logger('Error: missing parameter in request')
    jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    return
  }
  memberHandler.memberReports(req.body, (error, updateStatus) => {
    if (error) {
      logger(error)
      jsonResponse(res, responseCodes.InternalServer, errors.formatErrorForWire(error), null)
      return
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), updateStatus)
  })
})

router.get(
  '/:userId/promotions/:fleetId',
  validator.params(schema.promotions.listUserPromotions),
  async (req, res) => {
    try {
      logger('Listing user promotions', req.params)

      const response = await promotions.userPromotions(req.params)

      return jsonResponse(res, responseCodes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to list user promotions',
        req.params,
        'Error:',
        errors.errorWithMessage(error)
      )

      return jsonResponse(
        res,
        statusCodeFromError(error),
        errors.formatErrorForWire(error)
      )
    }
  }
)

module.exports = router
