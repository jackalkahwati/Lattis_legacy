const {
  errors
} = require('@velo-labs/platform')

const { schema, validator } = require('../validators')
const ovalResponse = require('../../utils/oval-response')
const responseCodes = require('@velo-labs/platform/helpers/response-codes')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const db = require('../../db')
const bikeHandler = require('../../handlers/bike-handler')
const util = require('util')
const requestHelper = require('../../helpers/request-helper')
const { executeCommand, client } = require('../../helpers/grow')
const Greenriders = require('../../helpers/greenriders')
const { grinIoTDefaultSettings } = require('../../constants/bike-constants')

const userAuthorizationValidatorAsync = util.promisify(requestHelper.userAuthorizationValidator)

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

module.exports = (router) => {
  router.get(
    '/:equipmentId/status',
    validator.query(schema.equipment.retrieve),
    async (req, res) => {
      const id = req.query.bike_id || req.query.port_id || req.query.hub_id
      if (!id) {
        ovalResponse(res, errors.customError('Provide a vehicle id as a query string', responseCodes.BadRequest, 'RequiresQRode'), null)
        return
      }
      let user
      try {
        user = await userAuthorizationValidatorAsync(req, res, true)
      } catch (error) {
        // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
        return
      }
      let vehicleType
      if (req.query.bike_id) {
        vehicleType = 'bike'
      }
      if (req.query.port_id) {
        vehicleType = 'port'
      }
      if (req.query.hub_id) {
        vehicleType = 'hub'
      }
      try {
        const equipmentId = req.params.equipmentId
        let controllerAndVehicle
        let status
        switch (vehicleType) {
          case 'bike':
            controllerAndVehicle = await db.main('controllers')
              .where({ 'controllers.controller_id': equipmentId })
              .leftJoin('bikes', 'bikes.bike_id', 'controllers.bike_id')
              .first().select(
                'vendor',
                'key',
                'bike_battery_level',
                'battery_level',
                'controllers.latitude as ioTLat',
                'controllers.longitude as ioTLng',
                'gps_log_time')
            if (controllerAndVehicle) {
              status = await bikeHandler.getIotStatus(req.query.bike_id, user.user_id, controllerAndVehicle.key, controllerAndVehicle.fleetId)
            }
            break
          case 'hub':
            controllerAndVehicle = await db.main('controllers')
              .where({ 'controllers.controller_id': equipmentId })
              .leftJoin('hubs_and_fleets', 'hubs_and_fleets.equipment', 'controllers.controller_id')
              .leftJoin('hubs', 'hubs.id', 'hubs_and_fleets.hub_id')
              .first().select(
                'vendor',
                'key',
                'controllers.latitude as ioTLat',
                'controllers.longitude as ioTLng',
                'controllers.fleet_id as fleetId',
                'gps_log_time')
            if (controllerAndVehicle) {
              status = await bikeHandler.getIotStatus(null, user.user_id, controllerAndVehicle.key, controllerAndVehicle.fleetId)
            }
            break
          case 'port':
            console.log('dealing with a port')
            controllerAndVehicle = await db.main('controllers')
              .where({ 'controllers.controller_id': equipmentId })
              .leftJoin('ports', 'ports.equipment', 'controllers.controller_id')
              .first().select(
                'vendor',
                'key',
                'controllers.latitude as ioTLat',
                'controllers.longitude as ioTLng',
                'controllers.fleet_id as fleetId',
                'gps_log_time'
              )
            if (controllerAndVehicle) {
              status = await bikeHandler.getIotStatus(null, user.user_id, controllerAndVehicle.key, controllerAndVehicle.fleetId)
            }
            break
          default:
            throw errors.customError(`Unknown vehicle type`, responseCodes.BadRequest, 'BadRequest', false)
        }
        return ovalResponse(res, errors.noError(), status)
      } catch (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
      }
    }
  )

  router.post('/:controller_id/unlock', async (req, res) => {
    const controllerId = req.params.controller_id
    if (!controllerId) {
      return ovalResponse(res, errors.customError(`Controller Id not provided`, responseCodes.BadRequest, 'MissingParameter', true), null)
    }
    let user
    try {
      user = await userAuthorizationValidatorAsync(req, res, true)
    } catch (error) {
    // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
      return
    }
    const controller = await db.main('controllers').where({controller_id: controllerId}).first()
    if (!controller) {
      return ovalResponse(res, errors.customError(`Controller not found`, responseCodes.ResourceNotFound, 'ResourceNotFound', true), null)
    }
    if (controller.vendor === 'Grow') {
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
          ovalResponse(res, errors.formatErrorForWire({ message: `Error unlocking Grow device:${response}` }), null)
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
          return ovalResponse(res, errors.customError(`Unable to unlock Grow device ${controller.key}`, responseCodes.InternalServer, 'DeviceUnlockError', true), null)
        }
      })
    } else if (controller.vendor === 'dck-mob-e') {
      const status = await new Greenriders().getStationById(controller.key)
      const data = status.Item
      if (!data.LockedScooterID) throw new Error('The station does not have a vehicle locked')
      else {
        const portWithvehicle = await db.main('ports').where({ equipment: controller.controller_id }).first()
        if (portWithvehicle) {
          const response = await new Greenriders().unlockStation(controller.key, data.LockedScooterID)
          await db.main('ports').where({ port_id: portWithvehicle.port_id }).update({ current_status: 'parked', vehicle_uuid: null })
          return ovalResponse(res, errors.noError(), response)
        }
      }
    } else {
      try {
        const unlockResponse = await bikeHandler.unlockEquipmentAsUser(user.user_id, controller)
        return ovalResponse(res, errors.noError(), unlockResponse)
      } catch (error) {
        return ovalResponse(res, errors.customError(error.message || 'An error occurred unlocking the device', error.code || responseCodes.InternalServer, error.name || 'DeviceUnlockError', true), null)
      }
    }
  })

  router.post('/:controller_id/lock', async (req, res) => {
    const controllerId = req.params.controller_id
    if (!controllerId) {
      return ovalResponse(res, errors.customError(`Controller Id not provided`, responseCodes.BadRequest, 'MissingParameter', true), null)
    }
    let user
    try {
      user = await userAuthorizationValidatorAsync(req, res, true)
    } catch (error) {
    // no-op because requestHelper.userAuthorizationValidator sends error response if authorization fails
      return
    }
    const controller = await db.main('controllers').where({controller_id: controllerId}).first()
    if (!controller) {
      return ovalResponse(res, errors.customError(`Controller not found`, responseCodes.ResourceNotFound, 'ResourceNotFound', true), null)
    }
    try {
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
        const lockResponse = await bikeHandler.lockEquipmentAsUser(user.user_id, controller)
        ovalResponse(res, errors.noError(), lockResponse)
      }
    } catch (error) {

    }
  })
}
