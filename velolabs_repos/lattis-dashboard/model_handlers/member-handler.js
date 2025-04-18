'use strict'

const platform = require('@velo-labs/platform')
const _ = require('underscore')
const parse = require('csv-parse')
const mv = require('mv')
const json2csv = require('json2csv')
const getCSVFromURL = require('get-csv')
const fs = require('fs')
const moment = require('moment')
const logger = platform.logger
const query = platform.queryCreator
const sqlPool = platform.sqlPool
const errors = platform.errors
const dbConstants = platform.dbConstants
const S3Handler = platform.S3Handler
const platformConfig = platform.config
const tripHandler = require('./trip-handler')
const fleetHandler = require('./fleet-handler')
const config = require('./../config')
const fileUpload = require('./../utils/file-upload')
const usersHandler = require('./users-handler')
const fleetMembershipHandlers = require('./fleet-memberships/fleet-membership-handlers')
const db = require('../db')
const Sentry = require('../utils/configureSentry')

/**
 * Queries to insert private Account
 *
 * @param {object} comparisonColumnsAndValues - filter params
 * @param done
 * @private
 */
const getPrivateAccounts = (comparisonColumnsAndValues, done) => {
  const privateAccountQuery = query.selectWithAnd(dbConstants.tables.private_fleet_users, null, comparisonColumnsAndValues)
  sqlPool.makeQuery(privateAccountQuery, done)
}
/**
 * Queries to get the member List.
 *
 * @param {object} requestParam - object to filter the columns and values
 * @param done
 * @private
 */
const getMemberList = (requestParam, done) => {
  tripHandler.getDistinctColumnInTrips('user_id', { fleet_id: requestParam.fleet_id }, (error, users) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger(`Error: Failed to get member list. Unable to get unique users with trips for fleet: ${requestParam.fleet_id} with error: ${error}`)
      return done(error, null)
    }
    if (users.length === 0) {
      return done(errors.noError(), users)
    }
    usersHandler.getUsers({ user_id: _.pluck(users, 'user_id') }, (error, members) => {
      if (error) {
        Sentry.captureException(error, {user_id: _.pluck(users, 'user_id')})
        logger(`Error: Failed to get member list. Unable to get users with user_id ${_.pluck(users, 'user_id')} with error: ${error}`)
        return done(error, null)
      }
      if (members.length === 0) {
        return done(errors.noError(), members)
      }
      getPrivateAccounts({ user_id: _.pluck(members, 'user_id'), fleet_id: requestParam.fleet_id }, async (error, privateFleets) => {
        if (error) {
          Sentry.captureException(error, {user_id: _.pluck(users, 'user_id')})
          logger(`Error: get private fleets: for ${_.pluck(members, 'user_id')} with error: ${error}`)
          done(errors.internalServer(false), null)
          return
        }
        let memberList = []
        const membersLen = members.length
        for (let i = 0; i < membersLen; i++) {
          const privateAccount = _.findWhere(privateFleets, { user_id: members[i].user_id })
          if (privateAccount) Object.assign(members[i], privateAccount)
          memberList.push(_.omit(members[i],
            'username', 'password', 'reg_id', 'accepted_terms', 'max_locks', 'rest_token', 'refresh_token', 'verified'))
        }

        const userIds = memberList.map(user => user.user_id)
        const subscriptions = _.groupBy(await fleetMembershipHandlers.subscriptions({
          fleet_id: requestParam.fleet_id,
          user_ids: userIds
        }), 'user_id')

        memberList.forEach(member => {
          member.membership_subscriptions = subscriptions[member.user_id]
        })

        done(errors.noError(), memberList)
      })
    })
  })
}

/**
 * Queries to get tripDetails.
 *
 * @param {object} requestParam - object to filter the columns and values
 * @param done
 */
const getTripDetails = (requestParam, done) => {
  tripHandler.getTrips(requestParam, (error, trips) => {
    if (error) {
      Sentry.captureException(error, {requestParam})
      logger('Error: Failed to get trip details with: ', requestParam)
      return done(error, null)
    }
    let tripDetails = []
    _.each(trips, (trip) => {
      tripDetails.push(_.omit(trip, 'username', 'password', 'reg_id', 'accepted_terms', 'max_locks'))
    })
    done(null, tripDetails)
  })
}

/*
 * Query to grant or revoke access to private fleet
 * @param {object} memberDetails - object to filter the columns and values
 * @param done  - callback
 */
