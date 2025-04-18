'use strict'

const tripHelper = require('@velo-labs/platform').tripHelper

/**
 * To format a trip for wire
 *
 * @param {Object} trip - trip object to format
 */
const formatTripForWire = (trip) => {
  trip = parseTripString(trip)
  trip.date_end_trip = trip.date_endtrip
  if (!trip.steps) trip.steps = []
  trip.trip_distance = tripHelper.getTripDistance(trip)
  trip.duration = (trip.date_end_trip && trip.date_created) ? trip.date_end_trip - trip.date_created : 0
  return trip
}

/**
 * To parse the trip steps string
 *
 * @param {Object} trip
 */
const parseTripString = (trip) => {
  if (trip.steps && !Array.isArray(trip.steps)) {
    trip.steps = JSON.parse(trip.steps)
  }
  return trip
}

/**
 * To parse the trips steps string
 *
 * @param {Array} trips
 */
const parseTripsString = (trips) => {
  const tripsLength = trips.length
  for (let i = 0; i < tripsLength; i++) {
    parseTripString(trips[i])
  }
  return trips
}

/**
 * To format trips objects for wire
 *
 * @param {Array} trips
 */
const formatTripsForWire = (trips) => {
  if (!Array.isArray(trips)) {
    trips = [trips]
  }
  const tripsLen = trips.length
  const newTrips = []
  let trip
  for (let i = 0; i < tripsLen; i++) {
    trip = trips[i]
    // For Payments fleet: Checking if the trip is cancelled before starting
    if (!trip.date_endtrip && trip.date_created) {
      newTrips.push(trip)
    } else if (trip.date_endtrip > trip.date_created) {
      newTrips.push(trip)
    } else if (trip.date_endtrip === trip.date_created && trip.reservation_id) {
      // Include cancelled reservation trips
      newTrips.push(trip)
    }
  }
  return newTrips
}

module.exports = Object.assign(module.exports, {
  formatTripForWire: formatTripForWire,
  parseTripString: parseTripString,
  parseTripsString: parseTripsString,
  formatTripsForWire: formatTripsForWire
})
