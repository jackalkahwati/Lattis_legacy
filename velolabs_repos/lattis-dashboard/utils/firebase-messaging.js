const admin = require('firebase-admin')

const serviceAccount = require('./lattis-app-firebase-adminsdk.json')
const { logger } = require('@velo-labs/platform')

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://lattis-app.firebaseio.com'
})

const sendPushNotification = (registrationToken, message, options) => {
  console.log('***************************************', registrationToken, message, options)
  admin.messaging().sendToDevice(registrationToken, message, options)
    .then(response => {
      logger(`Message Send Successfully to device ${registrationToken}`)
    })
    .catch(error => {
      logger(`An error occurred sending PN to device${registrationToken}`)
      logger(error)
    })
}

module.exports = sendPushNotification
