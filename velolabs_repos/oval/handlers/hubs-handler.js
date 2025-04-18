const {
  logger,
  pointHandler,
  responseCodes,
  encryptionHandler,
  errors
} = require('@velo-labs/platform')
const _ = require('underscore')
const db = require('../db')
const config = require('./../config')
const { KuhmuteAPI } = require('../helpers/kuhmute-api')
const { DucktApiClient } = require('../helpers/duckt-api')
const Greenriders = require('../helpers/greenriders')
const { Kisi } = require('../helpers/kisi')
const AWS = require('aws-sdk')
const bikeHandler = require('./bike-handler')
const dbHelpers = require('../helpers/db')
const { promotions } = require('./promotions')
const pricing = require('./pricing')
const ssmClient = new AWS.SSM({
  region: 'us-west-1'
})
const { configureSentry } = require('../utils/configureSentry')

const Sentry = configureSentry()

let DucktApi

const environment = process.env.CURRENT_ENVIRONMENT

const fleetsAndHubSelection = [
  'id as hubFleetJoinId',
  'hubs_and_fleets.fleet_id as fleetId',
  'hubs_and_fleets.hub_uuid as fleet_hub_uuid',
  'hubs_and_fleets.open_or_close',
  'hubs.hub_id as hub_id',
  'hubs.latitude',
  'hubs.longitude',
  'hubs.status as remote_hub_status',
  'hubs_and_fleets.status as local_hub_status',
  'hubs_and_fleets.fleet_id as fleet_id',
  'hubs_and_fleets.equipment as hub_equipment_id',
  'hubs_and_fleets.image',
  'hubs.name as hub_name',
  'hubs.uuid as hub_uuid',
  'make',
  'model',
  'type',
  'description',
  'integration',
  'ports.port_id as port_id',
  'ports.uuid as port_uuid',
  'ports.hub_uuid as port_hub_uuid',
  'ports.status as port_status',
  'ports.vehicle_uuid as port_vehicle_uuid',
  'ports.locked as port_locked',
  'ports.equipment as port_equipment_id',
  'ports.current_status as port_current_status',
  'number as port_number',
  'charging as port_charging_status',
  'bikes.fleet_id as bike_fleet_id',
  'bikes.bike_uuid',
  'bike_id',
  'bike_name',
  'bikes.status as bike_status',
  'bikes.current_status as current_bike_status'
]

