const axios = require('axios')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger
const envConfig = require('../envConfig')
class ActonAPIClient {
  constructor ({ baseUrl, MAPTEX_API_KEY, cached = 1, wait = 60 }) {
    this.baseUrl = baseUrl
    this.MAPTEX_API_KEY = MAPTEX_API_KEY
    this.cached = cached
    this.wait = wait
  }

  async getCurrentStatus (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/current-status?cached=${this.cached}&wait=${this.wait}`
      const config = {
        method: 'get',
        url: path,
        headers: {
          'MAPTEX-API-KEY': this.MAPTEX_API_KEY
        }
      }
      const response = await axios(config)
      return response.data
    } catch (error) {
      logger('ACTON getCurrentStatus Error::', error.response.data)
      logger('Error::::', error)
      throw new Error(error.message || (error.response && error.response.data && error.response.data.errorDetails) || `Error getting vehicle status ${imei}`)
    }
  }

  // Starts and unlocks
  async startVehicle (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/start-vehicle?cached=${this.cached}&wait=${this.wait}`
      const config = {
        method: 'post',
        url: path,
        headers: {
          'MAPTEX-API-KEY': this.MAPTEX_API_KEY
        }
      }
      const response = await axios(config).catch(error => {
        throw errors.customError(
          error.message || (error.response && error.response.data && error.response.data.errorDetails) || `Failed to start vehicle ${imei}`,
          platform.responseCodes.InternalServer,
          'Device Error',
          false
        )
      })
      return response.data
    } catch (error) {
      logger('ACTON start Vehicle Error::', error.response.data)
      throw errors.customError(
        error.message || (error.response && error.response.data && error.response.data.errorDetails) || `Failed to start vehicle ${imei}`,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  // Stops and locks
  async stopVehicle (imei) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/stop-vehicle?cached=${this.cached}&wait=${this.wait}`
      const config = {
        method: 'post',
        url: path,
        headers: {
          'MAPTEX-API-KEY': this.MAPTEX_API_KEY
        }
      }
      const response = await axios(config)
      return response.data
    } catch (error) {
      logger('ACTON stopVehicle Error::', error.response.data)
      throw errors.customError(
        error.message || (error.response && error.response.data && error.response.data.errorDetails) || `Failed to stop ACTON vehicle ${imei}`,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  // Set speed limit
  async setSpeedLimit (imei, limit) {
    try {
      const path = `${this.baseUrl}/devices/${imei}/actions/set-speed-limit?cached=${this.cached}&wait=${this.wait}`
      const config = {
        method: 'post',
        url: path,
        data: { SpeedLimit: limit },
        headers: {
          'MAPTEX-API-KEY': this.MAPTEX_API_KEY
        }
      }
      const response = await axios(config)
      return response.data
    } catch (error) {
      logger('ACTON set speed limit Error::', error.response.data)
      throw errors.customError(
        error.message || (error.response && error.response.data && error.response.data.errorDetails) || `Failed to set speed limit ACTON vehicle ${imei}`,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }
}

const ActonAPI = new ActonAPIClient({
  baseUrl: envConfig('ACTON_API_URL'),
  MAPTEX_API_KEY: envConfig('MAPTEX_API_KEY')
})

// Teltonika
const TeltonikaApiClient = new ActonAPIClient({
  baseUrl: envConfig('ACTON_API_URL'),
  MAPTEX_API_KEY: envConfig('OMNI_MAPTEX_API_KEY')
})

// Omni IoT
const OmniIoTApiClient = new ActonAPIClient({
  baseUrl: envConfig('ACTON_API_URL'),
  MAPTEX_API_KEY: envConfig('OMNI_MAPTEX_API_KEY')
})

// Omni Lock API
const OmniAPI = new ActonAPIClient({
  baseUrl: envConfig('OMNI_API_URL'),
  MAPTEX_API_KEY: envConfig('OMNI_MAPTEX_API_KEY')
})

// Omni Lock API
const OkaiAPIClient = new ActonAPIClient({
  baseUrl: envConfig('OKAI_API_URL'),
  MAPTEX_API_KEY: envConfig('OKAI_MAPTEX_API_KEY')
})

module.exports = { ActonAPIClient, ActonAPI, OmniAPI, TeltonikaApiClient, OmniIoTApiClient, OkaiAPIClient }
