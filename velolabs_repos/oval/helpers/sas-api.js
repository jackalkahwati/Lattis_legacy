const axios = require('axios')
const platform = require('@velo-labs/platform')
const { getParameterStoreValuesByPath } = require('../utils/parameterStore')
const db = require('../db')
const errors = platform.errors
const environment = process.env.CURRENT_ENVIRONMENT

class SasApiClient {
  constructor ({
    clientId,
    secretKey,
    baseUrl,
    domainName,
    userId,
    accessRights
  }) {
    this.clientId = clientId
    this.secretKey = secretKey
    this.baseUrl = baseUrl
    this.domainName = domainName
    this.userId = userId
    this.accessRights = accessRights
    this.token = {
      value: null,
      expires: Date.now()
    }
  }

  async getToken () {
    try {
      if (this.token.value && this.token.expires > Date.now()) {
        return this.token.value
      }
      const token = await axios.post(
        `https://${this.domainName}.eu-central-1.amazoncognito.com/oauth2/token?grant_type=client_credentials`,

        {},
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          auth: {
            username: this.clientId,
            password: this.secretKey
          }
        }
      )
      this.token.value = token.data.access_token
      this.token.expires = Date.now() + token.data.expires_in * 1000
      return this.token.value
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
      const accessToken = await axios.get(
        `${this.baseUrl}/users/${this.userId}/accessrights/${this.accessRights}/device/${details.deviceId}/token/${details.tokenId}`,

        {
          headers: {
            'Authorization': `${await this.getToken()}`
          }
        }
      )
      return accessToken.data
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

const fetchSasConfig = async (details) => {
  try {
    const { Parameters: sasUrls } = await getParameterStoreValuesByPath('SAS/URLS')
    const { Parameters: userIds } = await getParameterStoreValuesByPath('SAS/IDS')
    const { Parameters: accessCredentials } = await getParameterStoreValuesByPath('SAS/ACCESS_RIGHTS')
    const urlName = `/env/${environment}/SAS/URLS`
    const userIdPath = `/env/${environment}/SAS/IDS`
    const accessRightsPath = `/env/${environment}/SAS/ACCESS_RIGHTS`
    const baseUrl = sasUrls.find(url => url.Name === `${urlName}/BASE_URL`)
    const { Value: userId } = userIds.find(id => id.Name === `${userIdPath}/${details.fleetId}`)
    const { Value: accessRights } = accessCredentials.find(id => id.Name === `${accessRightsPath}/${details.fleetId}`)

    const integration = await db.main('integrations').where({fleet_id: details.fleetId, integration_type: 'sas'}).first()
    const {domainName} = integration && JSON.parse(integration.metadata)

    console.log('userId', userId)
    return {
      userId,
      accessRights,
      baseUrl: baseUrl.Value,
      clientId: integration.client_id,
      secretKey: integration.client_secret,
      domainName: domainName
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

module.exports = {
  fetchSasConfig,
  SasApiClient
}
