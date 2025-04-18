const axios = require('axios')
const db = require('../db')
const KISI_API_URL = 'https://api.kisi.io'
const { getParameterStoreValue } = require('../utils/parameterStore')
class Kisi {
  async init (fleetId) {
    try {
      const {Parameter} = await getParameterStoreValue('KISI_BASE_URL')
      this.baseUrl = Parameter.Value || KISI_API_URL
      const integration = await db
        .main('integrations')
        .where({ integration_type: 'kisi', fleet_id: fleetId })
        .first()
      if (!integration) {
        throw new Error(`Kisi integration not found for fleet ${fleetId}`)
      }
      this.apiKey = integration.api_key
    } catch (error) {
      throw new Error(
        error.message ||
        `Error initializing Kisi integration for fleet ${fleetId}`
      )
    }
  }

  async fetchLocks (fleetId) {
    try {
      await this.init(fleetId)
      const endpoint = `${this.baseUrl}/locks`
      const authHeader = `KISI-LOGIN ${this.apiKey}`

      const response = await axios.get(endpoint, {
        headers: {
          Authorization: authHeader,
          'Content-Type': 'application/json',
          Accept: 'application/json'
        }
      })
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error fetching Kisi locks for fleet ${this.fleetId}: `
      )
    }
  }

  async fetchLock (fleetId, lockId) {
    try {
      await this.init(fleetId)
      const endpoint = `${this.baseUrl}/locks/${lockId}`
      const authHeader = `KISI-LOGIN ${this.apiKey}`

      const response = await axios.get(endpoint, {
        headers: {
          Authorization: authHeader,
          'Content-Type': 'application/json',
          Accept: 'application/json'
        }
      })
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error fetching Kisi locks for fleet ${this.fleetId}: `
      )
    }
  }

  async lock (fleetId, lockId) {
    try {
      await this.init(fleetId)
      const endpoint = `${this.baseUrl}/locks/${lockId}/lock_down`
      const authHeader = `KISI-LOGIN ${this.apiKey}`

      const config = {
        method: 'post',
        url: endpoint,
        headers: { 'Authorization': authHeader, 'Content-Type': 'application/json', Accept: 'application/json' }
      }
      const response = await axios(config)
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error fetching Kisi locks for fleet ${this.fleetId}: `
      )
    }
  }

  async unlock (fleetId, lockId) {
    try {
      await this.init(fleetId)
      const endpoint = `${this.baseUrl}/locks/${lockId}/unlock`
      const authHeader = `KISI-LOGIN ${this.apiKey}`

      const response = await axios.post(
        endpoint,
        {
          lock: {
            proximity_proof: 'string'
          }
        },
        {
          headers: {
            Authorization: authHeader,
            'Content-Type': 'application/json',
            Accept: 'application/json'
          }
        }
      )
      return response.data
    } catch (error) {
      const message = error.response && error.response.data && error.response.data.message
      throw new Error(
        message || `Error fetching Kisi locks for fleet ${this.fleetId}: `
      )
    }
  }
}

module.exports = {
  Kisi
}
