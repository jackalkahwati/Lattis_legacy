const platform = require('@velo-labs/platform')
const fetch = require('node-fetch')
const axios = require('axios')
const querystring = require('querystring')
const errors = platform.errors
const logger = platform.logger
const qs = require('querystring')
const envConfig = require('../envConfig')

const SCOUT_BASE_URL = envConfig('SCOUT_BASE_URL')

const registerScoutUser = async (userDetails, done) => {
  const requestOptions = {
    method: 'POST',
    body: '',
    redirect: 'follow'
  }

  const queryString = querystring.stringify(
    {
      'first': userDetails.first,
      'last': userDetails.last,
      'email': userDetails.email,
      'password': userDetails.password
    })

  fetch(
    `${SCOUT_BASE_URL}/register/?${queryString}`,
    requestOptions
  )
    .then(response => response.json())
    .then((result) => {
      if (!result.success) {
        let error = errors.customError(
          `Username already exists!'`,
          platform.responseCodes.BadRequest,
          'BadRequest',
          false
        )
        logger('Error: Registering user.', result.message)
        done(error, result)
        return
      }
      done(null, result)
    })
    .catch(error => { throw error })
}

const logInScoutUser = async (user) => {
  try {
    const response = await axios.post(
      `${SCOUT_BASE_URL}/login/`,
      qs.stringify({
        username: user.email,
        password: user.password
      }),
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }
    )
    return response.data
  } catch (error) {
    logger('Error loging into scout::', error)
    throw errors.customError(
      error.message || `Error logging into scout:`, error,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}
const getCurrentScoutStatus = async (user, scoutId) => {
  try {
    const path = `${SCOUT_BASE_URL}/data/${scoutId}?username=${user.email}&api_key=${user.api_key}`
    const config = {
      method: 'get',
      url: path
    }
    const response = await axios(config)
    return response.data
  } catch (error) {
    logger('Scout getCurrentScoutStatus Error::', error)
    throw errors.customError(
      error.response.data.errorDetails || `Error getting Scout status for ${scoutId}`,
      platform.responseCodes.InternalServer,
      'Device Error',
      false
    )
  }
}

const getAllUserScouts = async (user, key) => {
  try {
    const path = `${SCOUT_BASE_URL}/device?username=${user.email}&api_key=${user.api_key}`
    const config = {
      method: 'get',
      url: path
    }
    const response = await axios(config)
    return response.data
  } catch (error) {
    logger('Scout getAllUserScouts Error::', error)
    throw errors.customError(
      error.response.data.errorDetails || `Error getting user scouts ${key}`,
      platform.responseCodes.InternalServer,
      'Device Error',
      false
    )
  }
}

module.exports = {
  registerScoutUser,
  logInScoutUser,
  getCurrentScoutStatus,
  getAllUserScouts
}
