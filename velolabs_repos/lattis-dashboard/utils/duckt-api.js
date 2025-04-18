const axios = require('axios')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const randomNumber = require('../utils/random-code-generator')

class DucktApiClient {
  constructor ({ baseUrl, password, userName, callbackUrl }) {
    this.baseUrl = baseUrl
    this.password = password
    this.userName = userName
    this.callbackUrl = callbackUrl
  }

  async getToken () {
    try {
      const {data: token} = await axios.post(
        `${this.baseUrl}/authenticate`,
        {
          password: this.password,
          username: this.userName
        })
      return token
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async registerStation (stationUuid, latitude, longitude) {
    const {jwt: token} = await this.getToken()
    try {
      const { data: stationResponse } = await axios.post(
        `${this.baseUrl}/stations/register?station=${stationUuid}`,
        {
          'name': randomNumber(),
          'lat': latitude,
          'lng': longitude
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token
          }
        }
      )
      return stationResponse
    } catch (error) {
      throw new Error(error.response.data || error.message)
    }
  }

  async addAdapter (adapterUid, bike) {
    const {jwt: token} = await this.getToken()
    try {
      const { data: adapterResponse } = await axios.post(
        `${this.baseUrl}/vehicles`,
        {
          'adapter': adapterUid,
          'name': bike.bike_id,
          'vehicleType': bike.vehicleType,
          'model': bike.model,
          'callbackUrl': this.callbackUrl
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token
          }
        }
      )
      return adapterResponse
    } catch (error) {
      throw errors.customError(
        error.response.data.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async unlock (bike) {
    const {jwt: token} = await this.getToken()
    try {
      await axios.post(
        `${this.baseUrl}/unlock?vehicle=${bike.bike_id}`,
        {
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token
          }
        }
      )
      return {data: 'success'}
    } catch (error) {
      throw errors.customError(
        error.response.data.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }
}

module.exports = {
  DucktApiClient
}
