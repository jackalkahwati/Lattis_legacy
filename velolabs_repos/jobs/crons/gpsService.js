const schedule = require('node-schedule')
const got = require('got')
const { generateToken } = require('../utils/auth')

const GPS_SERVICE_URL = process.env.GPS_SERVICE_URL

// Update data in the dashboard after every 5 minutes for vehicles updated within last 5 minutes
const updateDashboardData = () => {
  schedule.scheduleJob('*/5 * * * *', async () => { // 5 min
    try {
      const jwtToken = generateToken()
      const headers = { 'x-access-token': jwtToken }
      const response = await got.post(`${GPS_SERVICE_URL}/crons`, {
        headers,
        json: {
          operation: 'UpdateDashboardData'
        }
      })
      console.log('🚀 UpdateDashboardData:', response.body)
    } catch (error) {
      console.log(`An error occurred sending request: ${error.message || JSON.stringify(error)}`)
    }
  })
}

const trackGeotabTrips = () => {
  schedule.scheduleJob('*/10 * * * * *', async () => { // 10 sec
    try {
      const jwtToken = generateToken()
      const headers = { 'x-access-token': jwtToken }
      const response = await got.post(`${GPS_SERVICE_URL}/crons`, {
        headers,
        json: {
          operation: 'TrackGeotabTrips'
        }
      })
      console.log('🚀 TrackGeotabTrips:', response.body)
    } catch (error) {
      console.log(`An error occurred sending request:: ${error.message || JSON.stringify(error)}`)
    }
  })
}

module.exports = { updateDashboardData, trackGeotabTrips }
