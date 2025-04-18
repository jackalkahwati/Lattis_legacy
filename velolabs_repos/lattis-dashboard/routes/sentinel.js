const router = require('express').Router()
const sendPushNotification = require('../utils/firebase-messaging')
const db = require('../db')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const { authWithAPIKey } = require('../utils/auth')

const notificationTypes = {
  NetworkRegistration: '1007',
  LockOpened: '1001',
  LockClosed: '1002',
  GPSReported: '1003',
  MultipleGPSReported: '1023',
  HeartBeatReceived: '1006'
}

const formatAndSendNotificationMessage = async (lockId, action) => {
  const ctrl = await db.main('controllers').where({ key: lockId }).first()
  if (ctrl) {
    const bikeId = ctrl.bike_id
    const latestTrip = await db
      .main('trips')
      .where({ bike_id: bikeId })
      .orderBy('date_created', 'DESC')
      .first()
    if (latestTrip && !latestTrip.date_endtrip && action) {
      const message = {
        notification: {
          title: 'Sentinel Lock',
          body: 'lock status changed',
          title_loc_key: action,
          body_loc_key: action,
          sound: 'default',
          click_action: action
        },
        data: {
          trip_id: `${latestTrip.trip_id}`
        }
      }
      const options = {
        contentAvailable: true,
        priority: 'high'
      }
      await sendPushNotification(latestTrip.device_token, message, options)
      logger(`Info::PN send for ${action} for trip ${latestTrip.trip_id}`)
    }
  }
}

router.post('/', authWithAPIKey, async (req, res) => {
  const body = req.body
  try {
    const notificationType = body.notificationType
    const lockId = body.message.lockId
    switch (notificationType) {
      case notificationTypes.NetworkRegistration: {
        console.log(`${notificationType} Body:::: `, body)
        break
      }
      case notificationTypes.GPSReported: {
        const message = body.message
        const position = {
          latitude: message.latitude,
          longitude: message.longitude
        }
        const ctrl = await db
          .main('controllers')
          .where({ key: lockId })
          .first()
        if (ctrl) {
          await db.main('controllers')
            .update({ ...position, battery_level: message.voltagePercentage })
            .where({ key: lockId })
          if (ctrl.bike_id) {
            const updates = {
              ...position,
              bike_battery_level: message.voltagePercentage
            }
            await db.main('bikes')
              .update(updates)
              .where({ bike_id: ctrl.bike_id })
          }
        }
        break
      }
      case notificationTypes.HeartBeatReceived:
        await formatAndSendNotificationMessage(lockId, 'sentinel_lock_online')
        break
      case notificationTypes.LockClosed:
      case notificationTypes.LockOpened: {
        const lockId = body.message.lockId
        let action
        if (notificationType === notificationTypes.LockClosed) {
          action = 'sentinel_lock_closed'
        } else if (notificationType === notificationTypes.LockOpened) {
          action = 'sentinel_lock_opened'
        }
        await formatAndSendNotificationMessage(lockId, action)
        break
      }
      default:
        return res.json({ message: 'Unknown notification type' })
    }
    res.json({ message: 'Data Received' })
  } catch (error) {
    logger('An error occurred getting sentinel data', error)
    res.json({ message: 'Error' })
  }
})

module.exports = router
