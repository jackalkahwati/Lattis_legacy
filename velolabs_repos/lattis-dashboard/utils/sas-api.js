const axios = require('axios')
const platform = require('@velo-labs/platform')
const { getParameterStoreValuesByPath } = require('../utils/parameterStore')
const errors = platform.errors
const environment = process.env.CURRENT_ENVIRONMENT

class SasApiClient {
  constructor ({
    clientId,
    secretKey,
    baseUrl,
    domainName,
    userId
  }) {
    this.clientId = clientId
    this.secretKey = secretKey
    this.baseUrl = baseUrl
    this.domainName = domainName
    this.userId = userId
    this.token = {
      value: null,
      expires: Date.now()
    }
  }

  async getCredentials (details) {
    try {
      const accessToken = await axios.get(
        `${this.baseUrl}/users/${this.userId}/accessrights/${details.accessRights}/device/${details.deviceId}/token/${details.tokenId}`,

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
    const { Parameters: clientCredentials } = await getParameterStoreValuesByPath(`SAS/CREDENTIALS/${details.customerId}`)
    const credentialsPath = `/env/${environment}/SAS/CREDENTIALS/${details.customerId}`
    const clientId = clientCredentials.find(credential => credential.Name === `${credentialsPath}/CLIENT_ID`)
    const clientKey = clientCredentials.find(credential => credential.Name === `${credentialsPath}/CLIENT_KEY`)
    const domainName = clientCredentials.find(credential => credential.Name === `${credentialsPath}/DOMAIN_NAME`)

    return {
      clientId: clientId.Value,
      secretKey: clientKey.Value,
      domainName: domainName.Value
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
