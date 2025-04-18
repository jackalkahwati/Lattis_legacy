// NOTE: Don't run this script twice on the same DB esp prod.
const { v4: uuidv4 } = require('uuid')
const db = require('../db')

const addBikeUUIDs = async () => {
  try {
    await db.main.transaction(async trx => {
      const bikes = await trx('bikes').select('bike_id')
      const promises = bikes.map(bike => {
        return trx('bikes').update({'bike_uuid': uuidv4()}).where({bike_id: bike.bike_id})
      })
      await Promise.all(promises)
    })
    console.log('Successfully add UUIDs to DB')
    return
  } catch (error) {
    console.log('Adding UUIDS to bike Error:', error)
  }
}

addBikeUUIDs()
