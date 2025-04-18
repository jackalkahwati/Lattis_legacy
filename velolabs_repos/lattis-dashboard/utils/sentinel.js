const axios = require('axios')
const querystring = require('querystring')
const environment = process.env.CURRENT_ENVIRONMENT
const SENTINEL_API_URL = 'https://api.sentinel-tec.com/api/v2'
const { getParameterStoreValuesByPath } = require('../utils/parameterStore')
const platform = require('@velo-labs/platform')
const errors = platform.errors

const positioningInterval = 5
const reportGpsCycle = 5
class Sentinel {
  // TODO: Move fetchToken to constructor
  async fetchAccessToken () {
    try {
      const { clientId, secretKey } = await this.fetchSentinelConfig()
      const endpoint = `${SENTINEL_API_URL}/auth/token`
      const response = await axios.post(endpoint, querystring.stringify({ clientId, secretKey }), {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'Accept': 'application/json, text/plain, */*'
        }
      })
      return response.data.body
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error fetching Sentinel access token `
      )
    }
  }

  async fetchLockStatus (lockId) {
    try {
      const endpoint = `${SENTINEL_API_URL}/lock/query`
      const data = await this.fetchAccessToken()
      const response = await axios.post(endpoint, querystring.stringify({ lockId }), {
        headers: {
          Authorization: data.accessToken,
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error fetching Sentinel lock status for lock ${lockId}: `
      )
    }
  }

  async fetchSentinelConfig () {
    try {
      const { Parameters: sentinelCredentials } = await getParameterStoreValuesByPath('SENTINEL/CREDENTIALS')
      const credentialName = `/env/${environment}/SENTINEL/CREDENTIALS`
      const clientId = sentinelCredentials.find(credential => credential.Name === `${credentialName}/CLIENT_ID`)
      const secretKey = sentinelCredentials.find(credential => credential.Name === `${credentialName}/SECRET_KEY`)
      return {
        clientId: clientId.Value,
        secretKey: secretKey.Value
      }
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async unlock (lockId) {
    try {
      const endpoint = `${SENTINEL_API_URL}/lock/control/open`
      const data = await this.fetchAccessToken()
      const response = await axios.post(endpoint, querystring.stringify({ lockId }), {
        headers: {
          Authorization: data.accessToken,
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error unlocking Sentinel lock ${lockId}: `
      )
    }
  }

  async startTrip (lockId, tripUUID) {
    try {
      const endpoint = `${SENTINEL_API_URL}/lock/control/startTrip`
      const data = await this.fetchAccessToken()
      const response = await axios.post(endpoint, querystring.stringify({ lockId, positioningInterval, reportGpsCycle, tripUUID }), {
        headers: {
          'authorization': data.accessToken,
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error starting trip for Sentinel lock ${lockId}: `
      )
    }
  }

  async endTrip (lockId) {
    try {
      const endpoint = `${SENTINEL_API_URL}/lock/control/endTrip`
      const data = await this.fetchAccessToken()
      const response = await axios.post(endpoint, querystring.stringify({ lockId }), {
        headers: {
          Authorization: data.accessToken,
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error ending trip for Sentinel lock ${lockId}: `
      )
    }
  }
}

module.exports = Sentinel
