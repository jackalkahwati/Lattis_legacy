'use strict'

const platform = require('@velo-labs/platform')
const express = require('express')
const router = express.Router()
const _ = require('underscore')
const logger = platform.logger
const ovalResponse = require('./../utils/oval-response')
const lockHandler = require('./../handlers/lock-handler')
const errors = platform.errors
const requestHelper = require('./../helpers/request-helper')
const throttleHandler = platform.throttleHandler
const throttleTypes = platform.throttleTypes
const encryptionHandler = platform.encryptionHandler
const bikeHandler = require('./../handlers/bike-handler')
const bookingHandler = require('./../handlers/booking-handler')
const fleetHandler = require('./../handlers/fleet-handler')

router.post('/registration', (req, res) => {
  if (!_.has(req.body, 'mac_id')) {
    logger('Error: could not register lock. The request is missing the parameter `mac_id`')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.saveNewLock(req.body.mac_id, user, (error, lock) => {
      if (error) {
        logger('Error: could not save lock with id', req.body.mac_id, 'Failed with error', error)
        ovalResponse(
          res,
          lock ? errors.lockAlreadyRegistered(true) : errors.internalServer(true),
          null
        )
        return
      }

      logger('Successfully saved new lock:', req.body.mac_id)
      ovalResponse(res, errors.noError(), lockHandler.formatLockForWire(lock, user.users_id))
    })
  })
})

router.post('/registration-for-operator', (req, res) => {
  if (!_.has(req.body, 'mac_id') || !_.has(req.body, 'fleet_id')) {
    logger('Error: could not register lock. The request is missing the parameter `mac_id`')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.saveNewLockForFleet(req.body, (error, lock) => {
      if (error) {
        logger(`Error: could not saved new lock with mac_id: ${req.body.mac_id} for fleet: ${req.body.fleet_id} Failed with error: ${error}`)
        return ovalResponse(res, errors.formatErrorForWire(error), null)
      }
      logger(`successfully saved new lock with mac_id: ${req.body.mac_id} for fleet: ${req.body.fleet_id}`)
      ovalResponse(res, errors.noError(), lockHandler.formatLockForWire(lock))
    })
  })
})

router.get('/users-locks', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.getAllLocksForUser(user, true, (error, locks) => {
      if (error) {
        logger('Error: could not get lock for user:', user.user_id, 'Failed with error', error)
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), locks)
    })
  })
})

router.post('/get-locks', (req, res) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    lockHandler.getLockWithBikes(req.body, false, (error, locks) => {
      if (error) {
        logger('Error: failed to get locks for operator', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      if (locks.length === 0) {
        ovalResponse(res, errors.noError(), locks)
        return
      }
      fleetHandler.getFleets({fleet_id: _.pluck(locks, 'fleet_id')}, (error, fleets) => {
        if (error) {
          logger('Error: failed to get locks for operator', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
        }
        lockHandler.getPinCodes({lock_id: _.pluck(locks, 'lock_id')}, (error, pinCodes) => {
          if (error) {
            logger('Error: getting locks. Failed to get pin codes with lock_id', _.pluck(locks, 'lock_id'))
            ovalResponse(res, errors.formatErrorForWire(error), null)
          }
          ovalResponse(res, errors.noError(), lockHandler.formatLockEntityForWire(locks, fleets, pinCodes))
        })
      })
    })
  })
})

