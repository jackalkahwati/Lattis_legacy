const { errors, logger } = require('@velo-labs/platform')
const ovalResponse = require('./../utils/oval-response')
const controllerHandler = require('./../handlers/controller-handler')
const requestHelper = require('./../helpers/request-helper')

const router = require('express').Router()

const operatorAuthorizationValidator = (req, res) => new Promise((resolve, reject) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    resolve(error ? null : operator)
  })
})

router.post('/', async (req, res) => {
  // Auth
  const operator = await operatorAuthorizationValidator(req, res)
  if (!operator) {
    return
  }

  // Validation
  for (const param of ['device_type', 'vendor', 'key', 'fleet_id']) {
    if (!req.body[param]) {
      logger(`Error: Create controller: missing required parameter '${param}'`)
      ovalResponse(res, errors.missingParameter(true), null)
      return
    }
  }
  if (
    (req.body.latitude && !req.body.longitude) ||
    (req.body.longitude && !req.body.latitude)
  ) {
    logger('Error: Create controller: missing latitude or longitude')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  // Handle request
  try {
    const controller = await controllerHandler.create(
      req.body,
      operator
    )
    ovalResponse(res, errors.noError(), controller)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.get('/', async (req, res) => {
  // Auth
  const operator = await operatorAuthorizationValidator(req, res)
  if (!operator) {
    return
  }

  // Handle request
  try {
    const controllers = await controllerHandler.list(req.query)
    ovalResponse(res, errors.noError(), controllers)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.patch('/:controllerId', async (req, res) => {
  // Auth
  const operator = await operatorAuthorizationValidator(req, res)
  if (!operator) {
    return
  }

  // Validation
  const updatableColumns = [
    'bike_id',
    'fw_version',
    'hw_version',
    'make',
    'model',
    'gps_log_time',
    'status',
    'battery_level',
    'qr_code',
    'latitude',
    'longitude'
  ]
  const hasFieldToUpdate = updatableColumns.some(param => !!req.body[param])

  if (!hasFieldToUpdate) {
    logger(`Error: Update controller: missing a parameter to update`)
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (
    (req.body.latitude && !req.body.longitude) ||
    (req.body.longitude && !req.body.latitude)
  ) {
    logger('Error: Create controller: missing latitude or longitude')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  // Handle request
  try {
    const controller = await controllerHandler.update(
      req.params.controllerId,
      req.body
    )
    ovalResponse(res, errors.noError(), controller)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

router.post('/:controllerId/unassign', async (req, res) => {
  // Auth
  const operator = await operatorAuthorizationValidator(req, res)
  if (!operator) {
    return
  }

  // Handle request
  try {
    const controller = await controllerHandler.unassign(req.params.controllerId)
    ovalResponse(res, errors.noError(), controller)
  } catch (error) {
    ovalResponse(res, errors.formatErrorForWire(error), null)
  }
})

module.exports = router
