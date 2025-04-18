const platform = require('@velo-labs/platform')
const Api = require('mg-api-js')
const logger = platform.logger
const db = require('../db')
const errors = platform.errors

class GeotabApiClient {
  constructor ({ userName, password, sessionId, database, serverName }) {
    this.userName = userName
    this.password = password
    this.database = database
    this.sessionId = sessionId
    this.serverName = serverName
    this.authentication = {
      credentials: {
        userName: this.userName,
        sessionId: this.sessionId,
        database: this.database
      },
      path: this.serverName
    }
    this.options = {
      fullResponse: true
    }
  }

  async getDeviceId (controller) {
    const api = new Api(this.authentication, this.options)
    try {
      const {data} = await api.call('Get', {typeName: 'Device'})
      if (data.result) {
        const [targetDevice] = data.result.filter(device => device.serialNumber === controller.key.replace(/-/g, ''))
        return targetDevice
      }

      if (data.error) {
        this.authentication.credentials.password = this.password
        delete this.authentication.credentials.sessionId
        const api = new Api(this.authentication, this.options)
        try {
          const {data} = await api.call('Get', {typeName: 'Device'})
          if (data.result) {
            const [targetDevice] = data.result.filter(device => device.serialNumber === controller.key.replace(/-/g, ''))
            const {data: sessionData} = await api.getSession()
            targetDevice.sessionId = sessionData.result.credentials.sessionId
            try {
              await db.main('integrations')
                .update({session_id: sessionData.result.credentials.sessionId})
                .where({fleet_id: controller.fleet_id, integration_type: 'geotab'})
            } catch (error) {
              logger(`Warn: Unable to insert sessionId for fleet ${controller.fleet_id}: ${error}`)
              throw errors.customError(
                `Unable to update sessionId for ${controller.key}`,
                platform.responseCodes.InternalServer,
                'InternalServer',
                false
              )
            }
            return targetDevice
          }
          if (data.error) {
            throw errors.customError(
              `Unable to get deviceID for controller, ${data.error}`,
              platform.responseCodes.BadRequest,
              'BadRequest',
              false
            )
          }
        } catch (error) {
          throw errors.customError(
            `There is no deviceId for ${controller.key}, ${error}`,
            platform.responseCodes.ResourceNotFound,
            'ResourceNotFound',
            false
          )
        }
      }
    } catch (error) {
      throw errors.customError(
        `There is no deviceId for ${controller.key}, ${error}`,
        platform.responseCodes.ResourceNotFound,
        'ResourceNotFound',
        false
      )
    }
  }

  async lock (controller) {
    let authentication = this.authentication
    const options = this.options
    const device = await this.getDeviceId(controller)
    if (device.sessionId) {
      authentication.credentials.sessionId = device.sessionId
    }
    const api = new Api(authentication, options)

    try {
      const {data} = await api.call('Add', {
        typeName: 'TextMessage',
        entity: {
          device: {
            id: device.id
          },
          messageContent: {
            contentType: 'IoxOutput',
            isRelayOn: true,
            channel: '1'
          },
          isDirectionToVehicle: true
        }
      })
      if (data.result) {
        try {
          const { data: messageData } = await api.call('Get', {
            typeName: 'TextMessage',
            search: {
              id: data.result
            }
          })
          if (messageData.result) {
            return messageData.result
          }
          if (messageData.error) {
            return messageData.error
          }
        } catch (error) {
          throw errors.customError(
            `Unable to get status for Geotab with id ${device.id}: ${error}`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
      }
      if (data.error) {
        return data.error
      }
    } catch (error) {
      throw errors.customError(
        `Unable to lock Geotab with Id ${device.id}: ${error}`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
  }

  async unlock (controller) {
    let authentication = this.authentication
    const options = this.options
    const device = await this.getDeviceId(controller)
    if (device.sessionId) {
      authentication.credentials.sessionId = device.sessionId
    }
    const api = new Api(authentication, options)

    try {
      const {data} = await api.call('Add', {
        typeName: 'TextMessage',
        entity: {
          device: {
            id: device.id
          },
          messageContent: {
            contentType: 'IoxOutput',
            isRelayOn: false,
            channel: '1'
          },
          isDirectionToVehicle: true
        }
      })
      if (data.result) {
        try {
          const { data: messageData } = await api.call('Get', {
            typeName: 'TextMessage',
            search: {
              id: data.result
            }
          })
          if (messageData.result) {
            return messageData.result
          }
          if (messageData.error) {
            return messageData.error
          }
        } catch (error) {
          throw errors.customError(
            `Unable to get status for Geotab with id ${device.id}: ${error}`,
            platform.responseCodes.BadRequest,
            'BadRequest',
            false
          )
        }
      }
      if (data.error) {
        return data.error
      }
    } catch (error) {
      throw errors.customError(
        `Unable to unlock Geotab with Id ${device.id}: ${error}`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
  }

  async getCurrentStatus (controller) {
    let authentication = this.authentication
    const options = this.options
    const device = await this.getDeviceId(controller)
    if (device.sessionId) {
      authentication.credentials.sessionId = device.sessionId
    }
    const api = new Api(authentication, options)
    try {
      const { data: statusInfoData } = await api.call('Get', {
        typeName: 'DeviceStatusInfo',
        search: {
          deviceSearch: {
            id: device.id
          }
        }
      })
      if (statusInfoData.result) {
        return statusInfoData.result
      }
      if (statusInfoData.error) {
        return statusInfoData.error
      }
    } catch (error) {
      throw errors.customError(
        `Unable to get status for Geotab with id ${device.id}: ${error}`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
  }
  async getCurrentLocation (controller) {
    let authentication = this.authentication
    const options = this.options
    let time = new Date()
    let fromDate = new Date(time.setSeconds(time.getSeconds() - 30))
    const device = await this.getDeviceId(controller)
    if (device.sessionId) {
      authentication.credentials.sessionId = device.sessionId
    }
    const api = new Api(authentication, options)
    try {
      const { data: locationInfoData } = await api.call('Get', {
        typeName: 'LogRecord',
        search: {
          deviceSearch: {
            id: device.id
          },
          fromDate: fromDate.toISOString(),
          toDate: time.toISOString()
        }
      })
      if (locationInfoData.result) {
        return locationInfoData.result
      }
      if (locationInfoData.error) {
        return locationInfoData.error
      }
    } catch (error) {
      throw errors.customError(
        `Unable to get location for Geotab with id ${device.id}: ${error}`,
        platform.responseCodes.BadRequest,
        'BadRequest',
        false
      )
    }
  }
}

module.exports = {
  GeotabApiClient
}
