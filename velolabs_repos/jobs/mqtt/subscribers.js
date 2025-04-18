const mqtt = require('mqtt')
const got = require('got')
const { generateToken } = require('../utils/auth')
const GPS_SERVICE_URL = process.env.GPS_SERVICE_URL

const { GROW_SERVICE_URL, GROW_USERNAME, GROW_PASSWORD } = process.env

const subscribeToMQTTEvents = () => {
  try {
    const client = mqtt.connect(GROW_SERVICE_URL, {
      username: GROW_USERNAME,
      password: GROW_PASSWORD
    })

    client.on('connect', function () {
      console.log('Connected....')
      client.subscribe('s/h/#', function (err) {
        if (!err) {
          console.log('sub to s/h/#')
        }
      })

      client.subscribe('s/a/#', function (err) {
        if (!err) {
          console.log('sub to s/a/#')
        }
      })
    })

    client.on('message', async (topic, message) => {
      // message is Buffer
      try {
        const startsWith = topic.slice(0, 4)
        const jwtToken = generateToken()
        const headers = { 'x-access-token': jwtToken, 'Content-Type': 'application/json' }
        switch (startsWith) {
          case 's/h/': {
            const heartbeatData = JSON.parse(Buffer.from(message).toString())
            await got.post(`${GPS_SERVICE_URL}/grow-events/heartbeat`, {
              headers,
              json: {
                message: heartbeatData,
                topic
              }
            })
            break
          }
          case 's/a/': {
            const msg = Buffer.from(message).toString()
            if (msg === 'Burglar alarm' || msg === '30hkm+ alarm') {
              await got.post(`${GPS_SERVICE_URL}/grow-events/create-theft-ticket`, {
                headers,
                json: { topic }
              })
            }
            break
          }
          default:
            break
        }
      } catch (error) {
        console.log('🚀 ~ Error:::', error.message)
      }
    })
  } catch (error) {
    console.log(`An error occurred sending request: ${error.message || JSON.stringify(error)}`)
  }
}

module.exports = subscribeToMQTTEvents
