// const moment = require('moment')
const db = require('../db')
const { Transform } = require('json2csv')
const { Readable } = require('stream')
const { createWriteStream } = require('fs')
var fs = require('fs')

const dir = `${process.cwd()}/files/CSV`
if (!fs.existsSync(dir)) {
  fs.mkdirSync(dir)
}

let bikeIds = []
// const generateFleetReport = async (month, year) => {
//   let startDate = moment([year, month - 1, 1]).format('YYYY-MM-DD')
//   const daysInMonth = moment(startDate).daysInMonth()
//   let endDate = moment(startDate)
//     .add(daysInMonth - 1, 'days')
//     .format('YYYY-MM-DD')
//   startDate = Date.parse(startDate) / 1000
//   endDate = Date.parse(endDate) / 1000

//   const fleets = await db
//     .main('fleets')
//     .select(
//       'fleets.fleet_id as fleetId',
//       'fleets.fleet_name as fleetName',
//       'companies.name as companyName'
//     )
//     .leftJoin('companies', 'fleets.company_id', 'companies.company_id')
//   for (const fleet of fleets) {
//     let activeVehiclesInMonth = await db
//       .main('bikes_audit_log')
//       .whereBetween('timestamp', [startDate, endDate])
//       .andWhere({ column_name: 'status' })
//       .rightJoin('bikes', 'bikes_audit_log.bike_id', 'bikes.bike_id')
//       .leftJoin('controllers', 'bikes.bike_id', 'controllers.bike_id')
//       .select('bikes.bike_id', 'bikes.fleet_id', 'bikes_audit_log.*', 'controllers.vendor')
//       .where('bikes.fleet_id', fleet.fleetId)
//       .andWhere(builder => {
//         builder.where('controllers.vendor', 'like', 'Segway')
//           .orWhere('controllers.vendor', 'like', 'Segway IoT EU')
//       })
//       .distinct('bikes_audit_log.bike_id')

//     const odds = activeVehiclesInMonth.filter(
//       (b) => b.fleet_id === fleet.fleetId && (b.vendor !== 'Segway' && b.vendor !== 'Segway IoT EU')
//     )
//     console.log('odds', odds)

//     activeVehiclesInMonth = activeVehiclesInMonth.filter(
//       (b) => b.fleet_id === fleet.fleetId && (b.vendor === 'Segway' || b.vendor === 'Segway IoT EU')
//     )

//     // console.log("activeVehiclesInMonth==>", activeVehiclesInMonth);

//     activeVehiclesInMonth = activeVehiclesInMonth.map((bAH) => {
//       const month = new Date(bAH.timestamp * 1000).getMonth() + 1
//       const year = new Date(bAH.timestamp * 1000).getFullYear()
//       return { ...bAH, time: `${month}-${year}` }
//     })

//     for (let v of activeVehiclesInMonth) {
//       bikeIds.push(v.bike_id)
//       bikeIds = [...new Set(bikeIds)]
//     }

//     console.log(
//       `>>>>>>>>>>>, ${month}/${year} - ${fleet.fleetId}- ${fleet.companyName}, ':', ${activeVehiclesInMonth.length}`
//     )
//     /*
//     fleet.activeVehiclesInMonth = activeVehiclesInMonth.length
//     fleet.numberOfBikes = vehicles.length
//     const segways = await db.main('controllers').count({'numberOfSegway': 'controller_id'}).where('fleet_id', fleet.fleetId).whereIn('vendor', ['Segway', 'Segway IoT EU']).first()
//     fleet.numberOfSegway = segways.numberOfSegway
//     const AXAs = await db.main('controllers').count({'numberOfAXA': 'controller_id'}).where({'vendor': 'AXA', 'fleet_id': fleet.fleetId}).first()
//     fleet.numberOfAXA = AXAs.numberOfAXA
//     const fleetAdmins = await db.main('fleet_associations').where({'fleet_id': fleet.fleetId, acl: 'admin'}).select('operator_id as operatorId', 'fleet_id as fleet')
//     let admins = []
//     for (const fleetAdmin of fleetAdmins) {
//       const admin = await db.users('operators').where({operator_id: fleetAdmin.operatorId}).select('operator_id as operatorId', 'email', db.main.raw('CONCAT(first_name, \' \', last_name) as "Name"')).first()
//       admins.push(admin)
//     }
//     fleet.fleetAdmins = admins */
//   }
// }

// Just update months and year to get a new report.
// Run the script with node scripts/fleet-report.js . Don't use nodemon because of the file watcher
const months = [
//   { month: 1, year: 2021 },
//   { month: 2, year: 2021 },
//   { month: 3, year: 2021 },
//   { month: 4, year: 2021 },
//   { month: 5, year: 2021 },
//   { month: 6, year: 2021 },
//   { month: 7, year: 2021 },
//   { month: 8, year: 2021 },
//   { month: 9, year: 2021 },
//   { month: 10, year: 2021 },
//   { month: 11, year: 2021 },
//   { month: 12, year: 2021 },
//   { month: 1, year: 2022 },
//   { month: 2, year: 2022 },
//   { month: 3, year: 2022 },
//   { month: 4, year: 2022 },
//   { month: 5, year: 2022 },
//   { month: 6, year: 2022 },
//   { month: 7, year: 2022 },
//   { month: 8, year: 2022 },
  { month: 9, year: 2022 },
  { month: 10, year: 2022 },
  { month: 11, year: 2022 },
  { month: 12, year: 2022 },
  { month: 1, year: 2023 },
  { month: 2, year: 2023 },
  { month: 3, year: 2023 },
  { month: 4, year: 2023 },
  { month: 5, year: 2023 }
]

const mapMonths = months.map((m) => `${m.month}-${m.year}`)

