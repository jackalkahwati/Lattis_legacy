const axios = require('axios')
const qs = require('querystring')
const platform = require('@velo-labs/platform')
const jwt = require('jsonwebtoken')
const { getParameterStoreValuesByPath } = require('../utils/parameterStore')
const db = require('../db')
const errors = platform.errors
const environment = process.env.CURRENT_ENVIRONMENT
class TapKeyApiClient {
  constructor ({
    idpClientId,
    grantType,
    provider,
    loginUrl,
    passPhrase,
    tokenIssuer,
    refreshToken,
    lattisRefreshToken,
    accessToken,
    baseUrl,
    ownerAccountId,
    operatorAccountId,
    authCodeClientId,
    authCodeClientSecret,
    tapKeyPrivateKey
  }) {
    this.idpClientId = idpClientId
    this.grantType = grantType
    this.provider = provider
    this.loginUrl = loginUrl
    this.passPhrase = passPhrase
    this.tokenIssuer = tokenIssuer
    this.refreshToken = refreshToken
    this.lattisRefreshToken = lattisRefreshToken
    this.accessToken = accessToken
    this.baseUrl = baseUrl
    this.ownerAccountId = ownerAccountId
    this.operatorAccountId = operatorAccountId
    this.authCodeClientId = authCodeClientId
    this.authCodeClientSecret = authCodeClientSecret
    this.tapKeyPrivateKey = tapKeyPrivateKey
    this.authCodeToken = {
      value: null,
      expires: Date.now()
    }
  }
  async createGrantForContact (token, contactId, lockId) {
    try {
      const grant = await axios.put(
        `${this.baseUrl}/owners/${this.operatorAccountId}/grants`,
        {
          'contactId': contactId,
          'ipId': this.provider,
          'boundLockId': lockId
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      return grant.data
    } catch (error) {
      // do not throw error if issue is just an existing grant
      if (error.response.data.ErrorCode === 'Duplicate') {
        console.error('Grant already exists', error.response.data)
      } else {
        throw errors.customError(
          error.response.data,
          platform.responseCodes.InternalServer,
          'Device Error',
          false
        )
      }
    }
  }

  // used to revoke grants for trips that were not charged so tapkey doesn't charge.
  async revokeGrant (user) {
    const token = await this.getAuthorizationCodeToken(this.refreshToken)
    try {
      const { data: allGrants } = await axios.get(
        `${this.baseUrl}/owners/${this.operatorAccountId}/grants`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      const targetGrant = allGrants.find(grant => grant.contact.ipUserId === user.userId)
      const { data: revokeResponse } = await axios.post(
        `${this.baseUrl}/owners/${this.operatorAccountId}/grants/${targetGrant.id}/revoke?dryRun=false`,
        {
          dryRun: false
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      return revokeResponse
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async createContactForUser (token, ipUserId) {
    try {
      const contact = await axios.put(
        `${this.baseUrl}/owners/${this.operatorAccountId}/contacts`,
        {
          'ipUserId': ipUserId,
          'ipId': this.provider
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      console.log('contact created successfully: ', contact.data)
      return contact.data
    } catch (error) {
      console.log('Error creating contact for user:', error.response.data)
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getAuthorizationCodeToken (refreshToken) {
    try {
      const response = await axios.post(
        `${this.loginUrl}/token`,
        qs.stringify({
          client_id: this.authCodeClientId,
          client_secret: this.authCodeClientSecret,
          grant_type: 'refresh_token',
          refresh_token: refreshToken,
          scope: 'write:ip:users read:ip:users write:core:entities write:grants read:grants read:core:entities'
        })
      )
      this.authCodeToken.value = response.data.access_token
      this.authCodeToken.expires = Date.now() + response.data.expires_in * 1000
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

  async handleCreateUserError (error, details) {
    const token = await this.getAuthorizationCodeToken(this.refreshToken)
    try {
      if (error.response.status === 409) {
        const user = await this.getTapKeyUserById(details.userId)
        const userContact = await this.getUserContacts(user.ipUserId)
        await this.createGrantForContact(token, userContact.id, details.lockId)
        return user.ipUserId
      } else {
        // all other errors except 409s
        throw errors.customError(
          error.response.data,
          platform.responseCodes.InternalServer,
          'Device Error',
          false
        )
      }
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getLockById (lockId) {
    try {
      const { data } = await axios.get(
        `${this.baseUrl}/owners/${this.operatorAccountId}/BoundLocks/${lockId}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.getAuthorizationCodeToken(this.refreshToken)}`
          }
        }
      )
      return data
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }
  async getUserContacts (userId) {
    try {
      const token = await this.getAuthorizationCodeToken(this.refreshToken)
      const contacts = await axios.get(
        `${this.baseUrl}/owners/${this.operatorAccountId}/contacts`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      let contact = contacts.data.find(contact => contact.ipUserId === userId)
      if (!contact) {
        const {data: newContact} = await this.createContactForUser(token, userId)
        contact = newContact
      }
      console.log('userContact:', contact)
      return contact
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getTapKeyUserById (userId) {
    try {
      const user = await axios.get(
        `${this.baseUrl}/owners/${this.ownerAccountId}/identityproviders/${this.provider}/users?ipUserId=${userId}`,
        {
          headers: {
            'Authorization': `Bearer ${await this.getAuthorizationCodeToken(this.lattisRefreshToken)}`
          }
        }
      )
      return user.data
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async createTapKeyUser (details) {
    // use refresh token to get token.
    try {
      const user = await axios.put(
        `${this.baseUrl}/owners/${this.ownerAccountId}/identityproviders/${this.provider}/users`,
        {
          'ipUserId': details.userId
        },
        {
          headers: {
            'Authorization': `Bearer ${await this.getAuthorizationCodeToken(this.lattisRefreshToken)}`
          }
        }
      )
      // create a new contact for the user created above
      const token = await this.getAuthorizationCodeToken(this.refreshToken)
      const contact = await this.createContactForUser(token, user.data.ipUserId)
      // create grant for contact created above
      await this.createGrantForContact(token, contact.id, details.lockId)
      return user.data.ipUserId
    } catch (error) {
      // TODO: don't we need to await over here?
      return this.handleCreateUserError(error, details)
    }
  }

  async getToken (details) {
    try {
      const subject = await this.createTapKeyUser(details)
      const key = jwt.sign({
        'http://tapkey.net/oauth/token_exchange/client_id': this.idpClientId
      }, { key: this.tapKeyPrivateKey, passphrase: this.passPhrase }, {
        algorithm: 'RS256',
        audience: 'tapkey_api',
        expiresIn: '1h',
        issuer: this.tokenIssuer,
        subject: subject
      })
      return key
    } catch (error) {
      throw errors.customError(
        error.response.data,
        platform.responseCodes.InternalServer,
        'Device Error',
        false
      )
    }
  }

  async getCredentials (details) {
    try {
      const token = await this.getToken(details)
      const { data } = await axios.post(
        `${this.loginUrl}/token`,
        qs.stringify({
          client_id: this.idpClientId,
          grant_type: this.grantType,
          provider: this.provider,
          scope: 'read:core:entities register:mobiles handle:keys',
          subject_token_type: 'jwt',
          subject_token: token
        }),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
          }
        }
      )
      const lockData = await this.getLockById(details.lockId)
      return {
        token: data.access_token,
        physical_lock_id: lockData.physicalLockId
      }
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

const fetchTapKeyConfiguration = async (details) => {
  const { Parameters: tapkeyUrls } = await getParameterStoreValuesByPath('TAP_KEY/URLS')
  const { Parameters: tapkeyKeys } = await getParameterStoreValuesByPath('TAP_KEY/KEYS')
  const urlName = `/env/${environment}/TAP_KEY/URLS`
  const keyName = `/env/${environment}/TAP_KEY/KEYS`

  const passPhrase = tapkeyKeys.find(key => key.Name === `${keyName}/TAP_KEY_PASSPHRASE`)
  const tokenIssuer = tapkeyKeys.find(key => key.Name === `${keyName}/TAP_KEY_TOKEN_ISSUER`)
  const idpClientId = tapkeyKeys.find(url => url.Name === `${keyName}/IDP_CLIENT_ID`)
  const provider = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_ID_PROVIDER`)
  const ownerAccountId = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_OWNER_ACCOUNT_ID`)
  const authCodeClientId = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_AUTH_CODE_CLIENT_ID`)
  const authCodeClientSecret = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_AUTH_CODE_CLIENT_SECRET`)
  const tapKeyPrivateKey = tapkeyKeys.find(url => url.Name === `${keyName}/TAP_KEY_PRIVATE_KEY`)
  const lattisRefreshToken = tapkeyKeys.find(url => url.Name === `${keyName}/LATTIS_REFRESH_TOKEN`)

  const grantType = tapkeyUrls.find(url => url.Name === `${urlName}/TAP_KEY_GRANT_TYPE`)
  const loginUrl = tapkeyUrls.find(url => url.Name === `${urlName}/LOGIN_URL`)
  const baseUrl = tapkeyUrls.find(url => url.Name === `${urlName}/BASE_URL`)
  const redirectUri = tapkeyUrls.find(url => url.Name === `${urlName}/REDIRECT_URI`)

  const integration = await db.main('integrations').where({ fleet_id: details.fleetId, integration_type: 'tapkey' }).select('metadata').first()
  const controller = await db.main('controllers').where({ key: details.lockId }).select('metadata').first()
  let metadata = integration && integration.metadata
  let operatorAccountId = controller && controller.metadata
  if (metadata && operatorAccountId) {
    metadata = JSON.parse(metadata)
    operatorAccountId = JSON.parse(operatorAccountId)
  } else {
    throw new Error('Important info is missing to setup Tapkey connection')
  }

  return { passPhrase,
    tokenIssuer,
    idpClientId,
    provider,
    ownerAccountId,
    authCodeClientId,
    authCodeClientSecret,
    tapKeyPrivateKey,
    lattisRefreshToken,
    grantType,
    loginUrl,
    baseUrl,
    redirectUri,
    metadata,
    operatorAccountId
  }
}

module.exports = {
  fetchTapKeyConfiguration,
  TapKeyApiClient
}
