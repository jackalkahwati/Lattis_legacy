const axios = require('axios')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger
const { getParameterStoreValue } = require('../utils/parameterStore')
const db = require('../db')
const moment = require('moment')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

class EdgeApiClient {
  constructor (fleetId, userId) {
    this.fleetId = fleetId
    this.userId = userId
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

  async startTrip (vehicleId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`

      const config = {
        method: 'post',
        url: `${this.baseUrl}/trip?id=${vehicleId}&user-id=${this.userId}`,
        headers: {
          'Authorization': authToken
        }
      }

      const response = await axios(config)
      if (response.data && response.data.code === 514) throw new Error(response.data.message)
      return response.data
    } catch (err) {
      Sentry.captureException(err)
      logger(`Error { Start trip } from edge ${err}`)
      throw new Error(err.message || 'An error occurred starting Edge trip')
    }
  }

  async endTrip (connectTripId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`

      const config = {
        method: 'delete',
        url: `${this.baseUrl}/trip?id=${connectTripId}&user-id=${this.userId}`,
        headers: {
          'Authorization': authToken
        }
      }

      const response = await axios(config)
      return response.data
    } catch (err) {
      Sentry.captureException(err)
      logger(`Error { End Trip } from edge ${err}`)
      throw new Error(err.message || 'An error occurred starting Edge trip')
    }
  }

  async pauseTrip (connectTripId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`

      const config = {
        method: 'post',
        url: `${this.baseUrl}/trip/pause?id=${connectTripId}&user-id=${this.userId}`,
        headers: {
          'Authorization': authToken
        }
      }

      await axios(config)
      return { data: 'success' }
    } catch (err) {
      Sentry.captureException(err)
      logger(`Error{Pause Trip} from edge ${err}`)
      throw new Error(err.message || 'An error occurred pausing Edge trip')
    }
  }

  async resumeTrip (connectTripId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`

      const config = {
        method: 'post',
        url: `${this.baseUrl}/trip/resume?id=${connectTripId}&user-id=${this.userId}`,
        headers: {
          'Authorization': authToken
        }
      }

      const response = await axios(config)
      return response.data
    } catch (err) {
      Sentry.captureException(err)
      logger(`Error{Resume Trip} from edge ${err}`)
      throw new Error(err.message || 'An error occurred resuming Edge trip')
    }
  }

  // These function is used in MoM(manual override mode). Call this when when the vehicle is not in trip anymore
  async unlock (vehicleId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`
      const config = {
        method: 'post',
        url: `${this.baseUrl}/vehicle/unlock?id=${vehicleId}&user-id=${this.userId}`,
        headers: {
          'Authorization': authToken
        }
      }

      await axios(config)
      return {data: 'success'}
    } catch (error) {
      Sentry.captureException(error)
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

  // These function is used in MoM(manual override mode). Call this when when the vehicle is not in trip anymore
  async lock (vehicleId) {
    try {
      await this.init()
      const authToken = `Bearer ${this.apiKey}`

      const config = {
        method: 'post',
        url: `${this.baseUrl}/vehicle/lock?id=${vehicleId}&user-id=${this.userId}`,
        headers: {
          'Authorization': authToken
        }
      }

      await axios(config)
      return { data: 'success' }
    } catch (err) {
      Sentry.captureException(err)
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
      const URL = `${this.baseUrl}/vehicle?id=${vehicleId}&user-id=${this.userId}`
      let result = await axios.get(URL,
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.apiKey}`
          }
        }
      )
      let finalObj = { id: vehicleId }
      const vehicle = result && result.data && result.data.vehicle
      if (vehicle) {
        finalObj.powerPercent = vehicle.power && vehicle.power.batteries && vehicle.power.batteries[0] && vehicle.power.batteries[0].percent
        if (vehicle.location) {
          finalObj.latitude = vehicle.location.lat
          finalObj.longitude = vehicle.location.lon
          finalObj.gpsUtcTime = vehicle.location.updated || moment().unix()
        }

        finalObj.odometer = vehicle.odometer
        finalObj.mode = vehicle.mode
        if (vehicle.trip) finalObj.trip = vehicle.trip
        finalObj.locked = vehicle.locked
        finalObj.online = true
      }
      return finalObj
    } catch (err) {
      Sentry.captureException(err)
      logger(`Error getting vehicle from edge ${err}`)
    }
  }
}

module.exports = {
  EdgeApiClient
}
