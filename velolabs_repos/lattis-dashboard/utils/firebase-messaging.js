const admin = require('firebase-admin')
const { logger } = require('@velo-labs/platform')
const fs = require('fs')
const path = require('path')

// Path to the service account JSON (may be absent in development)
const servicePath = path.join(__dirname, 'lattis-app-firebase-adminsdk.json')

let pushEnabled = false

try {
  if (fs.existsSync(servicePath)) {
    const serviceAccount = require(servicePath)
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      databaseURL: 'https://lattis-app.firebaseio.com'
    })
    pushEnabled = true
    logger.info('Firebase messaging initialised')
  } else {
    logger.warn('Firebase service account file not found; push notifications disabled')
  }
} catch (err) {
  logger.error('Failed to initialise Firebase messaging:', err)
}

const sendPushNotification = (registrationToken, message, options) => {
  if (!pushEnabled) {
    // No-op in development when credentials are absent
    return Promise.resolve(null)
  }

  return admin.messaging().sendToDevice(registrationToken, message, options)
    .then((response) => {
      logger.info(`Message sent successfully to device ${registrationToken}`)
      return response
    })
    .catch((error) => {
      logger.error(`Error sending PN to device ${registrationToken}:`, error)
      throw error
    })
}

module.exports = sendPushNotification
