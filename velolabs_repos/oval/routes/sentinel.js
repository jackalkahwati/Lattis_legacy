const platform = require('@velo-labs/platform')
const responseCodes = platform.responseCodes
const jsonResponse = platform.jsonResponse
const errors = platform.errors
const logger = platform.logger
const router = require('express').Router()
const _ = require('underscore')
const db = require('../db')
const Sentinel = require('../helpers/sentinel')
const ovalResponse = require('./../utils/oval-response')

router.post('/:bikeId/unlock', async (req, res) => {
  // convert bikeId to a number
  const bikeId = parseInt(req.params.bikeId)
  if (!_.has(req.params, 'bikeId')) {
    logger('Error: Missing required lockId')
    jsonResponse(
      res,
      responseCodes.BadRequest,
      errors.missingParameter(true),
      null
    )
    return
  }
  const ctrl = await db.main('controllers').where({bike_id: bikeId}).first()
  if (!ctrl) {
    return ovalResponse(res, errors.customError('No controller found for bike'), responseCodes.ResourceNotFound, true)
  }
  const lockId = ctrl.key
  const sentinelClient = new Sentinel()
  const status = await sentinelClient.fetchLockStatus(lockId)
  if (status) {
    if (status.code === '000000') {
      const body = status.body
      db.main('bikes').update({ ...body.geoPoint, bike_battery_level: body.voltagePercentage }).where({ bike_id: bikeId })
      db.main('controllers').update({ ...body.geoPoint, battery_levl: body.voltagePercentage }).where({ key: ctrl.key })
      // a Sentinel lock has to be online for PSM to mean anything: refer to https://velolabs.atlassian.net/browse/VLSB-2760
      if (body.connectionStatus === 1) {
        if (body.workMode === 1) {
          return ovalResponse(res, errors.customError('The lock is offline. Tap to reconnect', responseCodes.ResourceNotFound, 'SentinelLockOffline', true), null)
        } else if (body.workMode === 0) {
          const result = await sentinelClient.unlock(lockId)
          if (result.code !== '000000') {
            return ovalResponse(res, errors.customError('Error unlocking Sentinel lock', responseCodes.InternalServer, 'SentinelError', true), null)
          } else if (result.code === '000000' && result.body) {
            return ovalResponse(res, errors.noError(), { message: 'The unlock request was send successfully. Wait for a notification confirmation' })
          }
        }
      } else {
        // TODO: We need to reach an agreement on what to do if lock is offline, this requires a hardware consideration.
        return ovalResponse(res, errors.customError('The sentinel lock is entirely offline', responseCodes.InternalServer, 'SentinelError', true), null)
      }
    }
  } else return ovalResponse(res, errors.customError('Error fetching Sentinel lock status', responseCodes.InternalServer, 'SentinelError', true), null)
})

module.exports = router
