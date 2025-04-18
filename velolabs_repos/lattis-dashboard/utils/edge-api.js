const axios = require('axios')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger
const { getParameterStoreValue } = require('../utils/parameterStore')
const db = require('../db')

class EdgeApiClient {
  constructor (fleetId) {
    this.fleetId = fleetId
  }

  async init () {
    const fleetData = await db.main('integrations').where({ fleet_id: this.fleetId, integration_type: 'Edge' }).first()
    if (!fleetData || !fleetData.api_key) {
      throw new Error('Missing API credentials. Please check the integration')
    }
    const apiKey = fleetData.api_key
    this.apiKey = apiKey
    const EdgeBaseURL = await getParameterStoreValue('EDGE/URLS/BASE_URL')
    this.baseUrl = (EdgeBaseURL && EdgeBaseURL.Parameter && EdgeBaseURL.Parameter.Value)
  }

  async unlock (vehicleId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`
      const config = {
        method: 'post',
        url: `${this.baseUrl}/vehicle/unlock?id=${vehicleId}`,
        headers: {
          'Authorization': authToken
        }
      }

      await axios(config)
      return {data: 'success'}
    } catch (error) {
      if (error.response && error.response.data && error.response.data === 'Vehicle already unlocked') {
        return { data: 'success' }
      }
      logger(`Error undocking from edge ${error}`)
      throw errors.customError(
        error.message || error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async lock (vehicleId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`

      const config = {
        method: 'post',
        url: `${this.baseUrl}/vehicle/lock?id=${vehicleId}`,
        headers: {
          'Authorization': authToken
        }
      }

      await axios(config)
      return { data: 'success' }
    } catch (err) {
      logger(`Error from edge ${err}`)
      throw errors.customError(
        err.message || err.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getVehicleInformation (vehicleId) {
    try {
      await this.init()
      const URL = `${this.baseUrl}/vehicle?id=${vehicleId}`
      let result = await axios.get(URL,
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.apiKey}`
          }
        }
      )
      let finalObj = {}
      const vehicle = result && result.data && result.data.vehicle
      if (vehicle) {
        finalObj.powerPercent = vehicle.power && vehicle.power.batteries && vehicle.power.batteries[0] && vehicle.power.batteries[0].percent
        finalObj.latitude = vehicle.location && vehicle.location.lat
        finalObj.longitude = vehicle.location && vehicle.location.lon
        finalObj.odometer = vehicle.odometer
      }
      return finalObj
    } catch (err) {
      logger(`Error getting vehicle from edge ${err}`)
    }
  }
}

module.exports = {
  EdgeApiClient
}
