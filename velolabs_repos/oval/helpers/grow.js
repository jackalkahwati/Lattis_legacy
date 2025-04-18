/** @format */

const mqtt = require('mqtt')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const { GROW_SERVICE_URL, GROW_USERNAME, GROW_PASSWORD } = process.env
const db = require('../db')

const client = mqtt.connect(GROW_SERVICE_URL, {
  username: GROW_USERNAME,
  password: GROW_PASSWORD
})

const getGrowDevices = async () => {
  try {
    const devices = await db.main('controllers').where({'vendor': 'Grow'})
    return devices.map(d => d.key)
  } catch (error) {
    logger('Error getting grow devices...', error)
  }
}

client.on('connect', async () => {
  try {
    const deviceIds = await getGrowDevices()
    if (deviceIds && deviceIds.length) {
      for (let deviceId of deviceIds) {
        client.subscribe(`s/a/17/${deviceId}`)
      }
    }
  } catch (error) {
    logger('An error occurred iterating Grow devices', error.message)
  }
})

/**
 * Execute a command on a GROW device
 *
 * @param {string} topic Topic to publish to reps the device ID
 * @param {string} command Command to execute
 * @param {string} callback Response
 */
const executeCommand = async (topic, command, callback) => {
  try {
    client.publish(topic, command, (error) => {
      if (error) {
        logger(`An error occurred executing the command ${command} on topic ${topic}`)
        callback(error)
      } else {
        logger(`Command ${command} to topic ${topic} executed successfully`)
        callback(null, 'Success')
      }
    })
  } catch (error) {
    logger(JSON.stringify({
      source: 'Grow Heartbeat',
      message: error.message || 'An error occurred updating Grow device from heartbeat',
      trace: error
    }, null, 2))
    callback(JSON.stringify({
      source: 'Grow Heartbeat',
      message: error.message || 'An error occurred updating Grow device from heartbeat',
      trace: error
    }, null, 2))
  }
}

module.exports = { executeCommand, client }
