const { JWT_SECRET } = process.env
const jwt = require('jsonwebtoken')
const db = require('../db')
const platform = require('@velo-labs/platform')
const logger = platform.logger

const formatResponse = (error, message, payload, stack) => {
  let msg
  if (!error) {
    msg = { error: null, payload }
    logger(msg)
  } else {
    msg = { error: true, message, stack }
  }
  return msg
}

const generateToken = (data = {}, { expiresIn = '3m' } = {}) => {
  try {
    return jwt.sign({ ...data, signedBy: 'lattis-dashboard' }, JWT_SECRET, {
      expiresIn
    })
  } catch (error) {
    logger(
      formatResponse(
        true,
        error.message || 'Failed to generate the token',
        null,
        error
      )
    )

    throw error
  }
}

// Some integrations only send a single header so we can't check for both key and client
// Not good, but we can live with it
const authWithAPIKey = async (req, res, next) => {
  try {
    const apiKey = req.headers['x-api-key']
    const apiClient = req.headers['x-api-client']
    if (!apiKey) {
      return res.status(401).json({ msg: 'An API key is required' })
    } else {
      let where = { key: apiKey }
      if (apiClient) where = { ...where, issued_to: apiClient }
      const apiKeyExists = await db.users('api_keys').where(where).first()
      if (!apiKeyExists) return res.status(401).json({ msg: 'Invalid API key' })
    }
    next()
  } catch (error) {
    logger('An error occurred validating the API key')
  }
}

module.exports = { generateToken, authWithAPIKey }
