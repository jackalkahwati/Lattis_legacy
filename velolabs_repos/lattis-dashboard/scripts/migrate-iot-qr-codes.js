// select b.bike_id, b.qr_code_id, c.`key`, c.qr_code, g.make, g.model, c.vendor from bikes b join bike_group g on b.bike_group_id = g.bike_group_id join controllers c on c.bike_id = b.bike_id;
const db = require('../db')

function getControllers () {
  return db.main({c: 'controllers'})
    .innerJoin({b: 'bikes'}, 'b.bike_id', 'c.bike_id')
    .select('c.controller_id', 'b.bike_id', 'b.qr_code_id', 'c.qr_code', 'c.vendor')
}

async function migrateIotQrCodes () {
  const controllers = await getControllers()

  for (let ctrl of controllers) {
    if (ctrl.vendor === 'Segway' && ctrl.qr_code == null) {
      await db.main('controllers')
        .where({ controller_id: ctrl.controller_id })
        .update({ qr_code: ctrl.qr_code_id.toString().padStart(6, '0') })
      if (process.argv[2] === '--destructive') {
        await db.main('bikes')
          .where({ bike_id: ctrl.bike_id })
          .update({ qr_code_id: null })
      }
    }
  }

  console.table(await getControllers())
}

try {
  (async () => {
    await migrateIotQrCodes()
    process.exit(0)
  })()
} catch (e) {
  console.log(e)
  process.exit(1)
}