router.post('/signed-message-and-public-key-for-trip', (req, res) => {
  if (!_.has(req.body, 'bike_id')) {
    logger('Error: failed to get signed message and public key. The request did not include `bike_id`.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    bikeHandler.getBikesWithLock(_.pick(req.body, 'bike_id'), (error, bikeDetails) => {
      if (error) {
        logger(
          'Error: failed to get signed message and public key for bike:',
          req.body.bike_id,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.resourceNotFound(true), null)
        return
      }

      if (bikeDetails.length <= 0) {
        logger(
          'Error: failed to get signed message and public key for bike:',
          req.body.bike_id,
          'There is no bike in the database with that id.'
        )
        ovalResponse(res, errors.resourceNotFound(true), null)
        return
      }
      bookingHandler.getActiveBooking(_.pick(bikeDetails[0], 'bike_id'), (error, bookingDetails) => {
        if (error) {
          logger('Error: Failed to get booking details with', bookingDetails.bike_id, ': ', error)
          ovalResponse(res, errors.resourceNotFound(true), null)
          return
        }
        fleetHandler.getFleets(_.pick(bikeDetails[0], 'fleet_id'),
          (error, fleetDetails) => {
            if (error) {
              logger(
                'Error: failed to get signed message and public key for bike:',
                req.body.bike_id,
                'Failed to get fleet with fleet_id:',
                bikeDetails[0].fleet_id
              )
              return ovalResponse(res, errors.unauthorizedAccess(true), null)
            }

            lockHandler.getSignedMessageAndPublicKey(_.pick(bikeDetails[0], 'lock_id', 'fleet_id'),
              null,
              fleetDetails[0].key,
              (error, messageAndKey) => {
                if (error) {
                  logger(
                    'Error: could not get public key and signed message for lock with lock_id:',
                    bikeDetails[0].lock_id,
                    'Failed with error',
                    error
                  )
                  ovalResponse(res, errors.formatErrorForWire(error), null)
                  return
                }

                const payload = {
                  signed_message: messageAndKey.signedMessage,
                  public_key: messageAndKey.publicKey
                }

                ovalResponse(res, errors.noError(), payload)
              })
          })
      })
    })
  })
})

router.post('/signed-message-and-public-key-for-operator', (req, res) => {
  if (!_.has(req.body, 'mac_id') || !_.has(req.body, 'fleet_id')) {
    logger('Error: failed to get signed message and public key. The request did not include `mac_id` or `fleet_id`.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    lockHandler.getSignedMessageAndPublicKeyForOperator(req.body, (error, messageAndKey) => {
      if (error) {
        logger(
          'Error: could not get public key and signed message for mac_id:',
          req.body.mac_id,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      const payload = {
        signed_message: messageAndKey.signedMessage,
        public_key: messageAndKey.publicKey
      }
      ovalResponse(res, errors.noError(), payload)
    })
  })
})

router.post('/signed-message-and-public-key', (req, res) => {
  if (!_.has(req.body, 'mac_id')) {
    logger('Error: failed to get signed message and public key. The request did not include `mac_id`.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.getSignedMessageAndPublicKey(req.body, user.user_id, user.users_id, (error, messageAndKey) => {
      if (error) {
        logger(
          'Error: could not get public key and signed message for mac_id:',
          req.body.mac_id,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      const payload = {
        signed_message: messageAndKey.signedMessage,
        public_key: messageAndKey.publicKey
      }
      ovalResponse(res, errors.noError(), payload)
    })
  })
})

router.get('/firmware-versions', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.getFirmwareVersions((error, versions) => {
      if (error) {
        logger(
          'Error: could not get firmware versions for user:',
          user.user_id,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), versions)
    })
  })
})

router.post('/firmware', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    const firmwareVersion = _.has(req.body, 'version') ? req.body.version : undefined
    lockHandler.getFirmware(firmwareVersion, (error, versions) => {
      if (error) {
        logger(
          'Error: could not get firmware versions for user:',
          user.user_id,
          'with version parameter:',
          firmwareVersion,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), versions)
    })
  })
})

router.post('/firmware-log', (req, res) => {
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    const firmwareVersion = _.has(req.body, 'version') ? req.body.version : undefined
    lockHandler.getFirmwareLog(firmwareVersion, (error, log) => {
      if (error) {
        logger(
          'Error: could not get firmware log for user:',
          user.user_id,
          'with version parameter:',
          firmwareVersion,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), log)
    })
  })
})

router.get('/firmware-versions-for-operator', (req, res) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    lockHandler.getFirmwareVersions((error, versions) => {
      if (error) {
        logger(
          'Error: could not get firmware versions for user:',
          operator.operator_id,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), versions)
    })
  })
})

