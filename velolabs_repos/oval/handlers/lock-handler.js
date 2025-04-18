'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const encryptionHandler = platform.encryptionHandler
const config = require('./../config')
const errors = platform.errors
const LockFormatter = require('./../helpers/lock-formatter')
const hash = require('./../utils/hash')
const conversions = require('./../utils/conversions')
const moment = require('moment')
const RestHandler = platform.RestHandler
const _ = require('underscore')
const FirmwareHandler = require('./firmware-handler')
const ContactHandler = require('./contact-handler')
const userHandler = require('./user-handler')
const randomCodeGenerator = require('./../helpers/random-code-gernerator')
const twilioHandler = require('./twilo-handler')
const phoneNumberHandler = require('./../helpers/phone-number-helper')
const textHelper = require('./../helpers/text-helper')
const async = require('async')
const FirebaseHandler = require('./firebase-handler')
const bikeHandler = require('./bike-handler')
const dbConstants = platform.dbConstants
const SQSHandler = platform.SQSHandler
const notificationTypes = platform.notificationTypes
const sqsConstants = platform.sqsConstants
const fleetHandler = require('./../handlers/fleet-handler')
const lockFormatter = new LockFormatter()
const db = require('../db')
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

/**
 * The method retrieves a single lock for given parameter.
 *
 * @param {string} filterParam
 * @param {function} callback
 */
const getLocks = (filterParam, callback) => {
  let comparisonColumn = {}
  if (filterParam.mac_id) {
    comparisonColumn.mac_id = filterParam.mac_id
  } else if (filterParam.lock_id) {
    comparisonColumn.lock_id = filterParam.lock_id
  } else if (filterParam.user_id) {
    comparisonColumn.user_id = filterParam.user_id
  } else if (filterParam.fleet_id) {
    comparisonColumn.fleet_id = filterParam.fleet_id
  } else {
    comparisonColumn.operator_id = filterParam.operator_id
  }
  const query = queryCreator.selectWithAnd(
    dbConstants.tables.locks,
    null,
    comparisonColumn
  )
  sqlPool.makeQuery(query, (error, locks) => {
    if (error) {
      Sentry.captureException(error, { comparisonColumn })
      logger(
        'Error: could not get lock with:',
        comparisonColumn,
        'Failed with error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    callback(null, locks)
  })
}

/**
 * The method retrieves a single lock for given mac id.
 *
 * @param {string} encryptedMacId
 * @param {function} callback
 */
const _getLockWithMacId = (encryptedMacId, callback) => {
  const query = queryCreator.selectWithAnd('locks', null, {
    mac_id: encryptedMacId
  })
  sqlPool.makeQuery(query, (error, locks) => {
    if (error) {
      Sentry.captureException(error, { encryptedMacId })
      logger(
        'Error: could not get lock with mac id:',
        encryptedMacId,
        'Failed with error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (!locks || locks.length === 0) {
      logger(
        'There is no lock in the database with mac id:',
        encryptionHandler.decryptDbValue(encryptedMacId)
      )
      callback(null, null)
      return
    }

    callback(null, locks[0])
  })
}

/**
 * The method retrieves a single lock for given lock id.
 *
 * @param {number} lockId
 * @param {function} callback
 */
const getLockWithLockId = (lockId, callback) => {
  const query = queryCreator.selectWithAnd('locks', null, { lock_id: lockId })
  sqlPool.makeQuery(query, (error, locks) => {
    if (error) {
      Sentry.captureException(error, { lockId })
      logger(
        'Error: could not get lock with lock id:',
        lockId,
        'Error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (!locks || locks.length === 0) {
      logger('There is no lock in the database with lock id:', lockId)
      callback(null, null)
      return
    }

    callback(null, locks[0])
  })
}

/**
 * This method saves a lock to the database for a given user.
 *
 * @param {string} macId - The un-encrypted mac Id of the lock.
 * @param {object} user
 * @param {function} callback
 */
const saveNewLock = (macId, user, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(macId)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { macId })
      logger(
        'Error: could not save new lock:',
        macId,
        'Failed with error',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (lock && lock.user_id) {
      if (lock.user_id === user.user_id) {
        callback(null, lock)
      } else {
        logger(
          'Error: can not save lock',
          macId,
          'It has already been registered'
        )
        callback(errors.lockAlreadyRegistered(true), macId)
      }

      return
    }

    const keys = encryptionHandler.getNewEllipticalKeys()
    const encryptedKey = encryptionHandler.encryptDbValue(keys.privateKey)
    let newLock = {
      user_id: user.user_id,
      mac_id: encryptedMacId,
      key: encryptedKey
    }

    if (lock) {
      newLock.lock_id = lock.lock_id
    }

    _saveLock(newLock, !lock, callback)
  })
}

const saveNewLockForFleet = (lockDetails, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(lockDetails.mac_id)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { macId: lockDetails.mac_id })
      logger(
        'Error: failed to save new lock with:',
        lockDetails,
        'Failed with error: ',
        error
      )
      return callback(error, null)
    }
    fleetHandler.getFleets(
      { fleet_id: lockDetails.fleet_id },
      (error, fleets) => {
        if (error) {
          Sentry.captureException(error, { fleetId: lockDetails.fleet_id })
          logger(
            'Error: failed to save new lock. Unable to get fleets with error: ',
            error
          )
          return callback(errors.internalServer(false), null)
        }
        if (fleets.length === 0) {
          logger(
            'Error: failed to save new lock. There is no fleet with:',
            lockDetails.fleet_id
          )
          return callback(errors.resourceNotFound(false), null)
        }
        if (lock && lock.fleet_id) {
          if (lock.fleet_id === lockDetails.fleet_id) {
            _getSignedMessageAndPublicKeyForLock(
              lock,
              encryptionHandler.decryptDbValue(fleets[0].key),
              false,
              (error, lockKeys) => {
                if (error) {
                  Sentry.captureException(error, { lockId: lock.lock_id })
                  return callback(error, null)
                }
                callback(
                  null,
                  Object.assign(lock, {
                    signed_message: lockKeys.signedMessage,
                    public_key: lockKeys.publicKey
                  })
                )
              }
            )
          } else {
            logger(
              'Error: can not save lock',
              lockDetails.mac_id,
              'It has already been registered'
            )
            callback(errors.lockAlreadyRegistered(true), lockDetails.mac_id)
          }

          return
        }

        var lockParams = {
          name: fleets[0].fleet_name + '-' + hash.randomDigits(10),
          mac_id: encryptedMacId,
          fleet_id: fleets[0].fleet_id,
          operator_id: fleets[0].operator_id,
          customer_id: fleets[0].customer_id
        }
        if (lock) {
          lockParams.lock_id = lock.lock_id
        } else {
          const keys = encryptionHandler.getNewEllipticalKeys()
          lockParams.key = encryptionHandler.encryptDbValue(keys.privateKey)
        }
        _saveLock(lockParams, lock === null, (error, newLock) => {
          if (error) {
            Sentry.captureException(error, { lockId: lock.lock_id })
            return callback(error, null)
          }
          _getSignedMessageAndPublicKeyForLock(
            newLock,
            encryptionHandler.decryptDbValue(fleets[0].key),
            false,
            (error, lockKeys) => {
              if (error) {
                Sentry.captureException(error, { lockId: newLock.lock_id })
                return callback(error, null)
              }
              callback(
                null,
                Object.assign(newLock, {
                  signed_message: lockKeys.signedMessage,
                  public_key: lockKeys.publicKey
                })
              )
            }
          )
        })
      }
    )
  })
}

/**
 * This method updates and saves a lock that is already in the database.
 *
 * @param {object} lock
 * @param {object} user
 * @param {function} callback
 */
const saveExistingLock = (lock, user, callback) => {
  logger('saving lock:', lock)
  getLockWithLockId(lock.lock_id, (error, dbLock) => {
    if (error) {
      Sentry.captureException(error, { lockId: lock.lock_id })
      logger(
        'Error: saving existing lock. Failed with error:',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false), null)
      return
    }

    if (!dbLock) {
      logger(
        'Failed to update lock properties for lock:',
        lock.lock_id,
        'Lock not found in db.'
      )
      callback(errors.resourceNotFound(false), null)
      return
    }

    if (user.user_id !== dbLock.user_id) {
      logger('Could not update lock:', lock.lock_id)
      return callback(errors.unauthorizedAccess(false), null)
    }

    _saveLock(updateLockProperties(dbLock, lock), false, callback)
  })
}

/**
 * This method saves a lock to the database and returns the updated lock
 * pulled from the database in the callback.
 *
 * @param {object} lock
 * @param {boolean} isNew
 * @param {function} callback
 * @private
 */
const _saveLock = (lock, isNew, callback) => {
  let query
  if (isNew) {
    query = queryCreator.insertSingle('locks', lock)
  } else {
    query = queryCreator.updateSingle('locks', _.omit(lock, 'lock_id'), {
      lock_id: lock.lock_id
    })
  }

  sqlPool.makeQuery(query, (error) => {
    if (error) {
      Sentry.captureException(error, { lockId: lock.lock_id })
      logger(
        'Error: could not save lock',
        lock.mac_id,
        'Failed with error:',
        error
      )
      callback(errors.internalServer(false), null)
      return
    }

    const selectQuery = queryCreator.selectWithAnd('locks', null, {
      mac_id: lock.mac_id
    })
    sqlPool.makeQuery(selectQuery, (error, locks) => {
      if (error) {
        Sentry.captureException(error, { lockId: lock.lock_id })
        logger(
          'Error: failed to save lock. Could not retrieve lock after saving.',
          'Failed with error:',
          errors.errorWithMessage(error)
        )
        callback(errors.internalServer(false), null)
        return
      }

      callback(null, locks && locks.length > 0 ? locks[0] : null)
    })
  })
}

