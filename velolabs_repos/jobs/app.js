require('dotenv').config()
const express = require('express')
const { updateDashboardData, trackGeotabTrips } = require('./crons/gpsService')
const subscribeToMQTTEvents = require('./mqtt/subscribers')

const app = express()
const PORT = process.env.PORT

app.get('/', (req, res) => {
  res.json({ message: 'Welcome to the cron job service' })
})

app.listen(PORT, () => {
  console.log(`App running on port ${PORT}`)
  updateDashboardData()
  trackGeotabTrips()
  subscribeToMQTTEvents()
})