const formatFleetsAndHubsData = (fleetsWithHubs) => {
  const data = fleetsWithHubs.map((fleetsWithHubsData) => {
    return {
      ...fleetsWithHubsData.hubs_and_fleets,
      hubs: [{ ...fleetsWithHubsData.hubs }].filter((hub) => hub.hub_id),
      ports: [{ ...fleetsWithHubsData.ports }].filter((port) => port.port_id),
      bikes: [{ ...fleetsWithHubsData.bikes }].filter((bike) => {
        return (
          bike.bike_fleet_id &&
          bike.current_bike_status === 'parked' &&
          bike.bike_status === 'active'
        )
      })
    }
  })

  const fleetInfoMap = {}
  for (let fleetInfo of data) {
    if (fleetInfoMap[fleetInfo.fleetId]) {
      const dataMap = fleetInfoMap[fleetInfo.fleetId]
      dataMap['fleetId'] = fleetInfo.fleetId
      dataMap['hubs'] = [...dataMap['hubs'], ...fleetInfo.hubs].map((hub) => {
        if (hub.name) {
          hub.hub_name = hub.name
          delete hub.name
        }
        if (fleetInfo.fleet_hub_uuid === hub.hub_uuid) {
          return Object.assign(hub, {
            local_hub_status: fleetInfo.local_hub_status,
            make: fleetInfo.make,
            model: fleetInfo.model,
            image: fleetInfo.image,
            description: fleetInfo.description,
            type: fleetInfo.type,
            integration: fleetInfo.integration,
            fleet_id: fleetInfo.fleet_id,
            hub_equipment_id: fleetInfo.hub_equipment_id || fleetInfo.equipment
          })
        }
        return hub
      })
      dataMap['ports'] = [...dataMap['ports'], ...fleetInfo.ports]
      dataMap['bikes'] = [...dataMap['bikes'], ...fleetInfo.bikes]
      addBikeToPort(fleetInfo.bikes, dataMap['ports'], fleetInfo.fleetId)
      dataMap['local_hub_status'] = fleetInfo.local_hub_status
      fleetInfoMap[fleetInfo.fleetId] = dataMap
    } else {
      const data = {}
      data['fleetId'] = fleetInfo.fleetId
      data['local_hub_status'] = fleetInfo.local_hub_status
      data['hubs'] = [...fleetInfo.hubs].map((hub) => {
        if (hub.name) {
          hub.hub_name = hub.name
          delete hub.name
        }
        if (fleetInfo.fleet_hub_uuid === hub.hub_uuid) {
          return Object.assign(hub, {
            local_hub_status: fleetInfo.local_hub_status,
            make: fleetInfo.make,
            model: fleetInfo.model,
            image: fleetInfo.image,
            description: fleetInfo.descriptionmake,
            type: fleetInfo.type,
            integration: fleetInfo.integration,
            fleet_id: fleetInfo.fleet_id,
            hub_equipment_id: fleetInfo.hub_equipment_id || fleetInfo.equipment
          })
        }
      })
      data['ports'] = [...fleetInfo.ports]
      data['bikes'] = [...fleetInfo.bikes]
      addBikeToPort(fleetInfo.bikes, data['ports'], fleetInfo.fleetId)
      fleetInfoMap[fleetInfo.fleetId] = data
    }
  }

  const mappedFleetInfoMap = Object.values(fleetInfoMap).map((data) => {
    let hubs = [
      ...new Map(data.hubs.map((item) => [item['hub_uuid'], item])).values()
    ]
    for (let port of data.ports) {
      for (let hub of hubs) {
        if (hub.hub_uuid === port.port_hub_uuid) {
          if (hub.ports) hub.ports = [...hub.ports, port]
          else hub.ports = [port]
        }
      }
    }
    delete data.ports
    delete data.local_hub_status
    delete data.local_hub_status
    return { ...data, hubs }
  })
  return mappedFleetInfoMap
}

const addBikeToPort = (bikes, ports, fleetId) => {
  if (bikes.length) {
    for (let bike of bikes) {
      for (let port of ports) {
        if (port.port_vehicle_uuid === bike.bike_uuid) {
          port.bike = bike
        }
      }
    }
  }
}

const getAllParameters = async (path) => {
  try {
    return await ssmClient
      .getParametersByPath({
        Path: `/env/${path}`,
        Recursive: true
      })
      .promise()
  } catch (error) {
    Sentry.captureException(error, { path })
    throw error
  }
}

/**
 * This method gets the user details from integrations
 *
 * @param {Object} bike - filter parameters
 * @param {string} integrationType - filter parameters
 */
const setUpApiUser = async (bike, integrationType) => {
  let user = {}
  const apiKeys = await db
    .main('integrations')
    .where('fleet_id', bike.fleet_id)
    .where('integration_type', integrationType)
    .select()
  if (apiKeys.length) {
    switch (integrationType) {
      case 'duckt':
        user.userName = apiKeys[0].email
        user.password = encryptionHandler.decryptDbValue(apiKeys[0].api_key)
        return user
      case 'ParcelHive':
        user.accessToken = JSON.parse(apiKeys[0].metadata).accessToken
        return user
      default:
        return {}
    }
  }
}