/**
 * This method is to get public message and signed message for the lock.
 *
 * @param {Object} lockDetails - lock identifier
 * @param {function} callback
 */
const getSignedMessageAndPublicKeyForOperator = (lockDetails, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(lockDetails.mac_id)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { lockId: lockDetails.lock_id })
      logger(
        'Error: failed to get signed message and public key for lock with:',
        lockDetails,
        'Failed with error',
        error
      )
      callback(error, null)
      return
    }
    fleetHandler.getFleets(
      { fleet_id: lockDetails.fleet_id },
      async (error, fleets) => {
        if (error) {
          Sentry.captureException(error, { lockId: lockDetails.lock_id })
          logger(
            'Error: failed to get signed message and public key. There is a problem to get fleet with:',
            error
          )
          callback(errors.internalServer(false), null)
          return
        }
        if (fleets.length === 0) {
          logger(
            'Error: failed to get signed message and public key. There is no fleet with:',
            lockDetails.fleet_id
          )
          callback(errors.resourceNotFound(false), null)
          return
        }
        if (!lock || !lock.fleet_id) {
          let newLock
          try {
            await db.main.transaction(async (trx) => {
              const result = await db
                .main('locks')
                .transacting(trx)
                .forUpdate()
                .count('*', { as: 'count' })
                .where({ fleet_id: lockDetails.fleet_id })

              const keys = encryptionHandler.getNewEllipticalKeys()
              const encryptedKey = encryptionHandler.encryptDbValue(
                keys.privateKey
              )
              const lockParams = {
                name: fleets[0].fleet_name + '-' + (result[0].count + 1),
                mac_id: encryptedMacId,
                key: encryptedKey,
                fleet_id: fleets[0].fleet_id,
                operator_id: fleets[0].operator_id,
                customer_id: fleets[0].customer_id
              }

              if (lock) {
                await trx('locks')
                  .where({ lock_id: lock.lock_id })
                  .update(lockParams)
              } else {
                await trx('locks').insert(lockParams)
              }
            })
            newLock = await db
              .main('locks')
              .first()
              .where({ mac_id: encryptedMacId })
          } catch (error) {
            Sentry.captureException(error, { lockId: lockDetails.lock_id })
            logger(
              'Error: updating lock query:',
              errors.errorWithMessage(error)
            )
            return callback(error, null)
          }
          _getSignedMessageAndPublicKeyForLock(
            _.pick(newLock, 'key', 'mac_id'),
            encryptionHandler.decryptDbValue(fleets[0].key),
            false,
            (error, messageAndKey) => {
              if (error) {
                Sentry.captureException(error, { lockId: lockDetails.lock_id })
                logger(
                  'Error: could not get signed message and key. Failed with error:',
                  error
                )
                callback(error, null)
                return
              }
              callback(null, messageAndKey)
            }
          )
        } else {
          if (lockDetails.fleet_id !== lock.fleet_id) {
            logger(
              'Error: failed to get signed message and public key. The lock is not registered with:',
              lockDetails.fleet_id
            )
            callback(errors.unauthorizedAccess(false), null)
            return
          }
          _getSignedMessageAndPublicKeyForLock(
            _.pick(lock, 'key', 'mac_id'),
            encryptionHandler.decryptDbValue(fleets[0].key),
            false,
            (error, messageAndKey) => {
              if (error) {
                Sentry.captureException(error, { lockId: lockDetails.lock_id })
                logger(
                  'Error: could not get signed message and key. Failed with error:',
                  error
                )
                callback(error, null)
                return
              }
              callback(null, messageAndKey)
            }
          )
        }
      }
    )
  })
}

/**
 * This method is to get public message and signed message for the lock.
 *
 * @param {Object} lockDetails - lock identifier
 * @param {Integer} userId - user identifier
 * @param {Object} hashKey - identifier of the user/operator
 * @param {function} callback
 */
const getSignedMessageAndPublicKey = (
  lockDetails,
  userId,
  hashKey,
  callback
) => {
  const encryptedMacId = lockDetails.mac_id
    ? encryptionHandler.encryptDbValue(lockDetails.mac_id)
    : null
  if (userId) {
    _getLockWithMacId(encryptedMacId, (error, lock) => {
      if (error) {
        Sentry.captureException(error, { lockId: lockDetails.lock_id })
        logger(
          'Error: failed to get signed message and public key for lock with mac_id:',
          lockDetails.mac_id,
          'Failed with error',
          error
        )
        callback(error, null)
        return
      }
      if (!lock) {
        logger(
          'Error: failed to get signed message and public for lock:',
          lockDetails.lock_id,
          'There is no lock in the database with that id.'
        )
        callback(errors.resourceNotFound(false), null)
        return
      }
      if (lock.user_id !== userId) {
        _isLockShared(lock, userId, (error) => {
          if (error) {
            Sentry.captureException(error, { lockId: lockDetails.lock_id })
            logger(
              'Error: failed to get signed message and public key for lock with mac_id:',
              lockDetails.mac_id,
              'Failed with error',
              error
            )
            return callback(error, null)
          }
          _getSignedMessageAndPublicKeyForLock(
            _.pick(lock, 'key', 'mac_id'),
            hashKey,
            true,
            (error, messageAndKey) => {
              if (error) {
                Sentry.captureException(error, { lockId: lockDetails.lock_id })
                logger(
                  'Error: could not get signed message and key. Failed with error:',
                  error
                )
                callback(error, null)
                return
              }
              callback(null, messageAndKey)
            }
          )
        })
      } else {
        _getSignedMessageAndPublicKeyForLock(
          _.pick(lock, 'key', 'mac_id'),
          hashKey,
          false,
          (error, messageAndKey) => {
            if (error) {
              Sentry.captureException(error, { lockId: lockDetails.lock_id })
              logger(
                'Error: could not get signed message and key. Failed with error:',
                error
              )
              callback(error, null)
              return
            }
            callback(null, messageAndKey)
          }
        )
      }
    })
  } else {
    getLocks(
      lockDetails.lock_id
        ? { lock_id: lockDetails.lock_id }
        : { mac_id: encryptedMacId },
      (error, locks) => {
        if (error) {
          Sentry.captureException(error, { lockId: lockDetails.lock_id })
          logger(
            'Error: failed to get signed message and public key for lock with:',
            lockDetails,
            'Failed with error',
            error
          )
          callback(error, null)
          return
        }
        if (locks.length === 0) {
          if (!lockDetails.fleet_id) {
            logger(
              'Error: failed to get signed message and public for lock:',
              lockDetails,
              'There is no lock in the database with that id.'
            )
            return callback(errors.resourceNotFound(false), null)
          } else {
            fleetHandler.getFleets(
              { fleet_id: lockDetails.fleet_id },
              (error, fleets) => {
                if (error) {
                  Sentry.captureException(error, {
                    lockId: lockDetails.lock_id
                  })
                  logger(
                    'Error: failed to get signed message and public key. There is a problem to get fleet with:',
                    error
                  )
                  callback(errors.internalServer(false), null)
                  return
                }
                if (fleets.length === 0) {
                  logger(
                    'Error: failed to get signed message and public key. There is no fleet with:',
                    lockDetails.fleet_id
                  )
                  callback(errors.resourceNotFound(false), null)
                  return
                }
                const keys = encryptionHandler.getNewEllipticalKeys()
                const encryptedKey = encryptionHandler.encryptDbValue(
                  keys.privateKey
                )
                _saveLock(
                  {
                    user_id: null,
                    mac_id: encryptedMacId,
                    key: encryptedKey,
                    fleet_id: fleets[0].fleet_id,
                    operator_id: fleets[0].operator_id,
                    customer_id: fleets[0].customer_id
                  },
                  true,
                  (error, newLock) => {
                    if (error) {
                      Sentry.captureException(error, {
                        lockId: lockDetails.lock_id
                      })
                      logger(
                        'Error: updating lock query:',
                        errors.errorWithMessage(error)
                      )
                      return callback(error, null)
                    }

                    _getSignedMessageAndPublicKeyForLock(
                      _.pick(newLock, 'key', 'mac_id'),
                      encryptionHandler.decryptDbValue(fleets[0].key),
                      false,
                      (error, messageAndKey) => {
                        if (error) {
                          Sentry.captureException(error, {
                            lockId: lockDetails.lock_id
                          })
                          logger(
                            'Error: could not get signed message and key. Failed with error:',
                            error
                          )
                          callback(error, null)
                          return
                        }
                        callback(null, messageAndKey)
                      }
                    )
                  }
                )
              }
            )
          }
        } else {
          fleetHandler.getFleets(
            _.pick(locks[0], 'fleet_id'),
            (error, fleets) => {
              if (error) {
                Sentry.captureException(error, { lockId: lockDetails.lock_id })
                logger(
                  'Error: failed to get signed message and public key. There is a problem to get fleet with:',
                  error
                )
                callback(errors.internalServer(false), null)
                return
              }
              if (fleets.length === 0) {
                logger(
                  'Error: failed to get signed message and public key. There is no fleet with:',
                  lockDetails.fleet_id
                )
                callback(errors.resourceNotFound(false), null)
                return
              }
              if (lockDetails.fleet_id !== locks[0].fleet_id) {
                logger(
                  'Error: failed to get signed message and public key. The lock is not registered with:',
                  lockDetails.fleet_id
                )
                callback(errors.unauthorizedAccess(false), null)
                return
              }
              _getSignedMessageAndPublicKeyForLock(
                _.pick(locks[0], 'key', 'mac_id'),
                fleetHandler.formatFleetForWire(fleets[0]).key,
                false,
                (error, messageAndKey) => {
                  if (error) {
                    Sentry.captureException(error, {
                      lockId: lockDetails.lock_id
                    })
                    logger(
                      'Error: could not get signed message and key. Failed with error:',
                      error
                    )
                    callback(error, null)
                    return
                  }
                  callback(null, messageAndKey)
                }
              )
            }
          )
        }
      }
    )
  }
}

