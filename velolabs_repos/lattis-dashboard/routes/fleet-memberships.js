const express = require('express')
const {
  logger,
  errors,
  jsonResponse,
  responseCodes: codes
} = require('@velo-labs/platform')

const { schema, validator } = require('../validators')

const {
  fleetMemberships: memberships
} = require('../model_handlers/fleet-memberships')

const router = express.Router()

const { statusCodeFromError } = require('../utils/responseCodes')

router.post(
  '/',
  validator.body(schema.fleetMemberships.createMembershipSchema),
  async (req, res) => {
    try {
      logger('Creating membership: ', req.body)
      const response = await memberships.create(req.body)
      return jsonResponse(res, codes.EntryCreated, null, response)
    } catch (error) {
      logger(
        'Failed to create fleet membership',
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
  '/:fleet_membership_id',
  validator.params(schema.fleetMemberships.updateMembershipParamsSchema),
  validator.body(schema.fleetMemberships.updateMembershipSchema),
  async (req, res) => {
    try {
      logger('Updating membership:', { params: req.params, body: req.body })

      const response = await memberships.update(req.params, req.body)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to update fleet membership',
        { params: req.params, body: req.body },
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
  '/:fleet_membership_id',
  validator.params(schema.fleetMemberships.updateMembershipParamsSchema),
  async (req, res) => {
    try {
      logger('Retrieving membership:', { params: req.params })

      const response = await memberships.retrieve(req.params)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to retrieve fleet membership',
        { params: req.params },
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
  '/:fleet_membership_id/deactivate',
  validator.params(schema.fleetMemberships.deactivate),
  async (req, res) => {
    try {
      logger('Deactivating membership:', { params: req.params })

      const response = await memberships.deactivate(req.params)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to deactivate fleet membership',
        { params: req.params },
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
  '/:fleet_membership_id/activate',
  validator.params(schema.fleetMemberships.activate),
  async (req, res) => {
    try {
      logger('Activating membership:', { params: req.params })

      const response = await memberships.activate(req.params)

      return jsonResponse(res, codes.OK, null, response)
    } catch (error) {
      logger(
        'Failed to activate fleet membership',
        { params: req.params },
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

module.exports = router
