const {PubSub} = require('@google-cloud/pubsub')
const got = require('got')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger
const AWS = require('aws-sdk')

let GOOGLE_APPLICATION_CLIENT_EMAIL
let GOOGLE_APPLICATION_PRIVATE_KEY
let pubSubClient

const { NODE_ENV, NIMBELINK_PROJECT_ID, NIMBELINK_SUBSCRIPTION_NAME, NIMBELINK_USER, NIMBELINK_AUTH, NIMBELINK_API_KEY, NIMBELINK_URL, S3_ACCESS_KEY_ID, S3_SECRET_ACCESS_KEY, BUCKET_NAME, BUCKET_KEY } = process.env

// In development, or when mandatory env vars are missing, stub out the module to avoid startup crash **before** any AWS calls.
if (NODE_ENV === 'development' || !BUCKET_NAME || !BUCKET_KEY) {
  module.exports = {
    listenForMessages: async () => [],
    getToken: async () => null,
    getDeviceConfig: async () => ({}),
    updateDeviceConfig: async () => ({}),
    getLatestDataForController: async () => 'Nimbelink disabled (dev)'
  }
  return;
}

const s3 = new AWS.S3({
  accessKeyId: S3_ACCESS_KEY_ID,
  secretAccessKey: S3_SECRET_ACCESS_KEY,
  Bucket: BUCKET_NAME
})

const params = {
  Bucket: BUCKET_NAME,
  Key: BUCKET_KEY
}

const initPubSub = async () => {
  try {
    const data = await s3.getObject(params).promise()
    const body = JSON.parse(data.Body.toString('utf-8'))
    GOOGLE_APPLICATION_CLIENT_EMAIL = body.client_email
    GOOGLE_APPLICATION_PRIVATE_KEY = body.private_key.replace(/\\n/g, '\n')
    pubSubClient = new PubSub({projectId: NIMBELINK_PROJECT_ID,
      credentials: { private_key: GOOGLE_APPLICATION_PRIVATE_KEY,
        client_email: GOOGLE_APPLICATION_CLIENT_EMAIL }})
  } catch (error) {
    logger('Error: Setting up pubsub.')
    logger('Error:', error)
    /* throw errors.customError(
      `AWS Error: ${error.message}`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    ) */
  }
}

initPubSub().then(() => {
  if (!pubSubClient) {
    logger('Error: Setting up pubsub.')
    throw errors.customError(
      `Failed to setup Google PubSub`,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}).catch((err) => {
  logger('Error: Setting up pubsub.', err)
  /*  throw errors.customError(
    `Failed to setup Google PubSub`,
    platform.responseCodes.BadRequest,
    'BadRequest',
    false
  ) */
})

// Return the last index(latest) data for a controller from the big stream/array
const lastIndexOfContollerData = (array, key) => {
  for (let i = array.length - 1; i >= 0; i--) {
    if (array[i].base.id === key) { return i }
  }
  return -1
}

// Returns a sorted stream(array) of controllers data
const listenForMessages = async () => {
  const subscription = pubSubClient.subscription(NIMBELINK_SUBSCRIPTION_NAME)

  const data = []
  // Create an event handler to handle messages
  const messageHandler = message => {
    const buffer = Buffer.from(message.data, 'base64')
    const str = buffer.toString('utf-8')
    data.push(JSON.parse(str).records[0])

    // "Ack" (acknowledge receipt of) the message.
    message.ack()
  }

  // Listen for new messages until timeout is hit
  subscription.on('message', messageHandler)

  return new Promise(function (resolve) {
    setTimeout(function () {
      subscription.removeListener('message', messageHandler)
      const sortedMessages = data.sort((a, b) => {
        return a.base.tss - b.base.tss
      })
      resolve(sortedMessages)
    }, 10 * 1000) // Listen for 10 seconds
  })
}

const getLatestDataForController = async (key) => {
  try {
    const sortedMessages = await listenForMessages()
    const lastIndex = lastIndexOfContollerData(sortedMessages, key)
    if (!sortedMessages.length || lastIndex === -1) return `No new status updates`
    const lastMessages = sortedMessages[lastIndex]
    if (!lastMessages) throw new Error(`Failed to fetch data for tracker: ${key}`)
    return lastMessages
  } catch (error) {
    logger('Error: getting data for ctrl.', error.message)
    throw errors.customError(
      error.message,
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

// Returns a promise with latest data for that controller
const getToken = async () => {
  const { body } = await got.post(`${NIMBELINK_URL}/auth`, {
    headers: { 'Content-Type': 'application/json', 'x-api-key': NIMBELINK_API_KEY },
    json: {
      'username': NIMBELINK_USER,
      'password': NIMBELINK_AUTH
    }
  })

  return body
}

const getDeviceConfig = async (SERIAL) => {
  try {
    const token = JSON.parse(await getToken()).token
    const res = await got.get(`${NIMBELINK_URL}/devices/${SERIAL}/config`, {
      headers: { 'x-api-key': NIMBELINK_API_KEY, 'Authorization': token }
    })
    return res
  } catch (error) {
    logger('Could not get device config. Check your serial number', error)
    throw errors.customError(
      'Could not get device config. Check your serial number',
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

const updateDeviceConfig = async (config, SERIAL) => {
  try {
    const token = JSON.parse(await getToken()).token
    const res = await got.put(`${NIMBELINK_URL}/devices/${SERIAL}/config`, {
      headers: {
        'Authorization': token,
        'x-api-key': NIMBELINK_API_KEY
      },
      json: { ...config }})
    return res
  } catch (error) {
    logger('Failed to update device config. Check your serial number', error)
    throw errors.customError(
      'Failed to update device config. Check your serial number',
      platform.responseCodes.BadRequest,
      'BadRequest',
      false
    )
  }
}

module.exports = {
  listenForMessages,
  getToken,
  getDeviceConfig,
  updateDeviceConfig,
  getLatestDataForController
}