/**
 * This method gets all firmware versions.
 *
 * @param {Object} lock - lock identifier
 * @param {number} userId
 * @param {function} callback
 * @private
 */
const _isLockShared = (lock, userId, callback) => {
  getSharedLocks(lock.lock_id, (error, sharedLocks) => {
    if (error) {
      Sentry.captureException(error, { lockId: lock.lock_id })
      logger(
        'Error: Failed to get shared locks with lock_id',
        lock.lock_id,
        'with error:',
        error
      )
      callback(error)
      return
    }

    let isUsersLock = false
    if (sharedLocks && sharedLocks.length > 0) {
      // Let's check to see if the lock is shared to the incoming user.
      let sharedLock
      for (let i = 0; i < sharedLocks.length; i++) {
        if (!sharedLocks[i].sharing_stopped) {
          sharedLock = sharedLocks[i]
          break
        }
      }

      if (sharedLock) {
        logger('sharedLock', sharedLock)
        isUsersLock = sharedLock && sharedLock.shared_to_user_id === userId
      }
    }

    if (!isUsersLock) {
      logger(
        'The lock with user id',
        lock.user_id,
        'does not belong to user with id:',
        userId
      )
      callback(error)
      return
    }
    callback(null)
  })
}

/**
 * This method gets all firmware versions.
 *
 * @param {function} callback
 */
const getFirmwareVersions = (callback) => {
  const firmwareHandler = new FirmwareHandler()
  firmwareHandler.fetchFirmwareFiles((error, files) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: failed to fetch firmware versions. Failed with error:', error)
      callback(errors.internalServer(false), null)
      return
    }

    let versionNumbers = []
    _.each(files, (file) => {
      versionNumbers.push(firmwareHandler.versionNumberFromFileName(file))
    })

    callback(null, versionNumbers)
  })
}

/**
 * This method will fetch the version of the firmware supplied in the argument.
 * If the version number is not supplied, it'll get the latest version.
 *
 * @param {string || null} version
 * @param {function} callback
 */
const getFirmware = (version = null, callback) => {
  const firmwareHandler = new FirmwareHandler()
  firmwareHandler.fetchFirmwareFiles((error, files) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: failed to fetch firmware version:', version, 'Failed with error:', error)
      callback(errors.internalServer(false), null)
      return
    }

    if (!files || files.length === 0) {
      callback(null, null)
      return
    }

    let fileName
    if (version) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        if (version === firmwareHandler.versionNumberFromFileName(file)) {
          fileName = file
          break
        }
      }
    } else {
      // If there is no firmware version passed in, let's just grab the latest.
      fileName = files[files.length - 1]
    }

    fileName ? firmwareHandler.fetchFirmware(fileName, callback) : callback(null, null)
  })
}

/**
 * This method will fetch the firmware log of the version in the supplied argument.
 * If the version number is not supplied, it'll get the latest log.
 *
 * @param {string || null} version
 * @param {function} callback
 */
const getFirmwareLog = (version = null, callback) => {
  const firmwareHandler = new FirmwareHandler()
  firmwareHandler.getUpdateLog((error, allLogs) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: failed to fetch firmware logs:', version, 'Failed with error:', error)
      callback(errors.internalServer(false), null)
      return
    }

    let keys = _.keys(allLogs)
    if (!allLogs || keys.length === 0) {
      callback(null, null)
      return
    }

    let logs
    if (version && _.has(allLogs, version)) {
      logs = allLogs[version]
    } else {
      if (keys.length === 1) {
        logs = allLogs[keys[0]]
      } else {
        keys.sort()
        logs = allLogs[keys[keys.length - 1]]
      }
    }

    callback(null, logs)
  })
}

/**
 * This method formats a single lock to be sent over the wire. Only properties that
 * are "safe" to be sent will be sent.
 *
 * @param {object} lock
 * @param {string} usersId
 * @returns {object} formatted lock
 */
const formatLockForWire = (lock, usersId) => {
  return lockFormatter.formatLockForWire(lock, usersId)
}

/**
 * This method formats a single lock to be sent over the wire. Properties that
 * are formatted properly for the client will be sent.
 *
 * @param {Array} locks
 * @param {Array} fleets
 * @param {Array} pinCodes
 * @returns {Array} formatted locks entity
 */
const formatLockEntityForWire = (locks, fleets, pinCodes) => {
  return lockFormatter.formatLockEntityForWire(locks, fleets, pinCodes)
}

/**
 * This method gets all locks for a given user.
 *
 * @param {number} userId
 * @param {function} callback
 */
const getLocksForUser = (userId, callback) => {
  const query = queryCreator.selectWithAnd('locks', null, {user_id: userId})
  sqlPool.makeQuery(query, callback)
}

/**
 * This method assembles the signed message for a lock.
 *
 * @param {string} privateKey - Hex encoded 32 byte string
 * @param {string} publicKey - Hex encoded 64 byte string
 * @param {string} macId - Locks decoded macId
 * @param {string} usersId
 * @param {string} timeStamp - 4 byte string time out value
 * @param {string} owner - 1 byte string either `00` for owner or `01` for guest
 * @param {string} security - 1 byte security
 * @param {function} callback
 */
const _signedMessageForLock = (privateKey,
  publicKey,
  macId,
  usersId,
  timeStamp = 'ffffffff',
  owner = '00',
  security = '00',
  callback) => {
  const payload = {
    mac_id: macId,
    user_id: usersId,
    private_key: privateKey,
    public_key: publicKey,
    time: timeStamp,
    security: security,
    owner: owner
  }

  const options = {
    host: config.keyMaster.host,
    path: config.keyMaster.path,
    port: config.keyMaster.port,
    payload: payload,
    headers: {'Content-Type': 'application/json'}
  }

  const restHandler = new RestHandler()
  restHandler.postRequest(options, (error, responseData) => {
    if (error) {
      Sentry.captureException(error, { options })
      logger('Error: failed to fetch response data for lock:', macId, 'Failed with error', error)
      callback(errors.failedRestCall(false), null)
      return
    }
    try {
      const response = JSON.parse(responseData);
      (response.status === 200 || response.status === 201)
        ? callback(null, response.payload) : callback(errors.failedRestCall(false), null)
    } catch (e) {
      callback(errors.internalServer(e))
    }
  })
}

// const signedMessageForLock = function(lock, timeStamp='ffffffff', owner='00', security='00') {
//     const privateKey = encryptionHandler.decryptedValue(
//         lock.key,
//         config.ellipticalKeys.database.publicKey,
//         config.ellipticalKeys.database.privateKey
//     );
//
//     const decryptedMacId = encryptionHandler.decryptedValue(
//         lock.mac_id,
//         config.ellipticalKeys.database.publicKey,
//         config.ellipticalKeys.database.privateKey
//     );
//
//     const seedData = decryptedMacId + moment().unix().toString('8');
//
//     const randomDataForLock = encryptionHandler.encryptValue(
//         seedData,
//         encryptionHandler.publicKeyForPrivateKey(privateKey),
//         privateKey
//     );
//
//     const randomData = randomDataForLock.substring(0, 62);
//     const message1 = owner + randomData + timeStamp + security;
//     logger('pre hash message', message1, 'with length:', message1.length/2);
//     const messageSHA256 = encryptionHandler.sha256Hash(message1);
//     logger('hashed message:', messageSHA256, 'with length:', messageSHA256.length/2);
//     const signature = encryptionHandler.ellipticallySign(messageSHA256, privateKey);
//     const signatureString = signature.r.toString('hex') + signature.s.toString('hex');
//     const signedMessage = message1 + signatureString;
//
//     logger('signedMessage:', signedMessage);
//     logger('signed message length:', signedMessage.length/2);
//
//     return message1 + signatureString;
// };

/**
 * This method retrieves the signed message and public key for a lock.
 *
 * @param {object} lock
 * @param {string} usersId
 * @param {boolean} isShared
 * @param {function} callback
 */
const _getSignedMessageAndPublicKeyForLock = (lock, usersId, isShared, callback) => {
  const privateKey = encryptionHandler.decryptDbValue(lock.key)
  const decryptedMacId = encryptionHandler.decryptDbValue(lock.mac_id)
  const publicKey = encryptionHandler.publicKeyForPrivateKey(privateKey)
  _signedMessageForLock(
    privateKey,
    publicKey,
    decryptedMacId,
    usersId,
    undefined,
    isShared ? '01' : '00',
    undefined,
    (error, response) => {
      if (error) {
        Sentry.captureException(error, { lock, usersId })
        logger(
          'Error: failed to fetch signed message for:',
          decryptedMacId,
          'Failed with error',
          error
        )
        callback(error, null)
        return
      }

      const payload = {
        signedMessage: response.signed_message,
        publicKey: publicKey
      }

      callback(null, payload)
    }
  )
}

/**
 * This method updates a lock with new properties from another lock object.
 *
 * @param {object} oldLock - The lock to copy properties into
 * @param newLock newLock - The lock to copy properties from
 * @returns {object} - The old lock with it's properties updated from the new lock
 */
