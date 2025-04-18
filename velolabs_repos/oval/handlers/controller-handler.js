const {pick} = require('lodash')
const { errors, logger } = require('@velo-labs/platform')
const db = require('../db')
const bikeConstants = require('./../constants/bike-constants')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

async function create (body, operator) {
  const controller = {
    added_by_operator_id: operator.operator_id,
    ...pick(body, [
      'device_type',
      'vendor',
      'key',
      'fw_version',
      'hw_version',
      'make',
      'model',
      'gps_log_time',
      'status',
      'battery_level',
      'qr_code',
      'latitude',
      'longitude',
      'fleet_id',
      'bike_id'
    ])
  }
  try {
    const [insertedId] = await db.main('controllers').insert(controller)

    return await db.main('controllers').select().where({
      controller_id: insertedId
    })
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error: failed to create controller ${error.message}`)
    if (error.code === 'ER_NO_REFERENCED_ROW_2') {
      throw errors.invalidParameter(false)
    }
    if (error.code === 'ER_DUP_ENTRY') {
      throw errors.lockAlreadyRegistered(false)
    }
    throw errors.internalServer(false)
  }
}

async function list (params) {
  // Only include params that are set. We do not want to end up with
  // where = { vendor: undefined } because that results in an error
  const where = {}
  for (const param of ['vendor', 'key']) {
    if (params[param]) {
      where[param] = params[param]
    }
  }
  try {
    return await db.main('controllers').select().where(where)
  } catch (error) {
    Sentry.captureException(error, {params})
    logger(`Error: Failed to get controllers: ${error.message}`)
    throw errors.internalServer(true)
  }
}

async function update (controllerId, body) {
  const updatableColumns = [
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
  const update = pick(body, updatableColumns)

  try {
    const updatedControllers = await db.main('controllers')
      .where({ controller_id: controllerId })
      .update(update)

    if (updatedControllers === 0) {
      throw errors.invalidParameter(true)
    }

    return await db.main('controllers').select().where({
      controller_id: controllerId
    })
  } catch (error) {
    Sentry.captureException(error, {controllerId, body})
    if (error.constructor.name === 'LattisError') {
      throw error
    }
    logger(`Error: Failed to update controllers: ${error.message}`)
    if (error.code === 'ER_NO_REFERENCED_ROW_2') {
      throw errors.invalidParameter(true)
    }
    throw errors.internalServer(true)
  }
}

async function unassign (controllerId) {
  try {
    const assignedBike = await db.main('bikes').first()
      .join('controllers', 'bikes.bike_id', 'controllers.bike_id')
      .where({ 'controllers.controller_id': controllerId })

    if (!assignedBike) {
      logger('The lock is not assigned to any bike!')
      throw errors.resourceNotFound(false)
    }

    await db.main.transaction(async trx => {
      await trx('controllers')
        .where({ controller_id: controllerId })
        .update({ bike_id: null })

      // Only update the status if the bike has no other controller(s)
      // or lock assigned
      await trx('bikes')
        .whereNotExists(function () {
          this.select().from('controllers').where({
            bike_id: assignedBike.bike_id
          })
        })
        .andWhere({
          bike_id: assignedBike.bike_id,
          lock_id: null
        })
        .update({
          qr_code_id: null,
          status: bikeConstants.status.inactive,
          current_status: bikeConstants.current_status.lockNotAssigned
        })
    })

    // Return the updated controller
    return await db.main('controllers').select().where({
      controller_id: controllerId
    })
  } catch (error) {
    Sentry.captureException(error, {controllerId})
    if (error.constructor.name === 'LattisError') {
      throw error
    }
    logger(`Error: Failed to update controllers: ${error.message}`)
    if (error.code === 'ER_NO_REFERENCED_ROW_2') {
      throw errors.invalidParameter(true)
    }
    throw errors.internalServer(true)
  }
}

module.exports = {
  create,
  list,
  update,
  unassign
}