const grantOrRevokeMemberAccess = (comparison, access, AccessMode, done) => {
  if (comparison.verified) {
    if (comparison.add_new_users) {
      let usersToAdd = comparison.add_new_users
      usersToAdd.forEach(function (user) {
        const insertMemberQuery = query.insertSingle(
          dbConstants.tables.private_fleet_users, {
            email: user.email,
            user_id: user.user_id,
            fleet_id: comparison.fleet_id,
            verified: comparison.verified,
            access: user.access,
            access_mode: AccessMode,
            updated_at: moment().unix()
          })
        sqlPool.makeQuery(insertMemberQuery, (error) => {
          if (error) {
            Sentry.captureException(error, {user, comparison})
            logger('Error: inserting new member query:', insertMemberQuery, ' with error: ', errors.errorWithMessage(error))
            return done(errors.internalServer(false))
          }
        })
      })
      done(errors.noError())
    } else {
      const insertMemberQuery = query.insertSingle(
        dbConstants.tables.private_fleet_users, {
          email: comparison.email,
          user_id: comparison.user_id,
          fleet_id: comparison.fleet_id,
          verified: comparison.verified,
          access: !!access,
          access_mode: AccessMode,
          updated_at: moment().unix()
        })
      sqlPool.makeQuery(insertMemberQuery, (error) => {
        if (error) {
          Sentry.captureException(error, {comparison, insertMemberQuery})
          logger('Error: inserting new member query:', insertMemberQuery, ' with error: ', errors.errorWithMessage(error))
          return done(errors.internalServer(false))
        }
        done(errors.noError())
      })
    }
  } else {
    let verified = 0
    if (comparison.publicFleet === 0 || comparison.publicFleet === 1) {
      verified = comparison.publicFleet
      delete comparison.publicFleet
    }
    const revokeMemberQuery = query.updateSingle(
      dbConstants.tables.private_fleet_users,
      { access: !!access, updated_at: moment().unix(), access_mode: AccessMode, verified: verified },
      comparison
    )
    sqlPool.makeQuery(revokeMemberQuery, (error) => {
      if (error) {
        Sentry.captureException(error, {comparison, revokeMemberQuery})
        logger('Error: making update private fleet user access query:', revokeMemberQuery, ' with error: ', errors.errorWithMessage(error))
        return done(errors.internalServer(false))
      }
      done(errors.noError())
    })
  }
}

/**
 * Updates member profile information
 *
 * @param {Object} userDetails - user information
 * @param {Function} done - Callback function with error and status
 */
const updateMemberProfile = (userDetails, done) => {
  let columnsAndValues = {
    first_name: userDetails.first_name,
    last_name: userDetails.last_name,
    phone_number: userDetails.phone_number
  }
  const updateMemberProfileQuery = query.updateSingle(dbConstants.tables.users, columnsAndValues, { user_id: userDetails.user_id })
  sqlPool.makeQuery(updateMemberProfileQuery, (error) => {
    if (error) {
      Sentry.captureException(error, {userDetails, updateMemberProfileQuery})
      logger('Error: making updateMemberProfile Query:', errors.errorWithMessage(error))
      done(errors.internalServer(false), null)
      return
    }
    done(null, null)
  })
}

/**
 * changes access permission from revoke to grant
 *
 * @param {Object} members - user information
 * @param {Object} access - revoke(0)/grant(1)
 * @param {Function} done - Callback function with error and status
 */
const changeAccessPermissionForMembers = (members, access, done) => {
  const membersLen = members.length
  if (membersLen === 0) {
    done(errors.noError())
  }
  for (let i = 0; i < membersLen; i++) {
    const changeAccessPermissionQuery = query.updateSingle(dbConstants.tables.private_fleet_users,
      { access: access, updated_at: moment().unix() }, { private_fleet_user_id: members[i].private_fleet_user_id })
    sqlPool.makeQuery(changeAccessPermissionQuery, (error) => {
      if (error) {
        Sentry.captureException(error, {members, changeAccessPermissionQuery})
        done(errors.internalServer(false))
        return
      }
      if (i === membersLen - 1) {
        return done(errors.noError())
      }
    })
  }
}

/**
 * Fetches the data for old CSV File
 *
 * @param {File} oldCSV - csvFile
 * @param {Function} done - Callback function with error and status
 */
const getOldCSVData = (oldCSV, done) => {
  let valuesFromOldCSV = []
  getCSVFromURL(oldCSV, { headers: false }).then(rows => {
    let row
    for (let i = 0, rowsLen = rows.length; i < rowsLen; i++) {
      row = rows[i]
      for (let j = 0, valuesLen = row.length; j < valuesLen; j++) {
        valuesFromOldCSV.push(row[j])
      }
    }
    done(errors.noError(), valuesFromOldCSV)
  })
}