const updateLockProperties = (oldLock, newLock) => {
  newLock = _.omit(newLock, 'lock_id', 'mac_id', 'user_id', 'key')
  _.each(newLock, (propertyValue, propertyKey) => {
    if (_.has(oldLock, propertyKey)) {
      oldLock[propertyKey] = propertyValue
    }
  })

  return oldLock
}

/**
 * This method saves the pin code for a user to the database.
 *
 * @param {object} lockParams
 * @param {object} user
 * @param {function} callback
 */
const savePinCode = (lockParams, user, callback) => {
  const filterParam = lockParams.mac_id ? {mac_id: encryptionHandler.encryptDbValue(lockParams.mac_id)}
    : _.pick(lockParams, 'lock_id')
  getLocks(filterParam, (error, locks) => {
    if (error) {
      Sentry.captureException(error, { lockParams, user })
      logger(
        'Error: could not save pin code for:',
        filterParam, 'Failed with error:',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false))
      return
    }

    if (locks.length < 1) {
      logger('Could not save pin code for:', filterParam, 'No matching lock in the db.')
      callback(null)
      return
    }
    const lock = locks[0]
    if (user.user_id) {
      if (lock.user_id !== user.user_id) {
        logger('Could not save pin code for lock:', lock.lock_id, "it is not user's:", user.user_id)
        callback(errors.unauthorizedAccess(false))
        return
      }
    }
    let query = queryCreator.selectWithAnd('pin_codes', null, {lock_id: lock.lock_id})
    sqlPool.makeQuery(query, (error, pinCodes) => {
      if (error) {
        Sentry.captureException(error, { lockParams, user })
        logger(
          'Error: could not save pin code for lock with lock_id:', lock.lock_id,
          'Could not retrieve pin codes. Failed with error:', errors.errorWithMessage(error))
        return callback(error)
      }

      const encryptedCode = encryptionHandler.encryptDbValue(lockParams.pin_code.toString())
      if (pinCodes.length === 0) {
        const pinCode = {
          lock_id: lock.lock_id,
          user_id: lock.user_id,
          code: encryptedCode,
          date: moment().unix()
        }
        query = queryCreator.insertSingle('pin_codes', pinCode)
      } else {
        let pinCode = pinCodes[0]
        const pinCodeId = pinCode.pin_code_id
        pinCode = _.omit(pinCode, 'pin_code_id')
        pinCode.user_id = lock.user_id
        pinCode.date = moment().unix()
        pinCode.code = encryptedCode
        query = queryCreator.updateSingle('pin_codes', pinCode, {pin_code_id: pinCodeId})
      }

      if (user.user_id || !lock.fleet_id) {
        sqlPool.makeQuery(query, callback)
      } else {
        fleetHandler.getFleetAssociations({fleet_id: lock.fleet_id, operator_id: user.operator_id},
          (error, fleetAssociations) => {
            if (error) {
              Sentry.captureException(error, { lockParams, user })
              logger('Error: could not save pin code for:', lock.lock_id, 'Unable to fetch fleet associations with',
                {fleet_id: lock.fleet_id, operator_id: user.operator_id}, errors.errorWithMessage(error)
              )
              callback(error)
              return
            }
            if (fleetAssociations.length === 0) {
              logger('Could not save pin code for lock:', lock.lock_id,
                'The operator:', user.operator_id, 'does not have permission to the fleet')
              return callback(errors.unauthorizedAccess(false))
            } else {
              sqlPool.makeQuery(query, callback)
            }
          })
      }
    })
  })
}

/**
 * This method removes a lock from the database.
 *
 * @param {string} macId
 * @param {object} user
 * @param {function} callback
 */
const deleteLock = (macId, user, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(macId)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { macId, user })
      logger('Error: could not delete lock with id:', macId, 'Failed with error:', error)
      callback(error)
      return
    }

    if (!lock) {
      logger('Could not delete lock with id:', macId, 'The lock is not in the database.')
      callback(null)
      return
    }

    if (user.user_id !== lock.user_id) {
      logger('Could not delete lock with id:', macId, 'The lock does not belong to user:', user.user_id)
      callback(errors.unauthorizedAccess(false))
      return
    }

    const updateValues = {
      user_id: null,
      key: null,
      name: null,
      status: null,
      battery_level: null,
      operator_id: null,
      customer_id: null,
      fleet_id: null
    }

    let query = queryCreator.updateSingle('locks', updateValues, {lock_id: lock.lock_id})
    sqlPool.makeQuery(query, callback)

    getSharedLocks(lock.lock_id, (error, sharedLocks) => {
      if (error) {
        Sentry.captureException(error, { macId, user })
        logger('While deleting lock failed to remove shared locks with error:', error)
        return
      }

      _.each(sharedLocks, (sharedLock) => {
        revokeSharing(user, sharedLock.shared_to_user_id, sharedLock.share_id, (error) => {
          // Keeping this a soft error since it does not affect the delete functionality .
          if (error) {
            Sentry.captureException(error, { macId, user })
            return logger('Error un-sharing while deleting lock', error)
          }

          logger(
            'Deleted lock:',
            lock.lock_id,
            'and removed it from sharing object:',
            sharedLock.share_id
          )
        })
      })
    })
  })
}

/**
 * Delete all locks (normal and shared) for user.
 *
 * @param user
 * @param callback
 */
const deleteLocksForUser = (user, callback) => {
  const query = queryCreator.selectWithAnd('locks', null, {user_id: user.user_id})
  sqlPool.makeQuery(query, (error, locks) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Failed to delete locks for deleted user:', user.user_id)
      callback(error)
      return
    }

    if (!locks || locks.length === 0) {
      return callback()
    }

    let asyncFunctions = []
    _.each(locks, (lock) => {
      asyncFunctions.push((completion) => {
        deleteLock(encryptionHandler.decryptDbValue(lock.mac_id), user, (error) => {
          if (error) {
            Sentry.captureException(error, { user })
            logger(
              'Error: could not delete lock for account. Error:',
              errors.errorWithMessage(error)
            )
            return completion(error)
          }

          completion()
        })
      })
    })

    async.series(asyncFunctions, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger('Failed to delete user with error:', error)
        callback(errors.internalServer(false))
        return
      }

      logger('deleted locks for user', user.user_id)
      callback()
    })
  })
}

/**
 * This method sends an emergency text to the supplied contacts.
 *
 * @param {object} user
 * @param {Array} contacts
 * @param {number} crashId
 * @param {boolean} isFleet
 * @param {function} callback
 */
const sendEmergencyText = (user, contacts, crashId, isFleet = false, callback) => {
  const contactHandler = new ContactHandler()
  _getCrashes({crash_id: crashId}, (error, crashes) => {
    if (error) {
      Sentry.captureException(error, { user })
      // Leaving this as a soft error since the error message was sent successfully.
      logger('Error: failed to get crash details', crashId, 'message sent field')
      return
    }

    if (crashes.length === 0) {
      logger('there are no crashes with crashId', crashId)
      callback(errors.resourceNotFound(false), null)
    }

    const crash = crashes[0]
    const location = {latitude: crash.latitude, longitude: crash.longitude}
    contactHandler.sendEmergencyText(
      userHandler.fullName(user),
      contacts,
      location,
      (error, failedContacts) => {
        Sentry.captureException(error, { user })
        if (failedContacts && failedContacts.length === contacts.length) {
          callback(error, failedContacts)
          return
        }

        const query = queryCreator.updateSingle('crashes', {message_sent: true}, {crash_id: crashId})
        sqlPool.makeQuery(query, (error) => {
          if (error) {
            Sentry.captureException(error, { user })
            // Leaving this as a soft error since the error message was sent successfully.
            logger('Error: failed to update crash', crashId, 'message sent field')
            return
          }

          callback(null, failedContacts)

          const message = {
            type: notificationTypes.crashConfirmed,
            crash_id: crashId,
            date: moment().unix(),
            message_sent: true
          }
          const sqsHandler = new SQSHandler(sqsConstants.lucy.queues.read)
          sqsHandler.sendMessage(message, (error) => {
            if (error) {
              Sentry.captureException(error, { user })
              logger('Error sending crash confirmed message to queue:', error)
            }
          })
        })
      }
    )
  })
}

/**
 * Saves crash data to the database
 *
 * @param {string} macId
 * @param {object} user
 * @param {object} accelerometerData
 * @param {object} location
 * @param {boolean} isFleet
 * @param {function} callback
 */
const saveCrashData = (macId, user, accelerometerData, location, isFleet = false, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(macId)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error: failed to record crash. Could not retrieve lock with id:', macId, 'Error:', error)
      callback(error)
      return
    }

    if (!lock) {
      callback(null)
      return
    }

    accelerometerData.user_id = user.user_id
    accelerometerData.lock_id = lock.lock_id
    accelerometerData.message_sent = false
    accelerometerData.latitude = location.latitude
    accelerometerData.longitude = location.longitude
    accelerometerData.date = moment().unix()

    let query = queryCreator.insertSingle('crashes', accelerometerData)
    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger('Error: failed to save crash data. Failed with error:', error)
        callback(error, null)
        return
      }

      const query = queryCreator.selectWithAnd(
        'crashes',
        null,
        {lock_id: lock.lock_id, message_sent: false}
      )

      sqlPool.makeQuery(query, (error, crashes) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger('Could not retrieve crashes when saving crash data. Error:', error)
          callback(error, null)
          return
        }

        crashes = _.filter(crashes, (crash) => {
          return !crash.message_sent
        })

        if (crashes.length === 0) {
          callback(null, null)
          return
        }

        let crash
        if (crashes.length === 1) {
          crash = crashes[0]
          callback(null, crash)
          return
        }

        crashes.sort((crash1, crash2) => {
          return crash2.date - crash1.date
        })

        crash = crashes[0]
        crash.message_sent = !!crash.message_sent
        callback(null, crash)

        if (isFleet) {
          const message = {
            type: notificationTypes.crashUnconfirmed,
            crash_id: crash.crash_id,
            date: crash.date,
            message_sent: !!crash.message_sent
          }

          const sqsHandler = new SQSHandler(sqsConstants.lucy.queues.read)
          sqsHandler.sendMessage(message, (error) => {
            if (error) {
              Sentry.captureException(error, { user })
              logger('Error sending unconfirmed crash message to queue:', error)
            }
          })
        }
      })
    })
  })
}

