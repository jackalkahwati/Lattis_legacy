const axios = require('axios')
const { generateToken } = require('../utils/auth')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const envConfig = require('../envConfig')

const GPS_SERVICE_URL = envConfig('GPS_TRACKING_SERVICE')

// Make request to GPS tracking service to start trip
const sendStartTripRequestToGPSService = async (tripDetails) => {
  try {
    await axios.post(
      `${GPS_SERVICE_URL}/trips/start-trip`,
      { tripDetails },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending start trip details to GPS service:::', error)
    logger('Error sending start trip details to GPS details:::', JSON.stringify(tripDetails))
    logger('Error:::', error.response.data.message)
  }
}

// Make request to GPS tracking service to end trip
const sendEndTripRequestToGPSService = async (tripDetails) => {
  try {
    await axios.post(
      `${GPS_SERVICE_URL}/trips/end-trip`,
      { tripDetails },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending end trip details to GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

const sendCreateGeofenceRequestToGPSService = async (geofence) => {
  try {
    await axios.post(
      `${GPS_SERVICE_URL}/geofence`,
      { geofence },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending geofence details to GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

const sendDeleteGeofenceRequestToGPSService = async (geofenceId) => {
  try {
    await axios.delete(
      `${GPS_SERVICE_URL}/geofence/${geofenceId}`,
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error deleting geofence details from GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

const sendUpdateGeofenceRequestToGPSService = async (geofenceId, geoUpdates) => {
  try {
    await axios.patch(
      `${GPS_SERVICE_URL}/geofence/${geofenceId}`,
      { geoUpdates },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending geofence updates to GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

const sendUpdateGeofencesRequestToGPSService = async (fleetId, status) => {
  try {
    await axios.patch(
      `${GPS_SERVICE_URL}/geofence/status/${fleetId}`,
      { status },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending geofence status updates to GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

const sendUpdateTicketRequestToGPSService = async (ticketId, status) => {
  try {
    await axios.patch(
      `${GPS_SERVICE_URL}/tickets/resolve/${ticketId}`,
      { status },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending ticket(resolve) updates to GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

const updateBikeStatus = async (filter, valuesToUpdate) => {
  try {
    await axios.patch(
      `${GPS_SERVICE_URL}/vehicles`,
      { values: valuesToUpdate, filter },
      { headers: { 'x-access-token': generateToken() } }
    )
  } catch (error) {
    logger('Error sending bike updates to GPS service:::', error)
    logger('Error:::', error.response.data.message)
  }
}

module.exports = {
  sendStartTripRequestToGPSService,
  sendEndTripRequestToGPSService,
  sendCreateGeofenceRequestToGPSService,
  sendDeleteGeofenceRequestToGPSService,
  sendUpdateGeofenceRequestToGPSService,
  sendUpdateGeofencesRequestToGPSService,
  sendUpdateTicketRequestToGPSService,
  updateBikeStatus
}
