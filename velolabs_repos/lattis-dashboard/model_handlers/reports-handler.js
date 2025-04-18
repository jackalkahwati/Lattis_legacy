'use strict'

const platform = require('@velo-labs/platform')
const moment = require('moment')
const _ = require('underscore')
const json2csv = require('json2csv')
const logger = platform.logger
const errors = platform.errors
const tripHelper = platform.tripHelper
const sqlPool = platform.sqlPool
const dbConstants = platform.dbConstants
const query = platform.queryCreator
const platformConfig = platform.config
const tripHandler = require('./trip-handler')
const conversions = require('./../utils/conversions')
const bikeFleetHandler = require('./bike-fleet-handler')
const bikeConstants = require('./../constants/bike-constants')
const maintenanceConstants = require('./../constants/maintenance-constants')
const ticketHandler = require('./ticket-handler')
const uploadFile = require('./../utils/file-upload')
const memberHandler = require('./member-handler')
const ticketConstants = require('./../constants/ticket-constants')
const tripUtil = require('./../utils/trip-util')
const db = require('../db')
const Sentry = require('../utils/configureSentry')

/*
 * This method is used to get the reports of fleet Utilisation
 * @param {Object} fleetDetails - contains operator_id and month
 * @param {Function} callback - done
 */

const getTripsByMonth = async (data) => {
  try {
    const dateMap = {
      'January': 1,
      'February': 2,
      'March': 3,
      'April': 4,
      'May': 5,
      'June': 6,
      'July': 7,
      'August': 8,
      'September': 9,
      'October': 10,
      'November': 11,
      'December': 12
    }
    const lastDay = (new Date(data.year, dateMap[data.month], 1)).getTime() / 1000
    const firstDay = (new Date(data.year, dateMap[data.month] - 1, 2)).getTime() / 1000
    const trips = await db.main('trips')
      .where({fleet_id: data.fleetId})
      .where('date_created', '>=', firstDay)
      .where('date_created', '<=', lastDay)
    for (let trip of trips) {
      if (trip.bike_id) {
        let bike = await db.main('bikes').where({bike_id: trip.bike_id}).first()
        trip.bike = bike
      } else if (trip.hub_id) {
        let hub = await db.main('hubs').where({hub_id: trip.hub_id}).first()
        trip.hub = hub
      } else if (trip.port_id) {
        let port = await db.main('ports').where({port_id: trip.port_id}).first()
        trip.port = port
      }
    }
    return trips
  } catch (error) {
    Sentry.captureException(error, 'get trip by month', {data})
    throw errors.customError(
      `An error occurred while fetching trips fleet ${data.fleetId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

const getFleetTimeZone = async (data) => {
  const fleetMetadata = await db.main('fleet_metadata').where({fleet_id: data.fleetId}).first()
  return (fleetMetadata && fleetMetadata.fleet_timezone) || 'US/Eastern'
}

const getFleetDistancePreference = async (data) => {
  const currentFleet = await db.main('fleets').where({fleet_id: data.fleetId}).first()
  return (currentFleet && currentFleet.distance_preference) || 'kilometers'
}

const getTripDistance = async (trip, fleetDetails) => {
  const distanceMeasurement = await getFleetDistancePreference(fleetDetails)
  const rawDistance = tripHelper.getTripDistance(trip)
  return distanceMeasurement === 'kilometers'
    ? conversions.metersToKilometers(rawDistance) : conversions.metersToMiles(rawDistance)
}
const generateFleetUtilizationReport = async (fleetDetails) => {
  try {
    const trips = await getTripsByMonth(fleetDetails)
    const fleetTimeZone = await getFleetTimeZone(fleetDetails)
    const distanceMeasurement = await getFleetDistancePreference(fleetDetails)
    if (trips.length) {
      const promises = trips.map(async (trip) => {
        const startDate = moment(new Date(trip.date_created * 1000))
        let tripDistance = 0
        // trip distance will only exist if the rental is a bike
        if (trip.bike_id) {
          tripDistance = await getTripDistance(trip, fleetDetails)
          console.log('tripDistance', tripDistance)
        }
        const ungroupedTrips = {}
        ungroupedTrips['date'] = startDate.tz(`${fleetTimeZone}`).format('YYYY-MM-DD')
        ungroupedTrips['trip_duration_in_seconds'] = tripUtil.formatTripForWire(trip).duration
        ungroupedTrips[`trip_distance_${distanceMeasurement}`] = tripDistance
        return ungroupedTrips
      })

      const tripMetrics = await Promise.all(promises)
      const groupedTrips = tripMetrics.reduce((trips, trip) => {
        const ndx = trips.findIndex(e => e.date.includes(trip.date))
        if (ndx > -1) {
          trips[ndx].date = trip.date
          trips[ndx].trip_duration += trip['trip_duration_in_seconds'] ? trip['trip_duration_in_seconds'] : 0
          trips[ndx][`trip_distance_${distanceMeasurement}`] += trip[`trip_distance_${distanceMeasurement}`]
          trips[ndx].number_of_trips += 1
        } else {
          trips.push({
            ...trip,
            date: trip.date,
            trip_duration: trip['trip_duration_in_seconds'],
            number_of_trips: 1
          })
        }
        const finalTrips = []
        trips.forEach(trip => {
          trip['average_trip_duration_in_seconds'] = trip.trip_duration / trip.number_of_trips
          finalTrips.push(trip)
        })
        return finalTrips
      }, [])
      const utilizationCsv = json2csv({
        data: groupedTrips,
        fields: ['date', 'average_trip_duration_in_seconds', `trip_distance_${distanceMeasurement}`, 'number_of_trips']
      })
      const uploadedFile = await uploadFile.uploadReportFile(
        `FleetUtilization-${fleetDetails.fleetId}.csv`,
        utilizationCsv, 'text/csv',
        platformConfig.aws.s3.fleetReports.bucket
      )
      return uploadedFile
    }
    return {message: `There is no fleet activity for ${fleetDetails.month} ${fleetDetails.year}`}
  } catch (error) {
    Sentry.captureException(error, {fleetDetails})
    throw errors.customError(
      `An error occurred while fetching utilization data for fleet ${fleetDetails.fleetId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

/* This method is used to get the reports of member
 * @param {Object} fleetDetails - contains operator_id and month
 * @param {Function} callback - done
 */
const memberReports = (fleetDetails, done) => {
  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ]
  memberHandler.getPrivateAccounts({fleet_id: fleetDetails.fleet_id}, (error, accounts) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: getting private Accounts for operator:', fleetDetails.operator_id, ': ', error)
      done(errors.internalServer(true), null)
      return
    }
    let verifiedAccounts = _.where(accounts, {verified: 1})
    if (accounts.length === 0 || verifiedAccounts.length === 0) {
      logger('Error: no members present for fleet_id:', fleetDetails.fleet_id)
      done(errors.noError(), null)
      return
    }
    const indexOfMonth = monthNames.indexOf(fleetDetails.month) + 1
    let days = conversions.daysInMonth(indexOfMonth, fleetDetails.year)
    tripHandler.getAllTrips({fleet_id: fleetDetails.fleet_id}, (error, data) => {
      let tripsData = data.trips
      if (error) {
        Sentry.captureException(error)
        logger('Error: getting trips for fleet_id:', fleetDetails.fleet_id, ': ', error)
        done(errors.internalServer(true), null)
        return
      }
      let trips = _.filter(tripsData, (userObj) => {
        return moment.unix(userObj.date_created).format('YYYY') === fleetDetails.year.toString() &&
                    moment.unix(userObj.date_created).format('MMMM') === fleetDetails.month
      })

      if (trips.length === 0) {
        logger('Error: getting trips for fleet:', fleetDetails.fleet_id)
        done(errors.noError(), null)
        return
      }

      let filteredTrips = []
      for (let i = 0; i < verifiedAccounts.length; i++) {
        let verifiedTrips = _.filter(trips, {user_id: verifiedAccounts[i].user_id})
        for (let i = 0; i < verifiedTrips.length; i++) {
          filteredTrips.push(verifiedTrips[i])
        }
      }

      if (filteredTrips.length === 0) {
        logger('Error: getting trips for fleet:', fleetDetails.fleet_id)
        done(errors.noError(), null)
        return
      }
      _.each(filteredTrips, (trip) => {
        trip.date = moment.unix(trip.date_created).format('M/D/YYYY')
      })
      const reports = _.sortBy(filteredTrips, 'date')
      const uniqueReports = []
      let totalMembers = 0 /* eslint-disable-line */
      let totalUsers = []
      let date
      let totalRating = 0
      let averageRating = 0
      let count = 0
      for (let k = 0; k < days; k++) {
        date = fleetDetails.year + '-' + indexOfMonth + '-' + (k + 1)
        const filterDateFormat = indexOfMonth + '/' + (k + 1) + '/' + fleetDetails.year
        const filteredReports = _.filter(reports, (report) => {
          return report.date === filterDateFormat
        })
        const uniqUsers = _.uniq(filteredReports, 'user_id')
        if (uniqUsers.length > 0) {
          uniqUsers.forEach(function (user) {
            if (!totalUsers.includes(user.user_id)) {
              totalUsers.push(user.user_id)
            }
            if (user.rating) {
              totalRating += user.rating
              count += 1
            }
          })
          if (totalRating > 0) {
            averageRating = totalRating / count
          }
        }
        if (filteredReports.length > 0) {
          uniqueReports.push({
            date: date,
            total_number_of_members: accounts.length,
            number_of_active_members: uniqUsers.length
          })
          totalMembers += parseInt(uniqUsers.length)
        } else {
          uniqueReports.push({
            date: date,
            total_number_of_members: accounts.length,
            number_of_active_members: uniqUsers.length
          })
          totalMembers += parseInt(uniqUsers.length)
        }
      }
      uniqueReports.push({
        date: 'total',
        total_number_of_members: accounts.length,
        number_of_active_members: totalUsers.length,
        ratings: 'Average ratings: ' + averageRating.toString()
      })
      const csv = json2csv({
        data: uniqueReports,
        fields: ['date', 'total_number_of_members', 'number_of_active_members', 'ratings']
      })
      uploadFile.uploadAndGetFileFromS3('MemberUtilisationReports-' + fleetDetails.fleet_id + '.csv', csv,
        'text/csv', platformConfig.aws.s3.fleetReports.bucket, (error, fileDetails) => {
          if (error) {
            Sentry.captureException(error, {fleetDetails})
            done(errors.internalServer(false), null)
            return
          }
          done(errors.noError(), {report_link: fileDetails.link})
        })
    })
  })
}

/* This method is used to get the reports of inventory
 * @param {Object} fleetDetails - contains operator_id and month
 * @param {Function} callback - done
 */
const inventoryReportsUtilisation = (fleetDetails, done) => {
  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ]
  const indexOfMonth = monthNames.indexOf(fleetDetails.month) + 1
  let days = conversions.daysInMonth(indexOfMonth, fleetDetails.year)
  bikeFleetHandler.getBikesForFleet(fleetDetails.fleet_id, (error, bikesData) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      logger('Error getting bike Audit logs', error)
      done(errors.internalServer(false), null)
      return
    }
    const bikeIds = []
    _.each(bikesData, (bikes) => {
      bikeIds.push(bikes.bike_id)
    })
    const bikeAuditLogQuery = {lattis_main: 'select * from bikes_audit_log where bike_id IN(' + bikeIds + ')'}
    sqlPool.makeQuery(bikeAuditLogQuery, (error, bikeAuditLog) => {
      if (error) {
        Sentry.captureException(error, {fleetDetails})
        logger('Error getting bike Audit logs', error)
        done(errors.internalServer(false), null)
        return
      }
      let bikeArray = {
        activeBikes: [],
        deletedBikes: [],
        stagingBikes: [],
        outOfServiceBikes: []
      }
      let uniqueReports = []
      if (bikeAuditLog.length === 0) {
        let totalBikes = _.filter(bikesData, (bike) => {
          return moment.unix(bike.date_created).format('YYYY') === fleetDetails.year.toString() &&
                        moment.unix(bike.date_created).format('MMMM') === fleetDetails.month
        })

        let activeBikes = _.filter(totalBikes, (bikes) => {
          return bikes.status === bikeConstants.status.active
        })
        let deletedBikes = _.filter(totalBikes, (bikes) => {
          return bikes.status === bikeConstants.status.deleted
        })
        let stagingBikes = _.filter(totalBikes, (bikes) => {
          return bikes.status === bikeConstants.status.inactive && bikes.current_status === bikeConstants.current_status.lockNotAssigned
        })
        let outOfServiceBikes = _.filter(totalBikes, (bikes) => {
          return bikes.status === bikeConstants.status.suspended
        })
        for (let i = 0; i < days; i++) {
          const date = fleetDetails.year + '-' + indexOfMonth + '-' + (i + 1)
          uniqueReports.push({
            date: moment.unix(new Date(date).getTime() / 1000).format('MM/DD/YYYY'),
            total_number_of_bikes_active: activeBikes.length,
            total_number_of_bikes_deleted: deletedBikes.length,
            total_number_of_bikes_out_of_service: outOfServiceBikes.length,
            total_number_of_bikes_staging: stagingBikes.length
          })
        }
        done(null, uniqueReports)
      } else {
        let bikes = _.filter(bikeAuditLog, (bikeObj) => {
          return moment.unix(bikeObj.timestamp).format('YYYY') === fleetDetails.year.toString() &&
                        moment.unix(bikeObj.timestamp).format('MMMM') === fleetDetails.month &&
                        bikeObj.column_name === 'status'
        })

        if (bikes.length === 0) {
          logger('Error: no trips present for operator:', fleetDetails.operator_id)
          done(errors.noError(), null)
          return
        }

        let reports = []
        _.each(bikes, (bike) => {
          reports.push(_.extend(bike, {
            date: moment.unix(bike.timestamp).format('MM/DD/YYYY'),
            dateWithTime: moment.unix(bike.timestamp).format()
          }))
        })
        for (let i = 0; i < days; i++) {
          const date = fleetDetails.year + '-' + indexOfMonth + '-' + (i + 1)
          let filteredReports = _.filter(reports, (report) => {
            return report.date === moment.unix(new Date(date).getTime() / 1000).format('MM/DD/YYYY')
          })
          if (filteredReports.length > 0) {
            let filteredReportsSorted = _.sortBy(filteredReports, (report) => {
              return report.bike_id && report.dateWithTime
            })
            _.each(filteredReportsSorted, (report) => {
              if (report.change_after === bikeConstants.status.active) {
                let activeBike = _.contains(bikeArray.activeBikes, report.bike_id)
                if (!activeBike) {
                  bikeArray.activeBikes.push(report.bike_id)
                }
              }
              if (report.change_after === bikeConstants.status.inactive) {
                let stagingBike = _.contains(bikeArray.stagingBikes, report.bike_id)
                if (!stagingBike) {
                  bikeArray.stagingBikes.push(report.bike_id)
                }
              }
              if (report.change_after === bikeConstants.status.suspended) {
                let outOfServiceBikes = _.contains(bikeArray.outOfServiceBikes, report.bike_id)
                if (!outOfServiceBikes) {
                  bikeArray.outOfServiceBikes.push(report.bike_id)
                }
              }
              if (report.change_after === bikeConstants.status.deleted) {
                let deletedBikes = _.contains(bikeArray.deletedBikes, report.bike_id)
                if (!deletedBikes) {
                  bikeArray.deletedBikes.push(report.bike_id)
                }
              }
              if (report.change_before === bikeConstants.status.active) {
                let activeBike = _.contains(bikeArray.activeBikes, report.bike_id)
                if (activeBike) {
                  let index = bikeArray.activeBikes.indexOf(report.bike_id)
                  bikeArray.activeBikes.splice(index, 1)
                }
              }
              if (report.change_before === bikeConstants.status.inactive) {
                let stagingBike = _.contains(bikeArray.stagingBikes, report.bike_id)
                if (stagingBike) {
                  let index = bikeArray.stagingBikes.indexOf(report.bike_id)
                  bikeArray.stagingBikes.splice(index, 1)
                }
              }
              if (report.change_before === bikeConstants.status.suspended) {
                let outOfServiceBikes = _.contains(bikeArray.outOfServiceBikes, report.bike_id)
                if (outOfServiceBikes) {
                  let index = bikeArray.outOfServiceBikes.indexOf(report.bike_id)
                  bikeArray.outOfServiceBikes.splice(index, 1)
                }
              }
              if (report.change_before === bikeConstants.status.inactive) {
                let deletedBikes = _.contains(bikeArray.deletedBikes, report.bike_id)
                if (deletedBikes) {
                  let index = bikeArray.deletedBikes.indexOf(report.bike_id)
                  bikeArray.deletedBikes.splice(index, 1)
                }
              }
            })
            uniqueReports.push({
              date: moment.unix(new Date(date).getTime() / 1000).format('MM/DD/YYYY'),
              total_number_of_bikes_active: bikeArray.activeBikes.length,
              total_number_of_bikes_deleted: bikeArray.deletedBikes.length,
              total_number_of_bikes_out_of_service: bikeArray.outOfServiceBikes.length,
              total_number_of_bikes_staging: bikeArray.stagingBikes.length
            })
          } else {
            uniqueReports.push({
              date: moment.unix(new Date(date).getTime() / 1000).format('MM/DD/YYYY'),
              total_number_of_bikes_active: bikeArray.activeBikes.length,
              total_number_of_bikes_deleted: bikeArray.deletedBikes.length,
              total_number_of_bikes_out_of_service: bikeArray.outOfServiceBikes.length,
              total_number_of_bikes_staging: bikeArray.stagingBikes.length
            })
          }
        }

        const csv = json2csv({
          data: uniqueReports,
          fields: ['date', 'total_number_of_bikes_active', 'total_number_of_bikes_deleted', 'total_number_of_bikes_out_of_service',
            'total_number_of_bikes_staging']
        })
        uploadFile.uploadAndGetFileFromS3('InventoryUtilisationReports-' + fleetDetails.fleet_id + '.csv', csv,
          'text/csv', platformConfig.aws.s3.fleetReports.bucket, (error, fileDetails) => {
            if (error) {
              Sentry.captureException(error)
              done(errors.internalServer(false), null)
              return
            }
            done(errors.noError(), {report_link: fileDetails.link})
          })
      }
    })
  })
}