/**
 * Saves theft data to the database
 *
 * @param {string} macId
 * @param {object} user
 * @param {object} accelerometerData
 * @param {object} location
 * @param {function} callback
 */
const saveTheftData = (macId, user, accelerometerData, location, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(macId)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error: failed to record theft. Could not retrieve lock with id:', macId, 'Error:', error)
      callback(error)
      return
    }

    if (!lock) {
      callback(null, null)
      return
    }

    const theft = {
      user_id: user.user_id,
      lock_id: lock.lock_id,
      latitude: location.latitude,
      longitude: location.longitude,
      date: moment().unix(),
      confirmed: false
    }

    let query = queryCreator.insertSingle(dbConstants.tables.thefts, _.extend(theft, accelerometerData))
    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger('Error: failed to save theft data. Failed with error:', error)
        callback(error, null)
        return
      }

      const query = queryCreator.selectWithAnd(
        dbConstants.tables.thefts,
        null,
        {lock_id: lock.lock_id}
      )

      sqlPool.makeQuery(query, (error, thefts) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger('Could not retrieve thefts when saving theft data. Error:', error)
          callback(error, null)
          return
        }

        if (thefts.length === 0) {
          callback(null, null)
          return
        }

        thefts = _.where(thefts, {confirmed: 0})

        if (thefts.length === 1) {
          callback(null, thefts[0])
          return
        }

        thefts.sort((theft1, theft2) => {
          return theft2.date - theft1.date
        })

        callback(null, thefts[0])
      })
    })
  })
}

/**
 * This method will update a theft object's confirmed field.
 *
 * @param {number} theftId
 * @param {boolean} isConfirmed
 * @param {function} callback
 */
const confirmOrDenyTheft = (theftId, isConfirmed, callback) => {
  let query = queryCreator.updateSingle('thefts', {confirmed: isConfirmed}, {theft_id: theftId})
  logger(query)
  sqlPool.makeQuery(query, (error) => {
    if (error) {
      Sentry.captureException(error)
      logger(
        'Error: failed to confirm theft:',
        theftId,
        'Failed with error:',
        errors.errorWithMessage(error)
      )
      callback(errors.internalServer(false))
      return
    }

    callback(null)
  })
}

/**
 * This method validates incoming accelerometer data.
 *
 * @param {object} crashData
 * @returns {boolean}
 */
const isAccelerometerDataValid = (crashData) => {
  return _.has(crashData, 'x_ave') &&
    _.has(crashData, 'y_ave') &&
    _.has(crashData, 'z_ave') &&
    _.has(crashData, 'x_dev') &&
    _.has(crashData, 'y_dev') &&
    _.has(crashData, 'z_dev')
}

/**
 * This method gets all shared locks for a given lock id.
 *
 * @param {number} lockId
 * @param {function} callback
 * @private
 */
const getSharedLocks = (lockId, callback) => {
  const query = queryCreator.selectWithAnd('share', null, {lock_id: lockId})
  sqlPool.makeQuery(query, callback)
}

const getSharedLock = (shareId, callback) => {
  const query = queryCreator.selectWithAnd('share', null, {share_id: shareId})
  sqlPool.makeQuery(query, (error, sharedLocks) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: getting shared lock:', shareId, 'Error:', errors.errorWithMessage(error))
      return callback(errors.internalServer(false), null)
    }

    if (!sharedLocks || sharedLocks.length === 0) {
      logger('No shares in database with id:', shareId)
      return callback(errors.resourceNotFound(false), null)
    }

    callback(null, sharedLocks[0])
  })
}

/**
 * This method will share a user's lock with one of their contacts.
 * The method flags the lock associated with the mac id as shared,
 * and it sends a message to the contact that the lock has been shared with them.
 *
 * @param {number} lockId - lock id
 * @param {object} user - the user sharing the lock
 * @param {object} contact - the contact that the lock is shared with
 * @param {function} callback
 */
const shareLock = (lockId, user, contact, callback) => {
  if (!_.has(contact, 'phone_number') || !_.has(contact, 'country_code')) {
    return callback(errors.missingParameter(false))
  }

  getLockWithLockId(lockId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { user, lockId })
      logger('Error: could not share lock. Failed to retrieve lock with error:', error)
      callback(errors.internalServer(false))
      return
    }

    if (!lock) {
      logger('Error: could not share lock. No matching locks in db for lock id:', lockId)
      callback(errors.resourceNotFound(false))
      return
    }

    if (lock.user_id !== user.user_id) {
      logger(
        'Error: could not share lock. This lock:',
        lock.lock_id,
        'does not belong to user:',
        user.user_id
      )
      callback(errors.unauthorizedAccess(false))
      return
    }

    getSharedLocks(lock.lock_id, (error, shares) => {
      if (error) {
        Sentry.captureException(error, { user, lockId })
        logger('Error: could not share lock. Failed to retrieve shared locks with error:', error)
        return callback(errors.internalServer(false))
      }

      const currentShares = _.where(shares, {sharing_stopped: null})
      if (currentShares.length >= config.maxNumberOfSharedLocks) {
        // sorts share in first to most recent order
        currentShares.sort((share1, share2) => {
          return share1.shared_on - share2.shared_on
        })

        // prune out shares that were never confirmed
        const unconfirmedShares = _.reject(currentShares, {confirmation_code: null})
        if (currentShares.length - unconfirmedShares.length >= config.maxNumberOfSharedLocks) {
          logger(
            'Could not share lock',
            lock.lock_id,
            'it has been shared the max number of times.'
          )
          return callback(errors.sharingLimitReached(false))
        }

        const numberToRemove = currentShares.length - config.maxNumberOfSharedLocks + 1
        const numberOfSharesToRemove = unconfirmedShares.length > numberToRemove
          ? numberToRemove : unconfirmedShares.length
        let asyncFunctions = []
        for (let i = 0; i < numberOfSharesToRemove; i++) {
          const share = unconfirmedShares[i]
          const query = queryCreator.updateSingle(
            dbConstants.tables.share,
            {confirmation_code: null, sharing_stopped: moment().unix()},
            {share_id: share.share_id}
          )

          asyncFunctions.push((completion) => {
            sqlPool.makeQuery(query, (error) => {
              if (error) {
                Sentry.captureException(error, { user, lockId })
                logger(
                  'Error: failed to update unconfirmed share with id:',
                  share.share_id,
                  'with error:',
                  errors.errorWithMessage(error)
                )
              }

              completion()
            })
          })
        }

        async.series(asyncFunctions, () => {
          _shareLock(lock, user, contact, callback)
        })
      } else {
        _shareLock(lock, user, contact, callback)
      }
    })
  })
}

/**
 * This method shares a lock with a given contact.
 *
 * @param {object} lock
 * @param {object} user
 * @param {object} contact
 * @param {function} callback
 * @private
 */
const _shareLock = (lock, user, contact, callback) => {
  const confirmationCode = randomCodeGenerator()
  logger('shared lock code:', confirmationCode)
  const newSharedLock = {
    lock_id: lock.lock_id,
    shared_by_user_id: user.user_id,
    shared_by_users_id: user.users_id,
    shared_on: moment().unix(),
    confirmation_code: encryptionHandler.encryptDbValue(confirmationCode)
  }

  const query = queryCreator.insertSingle('share', newSharedLock)
  sqlPool.makeQuery(query, (error) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error: could not share lock. Failed to save shared lock with error:', error)
      return callback(errors.internalServer(false))
    }

    twilioHandler.sendSMS(
      phoneNumberHandler.formatPhoneNumber(contact),
      textHelper.sharedUserText(confirmationCode),
      (error) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger(
            'Error: could not send sms for sharing from:',
            user.user_id,
            'to contact:',
            contact,
            'Error:',
            error
          )
        }

        callback(null)
      }
    )
  })
}

/**
 * This method checks that a shared user's confirmation code is valid for a shared lock.
 *
 * @param {string} confirmationCode
 * @param {object} sharedToUser
 * @param {function} callback
 */
const checkSharedConfirmationCode = (confirmationCode, sharedToUser, callback) => {
  const encryptedConfirmationCode = encryptionHandler.encryptDbValue(confirmationCode.toString())
  let query = queryCreator.joinWithAnd(
    'share',
    'locks',
    null,
    'lock_id',
    'confirmation_code',
    [encryptedConfirmationCode]
  )
  sqlPool.makeQuery(query, (error, sharedLocks) => {
    if (error) {
      Sentry.captureException(error)
      logger(
        'Failed to check shared confirmation code. Could not retrieve shared locks. Error',
        error
      )
      return callback(errors.internalServer(false), null)
    }

    if (sharedLocks.length === 0) {
      return callback(errors.resourceNotFound(false), null)
    }

    if (sharedLocks.length > 1) {
      sharedLocks.sort((lock1, lock2) => {
        return lock2.shared_on - lock1.shared_on
      })
    }

    const sharedLock = sharedLocks[0]
    const sharedLockPropertiesToSave = {
      shared_to_user_id: sharedToUser.user_id,
      confirmation_code: null
    }

    query = queryCreator.updateSingle(
      'share',
      sharedLockPropertiesToSave,
      {share_id: sharedLock.share_id}
    )
    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error)
        logger('Error: could not save share confirmation for user:', sharedToUser.user_id)
        return callback(errors.confirmationCodeInvalid(false), null)
      }
      callback(null, formatLockForWire(sharedLock))
    })
  })
}

