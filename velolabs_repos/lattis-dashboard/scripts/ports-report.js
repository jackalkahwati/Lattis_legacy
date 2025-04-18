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

let portIds = []
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
//       .main('ports_audit_log')
//       .whereBetween('timestamp', [startDate, endDate])
//       .andWhere({ column_name: 'status' })
//       .rightJoin('ports', 'ports_audit_log.ports_id', 'ports.port_id')
//       .leftJoin('hubs_and_fleets', 'ports.hub_uuid', 'hubs_and_fleets.hub_uuid')
//       .select('ports.port_id', 'ports_audit_log.*', 'hubs_and_fleets.*')
//       .where('hubs_and_fleets.fleet_id', fleet.fleetId)
//       .distinct('ports_audit_log.ports_id')

//     activeVehiclesInMonth = activeVehiclesInMonth.filter(
//       (b) => b.fleet_id === fleet.fleetId
//     )
//     activeVehiclesInMonth = activeVehiclesInMonth.map((bAH) => {
//       const month = new Date(bAH.timestamp * 1000).getMonth() + 1
//       const year = new Date(bAH.timestamp * 1000).getFullYear()
//       return { ...bAH, time: `${month}-${year}` }
//     })

//     for (let v of activeVehiclesInMonth) {
//       bikeIds.push(v.port_id)
//       bikeIds = [...new Set(bikeIds)]
//     }

//     console.log(
//       `>>>>>>>>>>> ${month}/${year} - ${fleet.fleetId}- ${fleet.companyName}, ':', ${activeVehiclesInMonth.length}`
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
// Run the script with node scripts/ports-report.js . Don't use nodemon because of the file watcher
const months = [
  { month: 1, year: 2021 },
  { month: 2, year: 2021 },
  { month: 3, year: 2021 },
  { month: 4, year: 2021 },
  { month: 5, year: 2021 },
  { month: 6, year: 2021 },
  { month: 7, year: 2021 },
  { month: 8, year: 2021 },
  { month: 9, year: 2021 },
  { month: 10, year: 2021 },
  { month: 11, year: 2021 },
  { month: 12, year: 2021 },
  { month: 1, year: 2022 },
  { month: 2, year: 2022 },
  { month: 3, year: 2022 },
  { month: 4, year: 2022 },
  { month: 5, year: 2022 },
  { month: 6, year: 2022 },
  { month: 7, year: 2022 },
  { month: 8, year: 2022 },
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
const output = createWriteStream(`${process.cwd()}/files/CSV/portsReport.csv`, {
  encoding: 'utf8'
})
input.pipe(json2csv).pipe(output); // eslint-disable-line

const monthsMap = months.map((m) => `${m.month}-${m.year}`)
const genReport = async () => {
  // for (let m of months) {
  //   await generateFleetReport(m.month, m.year)
  // }
  portIds = await db
    .main('ports')
    .select('ports.port_id')
  portIds = portIds.map(p => p.port_id)
}

const formatMassiveData = (history) => {
  const portsHistoryObject = {}
  portsHistoryObject.months = []

  for (let i = 0; i < months.length; i++) {
    const m = months[i]
    const mAY = `${m.month}-${m.year}`
    // let currentStatus = null
    for (let h of history) {
      if (mAY === h.time) {
        portsHistoryObject[mAY] = portsHistoryObject[mAY]
          ? [...portsHistoryObject[mAY], h.change_after]
          : [h.change_after]
        portsHistoryObject['months'][i] = mAY
      } else {
        if (!portsHistoryObject['months'][i]) { portsHistoryObject['months'][i] = null }
      }
    }
  }

  let nextStatus = null
  for (let h = 0; h < months.length; h++) {
    const m = months[h]
    const mAY = `${m.month}-${m.year}`
    if (portsHistoryObject[mAY]) {
      portsHistoryObject['months'][h] = nextStatus
      portsHistoryObject[mAY].forEach(status => {
        portsHistoryObject['months'][h] = 1
      })
      const lastState =
        portsHistoryObject[mAY][portsHistoryObject[mAY].length - 1]
      if (['Available'].includes(lastState)) {
        nextStatus = 1
      } else nextStatus = 0
    } else portsHistoryObject['months'][h] = nextStatus
  }

  portsHistoryObject['months'] = portsHistoryObject['months'].map((h) => {
    return [0, null].includes(h) ? 0 : 1
  })
  return portsHistoryObject.months
}

const fleetInfo = {}
const allBikeHistory = {}
const juneFirst = new Date('06-01-2022').valueOf() / 1000
const getFleetPortHistory = async () => {
  for (let portId of portIds) {
    let portAuditHistory = await db
      .main('ports')
      .rightJoin('ports_audit_log', 'ports.port_id', 'ports_audit_log.ports_id')
      .leftJoin('hubs_and_fleets', 'hubs_and_fleets.hub_uuid', 'ports.hub_uuid')
      .where({
        'ports.port_id': portId,
        'ports_audit_log.column_name': 'status'
      })
      .andWhere('timestamp', '>', juneFirst)
      .select('ports_audit_log.*', 'ports.hub_uuid', 'hubs_and_fleets.fleet_id')
      .orderBy('timestamp', 'asc')
    portAuditHistory = portAuditHistory.map((bAH) => {
      const month = new Date(bAH.timestamp * 1000).getMonth() + 1
      const year = new Date(bAH.timestamp * 1000).getFullYear()
      return { ...bAH, time: `${month}-${year}` }
    })
    const fleet = await db
      .main('hubs_and_fleets')
      .rightJoin('ports', 'ports.hub_uuid', 'hubs_and_fleets.hub_uuid')
      .rightJoin('fleets', 'hubs_and_fleets.fleet_id', 'fleets.fleet_id')
      .leftJoin('companies', 'companies.company_id', 'fleets.company_id')
      .select('hubs_and_fleets.fleet_id', 'fleet_name', 'companies.name as company_name')
      .where({ 'ports.port_id': portId })
      .first()
    let history = {
      ...fleet,
      portId,
      history: formatMassiveData(portAuditHistory)
    }
    if (portAuditHistory.length) allBikeHistory[portId] = history
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
    console.log('🚀 ~ GetFleetPortHistory ~ data', JSON.stringify(d, null, 2))
    input.push(d)
  }
}

genReport()
  .then(() => {
    getFleetPortHistory()
      .then(() => {
        console.log(`We're done here. Adios 🚀`)
      })
      .catch((err) => {
        console.log(err)
      })
  })
  .catch((err) => {
    console.log(err)
  })