/* This method is used to get the reports of maintenance
 * @param {Object} fleetDetails - contains operator_id and month
 * @param {Function} callback - done
 */
const maintenanceReportsUtilisation = (fleetDetails, done) => {
  const maintenanceQuery = query.selectWithAnd(dbConstants.tables.maintenance, null, {fleet_id: fleetDetails.fleet_id})
  sqlPool.makeQuery(maintenanceQuery, (error, maintenanceData) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      logger('Error: getting trips for operator:', fleetDetails.operator_id, ': ', error)
      done(errors.internalServer(true), null)
      return
    }
    let maintenance = _.filter(maintenanceData, (maintenanceObj) => {
      return moment.unix(maintenanceObj.service_start_date).format('YYYY') === fleetDetails.year.toString() &&
                moment.unix(maintenanceObj.service_start_date).format('MMMM') === fleetDetails.month &&
                maintenanceObj.service_start_date !== null
    })

    ticketHandler.getTickets({fleet_id: fleetDetails.fleet_id}, (error, ticketsData) => {
      if (error) {
        Sentry.captureException(error, {fleetDetails})
        logger('Error: getting trips for operator:', fleetDetails.fleet_id, ': ', error)
        done(errors.internalServer(true), null)
        return
      }

      let tickets = _.filter(ticketsData, (ticket) => {
        return moment.unix(ticket.date_created).format('YYYY') === fleetDetails.year.toString() &&
                    moment.unix(ticket.date_created).format('MMMM') === fleetDetails.month &&
                    ticket.type === ticketConstants.types.theft
      })

      if (maintenance.length === 0 && tickets.length === 0) {
        logger('Error: no maintenance present for fleet:', fleetDetails.fleet_id)
        done(errors.noError(), null)
        return
      }

      let reports = []
      _.each(maintenance, (maintenanceObj) => {
        reports.push({
          date: moment.unix(maintenanceObj.service_start_date).format('MM/DD/YYYY'),
          total_number_of_bikes_out_of_service: 0,
          average_time_out_of_service: 0,
          num_service_due: 0,
          start_time: maintenanceObj.service_start_date,
          end_time: maintenanceObj.service_end_date ? maintenanceObj.service_end_date : null,
          category: maintenanceObj.category
        })
      })

      if (tickets.length > 0) {
        _.each(tickets, (ticket) => {
          reports.push({
            date: moment.unix(ticket.date_created).format('MM/DD/YYYY'),
            total_number_of_bikes_out_of_service: 0,
            average_time_out_of_service: 0,
            num_service_due: 0,
            start_time: ticket.date_created,
            end_time: ticket.date_resolved ? ticket.date_resolved : null
          })
        })
      }
      reports = _.sortBy(reports, 'date')
      let uniqueReports = []
      let tempArray = []
      const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ]
      const indexOfMonth = monthNames.indexOf(fleetDetails.month) + 1
      let days = conversions.daysInMonth(indexOfMonth, fleetDetails.year)
      for (let i = 0; i < days; i++) {
        const date = fleetDetails.year + '-' + indexOfMonth + '-' + (i + 1)
        const time = moment.unix(new Date(date).getTime() / 1000)
        let filteredReports = _.filter(reports, (report) => {
          return report.date === time.format('MM/DD/YYYY')
        })
        let filteredMaintenanceDueBikes = _.filter(reports, (report) => {
          return report.date === time.format('MM/DD/YYYY') && report.category === maintenanceConstants.category.maintenanceDue
        })
        let currentDay = parseInt(time.format('MM/DD/YYYY').split('/')[1])
        let totalBikes
        let totalSeconds
        if (filteredReports.length > 0) {
          _.each(filteredReports, (report, index) => {
            if (!report.end_time) {
              let seconds = tempArray[currentDay] ? tempArray[currentDay].numberOfSeconds ? tempArray[currentDay].numberOfSeconds : 0 : 0
              let bikes = tempArray[currentDay] ? tempArray[currentDay].numberOfBikes ? tempArray[currentDay].numberOfBikes : 0 : 0
              tempArray[currentDay] = {
                numberOfSeconds: seconds + 3600,
                numberOfBikes: bikes + 1
              }
            } else {
              if (moment.unix(report.start_time).isSame(moment.unix(report.end_time), 'day')) {
                let seconds = tempArray[currentDay] ? tempArray[currentDay].numberOfSeconds ? tempArray[currentDay].numberOfSeconds : 0 : 0
                let bikes = tempArray[currentDay] ? tempArray[currentDay].numberOfBikes ? tempArray[currentDay].numberOfBikes : 0 : 0
                tempArray[currentDay] = {
                  numberOfSeconds: seconds + 3600,
                  numberOfBikes: bikes + 1
                }
              } else {
                let daysDifference = conversions.getDaysDiff(report.start_time, report.end_time)
                let firstDaySecondDiff = 3600 - parseInt(conversions.secondsToHours(report.start_time))
                let seconds = tempArray[currentDay] ? tempArray[currentDay].numberOfSeconds ? tempArray[currentDay].numberOfSeconds : 0 : 0
                let bikes = tempArray[currentDay] ? tempArray[currentDay].numberOfBikes ? tempArray[currentDay].numberOfBikes : 0 : 0
                tempArray[currentDay] = {
                  numberOfSeconds: seconds + firstDaySecondDiff,
                  numberOfBikes: bikes + 1
                }
                let currentDayIndex
                let betweenDaysBikes = 0
                for (let i = 0; i < daysDifference - 1; i++) {
                  currentDayIndex = currentDay + (i + 1)
                  let seconds = tempArray[currentDayIndex] ? tempArray[currentDayIndex].numberOfSeconds ? tempArray[currentDayIndex].numberOfSeconds : 0 : 0
                  let bikes = tempArray[currentDayIndex] ? tempArray[currentDayIndex].numberOfBikes ? tempArray[currentDayIndex].numberOfBikes : 0 : 0
                  betweenDaysBikes = bikes + 1
                  tempArray[currentDayIndex] = {
                    numberOfSeconds: seconds + 3600,
                    numberOfBikes: betweenDaysBikes
                  }
                }
                seconds = tempArray[currentDayIndex + 1] ? tempArray[currentDayIndex].numberOfSeconds ? tempArray[currentDayIndex].numberOfSeconds : 0 : 0
                const lastDayHourDiff = parseInt(conversions.secondsToHours(report.end_time))
                const lastDayBikes = tempArray[currentDayIndex + 1] ? tempArray[currentDayIndex + 1].numberOfBikes ? tempArray[currentDayIndex + 1].numberOfBikes : 0 : 0
                tempArray[currentDayIndex + 1] = {
                  numberOfSeconds: seconds + lastDayHourDiff,
                  numberOfBikes: lastDayBikes + 1
                }
              }
            }
            if (filteredReports.length - 1 === index) {
              totalBikes = tempArray[currentDay] ? tempArray[currentDay].numberOfBikes ? tempArray[currentDay].numberOfBikes : 0 : 0
              totalSeconds = tempArray[currentDay] ? tempArray[currentDay].numberOfSeconds ? tempArray[currentDay].numberOfSeconds : 0 : 0
            }
          })
          let averageSeconds = totalSeconds / totalBikes
          uniqueReports.push({
            date: time.format('MM/DD/YYYY'),
            total_number_of_bikes_out_of_service: totalBikes,
            average_time_out_of_service: conversions.secondsToMinutes(averageSeconds),
            num_service_due: filteredMaintenanceDueBikes.length
          })
        } else {
          let totalBikes = tempArray[currentDay] ? tempArray[currentDay].numberOfBikes ? tempArray[currentDay].numberOfBikes : 0 : 0
          let numberOfSeconds = tempArray[currentDay] ? tempArray[currentDay].numberOfSeconds ? tempArray[currentDay].numberOfSeconds : 0 : 0
          let averageCalculation = numberOfSeconds / totalBikes

          uniqueReports.push({
            date: moment.unix(new Date(date).getTime() / 1000).format('MM/DD/YYYY'),
            total_number_of_bikes_out_of_service: totalBikes,
            average_time_out_of_service: conversions.secondsToMinutes(averageCalculation),
            num_service_due: filteredMaintenanceDueBikes.length
          })
        }
      }
      const csv = json2csv({
        data: uniqueReports,
        fields: ['date', 'total_number_of_bikes_out_of_service', 'average_time_out_of_service', 'num_service_due']
      })
      uploadFile.uploadAndGetFileFromS3('MaintenanceUtilisationReports-' + fleetDetails.fleet_id + '.csv', csv,
        'text/csv', platformConfig.aws.s3.fleetReports.bucket, (error, fileDetails) => {
          if (error) {
            Sentry.captureException(error)
            done(errors.internalServer(false), null)
            return
          }
          done(errors.noError(), {report_link: fileDetails.link})
        })
    })
  })
}