const getAllLocksForUser = (user, shouldFormatForWire, callback) => {
  let allLocks = {
    user_locks: [],
    shared_locks: {
      to_user: [],
      by_user: {
        active: [],
        inactive: []
      }
    }
  }

  const userLocks = (completion) => {
    const query = queryCreator.selectWithAnd('locks', null, {user_id: user.user_id})
    sqlPool.makeQuery(query, (error, locks) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: failed to get user locks for user:',
          user.user_id,
          'Error:',
          errors.errorWithMessage(error)
        )
        return completion(error)
      }

      if (!locks || locks === 0) {
        return completion()
      }

      _.each(locks, (lock) => {
        allLocks.user_locks.push(
          shouldFormatForWire ? formatLockForWire(lock, lock.shared_by_users_id) : lock
        )
      })

      completion()
    })
  }

  const sharedByUserLocks = (completion) => {
    let query = queryCreator.selectWithAnd(
      'share',
      null,
      {shared_by_user_id: user.user_id}
    )
    sqlPool.makeQuery(query, (error, sharedLocks) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: failed to get user locks shared by user:',
          user.user_id,
          'Error:',
          errors.errorWithMessage(error)
        )
        return completion(error)
      }

      sharedLocks = _.where(sharedLocks, {sharing_stopped: null})
      if (sharedLocks.length === 0) {
        return completion()
      }

      let lockIds = []
      let columns = []
      _.each(sharedLocks, (sharedLock) => {
        lockIds.push(sharedLock.lock_id)
        columns.push('lock_id')
      })

      query = queryCreator.selectWithOr('locks', null, columns, lockIds)
      sqlPool.makeQuery(query, (error, locks) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger(errors.errorWithMessage(error))
          return completion(error)
        }

        _.each(sharedLocks, (sharedLock, index) => {
          const joinedLock = _.extend(sharedLock, locks[index])
          sharedLock.shared_to_user_id
            ? allLocks.shared_locks.by_user.active.push(
              shouldFormatForWire ? formatLockForWire(
                joinedLock,
                sharedLock.shared_by_users_id
              ) : joinedLock
            )
            : allLocks.shared_locks.by_user.inactive.push(
              shouldFormatForWire ? formatLockForWire(
                joinedLock,
                sharedLock.shared_by_users_id
              ) : joinedLock
            )
        })

        completion()
      })
    })
  }

  const sharedToUserLocks = (completion) => {
    let query = queryCreator.selectWithAnd('share', null, {shared_to_user_id: user.user_id})
    sqlPool.makeQuery(query, (error, sharedLocks) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger(
          'Error: failed to get user locks shared to user:',
          user.user_id,
          'Error:',
          errors.errorWithMessage(error)
        )
        return completion(error)
      }

      sharedLocks = _.where(sharedLocks, {sharing_stopped: null})
      if (sharedLocks.length === 0) {
        return completion()
      }

      let lockIdHash = {}
      _.each(sharedLocks, (sharedLock) => {
        lockIdHash[sharedLock.lock_id] = null
      })

      let lockIds = _.keys(lockIdHash)
      if (lockIds.length === 0) {
        return completion()
      }

      let columns = []
      for (let i = 0; i < lockIds.length; i++) {
        lockIds[i] = parseInt(lockIds[i])
        columns.push('lock_id')
      }

      query = queryCreator.selectWithOr('locks', null, columns, lockIds)
      sqlPool.makeQuery(query, (error, locks) => {
        if (error) {
          Sentry.captureException(error, { user })
          logger('Error getting shared to locks. Error:', errors.errorWithMessage(error))
          return completion(error)
        }

        _.each(sharedLocks, (sharedLock, index) => {
          if (!sharedLock.sharing_stopped) {
            const joinedLock = _.extend(sharedLock, locks[index])
            allLocks.shared_locks.to_user.push(
              shouldFormatForWire ? formatLockForWire(
                joinedLock,
                sharedLock.shared_by_users_id
              ) : joinedLock
            )
          }
        })

        completion()
      })
    })
  }

  async.parallel([userLocks, sharedByUserLocks, sharedToUserLocks], (error) => {
    if (error) {
      Sentry.captureException(error, { user })
      logger('Error failed to fetch all user locks with error:', error)
      return callback(errors.internalServer(false), null)
    }
    const usersLocksLength = allLocks.user_locks.length

    // check and return if the user doesn't own a lock
    if (usersLocksLength === 0) {
      logger('got all locks:', allLocks)
      return callback(null, allLocks)
    }

    getPinCodes({lock_id: _.uniq(_.pluck(allLocks.user_locks, 'lock_id'))}, (error, pincodes) => {
      if (error) {
        Sentry.captureException(error, { user })
        logger('Error while fetching all user locks. Failed to fetch pincodes with',
          _.uniq(_.pluck(allLocks.user_locks, 'lock_id')), 'with error:', error)
        return callback(errors.internalServer(false), null)
      }
      for (let i = 0; i < usersLocksLength; i++) {
        const pincode = _.findWhere(pincodes, {lock_id: allLocks.user_locks[i].lock_id})
        if (pincode) {
          allLocks.user_locks[i].pin_code = pincode.code
        }
      }
      logger('got all locks:', allLocks)
      callback(null, allLocks)
    })
  })
}

/**
 * This method gets lock with bikes
 *
 * @param {Object} filterParam - filter column and value to filter locks
 * @param {Boolean} shouldFormatForWire - to format lockWithBikes for wire
 * @param {function} callback
 */
const getLockWithBikes = (filterParam, shouldFormatForWire, callback) => {
  const comparisonColumnAndValues = filterParam.fleet_id
    ? _.pick(filterParam, 'fleet_id') : _.pick(filterParam, 'operator_id')
  const query = queryCreator.selectWithAnd(dbConstants.tables.locks, null, comparisonColumnAndValues)
  sqlPool.makeQuery(query, (error, locks) => {
    if (error) {
      Sentry.captureException(error, { comparisonColumnAndValues })
      logger(
        'Error: failed to get operator locks for operator:',
        filterParam.operator_id,
        'Error:',
        errors.errorWithMessage(error)
      )
      return callback(errors.internalServer(false), null)
    }

    if (locks.length === 0) {
      return callback(null, locks)
    }

    const lockFleets = _.keys(_.groupBy(locks, 'fleet_id'))
    if (lockFleets.length > 0) {
      bikeHandler.getBikes({fleet_id: lockFleets}, (error, bikes) => {
        if (error) {
          Sentry.captureException(error, { lockFleets })
          logger('Error: failed to get bikes for locks with', {fleet_id: lockFleets}, errors.errorWithMessage(error))
          callback(errors.internalServer(false), null)
          return
        }
        if (bikes.length !== 0) {
          bikeHandler.getElectricBikes(null, {bike_id: _.pluck(bikes, 'bike_id')}, (error, electricBikes) => {
            if (error) {
              Sentry.captureException(error, { bikeId: _.pluck(bikes, 'bike_id') })
              logger('Error: failed to get electric bikes for locks with', {fleet_id: lockFleets},
                errors.errorWithMessage(error))
              callback(errors.internalServer(false), null)
              return
            }
            let bike
            let eBike
            for (let i = 0, locksLen = locks.length; i < locksLen; i++) {
              bike = _.findWhere(bikes, {lock_id: locks[i].lock_id})
              eBike = bike ? _.findWhere(electricBikes, {bike_id: bike.bike_id}) : null
              locks[i] = bike ? eBike ? Object.assign(locks[i], bike, eBike)
                : Object.assign(locks[i], bike) : locks[i]
            }
            if (shouldFormatForWire) {
              locks = bikeHandler.formatLockWithBikesForWire(locks)
              callback(null, locks)
            } else {
              callback(null, locks)
            }
          })
        } else {
          if (shouldFormatForWire) {
            locks = bikeHandler.formatLockWithBikesForWire(locks)
            callback(null, locks)
          } else {
            callback(null, locks)
          }
        }
      })
    } else {
      if (shouldFormatForWire) {
        locks = bikeHandler.formatLockWithBikesForWire(locks)
        callback(null, locks)
      } else {
        callback(null, locks)
      }
    }
  })
}

/**
 * This method revokes sharing of the lock to the shared to user from the shared from user.
 *
 * @param {object} user
 * @param {number || null} sharedToUserId
 * @param {number} shareId
 * @param {function} callback
 */
