'use strict'

const encryptionHandler = require('@velo-labs/platform').encryptionHandler
const _ = require('underscore')

class LockFormatter {
  constructor () {
    this._locks = null
    this._formattedLocks = []
  }

  /**
     * This method formats a lock in preparation to send over the wire.
     * Only properties that are safe should be sent.
     *
     * @param {object} lock
     * @param {string} usersId
     * @returns {*}
     */
  formatLockForWire (lock, usersId) {
    if (!lock.key) {
      return null
    }

    const privateKey = encryptionHandler.decryptDbValue(lock.key)
    return this._formatLock(lock, usersId, encryptionHandler.publicKeyForPrivateKey(privateKey))
  };

  /**
     * This method formats a lock in preparation to send over the wire.
     * Only properties that are safe should be sent.
     *
     * @param {object} lockWithBike
     * @returns {*}
     */
  formatLockWithBikeForWire (lockWithBike) {
    return this._formatLockWithBike(lockWithBike, true)
  };

  /**
     * This method formats a lock in preparation to send over the wire.
     * Only properties that are safe should be sent.
     *
     * @param {object} bike
     * @returns {*}
     */
  formatBikeForWire (bike) {
    return this._formatLockWithBike(bike, false)
  };

  /**
     * This method formats a locks in preparation to send over the wire.
     * Only properties that are safe should be sent.
     *
     * @param {Array} locks
     * @param {string} usersId
     * @returns {Array}
     */
  formatLocksForWire (locks, usersId) {
    let formattedLocks = []
    _.each(locks, (lock) => {
      formattedLocks.push(this.formatLockForWire(lock, usersId))
    }, this)

    return formattedLocks
  };

  /**
     * This method formats a locks in preparation to send over the wire.
     * Only properties that are safe should be sent.
     *
     * @param {Array} lockWithBikes
     * @returns {Array}
     */
  formatLockWithBikesForWire (lockWithBikes) {
    const _this = this
    const formattedLockWithBikes = []
    const locksLen = lockWithBikes.length
    let lockWithBike
    for (let i = 0; i < locksLen; i++) {
      lockWithBike = lockWithBikes[i]
      formattedLockWithBikes.push(_this.formatLockWithBikeForWire(lockWithBike))
    }
    return formattedLockWithBikes
  };

  /**
     * This method white-lists the locks properties that should be sent over the wire.
     *
     * @param {object} lock
     * @param {string} usersId
     * @param {string} publicKey - elliptical public key
     * @returns {{lock_id: *, mac_id: (*|string), user_id: *, public_key: (null|*), name}}
     * @private
     */
  _formatLock (lock, usersId, publicKey) {
    let formattedLock = {
      lock_id: lock.lock_id,
      mac_id: encryptionHandler.decryptDbValue(lock.mac_id),
      user_id: lock.user_id,
      users_id: usersId,
      public_key: publicKey,
      name: lock.name,
      share_id: lock.share_id || null
    }
    if (lock.hasOwnProperty('signed_message')) {
      formattedLock.signed_message = lock.signed_message
    }
    if (_.has(lock, 'shared_to_user_id') && lock.shared_to_user_id) {
      formattedLock.shared_to_user_id = lock.shared_to_user_id
    }

    return formattedLock
  };

  /**
     * This method white-lists the locks properties that should be sent over the wire.
     *
     * @param {Array} lockWithBike
     * @param {Object} lockPresent
     * @returns {Object} lock details without any secure data
     * @private
     */
  _formatLockWithBike (lockWithBike, lockPresent) {
    if (lockPresent && lockWithBike.mac_id) {
      lockWithBike.mac_id = encryptionHandler.decryptDbValue(lockWithBike.mac_id)
    }
    return _.omit(lockWithBike, 'key', 'qr_code_id')
  };

  /**
     * This method format the lock entity for wire
     *
     * @param {Array} locks - lock details
     * @param {Array} fleets - fleet details
     * @param {Array} pinCodes - pin codes of the locks
     * @returns {Array} lock entity with proper format for wire
     */
  formatLockEntityForWire (locks, fleets, pinCodes) {
    if (fleets && Array.isArray(fleets) && fleets.length !== 0) {
      const fleetsLen = fleets.length
      for (let i = 0; i < fleetsLen; i++) {
        fleets[i].fleet_key = encryptionHandler.decryptDbValue(fleets[i].key)
      }
    }
    if (locks && Array.isArray(locks) && locks.length !== 0) {
      let fleet
      let lock
      let pinCode
      const locksLen = locks.length
      for (let k = 0; k < locksLen; k++) {
        lock = locks[k]
        lock = this._formatLockWithBike(lock, true)
        if (fleets) {
          fleet = _.findWhere(fleets, {fleet_id: lock.fleet_id})
          if (fleet) {
            lock = Object.assign(lock, fleet)
          }
        }
        if (pinCodes) {
          pinCode = _.findWhere(pinCodes, {lock_id: lock.lock_id})
          lock.empty_pin = true
          if (pinCode) {
            lock.empty_pin = !pinCode.code
          }
        }
        locks[k] = lock
      }
      return locks
    } else {
      return []
    }
  }
}

module.exports = LockFormatter
