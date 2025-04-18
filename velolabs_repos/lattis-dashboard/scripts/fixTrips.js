const sqlDB = require('../db')
const { MongoClient } = require('mongodb')

const { MONGO_CONNECTION_URL } = process.env

// Connection URL
const url = MONGO_CONNECTION_URL
const client = new MongoClient(url)

// eslint-disable-next-line
const findAndFixTrips = async () => {
  try {
    await client.connect()
    const mongoClient = client.db('GPSTracking')
    const tripsCollection = mongoClient.collection('trips')
    const bikesCollection = mongoClient.collection('bikesAndControllers')
    let bikesOnTrips = await bikesCollection.find({current_trip: {$ne: null}}).project({bike_id: 1, _id: 0}).toArray()
    bikesOnTrips = bikesOnTrips.map(b => b.bike_id)
    const onGoingTrips = await tripsCollection.find({date_endtrip: null}).project({bike_id: 1, trip_id: 1, _id: 0}).toArray()
    const tripBikes = onGoingTrips.map(trip => trip.bike_id)
    const tripIds = onGoingTrips.map(trip => trip.trip_id)
    // Update bikes not on trips
    for (let bikeId of bikesOnTrips) {
      if (!tripBikes.includes(bikeId)) {
        const update = {
          '$set': {
            'status': 'parked',
            'current_trip': null
          }
        }
        await bikesCollection.findOneAndUpdate({ bike_id: bikeId }, update)
      }
    }
    /* Update bikes that are not upto date */
    for (let trip of onGoingTrips) {
      const update = {
        '$set': {
          'status': 'on_trip',
          'current_trip': trip.trip_id,
          mainStatus: 'active'
        }
      }
      await bikesCollection.findOneAndUpdate({ bike_id: trip.bike_id }, update)
    }
    let sqlBikesOnTrips = await sqlDB.main('trips').whereIn('trip_id', tripIds).select('trip_id', 'bike_id', 'start_address', 'end_address', 'date_endtrip')
    let promises = []
    const updatedTrips = []
    for (let trip of sqlBikesOnTrips) {
      if (trip.date_endtrip) {
        if (!trip.end_address) {
          promises.push(sqlDB.main('trips').update({ end_address: trip.start_address }).where({ trip_id: trip.trip_id }))
        }
        const updateBikes = bikesCollection.findOneAndUpdate({ bike_id: trip.bikeId }, {$set: { current_trip: null, status: 'parked' }})
        const updateTrips = tripsCollection.findOneAndUpdate({ trip_id: trip.trip_id }, {$set: { manualEndTrip: true, date_endtrip: trip.date_endtrip, end_address: trip.end_address || trip.start_address }})
        promises.push(updateBikes, updateTrips)
        updatedTrips.push(trip.trip_id)
      }
    }
    await Promise.all(promises)
    console.log('>Updated:::', updatedTrips)
  } catch (error) {
    console.log('🚀 ~ file: fixTrips.js ~ line 27 ~ findAndFixTrips ~ error', error)
  }
}

// findAndFixTrips()