const revokeSharing = (user, sharedToUserId, shareId, callback) => {
  getSharedLock(shareId, (error, sharedLock) => {
    if (error) {
      Sentry.captureException(error, { user, shareId })
      logger('Error: revoking shared lock:', shareId, 'Error:', error)
      return callback(error)
    }

    if (!sharedLock) {
      logger('Could not revoke shared lock from owner. No share with id:', shareId)
      return callback(errors.resourceNotFound(false))
    }

    if (sharedLock.sharing_stopped) {
      return callback(null)
    }

    const shareUpdate = {sharing_stopped: moment().unix(), confirmation_code: null}
    const query = queryCreator.updateSingle('share', shareUpdate, {share_id: shareId})
    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error, { user, shareId })
        logger(
          'Error: failed stop sharing lock from owner. Failed to update share with error:',
          errors.errorWithMessage(error)
        )
        return callback(errors.internalServer(false))
      }

      // If we can't send a text message to let the shared to user know that the
      // lock has bee revoked, it should not affect the shared by user, so this is
      // where we'll send the information back to the user with the callback function.
      callback(null)

      // If there is no supplied shared to user, we can just bail out here.
      if (!sharedToUserId) {
        return
      }

      userHandler.getUserWithUserId(sharedToUserId, (error, sharedToUser) => {
        if (error) {
          Sentry.captureException(error, { user, sharedToUserId })
          logger(
            'Error: could not send text to user:',
            sharedToUserId,
            'tell them sharing has been revoked for share:',
            shareId,
            'Failed with error:',
            error
          )
          return
        }

        if (!sharedToUser) {
          logger(
            'Could not send text to notify shared user that sharing has been revoke. No number'
          )
          return
        }

        getLockWithLockId(sharedLock.lock_id, (error, lock) => {
          if (error) {
            Sentry.captureException(error, { user, sharedToUserId })
            logger('Error: could not get lock:', sharedLock.lock_id, 'to send un-share sms')
          }

          twilioHandler.sendSMS(
            sharedToUser.phone_number,
            textHelper.revokedSharingText.fromOwner.body(
              userHandler.fullName(sharedToUser),
              lock && lock.name ? lock.name : null
            ),
            (error) => {
              if (error) {
                Sentry.captureException(error, { user, sharedToUserId })
                logger(
                  'Error: could not send text to user:',
                  sharedToUserId,
                  'tell them sharing has been revoked for share:',
                  shareId,
                  'Failed with error:',
                  error
                )
                return
              }

              logger(
                'successfully sent text to user:',
                sharedToUser.user_id,
                'to revoke sharing for share:',
                shareId
              )
            }
          )

          if (!sharedToUser.reg_id || sharedToUser.reg_id === '') {
            return
          }

          const firebaseHandler = new FirebaseHandler()
          firebaseHandler.sendPushNotification(
            sharedToUser.reg_id,
            textHelper.revokedSharingText.fromOwner.title,
            textHelper.revokedSharingText.fromOwner.body(
              userHandler.fullName(sharedToUser),
              null
            ),
            (error1) => {
              if (error1) {
                Sentry.captureException(error1, { user, sharedToUserId })
                logger(
                  'Error: could not send sms to user:',
                  sharedToUserId,
                  'tell them sharing has been revoked for share:',
                  shareId,
                  'Failed with error:',
                  error1
                )
                return
              }

              logger(
                'successfully sent text to user:',
                sharedToUser.user_id,
                'to revoke sharing for share:',
                shareId
              )
            }
          )
        })
      })
    })
  })
}

/**
 * This method stops sharing a shared lock for a given borrower.
 *
 * @param borrowerUser
 * @param shareId
 * @param callback
 */
const stopSharingFromBorrower = (borrowerUser, shareId, callback) => {
  getSharedLock(shareId, (error, sharedLock) => {
    if (error) {
      Sentry.captureException(error, { borrowerUser, shareId })
      logger(
        'Error: failed stop sharing lock from borrower. Failed with error:',
        errors.errorWithMessage(error)
      )
      return callback(errors.internalServer(false))
    }

    if (!sharedLock) {
      logger('Could not stop sharing lock from borrower. No share with id:', shareId)
      return callback(errors.resourceNotFound(false))
    }

    if (sharedLock.shared_to_user_id !== borrowerUser.user_id) {
      logger(
        'Could not stop sharing lock from borrower.',
        'The user attempting to stop sharing is not the user that is borrowing the lock.',
        'borrowing user:', sharedLock.shared_to_user_id,
        'current user:', borrowerUser.user_id
      )
      return callback(errors.unauthorizedAccess(false))
    }

    const shareUpdate = {sharing_stopped: moment().unix()}
    const query = queryCreator.updateSingle('share', shareUpdate, {share_id: shareId})
    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error, { borrowerUser, shareId })
        logger(
          'Error: failed stop sharing lock from borrower. Failed to update share with error:',
          errors.errorWithMessage(error)
        )
        return callback(errors.internalServer(false))
      }

      callback(null)

      userHandler.getUserWithUserId(sharedLock.shared_by_user_id, (error, sharedByUser) => {
        if (error) {
          Sentry.captureException(error, { borrowerUser, shareId })
          logger('Error: Unable to get shared by user with: ',
            sharedLock.shared_by_user_id, ' with error:', error)
          return callback(errors.internalServer(false))
        }
        getLockWithLockId(sharedLock.lock_id, (error, lock) => {
          if (error) {
            Sentry.captureException(error, { borrowerUser, shareId })
            logger('Error: could not get lock:', sharedLock.lock_id, 'to send un-share sms')
          }

          twilioHandler.sendSMS(
            sharedByUser.phone_number,
            textHelper.revokedSharingText.fromBorrower.body(
              userHandler.fullName(sharedByUser),
              lock && lock.name ? lock.name : null
            ),
            (error) => {
              if (error) {
                Sentry.captureException(error, { borrowerUser, shareId })
                logger(
                  'Error: could not send text to lock owner:',
                  sharedByUser.phone_number,
                  'telling them sharing has been revoked for share:',
                  shareId,
                  'Failed with error:',
                  error
                )
                return
              }

              logger(
                'successfully sent text to user:',
                sharedByUser.user_id,
                'to revoke sharing for share:',
                shareId
              )
            }
          )

          if (!sharedByUser.reg_id || sharedByUser.reg_id === '') {
            return
          }

          const firebaseHandler = new FirebaseHandler()
          firebaseHandler.sendPushNotification(
            sharedByUser.reg_id,
            textHelper.revokedSharingText.fromBorrower.title,
            textHelper.revokedSharingText.fromBorrower.body(
              userHandler.fullName(sharedByUser),
              null
            ),
            (error) => {
              if (error) {
                Sentry.captureException(error, { borrowerUser, shareId })
                logger(
                  'Error: could not send sms to lock owner:',
                  sharedByUser.phone_number,
                  'telling them sharing has been revoked for share:',
                  shareId,
                  'Failed with error:',
                  error
                )
                return
              }

              logger(
                'successfully sent text to user:',
                sharedByUser.user_id,
                'to revoke sharing for share:',
                shareId
              )
            }
          )
        })
      })
    })
  })
}

/**
 * This method is intended for internal use. It will return all locks in the database.
 *
 * @param {function} callback
 */
const allLocks = (callback) => {
  const query = queryCreator.selectWithAnd(dbConstants.tables.locks, null, null)
  sqlPool.makeQuery(query, (error, locks) => {
    if (error) {
      Sentry.captureException(error)
      logger('Error: could not get all locks. Failed with error:', errors.errorWithMessage(error))
      return callback(error, null)
    }

    let allLocks = []
    _.each(locks, (lock) => {
      allLocks.push({
        lock_id: lock.lock_id,
        user_id: lock.user_id || null,
        mac_id: lock.mac_id ? encryptionHandler.decryptDbValue(lock.mac_id) : null,
        name: lock.name || null
      })
    })

    callback(null, allLocks)
  })
}

/**
 * This method is intended for internal use. It will return all the
 * decrypted pin codes in the database.
 *
 * @param {function} callback
 */
const allPinCodes = (callback) => {
  const query = queryCreator.selectWithAnd(dbConstants.tables.pin_codes, null, null)
  sqlPool.makeQuery(query, (error, pinCodes) => {
    if (error) {
      Sentry.captureException(error)
      logger(
        'Error: could not get all pin codes. Failed with error:',
        errors.errorWithMessage(error)
      )
      return callback(error, null)
    }

    for (let i = 0; i < pinCodes.length; i++) {
      pinCodes[i].code = encryptionHandler.decryptDbValue(pinCodes[i].code)
    }

    callback(null, pinCodes)
  })
}

// FIXME: Temporary Method for testing purpose. Has to be removed on Production
const addLock = (requestParam, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(requestParam.mac_id)
  const bikeFilterColumnAndValue = requestParam.bike_id
    ? _.pick(requestParam, 'bike_id')
    : _.pick(requestParam, 'bike_name', 'bike_battery_level')
  bikeHandler.getBike(bikeFilterColumnAndValue, (error, bike) => {
    if (error) {
      Sentry.captureException(error, { bikeFilterColumnAndValue })
      logger('Error: Unable to get bikes with: ', bikeFilterColumnAndValue, ' with error: ', error)
      return callback(error, null)
    }
    getLockWithLockId(bike[0].lock_id, (error, dbLock) => {
      if (error) {
        Sentry.captureException(error, { bikeFilterColumnAndValue })
        logger('Error: getting lock with lock_id:', bike[0].lock_id,
          'with Error:', errors.errorWithMessage(error))
        return callback(errors.internalServer(true), null)
      }

      const keys = encryptionHandler.getNewEllipticalKeys()
      const encryptedKey = encryptionHandler.encryptDbValue(keys.privateKey)
      const lockParams = {
        user_id: null,
        mac_id: encryptedMacId,
        key: encryptedKey
      }
      if (dbLock) {
        lockParams.lock_id = dbLock.lock_id
        lockParams.key = dbLock.key !== '' ? dbLock.key : encryptedKey
      }
      _saveLock(lockParams, !dbLock, (error, newLock) => {
        if (error) {
          Sentry.captureException(error, { bikeFilterColumnAndValue })
          logger('Error: updating lock query:', errors.errorWithMessage(error))
          return callback(error, null)
        }
        if (!bike[0].lock_id) {
          bike[0].lock_id = newLock.lock_id
          bikeHandler.updateBike(null, bike[0], (error) => {
            if (error) {
              Sentry.captureException(error, { bikeFilterColumnAndValue })
              logger('Error: updating bike query:', errors.errorWithMessage(error))
              return callback(error, null)
            }
            callback(null, null)
          })
        } else {
          callback(null, null)
        }
      })
    })
  })
}

