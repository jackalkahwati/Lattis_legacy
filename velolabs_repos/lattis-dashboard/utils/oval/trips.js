const { client: defaultClient } = require('./client')

/**
 * Make a request to Oval to end a ride on a vehicle.
 *
 * @param {object} params
 * @param {number} params.trip_id
 * @param {number} params.latitude
 * @param {number} params.longitude
 * @param {object} [context]
 * @param {import('axios').AxiosInstance} [context.client] - Configured client
 *        to make API requests with. See `utils/oval/client.js#configure`
 * @returns
 */
async function endTrip (params, { client = defaultClient } = {}) {
  return client.post('/trips/end-trip', {
    ...params,
    trip_id: params.trip_id,
    latitude: params.latitude,
    longitude: params.longitude,
    accuracy: 0,
    bike_damaged: false,
    parking_image: params.parking_image || ''
  })
}

module.exports = {
  endTrip
}
