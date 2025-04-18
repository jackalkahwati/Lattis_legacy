const axios = require('axios')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger

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
      logger(`Error getting token ${error}`)
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async unlock (bike) {
    try {
      const {jwt: token} = await this.getToken()
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
      if (error.response && error.response.data && error.response.data === 'Vehicle already unlocked') {
        return { data: 'success' }
      }
      logger(`Error undocking from Duckt ${error}`)
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
  DucktApiClient
}