router.post('/firmware-for-operator', (req, res) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    const firmwareVersion = _.has(req.body, 'version') ? req.body.version : undefined
    lockHandler.getFirmware(firmwareVersion, (error, versions) => {
      if (error) {
        logger(
          'Error: could not get firmware versions for user:',
          operator.operator_id,
          'with version parameter:',
          firmwareVersion,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), versions)
    })
  })
})

router.post('/firmware-log-for-operator', (req, res) => {
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    const firmwareVersion = _.has(req.body, 'version') ? req.body.version : undefined
    lockHandler.getFirmwareLog(firmwareVersion, (error, log) => {
      if (error) {
        logger(
          'Error: could not get firmware log for operator:',
          operator.operator,
          'with version parameter:',
          firmwareVersion,
          'Failed with error',
          error
        )
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, errors.noError(), log)
    })
  })
})

router.post('/save-pin-code', (req, res) => {
  if ((!_.has(req.body, 'mac_id') && !_.has(req.body, 'lock_id')) || !_.has(req.body, 'pin_code')) {
    logger('Error: could not save pin code. The request is missing a required parameter')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (req.body.mac_id) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return
      req.sentryObj.configureScope(scope => {
        scope.setTag('user_id', user.user_id)
      })
      lockHandler.savePinCode(req.body, user, (error) => {
        if (error) {
          logger('Error: failed to save pin code with error:', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        ovalResponse(res, null, null)
      })
    })
  } else {
    requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
      if (error) return
      lockHandler.savePinCode(req.body, operator, (error) => {
        if (error) {
          logger('Error: failed to save pin code with error:', error)
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        ovalResponse(res, null, null)
      })
    })
  }
})

router.post('/delete-lock', (req, res) => {
  if (!_.has(req.body, 'mac_id')) {
    logger('Error: could not delete lock. The request is missing the parameter `mac_id`')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.deleteLock(req.body.mac_id, user, (error) => {
      if (error) {
        logger('Error: failed to delete lock with error:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, null, null)
    })
  })
})

router.post('/crash-detected', (req, res) => {
  if (!_.has(req.body, 'mac_id') ||
        !_.has(req.body, 'accelerometer_data') ||
        !_.has(req.body, 'location') ||
        !lockHandler.isAccelerometerDataValid(req.body.accelerometer_data)
  ) {
    logger('Error: failed to send emergency message. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.saveCrashData(
      req.body.mac_id,
      user,
      req.body.accelerometer_data,
      req.body.location,
      req.body.is_fleet || false,
      (error, crash) => {
        if (error) {
          logger('Error: failed to save crash data for user', user.users_id, 'Error:', error)
          ovalResponse(res, errors.internalServer(true), null)
          return
        }

        ovalResponse(res, null, crash)
      })
  })
})

router.post('/send-emergency-message', (req, res) => {
  if (!_.has(req.body, 'contacts') || !_.has(req.body, 'crash_id')) {
    logger('Error: failed to send emergency message. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  if (throttleHandler.handleRequest(res, req, throttleTypes.sendEmergencyMessage)) {
    requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
      if (error) return
      req.sentryObj.configureScope(scope => {
        scope.setTag('user_id', user.user_id)
      })
      lockHandler.sendEmergencyText(
        user,
        req.body.contacts,
        req.body.crash_id,
        req.body.is_fleet || false,
        (error, failedMessages) => {
          if (error) {
            logger('Error: failed to send emergency message for user', user.users_id, 'Error:', error)
            ovalResponse(res, errors.internalServer(true), null)
            return
          }

          ovalResponse(res, null, failedMessages)
        }
      )
    })
  }
})

