const jwt = require('jsonwebtoken')
const { JWT_SECRET } = process.env

const verifyToken = (token) => {
  try {
    const verified = jwt.verify(token, JWT_SECRET)
    return verified
  } catch (error) {
    throw Error(error)
  }
}

const generateToken = (data = {}) => {
  try {
    const token = jwt.sign({ ...data, signedBy: 'CRON-JOBS-Servive' }, JWT_SECRET, {
      expiresIn: '2m' // Short enough to just send a request
    })
    return token
  } catch (error) {
    throw Error(error)
  }
}

module.exports = { generateToken, verifyToken }
