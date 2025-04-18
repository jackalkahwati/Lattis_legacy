'use strict'

const platform = require('@velo-labs/platform')
const _ = require('underscore')
const https = require('https')
const logger = platform.logger
const errors = platform.errors

class FacebookAuthenticator {
  constructor (user) {
    this._user = user
  }

  authenticate (callback) {
    if (!_.has(this._user, 'facebook_token') || !_.has(this._user, 'users_id')) {
      logger('Error: cannot authenticate facebook user. Missing parameter needed to authenticate.')
      callback(errors.missingParameter(false), null)
      return
    }

    const options = {
      hostname: 'graph.facebook.com',
      method: 'GET',
      path: '/me?access_token=' + this._user.facebook_token
    }

    const req = https.request(options, (response) => {
      let data = ''
      response.setEncoding('utf8')
      response.on('data', function (chunk) {
        data += chunk
      })

      response.on('end', () => {
        logger('Got facebook user authentication info:', data)
        this._checkAuthentication(JSON.parse(data), callback)
      })
    })

    req.on('error', (error) => {
      logger('Error: could not authenticate facebook user. Error:', error)
      callback(errors.unauthorizedAccess(false), null)
    })

    req.end()
  };

  _checkAuthentication (fbInfo, callback) {
    if (!_.has(fbInfo, 'id')) {
      logger('Error: no id in facebook authentication response')
      callback(errors.unauthorizedAccess(false), null)
      return
    }

    callback(null, fbInfo.id === this._user.users_id)
  };
}

module.exports = FacebookAuthenticator
