const axios = require('axios')
const { getParameterStoreValuesByPath, getParameterStoreValue } = require('./parameterStore')
const environment = process.env.CURRENT_ENVIRONMENT
const crypto = require('crypto')

const GREENRIDERS_API_URL = 'http://3.108.179.207:3000'

const algorithm = 'aes-256-ctr'

const encrypt = async (text) => {
  const iv = crypto.randomBytes(16)

  const { Parameter } = await getParameterStoreValue('ENCRYPTION_KEY', 'ca-central-1')
  const cipher = crypto.createCipheriv(algorithm, Parameter.Value, iv)

  const encrypted = Buffer.concat([cipher.update(text), cipher.final()])

  return {
    iv: iv.toString('hex'),
    content: encrypted.toString('hex')
  }
}
class Greenriders {
  async _init () {
    try {
      const { Parameters: keys } = await getParameterStoreValuesByPath('greenriders', 'ca-central-1')
      const keyName = `/env/${environment}/greenriders`
      const clientId = keys.find(key => key.Name === `${keyName}/CLIENT_ID`).Value
      const clientKey = keys.find(key => key.Name === `${keyName}/CLIENT_KEY`).Value
      const baseURL = keys.find(key => key.Name === `${keyName}/GREENRIDERS_BASE_URL`).Value || GREENRIDERS_API_URL
      this.baseURL = baseURL
      return {clientKey, clientId, baseURL}
    } catch (error) {
      throw new Error(
        error.message ||
        `Error initializing Greenriders integration for fleet`
      )
    }
  }

  async getJWTToken () {
    const { clientId, clientKey, baseURL } = await this._init()
    const endpoint = `${baseURL}/api/v1/auth/login`
    try {
      const { data } = await axios.post(endpoint, { ClientId: clientId, ClientKey: clientKey })
      return data.data
    } catch (error) {
      throw error || new Error('An error occurred while fetching JWT token')
    }
  }

  async getStations () {
    try {
      const { accessToken } = await this.getJWTToken()
      const { data } = await axios.get(`${this.baseURL}/api/v1/devices/stations`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
          Accept: 'application/json'
        }
      })
      if (!data.status) throw new Error(data.message)
      return data.data
    } catch (error) {
      throw new Error(error.message || 'An error occurred while fetching the stations')
    }
  }

  async registerNewStation (stationUID, password, clientId) {
    try {
      const { accessToken } = await this.getJWTToken()
      const config = {
        method: 'post',
        data: {
          StationUID: stationUID,
          password: await encrypt(password),
          clientId: await encrypt(clientId)
        },
        url: `${this.baseURL}/api/v1/devices/registerorupdatestation`,
        headers: { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const { data } = await axios(config)
      return data.data
    } catch (error) {
      throw error || new Error('An error occurred while fetching JWT token')
    }
  }

  async getStationById (stationId) {
    try {
      const { accessToken } = await this.getJWTToken()
      const { data } = await axios.get(`${this.baseURL}/api/v1/devices/stationsinfo/${stationId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
          Accept: 'application/json'
        }
      })
      if (!data.status) throw new Error(data.message)
      return data.data
    } catch (error) {
      throw new Error('An error occurred while fetching the station info')
    }
  }

  // Prefer this. It sends an actual request to the station.
  // getStationInfo gets stored info from the DB
  async getStationInfo (stationId) {
    try {
      const { accessToken } = await this.getJWTToken()
      const { data } = await axios.get(`${this.baseURL}/api/v1/devices/getInfo/${stationId}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
          Accept: 'application/json'
        }
      })
      if (!data.status) throw new Error(data.message)
      const response = {...data.data}
      delete data.data
      return { ...response, ...data }
    } catch (error) {
      throw new Error('An error occurred while fetching the station info')
    }
  }

  async unlockStation (stationId, scooterId) {
    try {
      const { accessToken } = await this.getJWTToken()
      const config = {
        method: 'put',
        data: {
          ScooterID: scooterId,
          StationUID: stationId
        },
        url: `${this.baseURL}/api/v1/devices/deviceunlock`,
        headers: { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const { data } = await axios(config)
      if (!data.status) throw new Error(data.message)
      return data
    } catch (error) {
      throw new Error(error.message || 'An error occurred while unlocking the station')
    }
  }

  async lockToStation (stationId, scooterId) {
    try {
      const { accessToken } = await this.getJWTToken()

      const config = {
        method: 'put',
        data: {
          ScooterID: scooterId,
          StationUID: stationId
        },
        url: `${this.baseURL}/api/v1/devices/devicelock`,
        headers: { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const { data } = await axios(config)
      if (!data.status) throw new Error(data.message)
      return data
    } catch (error) {
      throw new Error(error.message || 'An error occurred while unlocking the station')
    }
  }

  async resetStation (stationUID) {
    try {
      const { accessToken } = await this.getJWTToken()
      const config = {
        method: 'post',
        data: {
          StationUID: stationUID
        },
        url: `${this.baseURL}/api/v1/devices/reset`,
        headers: { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const { data } = await axios(config)
      if (!data.status) throw new Error(data.message)
      return data
    } catch (error) {
      throw new Error(error.message || 'An error occurred')
    }
  }

  async bookStation (stationUID) {
    try {
      const { accessToken } = await this.getJWTToken()
      const config = {
        method: 'post',
        data: {
          StationUID: stationUID
        },
        url: `${this.baseURL}/api/v1/devices/setbooking`,
        headers: { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const { data } = await axios(config)
      if (!data.status) throw new Error(data.message)
      return data
    } catch (error) {
      throw new Error(error.message || 'An error occurred')
    }
  }

  async setStationToMaintenance (stationUID) {
    try {
      const { accessToken } = await this.getJWTToken()
      const config = {
        method: 'post',
        data: {
          StationUID: stationUID
        },
        url: `${this.baseURL}/api/v1/devices/setmaintenance`,
        headers: { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const { data } = await axios(config)
      if (!data.status) throw new Error(data.message)
      return data
    } catch (error) {
      throw new Error(error.message || 'An error occurred')
    }
  }
}

module.exports = Greenriders
