const axios = require('axios')
const environment = process.env.CURRENT_ENVIRONMENT
const querystring = require('querystring')
const { getParameterStoreValuesByPath, getParameterStoreValue } = require('../utils/parameterStore')
const platform = require('@velo-labs/platform')
const errors = platform.errors

const SENTINEL_API_URL = 'https://api.sentinel-tec.com/api/v3'

const positioningInterval = 5
const reportGpsCycle = 5
class Sentinel {
  async init () {
    const {Parameter: ApiURL} = await getParameterStoreValue('SENTINEL/SENTINEL_API_URL')
    this.baseURL = ApiURL.Value || SENTINEL_API_URL
  }

  // TODO: Move fetchToken to constructor
  async fetchAccessToken () {
    try {
      const { clientId, secretKey } = await this.fetchSentinelConfig()
      const endpoint = `${this.baseURL}/auth/token`
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

  async fetchLockStatus (lockId) {
    try {
      await this.init()
      const endpoint = `${this.baseURL}/lock/query`
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

  async unlock (lockId) {
    await this.init()
    const tokens = await this.fetchAccessToken()
    try {
      const endpoint = `${this.baseURL}/lock/control/open`
      const response = await axios.post(endpoint, querystring.stringify({ lockId }), {
        headers: {
          Authorization: tokens.accessToken,
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
      await this.init()
      const endpoint = `${this.baseURL}/lock/control/startTrip`
      const tokens = await this.fetchAccessToken()
      const response = await axios.post(endpoint, querystring.stringify({ lockId, positioningInterval, reportGpsCycle, tripUUID }), {
        headers: {
          'authorization': tokens.accessToken,
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
      await this.init()
      const endpoint = `${this.baseURL}/lock/control/endTrip`
      const tokens = await this.fetchAccessToken()
      const response = await axios.post(endpoint, querystring.stringify({ lockId }), {
        headers: {
          Authorization: tokens.accessToken,
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
