const axios = require('axios')
const platform = require('@velo-labs/platform')
const errors = platform.errors

class LinkaApiClient {
  constructor ({ baseUrl, merchantUrl, apiKey, secretKey }) {
    this.baseUrl = baseUrl
    this.merchantUrl = merchantUrl
    this.apiKey = apiKey
    this.secretKey = secretKey
    this.token = {
      value: null
    }
  }

  async getToken () {
    try {
      const { data: linkaResponse } = await axios.post(
        `${this.baseUrl}/fetch_access_token`,
        {
          api_key: this.apiKey,
          secret_key: this.secretKey
        })
      this.token.value = linkaResponse.data.access_token
      return this.token.value
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async lock (macId) {
    try {
      const { data: linkaResponse } = await axios.post(
        `${this.baseUrl}/command_lock`,
        {
          access_token: `${await this.getToken()}`,
          mac_addr: macId
        })
      if (linkaResponse && linkaResponse.data) {
        const { data: lockResponse } = await this.getCurrentStatus(linkaResponse.data.command_id)
        lockResponse.integration_type = 'Linka'
        return lockResponse
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

  async unlock (macId) {
    try {
      const { data: linkaResponse } = await axios.post(
        `${this.baseUrl}/command_unlock`,
        {
          access_token: `${await this.getToken()}`,
          mac_addr: macId
        })
      if (linkaResponse && linkaResponse.data) {
        const { data: unlockResponse } = await this.getCurrentStatus(linkaResponse.data.command_id)
        unlockResponse.integration_type = 'Linka'
        return unlockResponse
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

  async getCurrentStatus (commandId) {
    try {
      const { data: linkaResponse } = await axios.post(
        `${this.baseUrl}/get_remote_command`,
        {
          access_token: `${await this.getToken()}`,
          command_id: commandId
        })

      // let's return the commandId for polls if need be
      linkaResponse.commnd_id = commandId
      linkaResponse.data.integration_type = 'Linka'
      return linkaResponse
    } catch (error) {
      throw errors.customError(
        error.response.data.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getCurrentLocation (macId) {
    try {
      const { data: linkaResponse } = await axios.put(
        `${this.merchantUrl}/fetch_lock`,
        {
          access_token: await this.getToken(),
          lock_serial_no: macId
        })
      return linkaResponse.data
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
  LinkaApiClient
}