const fields = ['fleet_id', 'fleet_name', 'company_name', ...mapMonths]
const opts = { fields }
const input = new Readable({ objectMode: true })
input._read = () => {}
const transformOpts = { objectMode: true }
const json2csv = new Transform(opts, transformOpts)
const output = createWriteStream(`${process.cwd()}/files/CSV/fleetSegwayReport.csv`, {
  encoding: 'utf8'
})
input.pipe(json2csv).pipe(output); // eslint-disable-line

const monthsMap = months.map((m) => `${m.month}-${m.year}`)
const genReport = async () => {
  // for (let m of months) {
  //   await generateFleetReport(m.month, m.year)
  // }
  bikeIds = await db
    .main('bikes')
    .select('bikes.bike_id')
  bikeIds = bikeIds.map(b => b.bike_id)
}

const formatMassiveData = (history) => {
  const bikesHistoryObject = {}
  bikesHistoryObject.months = []

  for (let i = 0; i < months.length; i++) {
    const m = months[i]
    const mAY = `${m.month}-${m.year}`
    // let currentStatus = null
    for (let h of history) {
      if (mAY === h.time) {
        bikesHistoryObject[mAY] = bikesHistoryObject[mAY]
          ? [...bikesHistoryObject[mAY], h.change_after]
          : [h.change_after]
        bikesHistoryObject['months'][i] = mAY
      } else {
        if (!bikesHistoryObject['months'][i]) { bikesHistoryObject['months'][i] = null }
      }
    }
  }

  let nextStatus = null
  for (let h = 0; h < months.length; h++) {
    const m = months[h]
    const mAY = `${m.month}-${m.year}`
    if (bikesHistoryObject[mAY]) {
      bikesHistoryObject['months'][h] = nextStatus
      bikesHistoryObject[mAY].forEach(status => {
        if (status === 'active' || status === 'suspended') bikesHistoryObject['months'][h] = 1
      })
      const lastState =
        bikesHistoryObject[mAY][bikesHistoryObject[mAY].length - 1]
      if (['active', 'suspended'].includes(lastState)) {
        nextStatus = 1
      } else nextStatus = 0
    } else bikesHistoryObject['months'][h] = nextStatus
  }

  bikesHistoryObject['months'] = bikesHistoryObject['months'].map((h) => {
    return [0, null].includes(h) ? 0 : 1
  })
  return bikesHistoryObject.months
}

const fleetInfo = {}
const allBikeHistory = {}
const juneFirst = new Date('06-01-2020').valueOf() / 1000
const getFleetBikeHistory = async () => {
  for (let bikeId of bikeIds) {
    let bikeAuditHistory = await db
      .main('bikes')
      .rightJoin('bikes_audit_log', 'bikes.bike_id', 'bikes_audit_log.bike_id')
      .where({
        'bikes.bike_id': bikeId,
        'bikes_audit_log.column_name': 'status'
      })
      .andWhere('timestamp', '>', juneFirst)
      .select('bikes_audit_log.*', 'bikes.bike_name', 'bikes.fleet_id')
      .orderBy('timestamp', 'asc')
    bikeAuditHistory = bikeAuditHistory.map((bAH) => {
      const month = new Date(bAH.timestamp * 1000).getMonth() + 1
      const year = new Date(bAH.timestamp * 1000).getFullYear()
      return { ...bAH, time: `${month}-${year}` }
    })
    const fleet = await db
      .main('bikes')
      .rightJoin('fleets', 'bikes.fleet_id', 'fleets.fleet_id')
      .leftJoin('companies', 'companies.company_id', 'fleets.company_id')
      .select('bikes.fleet_id', 'fleet_name', 'companies.name as company_name')
      .where({ 'bikes.bike_id': bikeId })
      .first()
    if (!fleet) continue
    let history = {
      ...fleet,
      bikeId,
      history: formatMassiveData(bikeAuditHistory)
    }
    if (bikeAuditHistory.length) allBikeHistory[bikeId] = history
    console.log(
      '🚀 ~ file: fleets-report.js ~ line 152 ~ getFleetBikeHistory ~ history',
      history
    )
    if (!fleetInfo[fleet.fleet_id]) {
      fleetInfo[fleet.fleet_id] = { ...fleet }
      for (let n = 0; n < monthsMap.length; n++) {
        if (fleetInfo[fleet.fleet_id][monthsMap[n]]) {
          fleetInfo[fleet.fleet_id][monthsMap[n]] =
            fleetInfo[fleet.fleet_id][monthsMap[n]] + history.history[n]
        } else {
          fleetInfo[fleet.fleet_id][monthsMap[n]] = history.history[n]
        }
      }
    } else {
      for (let n = 0; n < monthsMap.length; n++) {
        if (fleetInfo[fleet.fleet_id][monthsMap[n]]) {
          fleetInfo[fleet.fleet_id][monthsMap[n]] =
            fleetInfo[fleet.fleet_id][monthsMap[n]] + history.history[n]
        } else {
          fleetInfo[fleet.fleet_id][monthsMap[n]] = history.history[n]
        }
      }
    }
  }
  const data = Object.values(fleetInfo)
  for (let d of data) {
    console.log('🚀 ~ GetFleetBikeHistory ~ data', JSON.stringify(d, null, 2))
    input.push(d)
  }
}

genReport()
  .then(() => {
    getFleetBikeHistory()
      .then(() => {
        console.log(`We're done here. Adios 🚀`)
      })
      .catch((err) => {
        console.log(
          '🚀 ~ file: fleets-report.js ~ line 173 ~ getFleetBikeHistory ~ err',
          err
        )
      })
  })
  .catch((err) => {
    console.log(
      '🚀 ~ file: fleets-report.js ~ line 176 ~ genReport ~ err',
      err
    )
  })