/* This method is used to get the reports on revenue generation
 * @param {Object} fleetDetails - contains operator_id and month
 * @param {Function} callback - done
 */
const revenueReports = (fleetDetails, done) => {
  tripHandler.getTripPaymentTransaction({fleet_id: fleetDetails.fleet_id}, (error, revenueData) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails})
      logger('Error: getting trips for operator:', fleetDetails.fleet_id, ': ', error)
      done(errors.internalServer(true), null)
      return
    }
    let revenue = _.filter(revenueData, (revenueObj) => {
      return revenueObj.date_charged !== null && moment.unix(revenueObj.date_charged).format('YYYY') === fleetDetails.year.toString() &&
        moment.unix(revenueObj.date_charged).format('MMMM') === fleetDetails.month
    })
    if (revenue.length === 0) {
      logger('Error: no maintenance present for fleet:', fleetDetails.fleet_id)
      done(errors.noError(), null)
      return
    }
    // months < than 10 need to be made 2 digit
    const month = String(moment().month(fleetDetails.month).format('M')).padStart(2, '0')
    const days = conversions.daysInMonth(month, fleetDetails.year)
    let blankReports = []
    for (let i = 0; i < days; i++) {
      // dates < than 10 need to be made 2 digit
      let date = month + '/' + String(i + 1).padStart(2, '0') + '/' + fleetDetails.year
      blankReports[i] = {
        date: date,
        gross_revenue: 0,
        penalties: 0,
        refunds: 0
      }
    }
    let loadedReports = []
    _.each(revenue, (revenueObj) => {
      loadedReports.push({
        date: moment.unix(revenueObj.date_charged).format('MM/DD/YYYY'),
        gross_revenue: revenueObj.total,
        penalties: revenueObj.penalty_fees,
        refunds: revenueObj.total_refunded ? revenueObj.total_refunded : 0
      })
    })
    loadedReports = _.sortBy(loadedReports, 'date')
    const loadedGroups = _.groupBy(loadedReports, 'date')
    const blankGroups = _.groupBy(blankReports, 'date')
    let unifiedReports = Object.assign(blankGroups, loadedGroups)
    const reports = Object.keys(unifiedReports).reduce((totals, date) => {
      const values = unifiedReports[date]
      const report = values.reduce(
        (acc, value) => {
          acc.total_fee += value.gross_revenue
          acc.total_penalties += value.penalties
          acc.total_refunds += value.refunds
          acc.net_fee += value.gross_revenue - value.refunds
          return acc
        },
        {
          date,
          total_fee: 0,
          total_penalties: 0,
          total_refunds: 0,
          net_fee: 0
        }
      )
      totals.push(report)
      return totals
    }, [])
    const csv = json2csv({
      data: reports,
      fields: ['date', 'total_fee', 'total_penalties', 'total_refunds', 'net_fee']
    })
    uploadFile.uploadAndGetFileFromS3('RevenueReports-' + fleetDetails.fleet_id + '.csv', csv,
      'text/csv', platformConfig.aws.s3.fleetReports.bucket, (error, fileDetails) => {
        if (error) {
          Sentry.captureException(error)
          done(errors.internalServer(false), null)
          return
        }
        done(errors.noError(), {report_link: fileDetails.link})
      })
  })
}