router.post('/theft-detected', (req, res) => {
  if (!_.has(req.body, 'mac_id') ||
        !_.has(req.body, 'accelerometer_data') ||
        !_.has(req.body, 'location') ||
        !lockHandler.isAccelerometerDataValid(req.body.accelerometer_data)
  ) {
    logger('Error: failed to save detected theft. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.saveTheftData(
      req.body.mac_id,
      user,
      req.body.accelerometer_data,
      req.body.location,
      (error, theft) => {
        if (error) {
          logger('Error: failed to save theft data for user', user.users_id, 'Error:', error)
          ovalResponse(res, errors.internalServer(true), null)
          return
        }

        if (!theft) {
          logger('Error: could not save associated theft. There is no lock:', req.body.mac_id)
          ovalResponse(res, errors.resourceNotFound(true), null)
          return
        }

        theft.confirmed = !!theft.confirmed
        ovalResponse(res, null, theft)
      })
  })
})

router.post('/confirm-theft', (req, res) => {
  if (!_.has(req.body, 'theft_id') || !_.has(req.body, 'is_confirmed')) {
    logger('Error: failed to confirm theft. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.confirmOrDenyTheft(req.body.theft_id, req.body.is_confirmed, (error) => {
      if (error) {
        logger('Error: failed to confirm theft for user', req.body.theft_id, 'Error:', error)
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, null, null)
    })
  })
})

router.post('/update-lock', (req, res) => {
  if (!_.has(req.body, 'properties')) {
    logger('Error: failed to update lock properties. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.saveExistingLock(req.body.properties, user, (error, savedLock) => {
      if (error) {
        logger('Error: failed to update lock properties:', req.body.properties, 'Error:', error)
        ovalResponse(res, errors.internalServer(true), null)
        return
      }

      ovalResponse(res, null, lockHandler.formatLockForWire(savedLock, user.users_id))
    })
  })
})

router.post('/share', (req, res) => {
  if (!_.has(req.body, 'lock_id') || !_.has(req.body, 'contact')) {
    logger('Error: failed to share lock. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.shareLock(req.body.lock_id, user, req.body.contact, (error) => {
      if (error) {
        logger('Error: failed to share lock with error:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, null, null)
    })
  })
})

router.post('/share-confirmation', (req, res) => {
  if (!_.has(req.body, 'confirmation_code')) {
    logger('Error: failed to share lock. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.checkSharedConfirmationCode(req.body.confirmation_code, user, (error, lockInfo) => {
      if (error) {
        logger('Error: failed to share lock with error:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, null, lockInfo)
    })
  })
})

router.post('/revoke-sharing', (req, res) => {
  if (!_.has(req.body, 'share_id')) {
    logger('Error: failed to revoke sharing. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.revokeSharing(user, req.body.shared_to_user_id || null, req.body.share_id, (error) => {
      if (error) {
        logger('Error: failed to revoke sharing. Error:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), null)
    })
  })
})

router.post('/borrower-revoke-sharing', (req, res) => {
  if (!_.has(req.body, 'share_id')) {
    logger('Error: failed borrower revoke sharing. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }

  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    lockHandler.stopSharingFromBorrower(user, req.body.share_id, (error) => {
      if (error) {
        logger('Error: failed borrower revoke sharing. Error:', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }

      ovalResponse(res, errors.noError(), null)
    })
  })
})

router.post('/get-fleet-by-mac-id', (req, res) => {
  if (!_.has(req.body, 'mac_id')) {
    logger('Error: No Parameters sent in request to unassign Lock')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error) => {
    if (error) return
    lockHandler.getFleetByMacId(req.body.mac_id, (error, lock) => {
      if (error) {
        logger('Error: failed to update lock data', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), lock)
    })
  })
})

/**
 * To update metadata
 *
 * The request body should include the mac_id
 */