/**
 * getCSV file for member
 *
 * @param {Object} fleetDetails - fleet Information
 * @param {Function} done - Callback function with error and status
 */
const getCSV = (fleetDetails, done) => {
  const fleetDetailsQuery = query.selectWithAnd(dbConstants.tables.fleets, null, { fleet_id: fleetDetails.fleet_id })
  sqlPool.makeQuery(fleetDetailsQuery, (error, csvImage) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails, fleetDetailsQuery})
      done(errors.internalServer(true), null)
      return
    }
    done(null, csvImage)
  })
}

/**
 * used to revoke members from oldCSV
 *
 * @param {Object} fleetDetails - fleet Information
 * @param {Function} done - Callback function with error and status
 */
const revokeMembersFromOldCSV = (fleetDetails, done) => {
  const fleetDetailsQuery = query.selectWithAnd(dbConstants.tables.fleets, null, { fleet_id: fleetDetails.fleet_id })
  sqlPool.makeQuery(fleetDetailsQuery, (error, csvDetails) => {
    if (error) {
      Sentry.captureException(error, {fleetDetails, fleetDetailsQuery})
      logger('Error: Failed to make fleet query for fleet:', fleetDetails.fleet_id)
      done(errors.internalServer(false), null)
      return
    }
    if (csvDetails.length > 0 && csvDetails[0].member_csv) {
      getOldCSVData(csvDetails[0].member_csv, (error, existingMemberData) => {
        if (error) {
          Sentry.captureException(error, {fleetDetails})
          logger('Error: Failed to fetch old csv data for file:', csvDetails[0].member_csv)
          done(errors.internalServer(false), null)
          return
        }
        let comparisonColumns = []
        let comparisonValues = []
        _.each(existingMemberData, (value) => {
          comparisonColumns.push('email')
          comparisonValues.push(value)
        })
        getPrivateAccounts(comparisonColumns, comparisonValues, (error, privateAccountsToBeRevoked) => {
          if (error) {
            Sentry.captureException(error, {fleetDetails})
            logger('Error: Failed to make private fleet query for fleet:', fleetDetails.fleet_id)
            done(errors.internalServer(true), null)
            return
          }
          if (privateAccountsToBeRevoked.length === 0) {
            done(errors.noError(), null)
            return
          }
          changeAccessPermissionForMembers(privateAccountsToBeRevoked, 0, (error) => {
            if (error) {
              Sentry.captureException(error, {privateAccountsToBeRevoked})
              logger('Error: Failed to revoke access permission on members for fleet:', fleetDetails.fleet_id)
              done(errors.internalServer(true), null)
              return
            }
            const s3Handler = new S3Handler()
            let fileName = csvDetails[0].member_csv.toString().match(/.*\/(.+?)\./)
            fileName = fileName[1] + '.csv'
            s3Handler.deleteFile(fileName, platformConfig.aws.s3.memberLists.bucket, (error) => {
              if (error) {
                Sentry.captureException(error, {fileName})
                logger('Error: Failed to delete csv file:', fileName, 'from bucket', platformConfig.aws.s3.memberLists.bucket)
              }
              const updateFleetsQuery = query.updateSingle(dbConstants.tables.fleets, { member_csv: null },
                { fleet_id: fleetDetails.fleet_id })
              sqlPool.makeQuery(updateFleetsQuery, (error) => {
                if (error) {
                  Sentry.captureException(error, {fleetDetails, updateFleetsQuery})
                  logger('Error: Failed to remove old link from fleets table for fleet:', fleetDetails.fleet_id)
                  done(errors.internalServer(false), null)
                  return
                }
                done(errors.noError(), null)
              })
            })
          })
        })
      })
    } else {
      done(errors.noError(), null)
    }
  })
}