/* This method is used to get the reports of member
 * @param {Object} fleetDetails - contains operator_id and month
 * @param {Function} callback - done
 */
const locationReports = (fleetDetails, done) => {
  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ]
  const indexOfMonth = monthNames.indexOf(fleetDetails.month) + 1
  let locationReportQuery = query.customQuery('lattis_main', 'CALL proc_location_feed(' + fleetDetails.fleet_id + ',' + indexOfMonth + ', ' + fleetDetails.year + ' )')
  sqlPool.makeQuery(locationReportQuery, (error, locationData) => {
    if (error) {
      Sentry.captureException(error, {locationReportQuery})
      logger('Error: getting trips for fleet:', fleetDetails.fleet_id, ': ', error)
      done(errors.internalServer(true), null)
      return
    }
    let locData = []
    _.each(locationData, (locationObj) => {
      _.each(locationObj, (locationDataObj) => {
        locData.push(locationDataObj)
      })
    })
    if (locData.length === 0) {
      logger('Error: trips not found for operator:', fleetDetails.operator_id)
      done(errors.noError(), null)
      return
    }
    let uniqReports = []
    _.each(locData, (locationObj) => {
      uniqReports.push({
        date: locationObj.Date,
        location: locationObj.Location,
        pick_ups: locationObj.Pick_Ups,
        drop_offs: locationObj.Drop_offs
      })
    })
    const csv = json2csv({
      data: uniqReports,
      fields: ['date', 'location', 'pick_ups', 'drop_offs']
    })
    uploadFile.uploadAndGetFileFromS3('LocationUtilisationReports-' + fleetDetails.fleet_id + '.csv', csv,
      'text/csv', platformConfig.aws.s3.fleetReports.bucket, (error, fileDetails) => {
        if (error) {
          Sentry.captureException(error)
          done(errors.internalServer(true), null)
          return
        }
        done(errors.noError(), {report_link: fileDetails.link})
      })
  })
}

