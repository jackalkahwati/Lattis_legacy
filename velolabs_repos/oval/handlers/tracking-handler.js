const { SegwayApiClient } = require('../helpers/segway-api')
const comoduleApi = require('../helpers/comodule-api')
const { ActonAPI, OmniAPI } = require('../helpers/acton-api')
const { getLatestDataForController, getDeviceConfig } = require('../helpers/nimbelink-api')
const platform = require('@velo-labs/platform')
const logger = platform.logger
const { CronJob } = require('cron')
const knex = require('../db/main')
const moment = require('moment')
const tripHandler = require('./trip-handler')
const isInsidePolygon = require('point-in-polygon')
const { executeCommand } = require('../helpers/grow')
const { configureSentry } = require('../utils/configureSentry')
const envConfig = require('../envConfig')

const Sentry = configureSentry()

// knex.on('query', q => console.log('Query:', q.sql)) // Log queries

// const trackerVendors = ['Segway', 'Segway IoT EU', /* 'Nimbelink', */ 'COMODULE Ninebot ES4', 'ACTON']
const trackerVendors = []

const segwayApiClientForUs = new SegwayApiClient({
  baseUrl: envConfig('SEGWAY_API_URL'),
  clientId: envConfig('SEGWAY_CLIENT_ID'),
  clientSecret: envConfig('SEGWAY_CLIENT_SECRET')
})

const segwayApiClientForEu = new SegwayApiClient({
  baseUrl: envConfig('SEGWAY_EU_API_URL'),
  clientId: envConfig('SEGWAY_EU_CLIENT_ID'),
  clientSecret: envConfig('SEGWAY_EU_CLIENT_SECRET')
})

const getStatusFromSegway = async (apiClient, imei) => {
  const status = await apiClient.getVehicleInformation(imei)
  const normalizedStatus = {
    location: {
      latitude: status.latitude,
      longitude: status.longitude
    },
    bike: {
      bike_battery_level: status.powerPercent
    },
    controller: {
      battery_level: status.powerPercent,
      gps_log_time: status.gpsUtcTime
    }
  }

  try {
    const firmware = await apiClient.getFirmwareVersion(imei)
    if (firmware) {
      normalizedStatus.controller.fw_version = firmware.iotVersion
    }
  } catch (error) {
    Sentry.captureException(error)
    // this can fail if the vehicle is offline but that shouldn't prevent the update
  }

  return normalizedStatus
}

const updateControllerStatus = async (
  controllerStatus,
  tripsForOnTripBikes,
  bikeId
) => {
  const { latitude, longitude } = controllerStatus.location
  const newSteps = [latitude, longitude, moment().unix()]
  const stepToInsert = newSteps[2]
  const trip = tripsForOnTripBikes.find((trip) => trip.bike_id === bikeId)
  const steps = trip.steps
    ? Array.isArray(trip.steps)
      ? trip.steps
      : JSON.parse(trip.steps)
    : []
  const stepsTimestamp = steps.map((step) => step[2])
  for (const [i, val] of stepsTimestamp.entries()) {
    if (val > stepToInsert) {
      steps.splice(i, 0, newSteps)
      break
    } else {
      if (i === steps.length - 1) {
        steps.splice(steps.length, 0, newSteps)
        break
      }
    }
  }
  await knex('trips')
    .update('steps', JSON.stringify(steps))
    .where('trip_id', trip.trip_id)
}

