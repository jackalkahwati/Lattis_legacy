/** @format */

const mqtt = require('mqtt')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const { GROW_SERVICE_URL, GROW_USERNAME, GROW_PASSWORD } = process.env
const db = require('../db')
const bikeFleetHandler = require('../model_handlers/bike-fleet-handler')

const client = mqtt.connect(GROW_SERVICE_URL, {
  username: GROW_USERNAME,
  password: GROW_PASSWORD
})

const getGrowDevices = async () => {
  try {
    const devices = await db.main('controllers').where({ vendor: 'Grow' })
    return devices.map((d) => d.key)
  } catch (error) {
    logger('Error getting grow devices...')
  }
}

client.on('connect', async () => {
  try {
    const deviceIds = await getGrowDevices()
    if (deviceIds && deviceIds.length) {
      for (let deviceId of deviceIds) {
        client.subscribe(`s/a/17/${deviceId}`)
        client.subscribe(`s/h/17/${deviceId}`)
      }
    }
  } catch (error) {
    logger('An error occurred iterating Grow devices', error.message)
  }
})

const subscribeToPubs = () => {
  client.on('message', async (topic, message) => {
    try {
      const startsWith = topic.slice(0, 4)
      switch (startsWith) {
        case 's/h/': {
          // Heartbeat data
          let heartbeatData = JSON.parse(Buffer.from(message).toString())
          const deviceId = topic.split('/').slice(-1)[0]
          heartbeatData = { _id: deviceId, ...heartbeatData }
          const controller = await db
            .main('controllers')
            .where({ key: deviceId })
            .first()
          if (!controller) {
            throw new Error(
              `Could not find a GROW controller for the ID deviceId ${deviceId}`
            )
          }

          await db
            .main('controllers')
            .update({
              battery_level: heartbeatData.batiot,
              gps_log_time: heartbeatData.timestamp,
              latitude: heartbeatData['position.latitude'],
              longitude: heartbeatData['position.longitude']
            })
            .where({ key: deviceId })
          await db
            .main('bikes')
            .update({
              bike_battery_level: heartbeatData.batsco,
              latitude: heartbeatData['position.latitude'],
              longitude: heartbeatData['position.longitude']
            })
            .where({ bike_id: controller.bike_id })
          break
        }

        case 's/a/': {
          let msg = Buffer.from(message).toString()
          msg = JSON.parse(msg)
          const deviceId = topic.split('/').slice(-1)[0]
          if (msg.command && msg.command === 222) {
            bikeFleetHandler.getAndApplyGrowSettings(deviceId)
          }
          break
        }

        default:
          break
      }
    } catch (error) {
      const startsWith = topic.slice(0, 4)
      logger(
        JSON.stringify({
          source:
            startsWith === 's/h' ? 'Heartbeat Command' : 'Published Command',
          message:
            error.message ||
            `An error occurred executimg ${startsWith} command on topic ${topic}`,
          trace: error
        })
      )
    }
  })
}

/**
 *
 *
 * @param {string} topic Topic to publish to reps the device ID
 * @param {string} command Command to execute
 * @param {string} callback Response
 */
const executeCommand = async (topic, command, callback) => {
  try {
    client.publish(topic, command, (error) => {
      if (error) {
        logger(
          `An error occurred sending the command ${command} on topic ${topic}`
        )
        callback(error)
      } else {
        logger(
          `Command ${command} to topic ${topic} send. Success is not guaranteed`
        )
        callback(null, 'Success')
      }
    })
  } catch (error) {
    logger(
      JSON.stringify(
        {
          source: 'Grow Heartbeat',
          message:
            error.message ||
            'An error occurred updating Grow device from heartbeat',
          trace: error
        },
        null,
        2
      )
    )
    callback(
      JSON.stringify(
        {
          source: 'Grow Heartbeat',
          message:
            error.message ||
            'An error occurred updating Grow device from heartbeat',
          trace: error
        },
        null,
        2
      )
    )
  }
}

subscribeToPubs()

module.exports = { executeCommand, client }
