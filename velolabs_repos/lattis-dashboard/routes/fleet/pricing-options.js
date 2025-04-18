const {
  logger,
  jsonResponse,
  responseCodes,
  errors
} = require('@velo-labs/platform')

const { validator, schema } = require('../../validators')
const { statusCodeFromError } = require('../../utils/responseCodes')
const pricingOptions = require('../../model_handlers/pricing-options')

function pricingOptionsRoutes (router) {
  router.post(
    '/:fleet_id/pricing-options',
    validator.params(schema.pricingOptions.params),
    validator.body(schema.pricingOptions.createBody),
    async (req, res) => {
      const data = { fleet_id: req.params.fleet_id, ...req.body }

      try {
        logger('Creating pricing option', data)

        const pricingOption = await pricingOptions.create(data)

        return jsonResponse(
          res,
          responseCodes.EntryCreated,
          null,
          pricingOption
        )
      } catch (error) {
        logger(
          'Failed to create pricing option',
          data,
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
    '/:fleet_id/pricing-options',
    validator.params(schema.pricingOptions.params),
    async (req, res) => {
      const { params } = req

      try {
        logger('Listing pricing options', { params })

        const prices = await pricingOptions.list(params)

        return jsonResponse(res, responseCodes.OK, null, prices)
      } catch (error) {
        logger(
          'Failed to list pricing options',
          { params },
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
    '/:fleet_id/pricing-options/:pricing_option_id',
    validator.params(schema.pricingOptions.updateParams),
    validator.body(schema.pricingOptions.updateBody),
    async (req, res) => {
      const { params, body } = req

      try {
        logger('Updating pricing option', { params, body })

        const updated = await pricingOptions.update(params, body)

        return jsonResponse(res, responseCodes.OK, null, updated)
      } catch (error) {
        logger(
          'Failed to update pricing option',
          { params, body },
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
    '/:fleet_id/pricing-options/:pricing_option_id/activate',
    validator.params(schema.pricingOptions.updateParams),
    async (req, res) => {
      const { params } = req

      try {
        logger('Activating pricing option', { params })

        const prices = await pricingOptions.activate(params)

        return jsonResponse(res, responseCodes.OK, null, prices)
      } catch (error) {
        logger(
          'Failed to activate pricing option',
          { params },
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
    '/:fleet_id/pricing-options/:pricing_option_id/deactivate',
    validator.params(schema.pricingOptions.updateParams),
    async (req, res) => {
      const { params } = req

      try {
        logger('Deactivating pricing option', { params })

        const prices = await pricingOptions.deactivate(params)

        return jsonResponse(res, responseCodes.OK, null, prices)
      } catch (error) {
        logger(
          'Failed to deactivate pricing option',
          { params },
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

module.exports = pricingOptionsRoutes