const updateMembers = (userDetails, fleetId, done) => {
  const file = userDetails.member_csv
  const csvPath = config.filePaths.filesDir + '/CSV'
  if (!fs.existsSync(csvPath)) {
    fs.mkdirSync(csvPath, '0744')
  }
  mv(file.path, csvPath + '/' + file.originalFilename, (error) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: Unable to move member CSV file')
      done(errors.internalServer(false), null)
      return
    }
    readCsv((csvPath + '/' + file.originalFilename).toString(), (error, newMembers) => {
      if (error) {
        Sentry.captureException(error)
        return done(error, null)
      }
      getPrivateAccounts({ fleet_id: fleetId }, (error, privateAccounts) => {
        if (error) {
          Sentry.captureException(error, {fleetId})
          logger('Error: Failed to make select query for private accounts: parseCSVFile')
          return done(errors.internalServer(false), null)
        }
        fleetHandler.getDomains({ fleet_id: fleetId }, (error, domains) => {
          if (error) {
            Sentry.captureException(error, {fleetId})
            logger('Error: Failed to make select query for private accounts: parseCSVFile')
            return done(errors.internalServer(false), null)
          }
          usersHandler.getUsers({ email: newMembers, verified: 1 }, (error, users) => {
            if (error) {
              Sentry.captureException(error, {newMembers})
              logger('Error: Parsing CSV. Failed to get users', errors.errorWithMessage(error))
              return done(errors.internalServer(false), null)
            }
            const membersToBeAccessRevoked = _.difference(_.uniq(_.pluck(_.where(privateAccounts, { access: 1, access_mode: 'csv' }), 'email')), newMembers)
            const existingMembers = _.intersection(_.uniq(_.pluck(_.where(privateAccounts, {access: 0}), 'email')), newMembers)
            let insertNewMembers = _.difference(newMembers, _.uniq(_.pluck(_.where(privateAccounts, {}), 'email')))
            if (membersToBeAccessRevoked.length) {
              membersToBeAccessRevoked.forEach(async (email) => {
                await db.users('private_fleet_users').update({access: 0, verified: 0, user_id: null}).where({email: email})
              })
            }
            if (existingMembers.length) {
              existingMembers.forEach(async (email) => {
                await db.users('private_fleet_users').update({access: 1, verified: 0, user_id: null, access_mode: 'csv'}).where({email: email})
              })
            }
            if (insertNewMembers.length) {
              insertNewMembers.forEach(async (email) => {
                await db.users('private_fleet_users').insert(
                  {
                    access: 1,
                    verified: 0,
                    user_id: null,
                    email: email,
                    fleet_id: fleetId,
                    access_mode: 'csv',
                    updated_at: moment().unix()}
                )
              })
            }
            done(errors.noError())
          })
        })
      })
    })
  })
}

const revokeAccessToMembers = (uniqueKey, uniqueKeyValue, fleetId, grantAccess, done) => {
  if (!!fleetId && !!uniqueKey &&
    (Array.isArray(uniqueKeyValue) ? uniqueKeyValue.length : (!!uniqueKeyValue || Number.isFinite(uniqueKeyValue)))) {
    if (grantAccess === true) {
      grantOrRevokeMemberAccess({ [uniqueKey]: uniqueKeyValue, fleet_id: fleetId }, true, 'domain', done)
    } else {
      grantOrRevokeMemberAccess({ [uniqueKey]: uniqueKeyValue, fleet_id: fleetId }, false, 'domain', done)
    }
  } else {
    done(errors.noError())
  }
}

const filterMemberWithDomains = (membersEmails, domains) => {
  const domainsLength = domains.length
  if (domainsLength === 0) {
    return []
  }
  return _.filter(membersEmails, member => (domains.indexOf('@' + member.split('@')[1]) !== -1))
}

const readCsv = (filePath, done) => {
  const source = fs.createReadStream(filePath)
  const parser = parse({
    delimiter: ','
  })
  source.pipe(parser)
  // Read new members email addresses from CSV uploaded
  let rows = []
  parser.on('data', (data) => { rows = [...rows, data[0]] })

  parser.on('error', (error) => {
    Sentry.captureException(error, {filePath})
    logger('Error: while reading csv file', errors.errorWithMessage(error))
    return done(errors.internalServer(true), null)
  })

  // Completion of CSV reading
  parser.on('end', () => {
    fs.unlink(filePath, (err) => {
      if (err) {
        Sentry.captureException(err, {filePath})
        logger('Error: Unable to delete the CSV file in member CSV upload')
        done(errors.internalServer(false), null)
      }
      done(null, rows)
    })
  })
}

