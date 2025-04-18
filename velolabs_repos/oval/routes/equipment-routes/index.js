const express = require('express')

const {auth} = require('../middleware')
const equipmentRoutes = require('./equipment')

const router = new express.Router()
router.use(auth.authenticate)

equipmentRoutes(router)

module.exports = router