const generateTripsReport = async (data) => {
  try {
    let queryTripPayments = false
    const trips = await getTripsByMonth(data)
    const { type: fleetType } = await db.main('fleets').where({fleet_id: data.fleetId}).first()
    if (['private', 'public'].includes(fleetType)) {
      queryTripPayments = true
    }
    const fleetTimezone = await getFleetTimeZone(data)
    const distanceMeasurement = await getFleetDistancePreference(data)
    if (trips.length) {
      const taxColumns = ['Tax A', 'Tax B', 'Tax C']
      const promises = trips.map(async (trip) => {
        const steps = JSON.parse(trip.steps)
        const user = await db.users('users').where({user_id: trip.user_id}).first()
        const startDate = moment(new Date(trip.date_created * 1000))
        const endDate = moment(new Date(trip.date_endtrip * 1000))
        let tripDetails = {
          StartDate: startDate.tz(`${fleetTimezone}`).format('YYYY-MM-DD HH:mm:ss'),
          EndDate: endDate.tz(`${fleetTimezone}`).format('YYYY-MM-DD HH:mm:ss'),
          UserFirstName: user.first_name,
          UserLastName: user.last_name,
          UserEmail: user.email,
          UserPhoneNumber: user.phone_number
        }
        if (queryTripPayments) {
          const trxDetails = await db.main('trip_payment_transactions').where({trip_id: trip.trip_id}).first()
          const refundDetails = await db.main('user_refunded').where({trip_id: trip.trip_id}).first()
          tripDetails['Tax A'] = 'N/A'
          tripDetails['Tax B'] = 'N/A'
          tripDetails['Tax C'] = 'N/A'
          if (refundDetails) {
            tripDetails['RefundDetails'] = refundDetails.amount_refunded
          }
          if (trxDetails && trxDetails.taxes) {
            if (trxDetails.taxes[0]) {
              const tax = trxDetails.taxes[0]
              const columnDetails = `${tax.name}(${tax.percentage}%) ${Number(tax.amount).toFixed(2)}`
              tripDetails['Tax A'] = columnDetails
            }
            if (trxDetails.taxes[1]) {
              const tax = trxDetails.taxes[1]
              const columnDetails = `${tax.name}(${tax.percentage}%) ${Number(tax.amount).toFixed(2)}`
              tripDetails['Tax B'] = columnDetails
            }
            if (trxDetails.taxes[2]) {
              const tax = trxDetails.taxes[2]
              const columnDetails = `${tax.name}(${tax.percentage}%) ${Number(tax.amount).toFixed(2)}`
              tripDetails['Tax C'] = columnDetails
            }
          }
          tripDetails.TaxSubtotal = trxDetails && trxDetails.tax_sub_total
          tripDetails.TripCharges = (trxDetails && trxDetails.total && trxDetails.taxes !== null) ? trxDetails.total : (trxDetails && trxDetails.total) || 0
        }

        if (trip.bike_id) {
          tripDetails['StartGPSCoordinates'] = steps && steps.length ? `${steps[0][0]}, ${steps[0][1]}` : null
          tripDetails['StopGPSCoordinates'] = steps && steps.length ? `${steps[steps.length - 1][0]}, ${steps[steps.length - 1][1]}` : null
          tripDetails[`TripDistance_${distanceMeasurement}`] = await getTripDistance(trip, data)
          const bike = await db.main('bikes').where({bike_id: trip.bike_id}).first()
          tripDetails['RentalName'] = bike.bike_name
        }
        if (trip.port) {
          const hub = await db.main('hubs').where({uuid: trip.port.hub_uuid}).first()
          tripDetails['RentalName'] = `${hub.name} ${trip.port.number}`
        }
        tripDetails['Rating'] = trip.rating
        tripDetails['TripDuration_seconds'] = trip.date_endtrip - trip.date_created
        return tripDetails
      })

      const tripsAndUsers = await Promise.all(promises)
      const tripsCSV = json2csv({
        data: tripsAndUsers,
        fields: [
          'StartDate',
          'EndDate',
          'UserFirstName',
          'UserLastName',
          'UserEmail',
          'UserPhoneNumber',
          'TripDuration_seconds',
          `TripDistance_${distanceMeasurement}`,
          'StartGPSCoordinates',
          'StopGPSCoordinates',
          ...taxColumns,
          'TaxSubtotal',
          'TripCharges',
          'RefundDetails',
          'Rating',
          'RentalName'
        ]
      })
      const uploadedFile = await uploadFile.uploadReportFile(
        `TripReports-${data.fleetId}.csv`,
        tripsCSV, 'text/csv',
        platformConfig.aws.s3.fleetReports.bucket
      )
      return uploadedFile
    }
    return {message: `There is no fleet activity for ${data.month} ${data.year}`}
  } catch (error) {
    Sentry.captureException(error)
    throw errors.customError(
      `An error occurred while fetching trips report for fleet ${data.fleetId}`,
      platform.responseCodes.InternalServer,
      'InternalServerError',
      false
    )
  }
}

module.exports = {
  memberReports: memberReports,
  inventoryReportsUtilisation: inventoryReportsUtilisation,
  maintenanceReportsUtilisation: maintenanceReportsUtilisation,
  locationReports: locationReports,
  revenueReports: revenueReports,
  generateTripsReport,
  generateFleetUtilizationReport
}