// @deprecated
const tripTracking = () => {
  new CronJob('*/5 * * * * *', async () => {
    const bikesWithOnTripStatus = await knex
      .select('*')
      .from('controllers')
      .rightJoin('bikes', 'controllers.bike_id', 'bikes.bike_id')
      .where({ 'bikes.current_status': 'on_trip' })
    const mappedBikesOnTrip = bikesWithOnTripStatus.map((bike) => bike.bike_id)
    const tripsForOnTripBikes = await knex
      .select('trip_id', 'date_created', 'bike_id', 'steps')
      .from('trips')
      .whereIn('bike_id', mappedBikesOnTrip)
      .orderBy('date_created', 'desc')
      .where('date_endtrip', null)
      .distinct('bike_id')
    const bikeIds = tripsForOnTripBikes.map((trip) => trip.bike_id)
    const controllersForOnTripBikes = await knex
      .select('controller_id', 'vendor', 'key', 'bike_id')
      .from('controllers')
      .whereIn('bike_id', bikeIds)

    for (let controller of controllersForOnTripBikes) {
      const { vendor, key, bike_id: bikeId } = controller
      let controllerStatus
      if (!trackerVendors.includes(vendor)) {
        logger(`Info::: Could not find implementation for ${vendor}`)
        continue
      }
      switch (vendor) {
        case 'Segway':
          try {
            controllerStatus = await getStatusFromSegway(
              segwayApiClientForUs,
              key
            )
            const { latitude, longitude } = controllerStatus.location
            const currentLocation = [latitude, longitude]
            if (isBikeOutOfGeofence(bikeId, currentLocation)) {
              segwayApiClientForUs.alertOutOfGeofence(key)
            }
            await updateControllerStatus(
              controllerStatus,
              tripsForOnTripBikes,
              bikeId
            )
          } catch (error) {
            Sentry.captureException(error, { controllerStatus })
            logger(
              `Error updating location steps for Segway US: ${error}, ${key}`
            )
            logger(
              `Error updating location steps for ACTON: ${JSON.stringify(
                error
              )}, ${key}`
            )
          }
          break

        case 'Segway IoT EU':
          try {
            controllerStatus = await getStatusFromSegway(
              segwayApiClientForEu,
              key
            )
            const { latitude, longitude } = controllerStatus.location
            const currentLocation = [latitude, longitude]
            if (isBikeOutOfGeofence(bikeId, currentLocation)) {
              segwayApiClientForUs.alertOutOfGeofence(key)
            }
            await updateControllerStatus(
              controllerStatus,
              tripsForOnTripBikes,
              bikeId
            )
          } catch (error) {
            Sentry.captureException(error, { controllerStatus })
            logger(
              `Error updating location steps for Segway IOT EU: ${error}, ${key}`
            )
            logger(
              `Error updating location steps for ACTON: ${JSON.stringify(
                error
              )}, ${key}`
            )
          }
          break

        // TODO: Implement the following controllers
        case 'COMODULE Ninebot ES4':
          try {
            const vehicleState = await comoduleApi.getVehicleState(key)
            controllerStatus = {
              location: {
                latitude: vehicleState.gpsLatitude,
                longitude: vehicleState.gpsLongitude
              }
            }
            await updateControllerStatus(
              controllerStatus,
              tripsForOnTripBikes,
              bikeId
            )
          } catch (error) {
            Sentry.captureException(error, { controllerStatus })
            logger(
              `Error updating location steps for COMODULE Ninebot ES4: ${error}, ${key}`
            )
            logger(
              `Error updating location steps for ACTON: ${JSON.stringify(
                error
              )}, ${key}`
            )
          }
          break

        case 'ACTON':
        case 'Omni Lock':
          try {
            let clientApi
            if (vendor === 'ACTON') clientApi = ActonAPI
            else if (vendor === 'Omni Lock') clientApi = OmniAPI
            const status = await clientApi.getCurrentStatus(key)
            controllerStatus = {
              location: { latitude: status.lat, longitude: status.lon }
            }
            await updateControllerStatus(
              controllerStatus,
              tripsForOnTripBikes,
              bikeId
            )
          } catch (error) {
            Sentry.captureException(error, { controllerStatus })
            logger(
              `Error updating location steps for ${vendor}: ${error}, ${key}`
            )
            logger(
              `Error updating location steps for ${vendor}: ${JSON.stringify(
                error
              )}, ${key}`
            )
          }
          break

        // Minimum tracking time is 5 min so 5 sec won't work here
        case 'Nimbelink':
          try {
            await getDeviceConfig(key) // This is a check step for tracker validity
            const vehicleState = await getLatestDataForController(
              controller.key
            )
            const lastLocation =
              vehicleState &&
              vehicleState.data &&
              vehicleState.data.loc &&
              vehicleState.data.loc.pop()
            if (lastLocation) {
              controllerStatus = {
                location: {
                  latitude: lastLocation.lat,
                  longitude: lastLocation.lon
                }
              }
              await updateControllerStatus(
                controllerStatus,
                tripsForOnTripBikes,
                bikeId
              )
            }
          } catch (error) {
            Sentry.captureException(error, { controllerStatus })
            logger(
              `Error updating location steps for Nimbelink: ${error}, ${key}`
            )
            logger(
              `Error updating location steps for ACTON: ${JSON.stringify(
                error
              )}, ${key}`
            )
          }
          break

        case null:
          logger(`Error: doesn't support IoT: `)
          break

        default:
          logger(`Info: Could not find implementation for ${vendor}`)
      }
    }
  }).start()
}