router.post('/update-metadata', (req, res) => {
  if (!_.has(req.body, 'mac_id') && !_.has(req.body, 'lock_id')) {
    logger('Error: failed to get metadata. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  requestHelper.userAuthorizationValidator(req, res, true, (error, user) => {
    if (error) return
    req.body.user_id = user.user_id
    req.sentryObj.configureScope(scope => {
      scope.setTag('user_id', user.user_id)
    })
    if (req.body.mac_id) {
      req.body.mac_id = encryptionHandler.encryptDbValue(req.body.mac_id)
    }
    bikeHandler.updateMetadata(req.body, (error) => {
      if (error) {
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      ovalResponse(res, errors.noError(), null)
    })
  })
})

/**
 * FOR INTERNAL PURPOSE
 *
 * To un-assign lock from the fleet
 *
 * The request body should include the lock_id or mac_id
 */
router.post('/unassign-lock-from-fleet', (req, res) => {
  if (!_.has(req.body, 'mac_id') && !_.has(req.body, 'lock_id')) {
    logger('Error: failed to get metadata. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  let columns
  if (req.body.mac_id) {
    columns = {mac_id: encryptionHandler.encryptDbValue(req.body.mac_id)}
  } else {
    columns = {lock_id: req.body.lock_id}
  }
  lockHandler.getLocks(columns, (error, dbLocks) => {
    if (error) {
      logger('Error: saving existing lock. Failed with error:', errors.errorWithMessage(error))
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    if (dbLocks.length === 0) {
      logger('Failed to update lock properties for:', columns, 'Lock not found in db.')
      ovalResponse(res, errors.resourceNotFound(true), null)
      return
    }
    const dbLock = dbLocks[0]
    const newColumns = {name: null, fleet_id: null, operator_id: null, customer_id: null}
    bikeHandler.unAssignLock({lock_id: dbLock.lock_id}, (error) => {
      if (error && error.code === 500) {
        logger('Failed to update lock properties for lock:', dbLock.lock_id, 'with error', error)
        ovalResponse(res, errors.formatErrorForWire(error), null)
        return
      }
      lockHandler.updateLock(Object.assign(dbLock, newColumns), {lock_id: dbLock.lock_id}, (error) => {
        if (error) {
          ovalResponse(res, errors.formatErrorForWire(error), null)
          return
        }
        ovalResponse(res, errors.noError(), null)
      })
    })
  })
})

router.post('/get-lock-details', (req, res) => {
  if (!_.has(req.body, 'mac_id') || !_.has(req.body, 'super_admin_password')) {
    logger('Error: failed to get lock details. Missing parameters in request body.')
    ovalResponse(res, errors.missingParameter(true), null)
    return
  }
  lockHandler.getLockDetails(req.body.mac_id, req.body.super_admin_password, (error, lock) => {
    if (error) {
      ovalResponse(res, errors.formatErrorForWire(error), null)
      return
    }
    ovalResponse(res, errors.noError(), {lock: lock})
  })
})

router.post('/get-keys', (req, res) => {
  if (!_.has(req.body, 'mac_id')) {
    logger('Error: failed to get keys for lock. Missing parameters in request body.')
    return ovalResponse(res, errors.missingParameter(true), null)
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error, operator) => {
    if (error) return
    req.body.fleet_id = operator.fleet_id
    lockHandler.generateChallengeKey(req.body, (error, lock) => {
      if (error) {
        logger('Error: failed to get keys for lock with mac_id: ', req.body.mac_id)
        return ovalResponse(res, errors.formatErrorForWire(error), null)
      }
      ovalResponse(res, errors.noError(), lock)
    })
  })
})

router.post('/get-challenge-secret', (req, res) => {
  if (!_.has(req.body, 'mac_id') || !_.has(req.body, 'challenge_data')) {
    logger('Error: failed to get challenge secret for lock. Missing parameters in request body.')
    return ovalResponse(res, errors.missingParameter(true), null)
  }
  requestHelper.operatorAuthorizationValidator(req, res, (error, fleet) => {
    if (error) return
    lockHandler.getChallengeSecret(Object.assign(req.body, {fleet: fleet}), (error, lock) => {
      if (error) {
        logger('Error: failed to get challenge secret for lock with mac_id: ', req.body.mac_id)
        return ovalResponse(res, errors.formatErrorForWire(error), null)
      }
      ovalResponse(res, errors.noError(), lock)
    })
  })
})

module.exports = router
