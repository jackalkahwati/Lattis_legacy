const { responseCodes, logger } = require('@velo-labs/platform')

/**
 * @typedef {object} AuthorizationResult
 *
 * @property {boolean} authorized
 */

/**
 * @typedef {object} Authorizer
 *
 * @property {(req) => string | number} getResourceIdFromRequest
 * @property {(authContext: import("../middleware").AuthContext, resourceId: string | number) => AuthorizationResult} authorizingFn
 */

/**
 * @param {import("express").Response} res
 */
const defaultUnauthorized = (res) =>
  res.status(responseCodes.Forbidden).send({
    message: 'Forbidden'
  })

/**
 * Create an Express middleware function that checks if the incoming requester
 * has the necessary access as dictated by the `authorizingFn` in any of the `authorizers`.
 * NOTE: This has oneOf (some) semantics. The request is considered authorized
 * if one of the authorizing functions returns a successful result.
 *
 * @param {Authorizer[]} authorizers Array of authorizers
 * @param {object} [options]
 * @param {(res: import("express").Response) => void} [options.unauthorized]
 */
function authorize (authorizers, { unauthorized = defaultUnauthorized } = {}) {
  return async (req, res, next) => {
    const authPipeline = {
      operator: req.context.auth.operator.operator_id,
      path: req.path,
      pipeline: []
    }

    for (const { authorizingFn, getResourceIdFromRequest } of authorizers) {
      const resourceId = await getResourceIdFromRequest(req)

      const pipelineItem = {
        resourceId: resourceId || null,
        authorizer: authorizingFn.name
      }

      if (resourceId) {
        const { authorized } = await authorizingFn(
          req.context.auth,
          resourceId
        )

        if (authorized) {
          pipelineItem.authorized = authorized
          authPipeline.pipeline.push(pipelineItem)
          return next()
        }
      }

      pipelineItem.authorized = false
      authPipeline.pipeline.push(pipelineItem)
    }

    logger('Unauthorized access', authPipeline)

    return unauthorized(res)
  }
}

module.exports = authorize
