const express = require('express')

const { auth } = require('../middleware')
const reservationRoutes = require('./reservations')

const router = new express.Router()

router.use(auth.authenticate)

reservationRoutes(router)

module.exports = router
