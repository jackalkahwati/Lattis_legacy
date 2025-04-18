const db = require('../../db')
const passport = require('./passport')

/**
 * An operator, the primary user.
 * @typedef {object} Operator
 *
 * @property {number} operator_id
 */

/**
 * Relationship between a fleet and an operator.
 *
 * @typedef {object} FleetAssociation
 *
 * @property {number} operator_id
 * @property {number} fleet_id
 * @property {number} fleet_association_id
 * @property {number} customer_id
 * @property {string} acl
 */

/**
 * Relationships between fleets and an operator, keyed by the fleet id.
 *
 * @typedef {Object.<number, FleetAssociation>} FleetAssociations
 */

/**
 * @typedef {object} AuthContext
 *
 * @property {FleetAssociations} fleetAssociations
 * @property {Operator} operator
 */

/**
 * @param {Operator} operator
 * @returns {FleetAssociations}
 */
const fleetAssociations = async (operator) => {
  return (
    await db
      .main('fleet_associations')
      .where({ operator_id: operator.operator_id })
  ).reduce((acc, next) => {
    acc[next.fleet_id] = next

    return acc
  }, {})
}

/**
 * Construct an authentication context with fleet associations to use through the
 * entire authorization chain.
 *
 * @param {Operator} operator
 * @returns {Promise<AuthContext>}
 */
const constructAuthContext = async (operator) => {
  return {
    operator,
    fleetAssociations: await fleetAssociations(operator)
  }
}

const authenticateJWT = (req, res, next) => {
  passport.authenticate('jwt', async (err, user) => {
    if (err) {
      return next(err)
    }

    if (!user) {
      return res.status(401).send({
        message: 'Unauthorized',
        status: 401
      })
    }

    req.user = user

    req.context = {
      ...req.context,
      auth: await constructAuthContext(user)
    }

    return next()
  })(req, res, next)
}

module.exports = {
  authenticateJWT
}