const getFleetWithAllInfo = async (fleetId) => {
  const fleetInfo = await db.main('fleets').join('addresses', function () {
    this.on('addresses.type', '=', db.main.raw('?', ['fleet'])).andOn(
      'addresses.type_id',
      '=',
      'fleets.fleet_id'
    )
  }).leftOuterJoin(
    'fleet_payment_settings',
    'fleet_payment_settings.fleet_id',
    'fleets.fleet_id'
  ).leftOuterJoin('fleet_metadata', 'fleet_metadata.fleet_id', 'fleets.fleet_id')
    .where({ 'fleets.fleet_id': fleetId }).options({ nestTables: true, typeCast: dbHelpers.castDataTypes }).first()

  fleetInfo.fleets.skip_parking_image = !!fleetInfo.fleets.skip_parking_image
  const fleetPaymentSettings = fleetInfo.fleet_payment_settings
  if (fleetPaymentSettings && (fleetPaymentSettings.price_for_penalty_outside_parking || fleetPaymentSettings.price_for_penalty_outside_parking === 0)) {
    delete fleetPaymentSettings.price_for_penalty_outside_parking
  }
  if (fleetPaymentSettings && fleetPaymentSettings.usage_surcharge === 'No') {
    fleetPaymentSettings.excess_usage_fees = 0.00
    fleetPaymentSettings.excess_usage_type_after_value = 0.00
    fleetPaymentSettings.excess_usage_type_value = 0.00
    fleetPaymentSettings.excess_usage_type = ''
    fleetPaymentSettings.excess_usage_type_after_type = ''
  }

  fleetPaymentSettings.enable_preauth = !!fleetPaymentSettings.enable_preauth
  const fleetMeta = {...fleetInfo.fleet_metadata,
    do_not_track_trip: !!fleetInfo.fleet_metadata.do_not_track_trip,
    require_phone_number: !!fleetInfo.fleet_metadata.require_phone_number,
    show_parking_zones: !!fleetInfo.fleet_metadata.show_parking_zones,
    show_parking_spots: !!fleetInfo.fleet_metadata.show_parking_spots,
    email_on_start_trip: !!fleetInfo.fleet_metadata.email_on_start_trip,
    smart_docking_stations_enabled: !!fleetInfo.fleet_metadata.smart_docking_stations_enabled,
    geofence_enabled: !!fleetInfo.fleet_metadata.geofence_enabled
  }

  const info = {...fleetInfo.fleets, address: fleetInfo.addresses, fleet_payment_settings: fleetPaymentSettings, ...fleetMeta}
  return info
}

