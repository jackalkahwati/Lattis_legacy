const axios = require('axios')
const BASE_URL = 'https://api.comodule.com/vehicleapi/v3'
const envConfig = require('../envConfig')

async function getVehicleState (vehicleId) {
  const response = await axios.get(
    `${BASE_URL}/vehicle/${vehicleId}`,
    {
      headers: {
        'Content-Type': 'application/ninebot_es4+json'
      },
      params: {
        apiKey: envConfig('COMODULE_API_KEY')
      }
    }
  )

  return response.data
}

async function editVehicleState (vehicleId, newState, sync = true) {
  const response = await axios.post(
    `${BASE_URL}/vehicle/${vehicleId}`,
    newState,
    {
      headers: {
        'Content-Type': 'application/ninebot_es4+json'
      },
      params: {
        apiKey: envConfig('COMODULE_API_KEY'),
        sync
      }
    }
  )
  return response.data
}

async function lock (vehicleId) {
  return editVehicleState(vehicleId, { vehiclePowerOn: false })
}

async function unlock (vehicleId) {
  return editVehicleState(vehicleId, { vehiclePowerOn: true })
}

module.exports = {
  getVehicleState,
  lock,
  unlock
}
