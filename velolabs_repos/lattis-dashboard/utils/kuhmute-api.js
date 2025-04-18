const { logger } = require('@velo-labs/platform')
const got = require('got')
const db = require('../db')
const envConfig = require('../envConfig')
class KuhmuteApi {
  constructor (fleetId) {
    this.fleetId = fleetId
    this.kuhmuteDomain = envConfig('KUHMUTE_DOMAIN')
  }

  async init () {
    const fleetId = this.fleetId
    const fleetMeta = await db.main('fleet_metadata').where({ fleet_id: fleetId }).first()
    this.apiKey = fleetMeta.kuhmute_api_key
  }
  /**
 * Undock/unlock vehicle from kuhmute hub
 *
 * @param {*} uuid vehicle UUID
 * @memberof KuhmuteApiClient
 */
  async unlockVehicle (uuid) {
    try {
      await this.init()
      const response = await got.post(`${this.kuhmuteDomain}/request/unlock/${uuid}`, {
        headers: { 'Authorization': `Bearer ${this.apiKey}`, 'Content-Type': 'application/json' }
      })
      return response.body
    } catch (error) {
      logger('An error occurred undocking the vehicle')
      console.log('Error::', error)
    }
  }
  /**
 *
 *
 * @param {*} uuid vehicle UUID
 * @param {*} powerObject An array of power objects { number: int(position of battery), value: numeric(decimal rep of battery level)}
 * @memberof KuhmuteApiClient
 */
  async updateBattery (vehicleUUID, powerObject) {
    await this.init()
    const response = await got.post(`${this.kuhmuteDomain}/vehicle/${vehicleUUID}/battery`, {
      json: { battery: powerObject },
      headers: { 'Authorization': `Bearer ${this.apiKey}`, 'Content-Type': 'application/json' }
    })
    return response.body
  }

  async getHubsFromAPI () {
    try {
      await this.init()
      const response = await got.get(`${this.kuhmuteDomain}/provider/hubs`, {
        headers: { 'Authorization': `${this.apiKey}`, 'Content-Type': 'application/json' }
      })
      return response.body
    } catch (error) {
      logger('An error occurred getting the list of hubs')
      console.log('Error::', error)
    }
  }
}

module.exports = { KuhmuteApi }
