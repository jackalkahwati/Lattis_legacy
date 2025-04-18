const axios = require('axios')
require('querystring')
const platform = require('@velo-labs/platform')
const db = require('../db')
const errors = platform.errors

class ParcelHiveApiClient {
  constructor ({ accessToken, lockerId, boxId }) {
    this.accessToken = accessToken
    this.lockerId = lockerId
    this.boxId = boxId
    this.baseUrl = 'https://api.cyberhive.cloud'
  }

  // This function checks if current token is valid, updates stored token of invalid
  async validateToken (accessToken, controller) {
    try {
      const { data } = await axios.get(
        `${this.baseUrl}/lockers/all`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`
          }
        }
      )
      if (data.message && data.message.includes('expired!')) {
        // save the new token to integrations
        await db.main('integrations')
          .update({
            metadata: JSON.stringify({
              accessToken: data.token.trim()
            })
          }).where({fleet_id: controller.fleet_id})
        // return the new token
        return data.token
      }
      // accessToken is still valid, so we reuse it.
      return accessToken
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getLocker (lockerId, controller) {
    try {
      const { data: locker } = await axios.get(
        `${this.baseUrl}/locker?id=${lockerId}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.validateToken(this.accessToken, controller)}`
          }
        }
      )
      return locker
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getBox (lockerId, boxId, controller) {
    try {
      const { data: boxes } = await axios.get(
        `${this.baseUrl}/locker/getboxes?id=${lockerId}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.validateToken(this.accessToken, controller)}`
          }
        }
      )
      const box = boxes.find(box => box.box_id === boxId)
      return box
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async open (controller) {
    try {
      const { data } = await axios.post(
        `${this.baseUrl}/locker/openboxes?id=${this.lockerId}&box_id=[${this.boxId}]`, '',
        {
          headers: {
            'Authorization': `Bearer ${await this.validateToken(this.accessToken, controller)}`
          }
        }
      )
      data.status = data.status === true ? 1 : 0
      return data
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }
}

module.exports = {
  ParcelHiveApiClient
}