const updateTripInformation = async (tripDetails, callback) => {
  const tripId = tripDetails.trip_id
  const tripsAndControllers = await knex
    .select('*').from('trips').join('controllers', 'trips.bike_id', 'controllers.bike_id')
    .where('trips.trip_id', tripId)
    .options({ nestTables: true })
    .first()

  if (!tripsAndControllers) {
    // Just go the mobile way if the bike on the trip does not have a controller attached
    // logger(`The bike for the trip ${tripId} does not have a corresponding controller attached`)
    tripDetails.hasGPSController = false
    return tripHandler.updateTrip(null, tripDetails, callback)
  }

  const { controllers: bikeController = undefined } = tripsAndControllers

  let vendor
  if (bikeController) vendor = bikeController.vendor
  if (!trackerVendors.includes(vendor)) {
    // Use mobile tracking info
    tripDetails.hasGPSController = false
    return tripHandler.updateTrip(null, tripDetails, callback)
  } else {
    // Bike has controller that can track location
    tripDetails.hasGPSController = true
    return tripHandler.updateTrip(null, tripDetails, callback)
  }
}

const isBikeOutOfGeofence = async (bikeId, currentLocation) => {
  const bike = await knex('bikes').where({ bike_id: bikeId }).first()
  if (bike.status !== 'active') return false // Only check for out of geofence if bike is live
  const bikeAndGeofence = await knex('bikes')
    .join('geofences', 'bikes.fleet_id', 'geofences.fleet_id')
    .join('controllers', 'bikes.bike_id', 'controllers.bike_id')
    .select('bikes.bike_name', 'name', 'geometry', 'vendor', 'key', 'bikes.status')
    .where({'bikes.bike_id': bikeId})

  let vendor
  let key
  let values = []
  if (bikeAndGeofence) {
    for (let record of bikeAndGeofence) {
      const geometry = record.geometry
      vendor = record.vendor
      key = record.key
      let points
      if (geometry.shape === 'polygon') {
        points = geometry.points.map(point => [point.latitude, point.longitude])
        values.push(isInsidePolygon(currentLocation, points))
      } else if (geometry.shape === 'rectangle') {
        points = geometry.bbox
        values.push(isInBoundBox(currentLocation, points))
      } else if (geometry.shape === 'circle') {
        const { units, value } = geometry.radius
        let distanceInKm = value
        if (units === 'miles') distanceInKm = value / 0.62137
        const circle = { center: geometry.center, radius: distanceInKm }
        values.push(isInsideCircle(currentLocation, circle))
      }
    }
  }
  const outOfGeo = values.every((val) => val === false) // If all are false, then it's outside all geofences for that fleet

  if (vendor && outOfGeo) {
    switch (vendor) {
      case 'Segway':
        segwayApiClientForUs.alertOutOfGeofence(key)
        break
      case 'Segway IoT EU':
        segwayApiClientForUs.alertOutOfGeofence(key)
        break
      case 'Grow':
        executeCommand(`s/c/${key}`, '50,1')
        break
      default:
        break
    }
  }
}
/**
 *
 *
 * @param {*} currentLocation Array [latitude, longitude]
 * @param {*} points Array of bounding box coordinates sw and ne
 * @returns
 */
const isInBoundBox = (currentLocation, points) => {
  const bottomLeftLng = points[0]
  const bottomLeftLtd = points[1]
  const topRightLng = points[2]
  const topRightLtd = points[3]
  let isLongitudeInRange
  if (topRightLng < bottomLeftLng) {
    isLongitudeInRange = currentLocation[0] >= bottomLeftLng || currentLocation[0] <= topRightLtd
  } else isLongitudeInRange = currentLocation[0] >= bottomLeftLng && currentLocation[0] <= topRightLtd
  return currentLocation[1] >= bottomLeftLtd && currentLocation[1] <= topRightLtd && isLongitudeInRange
}
/**
 * check if point is inside geofence defined by circle
 *
 * @param {*} currentLocation Array [latitude, longitude]
 * @param {*} circle Object e.g {'center': {'latitude': 45.49407318666883, 'longitude': -73.61138788210224}, 'radius': 4.649803617592324}
 * @returns
 */
const isInsideCircle = (currentLocation, circle) => {
  const { center: { latitude, longitude }, radius } = circle
  var ky = 40000 / 360 // kilometers per degree for latitude
  var kx = Math.cos(Math.PI * latitude / 180.0) * ky // kilometres per degree for longitude
  var dx = Math.abs(longitude - currentLocation[1]) * kx
  var dy = Math.abs(latitude - currentLocation[0]) * ky
  return Math.sqrt(dx * dx + dy * dy) <= radius
}

module.exports = { tripTracking, updateTripInformation, isBikeOutOfGeofence }
