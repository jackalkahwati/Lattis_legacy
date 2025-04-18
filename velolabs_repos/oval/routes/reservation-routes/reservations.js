const {
  jsonResponse,
  logger,
  errors,
  responseCodes: codes
} = require('@velo-labs/platform')

const { schema, validator } = require('../validators')
const { statusCodeFromError } = require('../../utils/responseCodes')
const reservationHandlers = require('../../handlers/reservation-handlers')

module.exports = (router) => {
  router.post(
    '/',
    validator.body(schema.reservations.create),
    async (req, res) => {
      try {
        logger('Creating reservation', {
          user: req.user.user_id,
          params: req.body
        })

        const payload = { params: req.body, user: req.user }
        const response = await reservationHandlers.reservations.createAndNotify(
          payload
        )

        return jsonResponse(res, codes.EntryCreated, null, response)
      } catch (error) {
        logger(
          'Failed to create reservation',
          { user: req.user.user_id, body: req.body },
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
    '/next-reservation',
    validator.query(schema.reservations.nextReservation),
    async (req, res) => {
      try {
        logger('Retrieving next reservation', {
          user: req.user.user_id,
          query: req.query
        })

        const response = await reservationHandlers.reservations.retrieveNextReservation(
          { query: req.query, user: req.user }
        )

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to retrieve next reservation',
          { user: req.user.user_id, body: req.body },
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

  router.post(
    '/cost-estimate',
    validator.body(schema.reservations.create),
    async (req, res) => {
      try {
        const payload = { params: req.body, user: req.user }

        logger('Estimating reservation cost', {
          user: req.user.user_id,
          params: req.body
        })

        const response = await reservationHandlers.reservations.priceEstimate(
          payload
        )

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to estimate reservation cost',
          { user: req.user.user_id, body: req.body },
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
    '/available-vehicles',
    validator.query(schema.reservations.availableVehicles),
    async (req, res) => {
      try {
        const payload = { params: req.query, user: req.user }

        logger('Retrieving available vehicles', {
          user: req.user.user_id,
          query: req.query
        })

        const response = await reservationHandlers.reservations.availableVehicles(
          payload
        )

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to retrieve available vehicles',
          { user: req.user.user_id, query: req.query },
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
    '/:reservation_id',
    validator.params(schema.reservations.retrieve),
    async (req, res) => {
      try {
        logger('Retrieving reservation', {
          params: req.params,
          user: req.user.user_id
        })

        const payload = { params: req.params, user: req.user }
        const response = await reservationHandlers.reservations.retrieve(
          payload
        )

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to retrieve reservation',
          { params: req.params, user: req.user.user_id },
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

  router.put(
    '/:reservation_id/start-trip',
    validator.params(schema.reservations.retrieve),
    async (req, res) => {
      try {
        logger('Starting reservation trip', {
          params: req.params,
          user: req.user.user_id
        })

        const payload = { params: req.params, user: req.user }
        const response = await reservationHandlers.reservations.startTrip(
          payload
        )

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to start reservation trip',
          req.params,
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
    '/',
    validator.query(schema.reservations.list),
    async (req, res) => {
      try {
        logger('Listing reservations', {
          query: req.query,
          user: req.user.user_id
        })

        const payload = { query: req.query, user: req.user }
        const response = await reservationHandlers.reservations.list(payload)

        for (let reservation of response) {
          const tripPaymentTransaction = reservation && reservation.trip_payment_transaction
          if (tripPaymentTransaction && (!tripPaymentTransaction.id || !tripPaymentTransaction.reservation_id)) {
            delete reservation.trip_payment_transaction
          }
        }
        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to list reservations',
          req.body,
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

  router.put(
    '/:reservation_id/cancel',
    validator.params(schema.reservations.cancel),
    async (req, res) => {
      try {
        logger('Cancelling reservation...', {
          params: req.params,
          user: req.user.user_id
        })

        const payload = { params: req.params, user: req.user }
        const response = await reservationHandlers.reservations.cancel(payload)

        return jsonResponse(res, codes.OK, null, response)
      } catch (error) {
        logger(
          'Failed to cancel reservation',
          req.body,
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
