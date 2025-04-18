const axios = require('axios')
const qs = require('querystring')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger

class SegwayApiClient {
  constructor ({ baseUrl, clientId, clientSecret }) {
    this.baseUrl = baseUrl
    this.clientId = clientId
    this.clientSecret = clientSecret
    this.token = {
      value: null,
      expires: Date.now()
    }
  }

  async getToken () {
    if (this.token.value && this.token.expires > Date.now()) {
      return this.token.value
    }
    const response = await axios.post(
      `${this.baseUrl}/oauth/token`,
      qs.stringify({
        client_id: this.clientId,
        client_secret: this.clientSecret,
        grant_type: 'client_credentials'
      }),
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }
    )
    this.token.value = response.data.access_token
    this.token.expires = Date.now() + response.data.expires_in * 1000
    return this.token.value
  }

  async lock (imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/lock`,
        {
          iotCode: imei
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `bearer ${await this.getToken()}`
          }
        }
      )
      if (response.data.msg !== 'Success') throw new Error(`Failed to lock vehicle: ${response.data.msg}`)
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async unlock (imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/unlock`,
        {
          iotCode: imei
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `bearer ${await this.getToken()}`
          }
        }
      )
      if (response && response.data && response.data.msg !== 'Success') throw new Error(`Failed to lock vehicle: ${response.data.msg}`)
      return (response && response.data) || 'Missing data in response'
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async openCover (imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/vehicle/control/battery/cover`,
        {
          iotCode: imei,
          vehicleCode: imei
        },
        {
          headers: {
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getCurrentStatus (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/v2/vehicle/query/current/status?iotCode=${imei}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getVehicleLocation (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/v2/vehicle/query/current/location?iotCode=${imei}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getVehicleBatteryInfo (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/v2/vehicle/query/current/battery-info?iotCode=${imei}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getVehicleInformation (imei) {
    try {
      const currentStatus = this.getCurrentStatus(imei)
      const location = this.getVehicleLocation(imei)
      const batteryInfo = this.getVehicleBatteryInfo(imei)
      const response = await Promise.all([currentStatus, location, batteryInfo])
      const statusData = response[0] && response[0].data
      const locationData = response[1] && response[1].data
      let responseData = {
        iotCode: imei,
        vehicleCode: imei
      }
      if (statusData) {
        const statusInfo = {
          online: statusData.online,
          locked: statusData.locked,
          lockVoltage: statusData.lockVoltage,
          networkSignal: statusData.networkSignal,
          charging: statusData.charging,
          powerPercent: statusData.powerPercent,
          speedMode: statusData.speedMode,
          speed: statusData.speed,
          odometer: statusData.odometer,
          remainingRange: statusData.remainingRange,
          totalRidingSecs: statusData.totalRidingSecs,
          statusUtcTime: statusData.statusUtcTime
        }
        responseData = { ...responseData, ...statusInfo }
      } else {
        throw new Error('Could not retrieve Segway status')
      }
      if (locationData) {
        let locationsInfo = {
          latitude: locationData.latitude,
          longitude: locationData.longitude,
          satelliteNumber: locationData.satelliteNumber,
          hdop: locationData.hdop,
          altitude: locationData.altitude,
          gpsUtcTime: new Date(locationData.gpsUTCTime).toISOString()
        }
        responseData = { ...responseData, ...locationsInfo }
      }
      return responseData
    } catch (error) {
      throw errors.customError(
        error.message || (error.response.data && error.response.data.message),
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getFirmwareVersion (imei) {
    const response = await axios.get(
      `${this.baseUrl}/api/vehicle/query/version?iotCode=${imei}&vehicleCode=${imei}`,
      {
        headers: {
          'Authorization': `Bearer ${await this.getToken()}`
        }
      }
    )
    return response.data
  }

  async setSpeedMode (speedMode, imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/setting/speed-mode`,
        {
          iotCode: imei,
          speedMode: speedMode
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )

      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async setMaximumSpeedLimit (speedLimit, imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/setting/scooter`,
        {
          iotCode: imei,
          buttonSwitchSpeedMode: 0,
          lowSpeedLimit: speedLimit,
          mediumSpeedLimit: speedLimit,
          highSpeedLimit: speedLimit
        },
        {
          headers: {
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )

      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async toggleLights (headLight, imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/light`,
        {
          headLight: headLight,
          tailLight: headLight,
          iotCode: imei
        },
        {
          headers: {
            'Authorization': `Bearer ${await this.getToken()}`
          }
        }
      )
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  // Mode can be 0 →no setting, 1→off, 2→on
  async setSound (imei, mode) {
    try {
      const makeRequest = async () => {
        const res = await axios.post(
          `${this.baseUrl}/api/v2/vehicle/setting/sound`,
          {
            iotCode: imei,
            workMode: mode
          },
          {
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${await this.getToken()}`
            }
          }
        )
        return res
      }
      let response = await makeRequest()
      let retry = 1
      if (response.data.msg !== 'Success') {
        while (retry <= 3) {
          console.log(`Retrying... ${retry}: ${imei}`)
          const res = await makeRequest()
          response = res
          if (res.data.msg === 'Success') {
            console.log(`Retry ${retry} succeeded... ${imei}`)
            logger(`Retry ${retry} succeeded... ${imei}`)
            break
          } else if (retry === 3 && res.data.msg !== 'Success') {
            logger(`Ooops!! 3 retries did not work for ${imei}. Total failure with ${res.data.msg}`)
            break
          }
          retry++
        }
      }
      console.log(`🚀 Sound mode ${mode} set on ${imei}: ${response.data.msg}`)
      return response.data
    } catch (error) {
      logger('Error::', error.message || error.response.data)
      console.log(`🚀 ~ Error setting sound mode:: on ${imei}`, error.message || error.response.data)
    }
  }
}

module.exports = {
  SegwayApiClient
}