/**
 * This method is to check pin code for lock
 *
 * @param {Object} comparisonColumnAndValues - filter column and values
 * @param {function} callback
 */
const getPinCodes = (comparisonColumnAndValues, callback) => {
  const getPinCodesQuery = queryCreator.selectWithAnd(dbConstants.tables.pin_codes, null, comparisonColumnAndValues)
  sqlPool.makeQuery(getPinCodesQuery, (error, pinCodes) => {
    if (error) {
      Sentry.captureException(error, { comparisonColumnAndValues })
      logger('Error: could not get pin codes with', comparisonColumnAndValues)
      return callback(errors.internalServer(false), null)
    }
    const pinCodesLength = pinCodes.length
    for (let i = 0; i < pinCodesLength; i++) {
      pinCodes[i].code = encryptionHandler.decryptDbValue(pinCodes[i].code)
    }
    callback(null, pinCodes)
  })
}

/**
 * This method is to check pin code for lock
 *
 * @param {object} columnAndValues - values to change in the locks table
 * @param {object} comparisonColumnsAndValues - comparison column and values
 * @param {function} callback
 */
const updateLock = (columnAndValues, comparisonColumnsAndValues, callback) => {
  const query = queryCreator.updateSingle(
    dbConstants.tables.locks,
    _.omit(columnAndValues, 'lock_id'),
    comparisonColumnsAndValues)
  sqlPool.makeQuery(query, callback)
}

/**
 * This method is to get fleet_id of a lock
 *
 * @param {object} macId -  mac_id for lock identification
 * @param {function} callback
 */
const getFleetByMacId = (macId, callback) => {
  const encryptedMacId = encryptionHandler.encryptDbValue(macId)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { macId })
      callback(error, null)
      return
    }
    if (lock) {
      callback(null, {fleet_id: lock.fleet_id})
    } else {
      callback(null, null)
    }
  })
}

/**
 * To get complete lock details for support purpose
 *
 * @param {Object} macId -  mac_id for lock identification
 * @param {String} password - password for authentication
 * @param {Function} callback
 */

const getLockDetails = (macId, password, callback) => {
  if (encryptionHandler.encryptDbValue(config.superAdminPassword) !== encryptionHandler.encryptDbValue(password)) {
    logger('Error: getting lock details. The super admin password is wrong.')
    return callback(errors.invalidPassword(false), null)
  }
  const encryptedMacId = encryptionHandler.encryptDbValue(macId)
  _getLockWithMacId(encryptedMacId, (error, lock) => {
    if (error) {
      Sentry.captureException(error, { macId })
      return callback(error, null)
    }
    if (!lock) {
      return callback(errors.noError(), null)
    }
    lock = bikeHandler.formatLockWithBikeForWire(lock)
    getPinCodes({lock_id: lock.lock_id}, (error, pincodes) => {
      if (error) {
        Sentry.captureException(error, { macId })
        logger('Error while fetching lock details. Failed to fetch pincodes with lock_id',
          lock.lock_id, ' for lock with mac_id', macId, 'with error:', error)
        return callback(errors.internalServer(false), null)
      }
      if (pincodes.length > 0) {
        lock = Object.assign(lock, {pin_code: pincodes[0].code, pin_code_date: pincodes[0].date})
      }
      if (!lock.user_id) {
        return callback(errors.noError(), lock)
      }
      userHandler.getUserWithUserId(lock.user_id, (error, user) => {
        if (error) {
          Sentry.captureException(error, { macId })
          logger('Error while fetching lock details. Failed to get user with user_id',
            lock.user_id, ' for lock with mac_id', macId, 'with error:', error)
          return callback(errors.internalServer(false), null)
        }
        callback(errors.noError(), user ? Object.assign(lock, userHandler.formatUserForWire(user)) : lock)
      })
    })
  })
}

/**
 * To generate secret key
 *
 * @param {Object} macId -  mac_id for lock identification
 * @param {Function} callback
 */

const generateChallengeKey = (columnsAndValues, callback) => {
  fleetHandler.getFleets({fleet_id: columnsAndValues.fleet_id}, (error, fleet) => {
    if (error) {
      Sentry.captureException(error, { fleet_id: columnsAndValues.fleet_id })
      logger('Error while fetching fleets with error:', error)
      return callback(errors.internalServer(false), null)
    }
    _getLockWithMacId(encryptionHandler.encryptDbValue(columnsAndValues.mac_id), (error, lock) => {
      if (error) {
        Sentry.captureException(error, { mac_id: columnsAndValues.mac_id })
        return callback(error)
      }
      let challengeKey = _generateChallengeKey(fleet[0].key)
      const privateKey = encryptionHandler.decryptDbValue(lock.key)
      const publicKey = encryptionHandler.publicKeyForPrivateKey(privateKey)
      updateLock({challenge_key: encryptionHandler.encryptDbValue(challengeKey)}, {lock_id: lock.lock_id}, (error) => {
        if (error) {
          Sentry.captureException(error, { mac_id: columnsAndValues.mac_id })
          logger('Error while updating lock with', lock.lock_id, 'with error:', error)
          return callback(error)
        }
        callback(null, {challenge_key: challengeKey, public_key: publicKey})
      })
    })
  })
}

const getChallengeSecret = (properties, callback) => {
  _getLockWithMacId(encryptionHandler.encryptDbValue(properties.mac_id), (error, lock) => {
    if (error) {
      Sentry.captureException(error, { mac_id: properties.mac_id })
      return callback(error)
    }
    if (lock.fleet_id !== properties.fleet.fleet_id) {
      logger(`Error: unable to generate challenge key for the lock: ${lock.lock_id} with mac_id: ${properties.mac_id} The lock is not owned by fleet: ${properties.fleet_id}`)
      return callback(errors.unauthorizedAccess(false))
    }
    if (!lock.challenge_key) {
      logger(`Error: unable to generate challenge key for the lock: ${lock.lock_id} with mac_id: ${properties.mac_id} The challenge key does not exist for the lock`)
      return callback(errors.resourceNotFound(false))
    }
    _getSignedMessageAndPublicKeyForLock(lock, encryptionHandler.decryptDbValue(properties.fleet.key), false, (error, lockKeys) => {
      if (error) {
        Sentry.captureException(error, { mac_id: properties.mac_id })
        return callback(error, null)
      }
      const payload = {
        challenge_secret: _generateChallengeSecret(encryptionHandler.decryptDbValue(lock.challenge_key), properties.challenge_data),
        signed_message: lockKeys.signedMessage
      }
      callback(null, payload)
    })
  })
}

/**
   * This method is to get crashes
   *
   * @param {object} columnsAndValues -  columnsAndValues for lock identification
   * @param {function} callback
   */
const _getCrashes = (columnsAndValues, callback) => {
  const getCrashesQuery = queryCreator.selectWithAnd(dbConstants.tables.crashes, null, columnsAndValues)
  sqlPool.makeQuery(getCrashesQuery, callback)
}

/**
 * The method creates the hash of challengeKey
 *
 * @param {String} fleetKey
 * @private
 */
const _generateChallengeKey = (fleetkey) => {
  return encryptionHandler.sha256Hash(fleetkey + moment().unix().toString())
}

/**
 * The method creates the hash of challengeSecret out of challengeKey and challengeData combined
 *
 * @param {String} challengeKey
 * @param {String} challengeData
 * @private
 */
const _generateChallengeSecret = (challengeKey, challengeData) => {
  return encryptionHandler.sha256Hash(conversions.hexStringToByteArray(challengeKey + challengeData))
}

module.exports = Object.assign(module.exports = {
  getLocks: getLocks,
  saveNewLock: saveNewLock,
  saveNewLockForFleet: saveNewLockForFleet,
  formatLockForWire: formatLockForWire,
  formatLockEntityForWire: formatLockEntityForWire,
  getLocksForUser: getLocksForUser,
  getLockWithBikes: getLockWithBikes,
  saveExistingLock: saveExistingLock,
  getSignedMessageAndPublicKeyForOperator: getSignedMessageAndPublicKeyForOperator,
  getSignedMessageAndPublicKey: getSignedMessageAndPublicKey,
  getFirmwareVersions: getFirmwareVersions,
  getFirmware: getFirmware,
  getFirmwareLog: getFirmwareLog,
  savePinCode: savePinCode,
  deleteLock: deleteLock,
  deleteLocksForUser: deleteLocksForUser,
  sendEmergencyText: sendEmergencyText,
  isAccelerometerDataValid: isAccelerometerDataValid,
  saveCrashData: saveCrashData,
  saveTheftData: saveTheftData,
  confirmOrDenyTheft: confirmOrDenyTheft,
  getLockWithLockId: getLockWithLockId,
  shareLock: shareLock,
  checkSharedConfirmationCode: checkSharedConfirmationCode,
  getAllLocksForUser: getAllLocksForUser,
  revokeSharing: revokeSharing,
  stopSharingFromBorrower: stopSharingFromBorrower,
  allLocks: allLocks,
  allPinCodes: allPinCodes,
  getPinCodes: getPinCodes,
  addLock: addLock,
  updateLock: updateLock,
  getFleetByMacId: getFleetByMacId,
  getLockDetails: getLockDetails,
  generateChallengeKey: generateChallengeKey,
  getChallengeSecret: getChallengeSecret
})
