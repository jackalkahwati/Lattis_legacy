const {
  logger,
  jsonResponse,
  responseCodes,
  errors
} = require('@velo-labs/platform')

const {
  fleetMemberships: memberships
} = require('../../model_handlers/fleet-memberships')
const { statusCodeFromError } = require('../../utils/responseCodes')
const { validator, schema } = require('../../validators')

module.exports = (router) => {
  router.get(
    '/:fleet_id/memberships',
    validator.params(schema.fleetMemberships.list),
    async (req, res) => {
      try {
        logger('Listing memberships', req.params)

        let result = await memberships.list(req.params)

        return jsonResponse(res, responseCodes.OK, null, result)
      } catch (error) {
        logger(
          'Failed to list fleet memberships',
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

  router.get(
    '/:fleet_id/subscriptions/:user_id',
    validator.params(schema.fleetMemberships.subscriptions),
    async (req, res) => {
      try {
        logger('Listing subscriptions', req.params)

        const result = await memberships.subscriptions({
          fleet_id: req.params.fleet_id,
          user_ids: [req.params.user_id]
        })

        return jsonResponse(res, responseCodes.OK, null, result)
      } catch (error) {
        logger(
          'Failed to list subscriptions',
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
}