const getMemberCsv = (fleetId, done) => {
  getPrivateAccounts({fleet_id: fleetId, access: 1, access_mode: 'csv'}, (error, privateAccounts) => {
    if (error) {
      Sentry.captureException(error, {fleetId})
      logger('Error: Failed to download member CSV. Failed to get private accounts with fleet_id: ',
        fleetId, ' with error: ', error)
      return done(error, null)
    }
    fleetHandler.getFleetWithFleetId(fleetId, (error, {fleet_name: fleetName}) => {
      if (error) {
        Sentry.captureException(error, {fleetId})
        logger('Error: Fetching fleet for members file name with error ', error)
      }

      let activeMembers = privateAccounts.map((account) => account.email).map((member) => ({member: member}))
      const latestDate = Math.max.apply(Math, privateAccounts.map(function (account) { return account.updated_at }))
      const isValid = (new Date(latestDate)).getTime() > 0
      const lastUpdate = moment.unix(latestDate).format('YYYY-MM-DD_hh_mm')
      let fileName = lastUpdate && isValid ? `/Authorized_Members_${fleetName}_${lastUpdate}.csv` : `/Empty_Member_CSV_File.csv`
      logger('activeMembers: ', activeMembers)
      const csv = json2csv({
        data: activeMembers,
        fields: ['member'],
        hasCSVColumnTitle: false
      })
      const csvPath = config.filePaths.filesDir + '/CSV'
      if (!fs.existsSync(csvPath)) {
        fs.mkdirSync(csvPath, '0744')
      }
      const filePath = csvPath + fileName
      fs.writeFile(filePath, csv, function (error) {
        if (error) {
          Sentry.captureException(error, {fleetId})
          logger('Error: Failed to get member CSV. Unable to create a CSV file on path: ', filePath, error)
          return done(errors.internalServer(false), null)
        }
        fileUpload.uploadAndGetFileFromS3(fileName, csv,
          'text/csv', platformConfig.aws.s3.memberLists.bucket, async (error, fileDetails) => {
            if (error) {
              Sentry.captureException(error, {fleetId})
              logger('Error: Failed to get member CSV. Unable to upload CSV to S3 with error: ', error)
              done(errors.internalServer(false), null)
              return
            }
            fs.unlink(filePath, (fileError) => {
              if (fileError) {
                Sentry.captureException(fileError, {fleetId})
                logger(`Error: Unable to remove file from path: , ${filePath} with error: `, fileError)
              }
              done(errors.noError(), { file_location: fileDetails.link })
            })
          })
      })
    })
  })
}

const getLattisUser = async (userDetails) => {
  const user = await db.users('users')
    .where({user_id: userDetails.user_id}).first()
  const privateFleetUser = await db.users('private_fleet_users')
    .where({fleet_id: userDetails.fleet_id})
    .where({user_id: user.user_id}).first()
  if (privateFleetUser) {
    user.access = privateFleetUser.access
    user.private_fleet_user_id = privateFleetUser.id
    user.access_email_address = privateFleetUser.email
  }
  return user
}

const toggleIndividualAccess = async (userDetails) => {
  try {
    const existingUser = await db.users('private_fleet_users')
      .where({fleet_id: userDetails.fleet_id})
      .where({email: userDetails.email}).first()
    if (existingUser) {
      await db.users('private_fleet_users').update({access: userDetails.access, verified: 0, access_mode: 'csv'})
        .where({fleet_id: userDetails.fleet_id})
        .where({email: userDetails.email})
    } else {
      await db.users('private_fleet_users')
        .insert(
          {
            email: userDetails.email,
            user_id: userDetails.user_id,
            fleet_id: userDetails.fleet_id,
            verified: 0,
            access: userDetails.access,
            updated_at: moment().unix(),
            access_mode: 'csv'
          }
        )
    }
    const user = await getLattisUser(userDetails)
    return user
  } catch (error) {
    Sentry.captureException(error, {userDetails})
    throw errors.customError(
      `An error occurred while updating user access`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}
const revokeAccessForAllFleetMembers = async (fleetDetails) => {
  try {
    const updateResponse = await db.users('private_fleet_users').update({access: 0, verified: 0, access_mode: 'csv'})
      .where({fleet_id: fleetDetails.fleetId})

    return updateResponse
  } catch (error) {
    Sentry.captureException(error, {fleetDetails})
    throw errors.customError(
      `An error occurred while revoking fleet users access`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}

module.exports = {
  getMemberList: getMemberList,
  getTripDetails: getTripDetails,
  updateMemberProfile: updateMemberProfile,
  grantOrRevokeMemberAccess: grantOrRevokeMemberAccess,
  revokeAccessToMembers: revokeAccessToMembers,
  getCSV: getCSV,
  revokeMembersFromOldCSV: revokeMembersFromOldCSV,
  getPrivateAccounts: getPrivateAccounts,
  updateMembers: updateMembers,
  filterMemberWithDomains: filterMemberWithDomains,
  getMemberCsv: getMemberCsv,
  toggleIndividualAccess,
  getLattisUser,
  revokeAccessForAllFleetMembers
}
