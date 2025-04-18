const db = require('../db')

async function migrateQrCodes () {
  const bikeQrCodes = await db.main('bikes').select('bike_id', 'qr_code_id').whereNotNull('qr_code_id')
  for (const bike of bikeQrCodes) {
    try {
      console.log(`🚀 ~ Inserting ${JSON.stringify(bike, null, 2)}`)
      if (bike.qr_code_id !== 0) {
        await db.main('qr_codes').insert({ code: `${bike.qr_code_id}`, type: 'bike', equipment_id: bike.bike_id }).whereNot('qr_code', '=', 0)
      }
    } catch (error) {
      console.log(`Error inserting ${bike}: ${error.message}`)
    }
  }

  const controllerQrCodes = await db.main('controllers').select('bike_id', 'qr_code').whereNotNull('qr_code')
  for (const ctrl of controllerQrCodes) {
    try {
      if (ctrl.bike_id && ctrl.qr_code && ctrl.qr_code !== 0) {
        console.log(`🚀 ~ Inserting ${JSON.stringify(ctrl, null, 2)}`)
        await db.main('qr_codes').insert({ code: ctrl.qr_code, type: 'bike', equipment_id: ctrl.bike_id })
      }
    } catch (error) {
      console.log(`Error inserting ${ctrl}: ${error.message}`)
    }
  }
}

migrateQrCodes().then(() => {
  console.log("We're done here")
  process.exit()
})
