const axios = require('axios')
const qs = require('querystring')
const platform = require('@velo-labs/platform')
const errors = platform.errors
const logger = platform.logger
const AWS = require('aws-sdk')
const { getParameterStoreValue } = require('./parameterStore')
const db = require('../db')

const { CURRENT_ENVIRONMENT: environment } = process.env

const readTapKeyPrivateKeyFromS3 = async (fleetId) => {
  try {
    let accessKeyId = await getParameterStoreValue('DASHBOARD_S3_ACCESSKEY_ID')
    accessKeyId = accessKeyId && accessKeyId.Parameter.Value
    let secretAccessKey = await getParameterStoreValue('DASHBOARD_S3_SECRET_ACCESS_KEY')
    secretAccessKey = secretAccessKey && secretAccessKey.Parameter.Value
    let tapKeyPrivarekeysBucketName = await getParameterStoreValue('TAPKEY_PRIVATE_KEYS_BUCKET_NAME')
    tapKeyPrivarekeysBucketName = tapKeyPrivarekeysBucketName && tapKeyPrivarekeysBucketName.Parameter.Value
    const s3 = new AWS.S3({
      accessKeyId,
      secretAccessKey,
      Bucket: tapKeyPrivarekeysBucketName
    })

    const key = `${environment}/tapkey_${fleetId}.json`
    const params = {
      Bucket: tapKeyPrivarekeysBucketName,
      Key: key
    }
    let data = await s3.getObject(params).promise()
    data = JSON.parse(data.Body.toString('utf-8'))
    return data
  } catch (error) {
    logger('An error occurred reading s3 data')
  }
}

class TapKeyApiClient {
  constructor ({
    idpClientId,
    grantType,
    provider,
    loginUrl,
    passPhrase,
    tokenIssuer,
    refreshToken,
    accessToken,
    baseUrl,
    ownerAccountId,
    authCodeClientId,
    authCodeClientSecret,
    tapKeyPrivateKey,
    redirectUri
  }) {
    this.idpClientId = idpClientId
    this.grantType = grantType
    this.provider = provider
    this.loginUrl = loginUrl
    this.passPhrase = passPhrase
    this.tokenIssuer = tokenIssuer
    this.refreshToken = refreshToken
    this.accessToken = accessToken
    this.baseUrl = baseUrl
    this.ownerAccountId = ownerAccountId
    this.authCodeClientId = authCodeClientId
    this.authCodeClientSecret = authCodeClientSecret
    this.tapKeyPrivateKey = tapKeyPrivateKey
    this.redirectUri = redirectUri
    this.authCodeToken = {
      value: null,
      expires: Date.now()
    }
  }

  async boundLock (lock) {
    const token = await this.getAuthorizationCodeToken()
    try {
      const lockBind = await axios.put(
        `${this.baseUrl}/owners/${this.ownerAccountId}/BoundLocks`,
        {
          'physicalLockId': lock.lockId,
          'bindingCode': lock.bindingCode,
          'title': lock.title,
          'description': lock.description
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      return lockBind.data
    } catch (error) {
      console.log('error while binding lock:', error)
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getAuthorizationCodeToken () {
    try {
      const response = await axios.post(
        `${this.loginUrl}/token`,
        qs.stringify({
          client_id: this.authCodeClientId,
          client_secret: this.authCodeClientSecret,
          grant_type: 'refresh_token',
          refresh_token: this.refreshToken,
          scope: 'write:ip:users read:ip:users write:core:entities write:grants read:grants'
        }),
        {
          headers: {
            // 'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
      )
      return response.data.access_token
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getOwners () {
    const token = await this.getAuthorizationCodeToken()
    let accountIds = []
    try {
      const {data: owners} = await axios.get(
        `${this.baseUrl}/owners/`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      if (owners && owners.length) {
        accountIds = owners.map(account => { return account.id })
      }
      return accountIds
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'lock system error',
        false
      )
    }
  }

  async getLocksByFilter (filterEntity, lockId) {
    // TODO: Only call this when the accessToken has expired. Use a try/catch block to check for expired accessToken
    // REFER: https://github.com/auth0/node-jsonwebtoken/blob/master/README.md#tokenexpirederror
    const token = await this.getAuthorizationCodeToken()
    const owners = await this.getOwners()
    let boundLocks = []
    try {
      const promises = owners.map((owner) => {
        return axios.get(
          `${this.baseUrl}/owners/${owner}/BoundLocks?$filter=${filterEntity} eq '${lockId}'`,
          {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          }
        )
      })
      let response = await Promise.all(promises).then(response => response.map(res => { return res.data }))
      response = [].concat.apply([], response)
      boundLocks.push(...response)
      if (boundLocks && boundLocks.length) {
        boundLocks.map(async (lock) => {
          await db.main('controllers')
            .update({metadata: JSON.stringify({ownerAccountId: lock.ownerAccountId})})
            .where({key: lock.id})
        })
      }
      return boundLocks
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getRefreshToken (code) {
    try {
      const response = await axios.post(
        `${this.loginUrl}/token`,
        qs.stringify({
          client_id: this.authCodeClientId,
          client_secret: this.authCodeClientSecret,
          grant_type: 'authorization_code',
          redirect_uri: this.redirectUri,
          code: code
        }),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
      )
      return response.data
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }
}

module.exports = {
  TapKeyApiClient,
  readTapKeyPrivateKeyFromS3
}
