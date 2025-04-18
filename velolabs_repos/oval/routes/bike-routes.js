'use strict'

const express = require('express')
const router = express.Router()
const bookingsRouter = express.Router()
const _ = require('underscore')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const errors = platform.errors
const bookingHandler = require('./../handlers/booking-handler')
const requestHelper = require('./../helpers/request-helper')
const bikeHandler = require('./../handlers/bike-handler')
const userHandler = require('./../handlers/user-handler')
const util = require('util')
const db = require('../db')
const { executeCommand, client } = require('../helpers/grow')
const { grinIoTDefaultSettings } = require('../constants/bike-constants')
const responseCodes = require('@velo-labs/platform/helpers/response-codes')

const { configureSentry } = require('./../utils/configureSentry')
const Sentry = configureSentry()

/**
 * Used to find bikes for a given trip
 *
 * The request body should include the latitude ,longitude,number Of Bikes to be filtered,
 * closest distance in miles and fleet_id
 */
router.post('/find-bikes', (req, res) => {
  if (!_.has(req.body, 'latitude') || !_.has(req.body, 'longitude')) {
    logger('Error: No Parameters sent in request: findBikes')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    bikeHandler.findBikes(req.body, (error, bikes) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

/**
 * To update metadata
 *
 * The request body should include the bike_id or lock_id
 */
router.post('/update-metadata-for-user', (req, res) => {
  if (!_.has(req.body, 'bike_id') && !_.has(req.body, 'lock_id')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bikeHandler.updateMetadata(req.body, (error) => {
      if (error) {
        logger('Error: updating metadata with error', errors.errorWithMessage(error))
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

/**
 * To update metadata
 *
 * The request body should include the bike_id or lock_id
 */
router.post('/update-metadata-for-operator', (req, res) => {
  if (!_.has(req.body, 'lock_id')) {
    logger('Error: lock_id is missing in the request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    bikeHandler.updateMetadata(req.body, (error) => {
      if (error) {
        logger('Error: updating metadata with error', errors.errorWithMessage(error))
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

/**
 * To get details of a bike
 *
 * The request body should include the bike_id
 */
router.post('/get-bike-details', (req, res) => {
  const oneOfParams = ['bike_id', 'qr_code_id', 'iot_qr_code']
  if (!oneOfParams.some(param => _.has(req.body, param))) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bikeHandler.getBikeDetails(req.body, (error, bikeDetails) => {
      if (error) {
        ovalResponse(res, errors.customError(error.message || 'Failed to get bike details', error.code || responseCodes.InternalServer, error.name || 'BikeDetailsError', true), null)
        return
      }
      ovalResponse(res, errors.noError(), bikeDetails)
    })
  })
})

/**
 * Used to create a booking for the user for the available bikes
 *
 * The request body should include the bike_id, user_id
 */
router.post('/create-booking', (req, res) => {
  if (!_.has(req.body, 'bike_id')) {
    logger('Error: Some Parameters are missing in this request')
    ovalResponse(res, errors.customError('Provide a bike_id', responseCodes.BadRequest, 'MissingParameter'), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.body.email = user.email
    req.body.phone_number = user.phone_number
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    logger('Create booking', { user_id: user.user_id, bike_id: req.body.bike_id })
    bookingHandler.createBooking(req.body, (error, bookingDetails) => {
      if (error) {
        error.code = (error.name === 'stripeConnectAccount') ? 411 : error.code
        logger('Error: processing create booking:', errors.errorWithMessage(error))
        ovalResponse(res, errors.customError(error.message || 'An error occurred creating bike booking', error.code || responseCodes.InternalServer, error.name || 'BookingError', true), null)
        return
      }
      ovalResponse(res, errors.noError(), bookingDetails)
    })
  })
})

/** v2
 * Used to create a booking for the user for the available bikes/hubs/ports
 *
 * The request body should include the bike_id/port_id/hub_id, user_id
 */
const createBooking = (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.body.email = user.email
    req.body.phone_number = user.phone_number
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bookingHandler.createBookingV2(req.body, (error, bookingDetails) => {
      if (error) {
        error.code = (error.name === 'stripeConnectAccount') ? 411 : error.code
        logger('Error: processing create booking:', errors.errorWithMessage(error))
        ovalResponse(res, errors.customError(error.message || 'An error occurred creating bike booking', error.code || responseCodes.InternalServer, error.name || 'BookingError', true), bookingDetails)
        return
      }
      ovalResponse(res, errors.noError(), bookingDetails)
    })
  })
}

const isParameterForBookingCreationMissing = (req, res, errorFor) => {
  if (!_.has(req.body, `${errorFor}_id`)) {
    logger(`Error: ${errorFor}_id missing in this request`)
    ovalResponse(res, errors.customError(`Provide a ${errorFor}_id for booking`, responseCodes.BadRequest, 'MissingParameter', true), null)
    return true
  }
}

const isParameterForCancelBookingMissing = (req, res) => {
  if (!req.params.booking_id) {
    logger(`Error: booking_id missing in this request params`)
    ovalResponse(res, errors.customError(`Provide a booking_id_id for booking cancelling`, responseCodes.BadRequest, 'MissingParameter', true), null)
    return true
  }
}

bookingsRouter.post('/hub', (req, res) => {
  const isParamMissing = isParameterForBookingCreationMissing(req, res, 'hub')
  if (isParamMissing) return
  req.body.bookingType = 'hub'
  return createBooking(req, res)
})

bookingsRouter.post('/port', (req, res) => {
  const isParamMissing = isParameterForBookingCreationMissing(req, res, 'port')
  if (isParamMissing) return
  req.body.bookingType = 'port'
  return createBooking(req, res)
})

bookingsRouter.post('/bike', (req, res) => {
  const isParamMissing = isParameterForBookingCreationMissing(req, res, 'bike')
  if (isParamMissing) return
  req.body.bookingType = 'bike'
  return createBooking(req, res)
})

/**
 * Used to cancel a trip for a given user
 *
 * The request body should include the bike_id
 */
router.post('/cancel-booking', (req, res) => {
  if (!_.has(req.body, 'bike_id')) {
    logger('Error: No Parameters sent in request: cancelBooking')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) {
      Sentry.captureException(error)
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    req.body.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bookingHandler.cancelBooking(req.body, (error) => {
      if (error) {
        Sentry.captureException(error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), null)
    })
  })
})

/** v2
 * Used to cancel a trip for a given user
 *
 * The request body should include the bike_id or port_id or hub_id
 */
bookingsRouter.patch('/:booking_id/cancel', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    req.body.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    const isParamMissing = isParameterForCancelBookingMissing(req, res)
    if (isParamMissing) return
    bookingHandler.cancelBookingV2(req.body, req.params.booking_id, (error) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

/**
 * Used to assign a lock to a bike
 *
 * The request body should include the bike_id
 */
router.post('/assign-lock', (req, res) => {
  const hasLockId = _.has(req.body, 'lock_id')
  const hasLattisQrCode = _.has(req.body, 'qr_code_id') && _.has(req.body, 'bike_name')
  const hasIotQrCode = _.has(req.body, 'iot_qr_code')
  if (!hasLockId || !(hasLattisQrCode || hasIotQrCode)) {
    logger('Error: No Parameters sent in request to assign Lock')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    req.body.operator_id = operator.operator_id
    bikeHandler.assignLock(req.body, (error, bikes) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

router.post('/assign-controller', async (req, res) => {
  // Auth
  const operator = await new Promise((resolve) => {
    requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
      resolve(error ? null : operator)
    })
  })
  if (!operator) {
    return
  }

  // Validation
  for (const param of ['qr_code_id', 'controller_id', 'bike_name', 'bike_group_id']) {
    if (!req.body[param]) {
      logger('Error: Missing controller_id in request to assign controller')
      ovalResponse(res, errors.missingParameter(true), null)
      return
    }
  }

  // Handle request
  try {
    const bike = await bikeHandler.assignControllerToRandomBike(req.body)
    ovalResponse(res, errors.noError(), bike)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.post('/:bikeId/assign-controller', async (req, res) => {
  // Auth
  const operator = await new Promise((resolve) => {
    requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
      resolve(error ? null : operator)
    })
  })
  if (!operator) {
    return
  }

  // Validation
  if (!req.body.controller_id) {
    logger('Error: Missing controller_id in request to assign controller')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  // Handle request
  try {
    const bike = await bikeHandler.assignController({
      bikeId: req.params.bikeId,
      controllerId: req.body.controller_id
    })
    ovalResponse(res, errors.noError(), bike)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

/**
 * Used to get all the bikes for a given bike
 *
 * The request body should include the fleet_id
 */
router.post('/get-bikes', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: No Parameters sent in request: getBikes')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  bikeHandler.getBikesWithFleets(_.pick(req.body, 'fleet_id'), (error, bikes) => {
    if (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }

    ovalResponse(res, errors.noError(), bikes)
  })
})

/**
 * Used to get the terms and conditions
 */
router.get('/terms-and-conditions', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    userHandler.getTermsAndConditions(user, (error, termsAndConditions) => {
      if (error) {
        logger('Error: failed to retrieve terms and conditions', 'Failed with error:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), { terms_and_conditions: JSON.parse(termsAndConditions) })
    })
  })
})

/**
 * Used to assign a lock to a bike
 *
 * The request body should include the bike_id
 */
router.post('/unassign-lock', (req, res) => {
  if (!_.has(req.body, 'lock_id')) {
    logger('Error: No Parameters sent in request to unassign Lock')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    bikeHandler.unAssignLock(req.body, (error, bikes) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

router.post('/get-staging-bike-groups', (req, res) => {
  if (!_.has(req.body, 'fleet_id')) {
    logger('Error: No Parameters sent in request to un-assign Lock')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    bikeHandler.getStagingBikeGroups(req.body, (error, bikegroups) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), { bike_groups: bikegroups })
    })
  })
})

/**
 * Used to change the bike status of the bike
 *
 * The request body should include the bike_id, to
 */
router.post('/change-bike-status', (req, res) => {
  if (!_.has(req.body, 'bike_id') || !_.has(req.body, 'to') ||
    !_.has(req.body, 'latitude') || !_.has(req.body, 'longitude')) {
    logger('Error: No Parameters sent in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    bikeHandler.changeBikeStatus(req.body, (error) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

/**
 * Used to search bikes for a given latitude and longitude
 *
 * The request body should include the latitude ,longitude
 */
router.post('/search-bikes', (req, res) => {
  if (!_.has(req.body, 'latitude') || !_.has(req.body, 'longitude')) {
    logger('Error: No Parameters sent in request: findBikes')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bikeHandler.searchBikes(req.body, (error, bikes) => {
      bikes.nearest.forEach(bike => {
        bike.do_not_track_trip = Boolean(bike.do_not_track_trip)
        bike.require_phone_number = Boolean(bike.require_phone_number)
      })
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

router.get('/search', (req, res) => {
  if (!_.has(req.query, 'sw') || !_.has(req.query, 'ne')) {
    logger('Error: No Parameters sent in request: findBikes')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, async (error, user) => {
    const appType = req.headers['user-agent']
    let fleetBikes = []
    if (error) return
    req.query.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    const whiteLabelFleetIds = appType ? await bikeHandler.getAppFleets(appType) : []
    bikeHandler.searchBikesByCoordinates(req.query, (error, bikes) => {
      bikes.forEach(bike => {
        bike.do_not_track_trip = Boolean(bike.do_not_track_trip)
        bike.require_phone_number = Boolean(bike.require_phone_number)
      })
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      if (whiteLabelFleetIds.length && bikes.length) {
        fleetBikes = bikes.filter(bike => whiteLabelFleetIds.includes(bike.fleet_id))
        ovalResponse(res, errors.noError(), fleetBikes)
        return
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

/**
 * Used to get lock by qr code
 *
 * The request body should include the qr_code_id
 */
router.post('/get-lock-by-qr-code', (req, res) => {
  if (!_.has(req.body, 'qr_code_id')) {
    logger('Error: No Parameters sent in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    bikeHandler.getLockByQrCode(req.body, (error, bikes) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), bikes)
    })
  })
})

/**
 * Used to change the bike status of the bike
 *
 * The request body should include the bike_id, bike_name and qr_code_id
 */
router.put('/change-label', (req, res) => {
  if (!_.has(req.body, 'bike_id') || !_.has(req.body, 'qr_code_id') || !_.has(req.body, 'bike_name')) {
    logger('Error: No Parameters sent in request')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    bikeHandler.changeLabel(req.body, (error, lockEntity) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), lockEntity)
    })
  })
})

const userAuthorizationValidatorAsync = util.promisify(requestHelper.userAuthorizationValidator)
const operatorAuthorizationValidatorAsync = util.promisify(requestHelper.operatorAuthorizationValidator)

const processResponseCodes = (responseCode) => {
  switch (responseCode) {
    case 0:
      return 'Success'
    case -1:
      return 'Processing Error'
    case -2:
      return 'Parameter Error'
    case -3:
      return 'Unknown Command Error'
    default:
      return 'Unknown response code'
  }
}

router.post('/:bikeId/user-lock', async (req, res) => {
  let user
  try {
    user = await userAuthorizationValidatorAsync(req, res, true)
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
  } catch (error) {
    // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
    return
  }
  try {
    const controller = await db.main('controllers').where({ bike_id: req.params.bikeId }).first()
    if (controller && controller.vendor === 'Grow') {
      let longPollData
      const commandCode = 1
      const commandValue = 1
      const command = `${commandCode},${commandValue}`
      client.on('message', function (topic, payload, pkt) {
        longPollData = Buffer.from(payload).toString()
        longPollData = JSON.parse(longPollData)
        const commandResponse = longPollData.response
        const responseCommand = longPollData.command
        const timeout = setTimeout(() => {
          if (res.headersSent) return
          return ovalResponse(res, errors.formatErrorForWire({ message: `The IoT might be offline. Took too long to respond` }), null)
        }, 10000)
        if (res.headersSent) return // Don't respond twice
        if ([-1, -2, -3].includes(commandResponse)) {
          clearTimeout(timeout)
          let response = processResponseCodes(commandResponse)
          ovalResponse(res, errors.formatErrorForWire({ message: `Error locking Grow device:${response}` }), null)
        } else {
          if (commandCode === responseCommand) {
            clearTimeout(timeout)
            longPollData = undefined
            ovalResponse(res, errors.noError(), { message: `Grow device ${controller.key} locked successfully` })
          }
        }
      })
      executeCommand(`s/c/${controller.key}`, `39,${grinIoTDefaultSettings.lockedHeartBeatInterval}`, (err, message) => {
        if (err) {
          logger(`Error setting heartbeat interval for Grow ${controller.key}`)
        }
      })
      executeCommand(`s/c/${controller.key}`, command, (err, message) => {
        if (err) {
          logger(`Error locking Grow ${controller.key}`)
          ovalResponse(res, errors.formatErrorForWire({ message: `Unable to lock Grow device ${controller.key}` }), null)
        }
      })
    } else {
      const lockResponse = await bikeHandler.lockBikeAsUser(req.params.bikeId, user.user_id, req.sentryObj)
      ovalResponse(res, errors.noError(), lockResponse)
    }
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.post('/:bikeId/user-unlock', async (req, res) => {
  let user
  try {
    user = await userAuthorizationValidatorAsync(req, res, true)
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
  } catch (error) {
    // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
    return
  }
  try {
    let controllers = []
    let controllerKeys = req.body.controller_key
    if (controllerKeys && Array.isArray(controllerKeys) && controllerKeys.length) {
      controllers = await db.main('controllers').whereIn('key', controllerKeys)
      const greenridersAdapterIndex = controllers.findIndex(ctr => ctr.vendor === 'dck-mob-e')
      if (greenridersAdapterIndex !== -1) {
        // This is to push the adapter to the end of the array since it should unlock
        // after the Segway IoT lock has unlocked
        const greenCtrlKey = controllers[greenridersAdapterIndex].key
        const indexInParams = controllerKeys.indexOf(greenCtrlKey)
        controllerKeys.splice(indexInParams, 1)
        controllerKeys.push(greenCtrlKey)
      }
    } else {
      const ctrl = await db.main('controllers').where({ bike_id: req.params.bikeId }).first()
      if (!ctrl || (ctrl && !ctrl.bike_id)) throw errors.customError('The bike ID provided is not linked to any controller', responseCodes.ResourceNotFound, 'VehicleUserUnlockError', true)
      controllerKeys = [ctrl.key]
    }
    const growVehicle = controllers.find(ctrl => ctrl.vendor === 'Grow')
    if (growVehicle) {
      const controller = growVehicle
      let longPollData
      const commandCode = 1
      const commandValue = 0
      const command = `${commandCode},${commandValue}`
      client.on('message', function (topic, payload, pkt) {
        longPollData = Buffer.from(payload).toString()
        longPollData = JSON.parse(longPollData)
        const commandResponse = longPollData.response
        const responseCommand = longPollData.command
        const timeout = setTimeout(() => {
          if (res.headersSent) return
          return ovalResponse(res, errors.formatErrorForWire({ message: `The IoT might be offline. Took too long to respond` }), null)
        }, 10000)
        if (res.headersSent) return // Don't respond twice
        if ([-1, -2, -3].includes(commandResponse)) {
          clearTimeout(timeout)
          const response = processResponseCodes(commandResponse)
          ovalResponse(res, errors.formatErrorForWire({ message: `Error locking Grow device:${response}` }), null)
        } else {
          if (responseCommand === commandCode) {
            clearTimeout(timeout)
            longPollData = undefined
            ovalResponse(res, errors.noError(), { message: `Grow device ${controller.key} unlocked successfully` })
          }
        }
      })
      executeCommand(`s/c/${controller.key}`, `39,${grinIoTDefaultSettings.heartBeatInterval}`, (err, message) => {
        if (err) {
          logger(`Error setting heartbeat interval for Grow ${controller.key}`)
        }
      })
      executeCommand(`s/c/${controller.key}`, command, (err, message) => {
        if (err) {
          logger(`Error unlocking Grow ${controller.key}`)
          ovalResponse(res, errors.formatErrorForWire({ message: `Unable to unlock Grow device ${controller.key}` }), null)
        }
      })
    } else {
      const unlockResponses = await Promise.all(
        controllerKeys.map(async (ctrlKey) => {
          const unlockResponse = await bikeHandler.unlockBikeAsUser(req.params.bikeId, user.user_id, ctrlKey, req.sentryObj)
          return unlockResponse
        })
      )
      const iot = unlockResponses.find(resp => !resp.type)
      ovalResponse(res, errors.noError(), iot)
    }
  } catch (error) {
    ovalResponse(res, errors.customError(error.message || 'An error occurred unlocking the vehicle', error.code || responseCodes.InternalServer, error.name || 'VehicleUserUnlockError', true), null)
  }
})

router.post('/:bikeId/operator-lock', async (req, res) => {
  try {
    await operatorAuthorizationValidatorAsync(req, res)
  } catch (error) {
    // no-op because requestHelper.operatorAuthorizationValidator sends error response if authorization fails
    return
  }
  try {
    const controller = await db.main('controllers').where({ bike_id: req.params.bikeId }).first()
    if (controller && controller.vendor === 'Grow') {
      let longPollData
      const commandCode = 1
      const commandValue = 1
      const command = `${commandCode},${commandValue}`
      client.on('message', function (topic, payload, pkt) {
        longPollData = Buffer.from(payload).toString()
        longPollData = JSON.parse(longPollData)
        const commandResponse = longPollData.response
        const responseCommand = longPollData.command
        const timeout = setTimeout(() => {
          if (res.headersSent) return
          return ovalResponse(res, errors.formatErrorForWire({ message: `The IoT might be offline. Took too long to respond` }), null)
        }, 10000)
        if (res.headersSent) return // Don't respond twice
        if ([-1, -2, -3].includes(commandResponse)) {
          clearTimeout(timeout)
          let response = processResponseCodes(commandResponse)
          ovalResponse(res, errors.formatErrorForWire({ message: `Error locking Grow device:${response}` }), null)
        } else {
          if (commandCode === responseCommand) {
            clearTimeout(timeout)
            longPollData = undefined
            ovalResponse(res, errors.noError(), { message: `Grow device ${controller.key} locked successfully` })
          }
        }
      })
      executeCommand(`s/c/${controller.key}`, `39,${grinIoTDefaultSettings.lockedHeartBeatInterval}`, (err, message) => {
        if (err) {
          logger(`Error setting heartbeat interval for Grow ${controller.key}`)
        }
      })
      executeCommand(`s/c/${controller.key}`, command, (err, message) => {
        if (err) {
          logger(`Error locking Grow ${controller.key}`)
          ovalResponse(res, errors.formatErrorForWire({ message: `Unable to lock Grow device ${controller.key}` }), null)
        }
      })
    } else {
      await bikeHandler.lockBike(req.params.bikeId)
      ovalResponse(res, errors.noError(), null)
    }
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.post('/:bikeId/operator-unlock', async (req, res) => {
  try {
    await operatorAuthorizationValidatorAsync(req, res)
  } catch (error) {
    // no-op because requestHelper.operatorAuthorizationValidator sends error response if authorization fails
    return
  }
  try {
    let controllers = []
    let controllerKeys = req.body.controller_key
    if (controllerKeys && Array.isArray(controllerKeys) && controllerKeys.length) {
      controllers = await db.main('controllers').whereIn('key', controllerKeys)
    } else {
      const ctrl = await db.main('controllers').where({ bike_id: req.params.bikeId }).first()
      controllerKeys = [ctrl.key]
    }
    const growVehicle = controllers.find(ctrl => ctrl.vendor === 'Grow')
    if (growVehicle) {
      const controller = growVehicle
      let longPollData
      const commandCode = 1
      const commandValue = 0
      const command = `${commandCode},${commandValue}`
      client.on('message', function (topic, payload, pkt) {
        longPollData = Buffer.from(payload).toString()
        longPollData = JSON.parse(longPollData)
        const commandResponse = longPollData.response
        const responseCommand = longPollData.command
        const timeout = setTimeout(() => {
          if (res.headersSent) return
          return ovalResponse(res, errors.formatErrorForWire({ message: `The IoT might be offline. Took too long to respond` }), null)
        }, 10000)
        if (res.headersSent) return // Don't respond twice
        if ([-1, -2, -3].includes(commandResponse)) {
          clearTimeout(timeout)
          let response = processResponseCodes(commandResponse)
          ovalResponse(res, errors.formatErrorForWire({ message: `Error locking Grow device:${response}` }), null)
        } else {
          if (commandCode === responseCommand) {
            clearTimeout(timeout)
            longPollData = undefined
            ovalResponse(res, errors.noError(), { message: `Grow device ${controller.key} unlocked successfully` })
          }
        }
      })
      executeCommand(`s/c/${controller.key}`, `39,${grinIoTDefaultSettings.heartBeatInterval}`, (err, message) => {
        if (err) {
          logger(`Error setting heartbeat interval for Grow ${controller.key}`)
        }
      })
      executeCommand(`s/c/${controller.key}`, command, (err, message) => {
        if (err) {
          logger(`Error unlocking Grow ${controller.key}`)
          ovalResponse(res, errors.formatErrorForWire({ message: `Unable to unlock Grow device ${controller.key}` }), null)
        }
      })
    } else {
      await Promise.all(
        controllerKeys.map(async (ctrlKey) => {
          const unlockResponse = await bikeHandler.unlockBike(req.params.bikeId, ctrlKey)
          return unlockResponse
        })
      )
      ovalResponse(res, errors.noError(), null)
    }
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.get('/:bikeId/iot/status', async (req, res) => {
  let user
  try {
    user = await userAuthorizationValidatorAsync(req, res, true)
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
  } catch (error) {
    // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
    return
  }
  const controllerKey = req.query.controller_key
  const controllerAndBike = await db.main('controllers')
    .where({ 'controllers.bike_id': req.params.bikeId })
    .leftJoin('bikes', 'bikes.bike_id', 'controllers.bike_id')
    .first().select('vendor', 'key', 'bike_battery_level', 'battery_level', 'controllers.latitude as ioTLat', 'controllers.longitude as ioTLng', 'gps_log_time')
  if (controllerAndBike && controllerAndBike.vendor === 'Grow') {
    client.subscribe(`s/a/${controllerAndBike.key}`)
    let longPollData
    let command = 101
    const timeout = setTimeout(() => {
      if (res.headersSent) return
      return ovalResponse(res, errors.formatErrorForWire({ message: `The IoT might be offline. Took too long to respond`, code: 500, name: 'InternalServerError' }), null)
    }, 10000)
    client.on('message', function (topic, payload, pkt) {
      longPollData = Buffer.from(payload).toString()
      longPollData = JSON.parse(longPollData)
      const commandResponse = longPollData.response
      const responseCommand = longPollData.command
      if (res.headersSent) return // Don't respond twice
      if ([-1, -2, -3].includes(responseCommand)) {
        clearTimeout(timeout)
        const response = processResponseCodes(responseCommand)
        return ovalResponse(res, errors.formatErrorForWire({ message: `Error getting Grow status:${response}` }), null)
      } else {
        if (command === responseCommand) {
          clearTimeout(timeout)
          longPollData = undefined
          let data = {
            latitude: +controllerAndBike.ioTLat,
            longitude: +controllerAndBike.ioTLng,
            online: true,
            locked: !!+commandResponse,
            battery_percent: controllerAndBike.battery_level,
            bike_battery_percent: controllerAndBike.bike_battery_level,
            last_update_time: controllerAndBike.gps_log_time
          }
          ovalResponse(res, errors.noError(), data)
        }
      }
    })
    executeCommand(`s/c/${controllerAndBike.key}`, `${command}`, (err, message) => {
      if (err) {
        logger(`Error getting Grow lock status ${controllerAndBike.key}`)
        ovalResponse(res, errors.formatErrorForWire({ message: `Error getting Grow lock status ${controllerAndBike.key}` }), null)
      }
    })
  } else {
    try {
      const status = await bikeHandler.getIotStatus(req.params.bikeId, user.user_id, controllerKey)
      ovalResponse(res, errors.noError(), status)
    } catch (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
    }
  }
})

// Get the command status of a linka command (lock / unlock)
router.get('/:bikeId/command/:commandId', async (req, res) => {
  try {
    let user = await userAuthorizationValidatorAsync(req, res, true)
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
  } catch (error) {
    return
  }
  try {
    const commandId = req.params.commandId
    const bikeId = req.params.bikeId
    const commandStatus = await bikeHandler.getLinkaCommandStatus(commandId, bikeId)
    ovalResponse(res, errors.noError(), commandStatus)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.get('/:bikeId/code', async (req, res) => {
  try {
    let user = await userAuthorizationValidatorAsync(req, res, true)
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
  } catch (error) {
    // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
    return
  }
  try {
    const status = await bikeHandler.getLockCode(req.params.bikeId)
    ovalResponse(res, errors.noError(), status)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

module.exports = { bikeRoutes: router, bookingsRouter }
