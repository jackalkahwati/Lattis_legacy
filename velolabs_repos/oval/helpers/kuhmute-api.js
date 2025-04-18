const { logger } = require('@velo-labs/platform')
const got = require('got')
const db = require('../db')
const envConfig = require('../envConfig')
class KuhmuteAPI {
  constructor (fleetId) {
    this.fleetId = fleetId
    this.kuhmuteDomain = envConfig('KUHMUTE_DOMAIN') || 'https://api.kuhmute.org/api/v1'
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
 * @memberof KuhmuteAPI
 */
  async unlockVehicle (uuid) {
    await this.init()
    const response = await got.post(`${this.kuhmuteDomain}/request/unlock/${uuid}`, {
      headers: { 'Authorization': `Bearer ${this.apiKey}`, 'Content-Type': 'application/json' }
    })
    return response.body
  }
  /**
 *
 *
 * @param {*} uuid vehicle UUID
 * @param {*} powerObject An array of power objects { number: int(position of battery), value: numeric(decimal rep of battery level)}
 * @memberof KuhmuteAPI
 */
  async updateBattery (vehicleUUID, powerObject) {
    try {
      await this.init()
      const response = await got.post(`${this.kuhmuteDomain}/vehicle/${vehicleUUID}/battery`, {
        json: { battery: powerObject },
        headers: { 'Authorization': `Bearer ${this.apiKey}`, 'Content-Type': 'application/json' }
      })
      return response.body
    } catch (error) {
      logger('Error sending battery information')
    }
  }
}

module.exports = { KuhmuteAPI }
