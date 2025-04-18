const {
  logger,
  errors,
  jsonResponse,
  responseCodes: codes
} = require('@velo-labs/platform')

const { schema, validator } = require('../../validators')
const { statusCodeFromError } = require('../../utils/responseCodes')

const { reservationSettings } = require('../../model_handlers/reservations')

module.exports = (router) => {
  router.post(
    '/:fleet_id/reservation-settings',
    validator.params(schema.reservations.reservationSettings.createParams),
    validator.query(schema.reservations.reservationSettings.createQuery),
    validator.body(schema.reservations.reservationSettings.createBody),
    async (req, res) => {
      const params = { fleet_id: req.params.fleet_id, ...req.body }
      const options = { parse: true, ...req.query }

      try {
        logger('Creating reservation settings:', { params, options })

        const response = await reservationSettings.create(params, options)

        return jsonResponse(res, codes.EntryCreated, null, response)
      } catch (error) {
        logger(
          'Failed to create fleet reservation settings',
          { params, options },
          'Error: ',
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

  router.get(
    '/:fleet_id/reservation-settings',
    validator.params(schema.reservations.reservationSettings.createParams),
    validator.query(schema.reservations.reservationSettings.createQuery),
    async (req, res) => {
      const params = { fleetId: req.params.fleet_id }
      const options = req.query

      try {
        logger('Retrieving reservation settings:', { params, options })

        const response = await reservationSettings.retrieve(params, options)

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to retrieve fleet reservation settings',
          { params, options },
          'Error: ',
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

  router.patch(
    '/:fleet_id/reservation-settings/activate',
    validator.params(schema.reservations.reservationSettings.activationParams),
    async (req, res) => {
      const options = req.params

      try {
        logger('Activating reservation settings:', options)

        const response = await reservationSettings.activate(options)

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        console.log(error.stack)
        logger(
          'Failed to activate fleet reservation settings',
          options,
          'Error: ',
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

  router.patch(
    '/:fleet_id/reservation-settings/deactivate',
    validator.params(schema.reservations.reservationSettings.activationParams),
    async (req, res) => {
      const options = req.params

      try {
        logger('Deactivating reservation settings:', options)

        const response = await reservationSettings.deactivate(options)

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        console.log(error.stack)
        logger(
          'Failed to deactivate fleet reservation settings',
          options,
          'Error: ',
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

  router.patch(
    '/:fleet_id/reservation-settings/:reservation_settings_id',
    validator.query(schema.reservations.reservationSettings.createQuery),
    validator.params(schema.reservations.reservationSettings.updateParams),
    validator.body(schema.reservations.reservationSettings.updateBody),
    async (req, res) => {
      const options = {
        params: req.params,
        query: req.query,
        body: req.body
      }

      try {
        logger('Updating reservation settings:', options)

        const response = await reservationSettings.update(options)

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        console.log(error.stack)
        logger(
          'Failed to update fleet reservation settings',
          options,
          'Error: ',
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
}
