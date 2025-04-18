'use strict'

const platform = require('@velo-labs/platform')
const logger = platform.logger
const encryptionHandler = platform.encryptionHandler
const randomCodeGenerator = require('./../helpers/random-code-gernerator')
const queryCreator = platform.queryCreator
const sqlPool = platform.sqlPool
const moment = require('moment')
const errors = platform.errors
const dbConstants = platform.dbConstants
const _ = require('underscore')
const confirmationCodeResults = require('./../constants/confirmation-code-results')
const {configureSentry} = require('../utils/configureSentry')

const Sentry = configureSentry()

class ConfirmationCodeHandler {
  /**
     * Initializes the Confirmation Code Handler class
     *
     * @param {object} user
     */
  constructor (user) {
    this._user = user
  }

  /**
     * This method creates a `confirmation code` object.
     *
     * @param {string} type
     * @param {function} callback
     * @returns {{date: number, type: (string|*), code}}
     */
  createCode (type, callback) {
    const confirmationCode = randomCodeGenerator()
    const code = {
      date: moment().unix(),
      type: type,
      code: encryptionHandler.encryptDbValue(confirmationCode),
      user_id: this._user.user_id
    }
    this.getCode(type, (error, dbCode) => {
      if (error) {
        Sentry.captureException(error, { code, type, user_id: this._user.user_id })
        callback(error, null)
        return
      }
      this.saveCode(code, !dbCode, type, (error) => {
        if (error) {
          Sentry.captureException(error, { code, type, user_id: this._user.user_id })
          logger(
            'Error: failed to create confirmation code with error:',
            errors.errorWithMessage(error)
          )
          return callback(errors.internalServer(false), null)
        }
        callback(null, confirmationCode)
      })
    })
  };

  /**
     * This method gets a confirmation code object of the given type from the database.
     *
     * @param {string} type
     * @param {function} callback
     */
  getCode (type, callback) {
    let query = queryCreator.selectWithAnd(
      dbConstants.tables.confirmation_codes,
      null,
      {type: type, user_id: this._user.user_id}
    )

    sqlPool.makeQuery(query, (error, codes) => {
      if (error) {
        Sentry.captureException(error, { type, user_id: this._user.user_id, query })
        logger(
          'Error: failed to create confirmation code with error:',
          errors.errorWithMessage(error)
        )
        callback(errors.internalServer(false), null)
        return
      }

      if (!codes || codes.length === 0) {
        callback(null, null)
        return
      }

      callback(null, codes[0])
    })
  };

  /**
     * This method saves a code object to the database.
     *
     * @param {object} code
     * @param {boolean} isNew
     * @param {string} type
     * @param {function} callback
     */
  saveCode (code, isNew, type, callback) {
    code = _.omit(code, 'confirmation_code_id')
    const query = isNew ? queryCreator.insertSingle(dbConstants.tables.confirmation_codes, code)
      : queryCreator.updateSingle(
        dbConstants.tables.confirmation_codes,
        code,
        {user_id: this._user.user_id, type: type}
      )

    sqlPool.makeQuery(query, (error) => {
      if (error) {
        Sentry.captureException(error, { code, type, user_id: this._user.user_id })
        logger(
          'Error: failed to save confirmation code with error:',
          errors.errorWithMessage(error)
        )
        callback(errors.internalServer(false))
        return
      }

      callback(null)
    })
  };

  /**
     * This method checks an incoming code against the value in the database.
     * If the code matches, the code object from the database is updated and saved.
     *
     * @param {string| number} code
     * @param {string} type
     * @param {function} callback
     */
  checkCode (code, type, callback) {
    this.getCode(type, (error, codeObject) => {
      if (error) {
        Sentry.captureException(error, { type, user_id: this._user.user_id })
        logger('Error: checking confirmation code:', code, 'error:', error)
        callback(error, null)
        return
      }

      if (!codeObject) {
        logger(
          'Could not check confirmation code of type:',
          type,
          'user:',
          this._user.user_id,
          'no matching entry in database'
        )
        callback(null, confirmationCodeResults.noCodeInDb)
        return
      }

      if (!!codeObject.code && encryptionHandler.encryptDbValue(code.toString()) === codeObject.code) {
        codeObject.code = null
        codeObject.action_taken = true
        this.saveCode(codeObject, false, type, (error) => {
          if (error) {
            Sentry.captureException(error, { code, type, user_id: this._user.user_id })
            logger('Error: failed to check confirmation code')
            callback(error, null)
            return
          }

          callback(null, confirmationCodeResults.confirmed)
        })
      } else {
        logger(
          'Confirmation code:',
          code,
          'of type:',
          type,
          'does not match code in db for',
          this._user.user_id
        )

        return callback(null, confirmationCodeResults.notConfirmed)
      }
    })
  }
}

module.exports = ConfirmationCodeHandler
