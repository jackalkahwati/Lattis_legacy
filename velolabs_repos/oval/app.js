'use strict'

require('dotenv').config()
const logger = require('@velo-labs/platform').logger
const config = require('./config')
const Sentry = require('./utils/configureSentry')

if (process.argv.length > 2 && process.argv[process.argv.length - 1] === 'locks') {
  const lockHandler = require('./handlers/lock-handler')
  const _ = require('underscore')

  lockHandler.allLocks((error, locks) => {
    if (error) return

    _.each(locks, (lock) => {
      logger(lock)
    })
  })
} else if (process.argv.length > 2 && process.argv[process.argv.length - 1] === 'pins') {
  const lockHandler = require('./handlers/lock-handler')
  const _ = require('underscore')

  lockHandler.allPinCodes((error, pinCodes) => {
    if (error) return

    _.each(pinCodes, (pinCode) => {
      logger(pinCode)
    })
  })
} else {
  const bodyParser = require('body-parser')
  const morgan = require('morgan')
  const path = require('path')
  const express = require('express')
  const _ = require('lodash')

  const app = express()
  const multiparty = require('connect-multiparty')
  const multipartyMiddleWare = multiparty()
  const userRoutes = require('./routes/user-routes')
  const lockRoutes = require('./routes/lock-routes')
  const {router: tripRoutes, v2Router: tripRoutesV2} = require('./routes/trip-routes')
  const parkingRoutes = require('./routes/parking-routes')
  const { bikeRoutes, bookingsRouter } = require('./routes/bike-routes')
  const maintenanceRoutes = require('./routes/maintenance-routes')
  const miscRoutes = require('./routes/misc-routes')
  const operatorRoutes = require('./routes/operator-routes')
  const ticketRoutes = require('./routes/ticket-routes')
  const fleetRoutes = require('./routes/fleet-routes')
  const controllerRoutes = require('./routes/controller-routes')
  const membershipRoutes = require('./routes/membership-routes')
  const subscriptionRoutes = require('./routes/subscription-routes')
  const memberships = require('./handlers/fleet-membership-handlers')
  const reservationRoutes = require('./routes/reservation-routes')
  const appRoutes = require('./routes/application-routes')
  const reservations = require('./handlers/reservation-handlers')
  const promotionRoutes = require('./routes/promotions')
  const hubs = require('./routes/hub-routes')
  const rentals = require('./routes/rentals-route')
  const tapkeyRoutes = require('./routes/tapkey-routes')
  const sasRoutes = require('./routes/sas-routes')
  const sentinel = require('./routes/sentinel')
  const equipmentRoutes = require('./routes/equipment-routes')
  const docRoutes = require('./routes/docs')
  app.use(bodyParser.json({ limit: '50mb' }))
  app.use(bodyParser.urlencoded({ extended: true, limit: '50mb' }))

  app.use(morgan('dev'))
  app.use(multipartyMiddleWare)
  app.use(express.static(path.join(__dirname, 'public')))

  app.use((req, res, next) => {
    logger({
      endpoint: req.path,
      payload: _.omit(req.body, 'password', 'cvc', 'cc_no'),
      params: req.params,
      querystrings: req.query
    })
    const transactionId = Math.random().toString(36).substr(2, 9)
    Sentry.configureScope(scope => {
      scope.setTag('transaction_id', transactionId)
    })
    req.sentryObj = Sentry
    next()
  })

  app.get('/api/', (req, res) => {
    res.json({msg: 'Welcome to Oval, the mobile backend'})
  })
  app.use('/api/users', userRoutes)
  app.use('/api/locks', lockRoutes)
  app.use('/api/trips', tripRoutes)
  app.use('/v2/api/trips', tripRoutesV2)
  app.use('/api/parking', parkingRoutes)
  app.use('/api/bikes', bikeRoutes)
  app.use('/v2/api/bookings', bookingsRouter)
  app.use('/api/maintenance', maintenanceRoutes)
  app.use('/api/misc', miscRoutes)
  app.use('/api/operator', operatorRoutes)
  app.use('/api/ticket', ticketRoutes)
  app.use('/api/fleet', fleetRoutes)
  app.use('/api/controllers', controllerRoutes)
  app.use('/api/memberships', membershipRoutes)
  app.use('/api/subscriptions', subscriptionRoutes)
  app.use('/api/reservations', reservationRoutes)
  app.use('/api/apps', appRoutes)
  app.use('/api/promotions', promotionRoutes)
  app.use('/api/hubs', hubs)
  app.use('/api/rentals', rentals)
  app.use('/api/tapkey', tapkeyRoutes)
  app.use('/api/sas', sasRoutes)
  app.use('/api/sentinel', sentinel)
  app.use('/api/equipment', equipmentRoutes)
  app.use('/api/v1/docs', docRoutes)

  app.use((err, req, res, next) => {
    if (err && err.error && err.error.isJoi) {
      // we had a joi error, let's return a custom 400 json response
      res.status(400).send({
        type: err.type,
        message: 'There are one or more parameters missing in the supplied request',
        error: err.error.details
      })
    } else {
      next(err)
    }
  })

  app.listen(config.port, function () {
    logger('Listening on port ' + config.port)
    memberships.subscriptionRenewals.scheduleRenewals()
    reservations.notifications.scheduleNotifications()
  })
}
