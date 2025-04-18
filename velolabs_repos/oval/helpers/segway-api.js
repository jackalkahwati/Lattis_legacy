const axios = require('axios')
const qs = require('querystring')
const platform = require('@velo-labs/platform')
const errors = platform.errors
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
      const token = await this.getToken()
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/lock`,
        {
          iotCode: imei
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `bearer ${token}`
          }
        }
      )
      if (response.data.msg !== 'Success') throw new Error(`Failed to lock vehicle: ${response.data.msg}`)
      return response.data
    } catch (error) {
      throw errors.customError(
        error.message || error.response.data || 'Failed to lock the vehicle',
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async unlock (imei) {
    try {
      const token = await this.getToken()
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/unlock`,
        {
          iotCode: imei
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `bearer ${token}`
          }
        }
      )
      if (response.data.msg !== 'Success') throw new Error(`Failed to lock vehicle: ${response.data.msg}`)
      return response.data
    } catch (error) {
      throw errors.customError(
        error.message || error.response.data || 'Failed to unlock the vehicle',
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
            'Authorization': `bearer ${await this.getToken()}`
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
            'Authorization': `bearer ${await this.getToken()}`
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
            'Authorization': `bearer ${await this.getToken()}`
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
        error.response.data && error.response.data.message,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getFirmwareVersion (imei) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/api/vehicle/query/version?iotCode=${imei}&vehicleCode=${imei}`,
        {
          headers: {
            'Authorization': `bearer ${await this.getToken()}`
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

  async alertOutOfGeofence (imei) {
    try {
      const response = await axios.post(
        `${this.baseUrl}/api/v2/vehicle/control/sound`, { iotCode: imei, controlType: 1 },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `bearer ${await this.getToken()}`
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
            'Authorization': `bearer ${await this.getToken()}`
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
            'Content-Type': 'application/json',
            'Authorization': `bearer ${await this.getToken()}`
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
            'Content-Type': 'application/json',
            'Authorization': `bearer ${await this.getToken()}`
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
}

module.exports = {
  SegwayApiClient
}
