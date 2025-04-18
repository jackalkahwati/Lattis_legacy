const { JWT_SECRET } = process.env
const jwt = require('jsonwebtoken')
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

const generateToken = (data = {}) => {
  try {
    const token = jwt.sign({ ...data, signedBy: 'GPSTrackingServive' }, JWT_SECRET, {
      expiresIn: '2m'
    })
    return token
  } catch (error) {
    const message = formatResponse(
      true,
      error.message || 'Failed to generate the token',
      null,
      error
    )
    logger(message)
    throw Error(error)
  }
}

module.exports = { generateToken }