const getAdjacentHubs = async (searchParameters, fleetIds) => {
  try {
    let latLon = searchParameters.ne.replace(/[()]/g, '').split(',')
    const ne = {
      latitude: parseFloat(latLon[0]),
      longitude: parseFloat(latLon[1])
    }
    latLon = searchParameters.sw.replace(/[()]/g, '').split(',')
    const sw = {
      latitude: parseFloat(latLon[0]),
      longitude: parseFloat(latLon[1])
    }
    const center = {
      latitude: sw.latitude + (ne.latitude - sw.latitude) / 2,
      longitude: sw.longitude + (ne.longitude - sw.longitude) / 2
    }
    let hubsQuery = db
      .main('hubs_and_fleets')
      .leftJoin('hubs', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
      .leftJoin('ports', 'hubs.uuid', 'ports.hub_uuid')
      .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
      .whereNot({ 'ports.port_id': null })
      .options({ nestTables: true })
    if (Array.isArray(fleetIds) && fleetIds.length) {
      hubsQuery = hubsQuery.whereIn('hubs_and_fleets.fleet_id', fleetIds)
    }
    hubsQuery = hubsQuery
      .andWhere({
        'hubs.status': 'Available',
        'hubs_and_fleets.status': 'live'
        // 'ports.status': 'Available'
      })
      .select(fleetsAndHubSelection)
    let hubs = await hubsQuery
    hubs = formatFleetsAndHubsData(hubs)
    if (!hubs.length) {
      return []
    }
    hubs = hubs.map((hub) =>
      Object.assign([...hub.hubs], { bikes: hub.bikes })
    )
    hubs = Array.prototype.concat.apply([], hubs)
    hubs = [...new Map(hubs.map((hub) => [hub['hub_uuid'], hub])).values()]
    hubs = hubs.map(async (hub) => {
      const areAllPortsAvailable = (port) => port.port_status === 'Available'
      const areAllPortsUnavailable = (port) => port.port_status === 'Unavailable'

      if (hub.type === 'docking_station' && hub.ports.every(areAllPortsAvailable)) return {...hub, VISIBLE: null}
      if (hub.type === 'parking_station' && hub.ports.every(areAllPortsUnavailable)) return {...hub, VISIBLE: null}

      if (!hub.image) hub.image = 'https://hubs-and-fleets-images.s3.ca-central-1.amazonaws.com/default_closed.png'
      const ports = hub.ports.map(async (port) => {
        const mappedPort = {
          ...port,
          port_name: `${hub.hub_name} #${port.port_number}`
        }
        if (port.port_equipment_id) {
          const equipment = await db
            .main('controllers')
            .where({ controller_id: port.port_equipment_id })
            .first()
          mappedPort.equipment = equipment
        } else mappedPort.equipment = null
        if (port.port_vehicle_uuid) {
          const vehicle = await db
            .main('bikes')
            .where({ bike_uuid: port.port_vehicle_uuid })
            .first()
          mappedPort.vehicle = vehicle
        } else mappedPort.vehicle = null
        const qrCodeInfo = await db
          .main('qr_codes')
          .where({ equipment_id: `${port.port_id}`, type: 'port' })
          .first()
        mappedPort.qr_code = (qrCodeInfo && qrCodeInfo.code) || null
        if (mappedPort.port_status === 'Available' && mappedPort.port_current_status !== 'on_trip' && hub.type === 'parking_station') {
          return mappedPort
        } else if (mappedPort.port_status === 'Unavailable' && hub.type === 'docking_station') {
          return mappedPort
        } else {
          return {}
        }
      })
      if (hub.hub_equipment_id) {
        const equipment = await db
          .main('controllers')
          .where({ controller_id: hub.hub_equipment_id })
          .first()
        hub.equipment = equipment
      }

      const qrCodeInfo = await db
        .main('qr_codes')
        .where({ equipment_id: `${hub.hub_id}`, type: 'hub' })
        .first()
      hub.qr_code = (qrCodeInfo && qrCodeInfo.code) || null
      hub.ports = await Promise.all(ports)
      let bikes = []
      if (hub.ports.length) {
        let newAry = []
        hub.ports.map(port => {
          if (port.port_id) {
            newAry.push(port)
          }
          if (port.vehicle) {
            bikes.push(port.vehicle)
          }
        })
        hub.ports = newAry
      }
      hub.fleet = await getFleetWithAllInfo(hub.fleet_id)
      hub.bikes = bikes

      const promos = await promotions.list({
        user: { user_id: searchParameters.user_id },
        fleetId: hub.fleet_id
      })
      if (promos.length) {
        hub.promotions = promos
      }

      hub.pricing_options = await pricing.handlers.list({
        fleet_id: hub.fleet_id
      })
      return hub
    })

    hubs = (await Promise.all(hubs)).filter(h => !(h.VISIBLE === null))

    let adjacentHubs = []
    const hubRange = pointHandler.distanceBetweenPoints(center, ne)
    for (let hub of hubs) {
      // Hub location
      const { latitude, longitude } = hub
      const hubLocation = { latitude, longitude }
      const distance = pointHandler.distanceBetweenPoints(center, hubLocation)
      if (distance <= hubRange) {
        adjacentHubs.push(hub)
      }
    }
    adjacentHubs = adjacentHubs.map((hub) => {
      return {
        ...hub,
        latitude: parseFloat(hub.latitude),
        longitude: parseFloat(hub.longitude)
      }
    })
    return adjacentHubs
  } catch (error) {
    Sentry.captureException(error)
    logger('Error getting list of adjacent hubs')
    throw errors.customError(
      error.message || 'Failed to fetch hubs',
      responseCodes.InternalServer,
      'BadRequest',
      false
    )
  }
}

// If status is true, it means we are getting  an Available and live hub
const getHubById = async (hubId, status) => {
  try {
    let hubsQuery = db
      .main('hubs_and_fleets')
      .leftJoin('hubs', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
      .leftJoin('ports', 'hubs.uuid', 'ports.hub_uuid')
      .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
      .where({ 'hubs_and_fleets.hub_id': hubId })
      .whereNot({ 'ports.port_id': null })
      .select(fleetsAndHubSelection)
      .options({ nestTables: true })
    if (status) {
      hubsQuery = hubsQuery.andWhere({
        'hubs.status': 'Available',
        'hubs_and_fleets.status': 'live',
        'ports.status': 'Available'
      })
    }
    let hubs = await hubsQuery
    hubs = formatFleetsAndHubsData(hubs)
    if (!hubs.length) {
      throw errors.customError(
        `Hub with Id ${hubId} not found in Available, live mode`,
        responseCodes.InvalidRequest,
        'HubNotFound',
        false
      )
    }
    hubs = hubs.map((hub) => Object.assign(...hub.hubs, { bikes: hub.bikes }))
    hubs = [...new Map(hubs.map((hub) => [hub['hub_uuid'], hub])).values()]
    hubs = hubs.map(async (hub) => {
      if (hub.latitude) hub.latitude = +hub.latitude
      if (hub.longitude) hub.longitude = +hub.longitude
      const ports = hub.ports.map(async (port) => {
        const mappedPort = {
          ...port,
          port_name: `${hub.hub_name} #${port.port_number}`
        }
        if (port.port_equipment_id || port.equipment) {
          const equipment = await db
            .main('controllers')
            .where({ controller_id: port.port_equipment_id || port.equipment })
            .first()
          mappedPort.equipment = equipment
        } else mappedPort.equipment = null
        const qrCodeInfo = await db
          .main('qr_codes')
          .where({ equipment_id: `${port.port_id}`, type: 'port' })
          .first()
        mappedPort.qr_code = (qrCodeInfo && qrCodeInfo.code) || null
        return mappedPort
      })
      hub.ports = await Promise.all(ports)
      if (hub.hub_equipment_id) {
        const equipment = await db
          .main('controllers')
          .where({ controller_id: hub.hub_equipment_id })
          .first()
        hub.equipment = equipment
      }
      hub.fleet = await getFleetWithAllInfo(hub.fleet_id)
      return hub
    })

    hubs = await Promise.all(hubs)
    if (hubs.length) {
      const response = hubs[0]
      // TODO: Until we upload to s3, no need to send the image data object
      return response
    }
    throw errors.customError(
      `Hub with Id ${hubId} not found`,
      responseCodes.ResourceNotFound,
      'HubNotFound',
      false
    )
  } catch (error) {
    Sentry.captureException(error, { hubId })
    logger(`Error getting hub by Id ${hubId}`)
    throw errors.customError(
      error.message || `Failed to fetch hub by Id ${hubId}`,
      responseCodes.InvalidRequest,
      'BadRequest',
      false
    )
  }
}

// Get a list of hubs within a certain gps locale
const getHubParkingSpots = async (lat, lng, bikeId, hubRange = 5000) => {
  const bike = await db
    .main('bikes')
    .select(['bike_id as bikeId', 'fleet_id as bikeFleetId'])
    .where({ bike_id: bikeId })
    .first()
  try {
    let parkingSpots = []
    let hubs = await db
      .main('hubs_and_fleets')
      .leftJoin('hubs', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
      .leftJoin('ports', 'hubs.uuid', 'ports.hub_uuid')
      .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
      .whereNot({ 'ports.port_id': null })
      .options({ nestTables: true })
      .where({
        'hubs.status': 'Available',
        'hubs_and_fleets.fleet_id': bike.bikeFleetId
      })
      .select(fleetsAndHubSelection)

    const vehicleLocation = {
      latitude: parseFloat(lat),
      longitude: parseFloat(lng)
    }

    hubs = formatFleetsAndHubsData(hubs)
    if (!hubs.length) {
      return []
    }
    hubs = hubs[0].hubs
    for (let [index, hub] of hubs.entries()) {
      if (hub.ports && hub.ports.length && hub.type !== 'parking_station') {
        hub.ports = [
          ...new Map(hub.ports.map((p) => [p.port_id, p])).values()
        ].filter((p) => p.port_status === 'Available')
      } else hubs.splice(index, 1)
    }

    const searchRange = hubRange || config.lattisSearchRange
    for (let hub of hubs) {
      // Hub location
      const { latitude, longitude } = hub
      const hubLocation = { latitude, longitude }
      const distance = pointHandler.distanceBetweenPoints(
        vehicleLocation,
        hubLocation
      )
      if (distance <= searchRange) {
        parkingSpots.push(hub)
      }
    }
    if (!parkingSpots.length) {
      return []
    } else {
      parkingSpots = parkingSpots.map((hub) => {
        return {
          ...hub,
          latitude: parseFloat(hub.latitude),
          longitude: parseFloat(hub.longitude)
        }
      })
    }
    return parkingSpots
  } catch (error) {
    Sentry.captureException(error, { bikeId })
    logger('Error getting list of adjacent hubs')
    throw errors.customError(
      error.message || 'Failed to fetch hubs',
      responseCodes.InternalServer,
      'BadRequest',
      false
    )
  }
}

const undockVehicle = async ({ uuid }, hubType) => {
  try {
    const vehicle = await db.main('bikes').where({ bike_uuid: uuid }).first()
    if (!vehicle) throw new Error('Vehicle with provided uuid does not exist')
    const fleetId = vehicle.fleet_id
    switch (hubType) {
      case 'duckt':
        const ducktUser = await setUpApiUser(vehicle, 'duckt')
        const { Parameters: ducktUrls } = await getAllParameters(
          `${environment}/DUCKT/URLS`
        )
        const urlName = `/env/${environment}/DUCKT/URLS`
        const [baseUrl] = ducktUrls.filter(
          (key) => key.Name === `${urlName}/BASE_URL`
        )
        const [callBackUrl] = ducktUrls.filter(
          (key) => key.Name === `${urlName}/CALL_BACK_URL`
        )
        if (!_.isEmpty(ducktUser)) {
          DucktApi = new DucktApiClient({
            baseUrl: baseUrl.Value,
            password: ducktUser.password,
            userName: ducktUser.userName,
            callbackUrl: callBackUrl.Value
          })
        }
        const unlock = await DucktApi.unlock(vehicle)
        return unlock
      case 'kuhmute':
        const KuhmuteAPIClient = new KuhmuteAPI(fleetId)
        const controller = await db
          .main('controllers')
          .where({ bike_id: vehicle.bike_id })
          .first()
        const response = await KuhmuteAPIClient.unlockVehicle(controller.key)
        return JSON.parse(response)
      case 'dck-mob-e':
        const portWithvehicle = await db
          .main('ports')
          .where({ vehicle_uuid: uuid })
          .first()
        if (portWithvehicle && portWithvehicle.equipment) {
          const controller = await db
            .main('controllers')
            .where({ controller_id: portWithvehicle.equipment })
            .first()
          const status = await new Greenriders().getStationById(controller.key)
          const data = status.Item
          if (!data.LockedScooterID) {
            throw new Error('The station does not have a vehicle locked')
          } else {
            const response = await new Greenriders().unlockStation(
              controller.key,
              data.LockedScooterID
            )
            await db
              .main('ports')
              .where({ port_id: portWithvehicle.port_id })
              .update({ current_status: 'parked', vehicle_uuid: null })
            return response
          }
        }
        break
      default:
        logger('Operation is not supported for that integration yet')
        throw errors.customError(
          'Operation is not supported for that integration yet',
          responseCodes.InternalServer,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    Sentry.captureException(error, { uuid })
    logger('Error undocking vehicle')
    throw errors.customError(
      error.message || 'Failed to undock vehicle from hub',
      responseCodes.InternalServer,
      'BadRequest',
      false
    )
  }
}

const unlockCustomPorts = async (portUUID) => {
  try {
    const port = await db.main('ports').where({ uuid: portUUID }).first()
    if (!port) throw new Error('Port not found')
    if (!port.equipment) throw new Error('Port does not have an equipment')
    const controller = await db
      .main('controllers')
      .where({ controller_id: port.equipment })
      .first()
    switch (controller.vendor) {
      case 'Kisi':
        const kisiUnlockResponse = await new Kisi().unlock(
          controller.fleet_id,
          controller.key
        )
        if (kisiUnlockResponse.error) {
          logger('Failed to unlock Kisi lock:', kisiUnlockResponse.error)
          throw errors.customError(
            'Failed to unlock Kisi lock',
            responseCodes.InternalServer,
            'BadRequest',
            false
          )
        }
        bikeHandler.updateKisiLockStatus(controller)
        await db
          .main('ports')
          .where({ uuid: portUUID })
          .update({ current_status: 'parked' })
        return kisiUnlockResponse
      case 'dck-mob-e': {
        const status = await new Greenriders().getStationById(controller.key)
        const data = status.Item
        if (!data.LockedScooterID) {
          throw new Error('The station does not have a vehicle locked')
        } else {
          const response = await new Greenriders().unlockStation(
            controller.key,
            data.LockedScooterID
          )
          await db
            .main('ports')
            .where({ uuid: portUUID })
            .update({ current_status: 'parked', vehicle_uuid: null })
          return response
        }
      }
      default:
        logger('Operation is not supported for that integration yet')
        throw errors.customError(
          'Operation is not supported for that integration yet',
          responseCodes.InternalServer,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    Sentry.captureException(error, { portUUID })
    logger('Error unlocking ports')
    throw errors.customError(
      error.message || 'Failed to unlock port',
      responseCodes.InternalServer,
      'BadRequest',
      false
    )
  }
}

const unlockCustomHubs = async (hubUUID) => {
  try {
    const hub = await db.main('hubs').where({ uuid: hubUUID }).first()
    if (!hub) throw new Error('The hub not found')
    if (!hub.equipment) throw new Error('The hub does not have an equipment')
    const controller = await db
      .main('controllers')
      .where({ controller_id: hub.equipment })
      .first()
    switch (controller.vendor) {
      case 'Kisi':
        const kisiUnlockResponse = await new Kisi().unlock(
          controller.fleet_id,
          controller.key
        )
        if (kisiUnlockResponse.error) {
          logger('Failed to unlock Kisi lock:', kisiUnlockResponse.error)
          throw errors.customError(
            'Failed to unlock Kisi lock',
            responseCodes.InternalServer,
            'BadRequest',
            false
          )
        }
        bikeHandler.updateKisiLockStatus(controller)
        return kisiUnlockResponse
      default:
        logger('Operation is not supported for that integration yet')
        throw errors.customError(
          'Operation is not supported for that integration yet',
          responseCodes.InternalServer,
          'BadRequest',
          false
        )
    }
  } catch (error) {
    Sentry.captureException(error, { hubUUID })
    logger(`Error unlocking hub with uuid ${hubUUID}`)
    throw errors.customError(
      error.message || 'Failed to unlock hub',
      responseCodes.InternalServer,
      'BadRequest',
      false
    )
  }
}

const retrievePort = async ({ portUUID, portId }) => {
  try {
    let result = {}
    const whereClause = {}
    if (portUUID) whereClause.uuid = portUUID
    if (portId) whereClause.port_id = portId
    if (!portUUID && !portId) {
      throw new Error('Port UUID or Port ID is required')
    }
    const selection = [
      'ports.port_id as port_id',
      'ports.uuid as port_uuid',
      'ports.hub_uuid as port_hub_uuid',
      'ports.status as port_status',
      'ports.vehicle_uuid as port_vehicle_uuid',
      'ports.locked as port_locked',
      'ports.equipment as port_equipment_id',
      'ports.current_status as port_current_status',
      'ports.number as port_number'
    ]
    const port = await db
      .main('ports')
      .where(whereClause)
      .select(selection)
      .first()

    if (port) {
      result = port
      const hub = await db
        .main('hubs')
        .where({ 'hubs.uuid': port.port_hub_uuid })
        .leftJoin('hubs_and_fleets', 'hubs_and_fleets.hub_uuid', 'hubs.uuid')
        .first()
      port.name = `${hub.name}${port.port_number}`
      if (hub) {
        result.hub = {
          ...hub,
          latitude: +hub.latitude,
          longitude: +hub.longitude
        }
        const fleet = await getFleetWithAllInfo(hub.fleet_id)
        port.fleet = fleet
      }
      if (port.port_equipment_id) {
        const ctrl = await db
          .main('controllers')
          .where({ controller_id: port.port_equipment_id })
          .first()
        result.equipment = ctrl
      }
      if (port.port_vehicle_uuid) {
        const bike = await db
          .main('bikes')
          .where({ 'bikes.bike_uuid': port.port_vehicle_uuid })
          .first()
        if (bike) {
          result.bike = bike
        }
      }
    }
    return port
  } catch (error) {
    Sentry.captureException(error, { portUUID, portId })
    logger('Error retrieving port information', error.message)
    throw errors.customError(
      error.message || 'Failed to fetch port',
      responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }
}

module.exports = {
  getAdjacentHubs,
  undockVehicle,
  getHubParkingSpots,
  setUpApiUser,
  unlockCustomPorts,
  retrievePort,
  getHubById,
  unlockCustomHubs
}
