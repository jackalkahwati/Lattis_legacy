const platform = require('@velo-labs/platform')
const db = require('../db')
const _ = require('underscore')
const util = require('util')
const {
  logger,
  pointHandler,
  responseCodes,
  errors
} = require('@velo-labs/platform')
const bikeConstants = require('./../constants/bike-constants')
const sendPushNotification = require('../utils/firebase-messaging')
let { endTripOval } = require('./trip-handler')
endTripOval = util.promisify(endTripOval)
const { lockOrUnlockBike } = require('./bike-fleet-handler')
const { getParameterStoreValue } = require('../utils/parameterStore')
const { Kisi } = require('../utils/kisi')
const Greenriders = require('../utils/greenriders')
const Sentry = require('../utils/configureSentry')
const uploadFile = require('../utils/file-upload')
const passwordHandler = require('../utils/password-handler')
const bikeFleetHandler = require('../model_handlers/bike-fleet-handler')

const platformConfig = platform.config

// const BASE_URL = 'https://api.kuhmute.org/api/v1'
// const { KUHMUTE_API_KEY, KUHMUTE_DOMAIN } = process.env

const { DucktApiClient } = require('../utils/duckt-api')
const { ParcelHiveApiClient } = require('../utils/parcel-hive')
const encryptionHandler = platform.encryptionHandler
let ducktApiClient

async function upsertData (trx, { uuid, status, vehicleUUID, number, hubUUID }) {
  const insert = trx('ports')
    .insert({
      uuid,
      status,
      vehicle_uuid: vehicleUUID || '',
      number,
      hub_uuid: hubUUID
    })
    .toString()

  const update = trx('ports').update({
    status,
    vehicle_uuid: vehicleUUID,
    number,
    hub_uuid: hubUUID
  })
  const query = util.format(
    '%s ON DUPLICATE KEY UPDATE %s',
    insert.toString(),
    update.toString().replace(/^update\s.*\sset\s/i, '')
  )

  await trx.raw(query)
}

const updatePortsOnHubCreation = async (hubPorts, hubUUID) => {
  try {
    /*     const response = await got.get(
      `${KUHMUTE_DOMAIN}/provider/hub/${hubUUID}`,
      {
        headers: {
          Authorization: `${KUHMUTE_API_KEY}`,
          'Content-Type': 'application/json'
        }
      }
    )
    const responseBody = JSON.parse(response.body)
    const hubId = responseBody.hub.uuid
    const hubPorts = responseBody && responseBody.hub && responseBody.hub.ports */
    if (hubPorts) {
      await db.main.transaction(async (trx) => {
        for (let port of hubPorts) {
          const {
            uuid,
            number = undefined,
            status = undefined
            // vin = undefined,
            // charging
          } = port
          // const vehicleUUID = vin
          const valuesToUpdate = _.pick(
            { uuid, number, status, hubUUID },
            (value) => value
          )
          await upsertData(trx, valuesToUpdate)
        }
      })
    }
  } catch (error) {
    Sentry.captureException(error)
    logger(`Error updating hub ports`, error)
  }
}

const createHub = async (data) => {
  const {
    payload: { uuid, name, location, status, ports }
  } = data
  try {
    await db.main('hubs').insert({
      uuid,
      name,
      latitude: location.lat,
      longitude: location.lng,
      status
    })
    await db.main('hubs_and_fleets').insert({
      hub_uuid: uuid
    })
    await updatePortsOnHubCreation(ports, uuid)
  } catch (error) {
    Sentry.captureException(error)
    console.log('Error creating hub:::', error)
    logger(`Error event details`, JSON.stringify(data))
  }
}

const updatePorts = async (ports) => {
  try {
    const portUpdates = ports.map(async (port) => {
      const {
        uuid,
        number = undefined,
        status = undefined,
        vehicle = undefined
      } = port
      const vehicle_uuid = vehicle && vehicle.uuid; // eslint-disable-line
      const valuesToUpdate = _.pick(
        { number, status, vehicle_uuid },
        (value) => value
      )
      const update = await db
        .main('ports')
        .update(valuesToUpdate)
        .where({ number })
        .orWhere({ uuid })
      return update
    })
    return await Promise.all(portUpdates)
  } catch (error) {
    Sentry.captureException(error)
    logger('Error updating ports on hub')
    console.log('Error updating ports on hub:::', error)
  }
}
/**
 * Send vehicles to staging if hub goes to staging
 *
 * @param {string[]} uuids An array of vehicle UUIDs
 */
const updateVehicleStatus = async (data, status, newBikeLocation) => {
  if (data.ports && data.ports.length) {
    const vehicleIds = data.ports
      .filter((port) => port.bikeId)
      .map((port) => port.bikeId)
    if (vehicleIds.length) {
      try {
        let updates = {}
        if (status) {
          updates = { status }
          if (status === bikeConstants.status.inactive) {
            updates = {
              ...updates,
              current_status: bikeConstants.current_status.lockAssigned
            }
          }
          if (status === bikeConstants.status.active) {
            updates = {
              ...updates,
              current_status: bikeConstants.current_status.parked
            }
          }
        }
        if (newBikeLocation) updates = { ...updates, ...newBikeLocation }
        if (Object.keys(updates).length) {
          await db.main('bikes').update(updates).whereIn('bike_id', vehicleIds)
        }
      } catch (error) {
        Sentry.captureException(error)
        logger('Error occurred updating bikes status')
        console.log('Error updating ports on hub:::', error)
      }
    }
  }
}

// This updates hub status as a hook(data from a kuhmute event.data)
const updateHub = async (data) => {
  const {
    payload: {
      location = undefined,
      status = undefined,
      name = undefined,
      ports = undefined,
      uuid
    }
  } = data
  const { link } = data.image ? await uploadFile.uploadBase64ImageFile(data.image,
    `${uuid}-${passwordHandler.randString(10)}`,
    platformConfig.aws.s3.fleetDetails.bucket) : { link: undefined }
  const valuesToInsert = _.pick({ status, name }, (value) => value)
  if (location) {
    valuesToInsert.latitude = location.lat
    valuesToInsert.longitude = location.lng
  }
  try {
    await db.main('hubs').update(valuesToInsert).where({ uuid })
    await db.main('hubs_and_fleets').update({ image: link, ...valuesToInsert }).where({ hub_uuid: uuid })
    let hubStatus
    // Start with hub in staging b4 it's assigned to any fleet
    if (status && !['Available'].includes(status)) {
      hubStatus = bikeConstants.status.inactive
      await db
        .main('hubs_and_fleets')
        .update({ status: 'staging' })
        .where({ hub_uuid: uuid })
    } else if (status && status === 'Available') {
      await db
        .main('hubs_and_fleets')
        .update({ status: 'live' })
        .where({ hub_uuid: uuid })
    }
    if (ports) {
      await updatePorts()
    }
    const newBikeLocation = location && {
      latitude: location.lat,
      longitude: location.lng
    }
    const portsToUpdate = await db
      .main('ports')
      .innerJoin('bikes', 'ports.vehicle_uuid', 'bikes.bike_uuid')
      .where({ hub_uuid: uuid })
      .select(['bikes.bike_id as bikeId'])
    await updateVehicleStatus(
      { ports: portsToUpdate },
      hubStatus,
      newBikeLocation
    )
  } catch (error) {
    Sentry.captureException(error)
    console.log('Error updating hub:::', error)
    logger(`Error updating hub:::`, JSON.stringify(data))
  }
}

const fleetsAndHubSelection = [
  'id as hubFleetJoinId',
  'hubs_and_fleets.fleet_id as fleetId',
  'hubs_and_fleets.hub_uuid as fleetHubUUID',
  'hubs.hub_id as hubID',
  'hubs.created_at as dateCreated',
  'hubs.latitude',
  'hubs.longitude',
  'hubs.status as remoteHubStatus',
  'hubs_and_fleets.status as localHubStatus',
  'hubs_and_fleets.equipment as equipmentId',
  'hubs_and_fleets.open_or_close as enclosureType',
  'controllers.vendor as equipmentType',
  'controllers.key as equipmentKey',
  'controllers.controller_id as controllerId',
  'controllers.make as equipmentMake',
  'controllers.model as equipmentModel',
  'controllers.bike_id as controllerBikeId',
  'name as hubName',
  'hubs.uuid as hubUUID',
  'hubs_and_fleets.make as make',
  'hubs_and_fleets.model as model',
  'image',
  'type',
  'description',
  'integration',
  'ports.port_id as portId',
  'ports.uuid as portUUID',
  'ports.hub_uuid as portHubUUID',
  'ports.status as portStatus',
  'ports.vehicle_uuid as portVehicleUUID',
  'ports.equipment as portEquipmentKey',
  'ports.current_status as currentPortStatus',
  'ports.current_user_id as currentUserId',
  'ports.current_trip_id as currentTripId',
  'ports.last_user_id as lastUserId',
  'number as portNumber',
  'charging as portChargingStatus',
  'bikes.fleet_id as bikeFleetId',
  'bikes.bike_uuid as bikeUUID',
  'bikes.bike_id as bikeId',
  'bike_name as bikeName'
]

/**
 * This method gets the user details from integrations
 *
 * @param {Object} bike - filter parameters
 */
const setUpDucktUser = async (fleetId) => {
  let user = {}
  const ducktCredentials = await db
    .main('integrations')
    .where('fleet_id', fleetId)
    .where('integration_type', 'duckt')
    .select()
  if (ducktCredentials.length) {
    user.userName = ducktCredentials[0].email
    user.password = encryptionHandler.decryptDbValue(
      ducktCredentials[0].api_key
    )
  }
  return user
}

const registerDucktStation = async (uuid, latitude, longitude, fleetId) => {
  try {
    const ducktUser = await setUpDucktUser(fleetId)
    if (!_.isEmpty(ducktUser)) {
      const { Parameter: baseURL } = await getParameterStoreValue(
        'DUCKT/URLS/BASE_URL'
      )
      const { Parameter: callbackUrl } = await getParameterStoreValue(
        'DUCKT/URLS/CALL_BACK_URL'
      )
      ducktApiClient = new DucktApiClient({
        baseUrl: baseURL.Value,
        password: ducktUser.password,
        userName: ducktUser.userName,
        callbackUrl: callbackUrl.Value
      })
      const registeredStation = await ducktApiClient.registerStation(
        uuid,
        latitude,
        longitude
      )
      registeredStation.isNewStation = true
      return registeredStation
    } else {
      throw new Error(
        'Could not setup a duckt user. A possible missing integration in the fleet'
      )
    }
  } catch (error) {
    Sentry.captureException(error)
    if (error.message === 'Station name is already in use') {
      // fetch station and ports from db
      const registeredStation = await db
        .main('hubs')
        .where({ uuid: uuid })
        .first()
      const ports = await db.main('ports').where({ hub_uuid: uuid }).first()
      registeredStation.isNewStation = false
      registeredStation.ports = ports
      return registeredStation
    }
    logger('Error: An error occurred updating a hub')
    throw new Error(`${error.message}` || 'An error occurred updating a hub')
  }
}

const updateQrCode = async (qrCode, type, equipmentId, trx) => {
  // equipmentId here is either hub_id, port_id or bike_id
  try {
    const isQRCodeSet = await (trx ? trx('qr_codes') : db.main('qr_codes'))
      .where({ code: qrCode })
      .first()
    // Upsert operation
    if (isQRCodeSet && isQRCodeSet.equipment_id !== +equipmentId) {
      throw new Error('The QR code already exists')
    } else if (!isQRCodeSet) {
      const isEquipmentInQrCodeTable = await (trx
        ? trx('qr_codes')
        : db.main('qr_codes')
      )
        .where({ equipment_id: equipmentId, type })
        .first()
      if (isEquipmentInQrCodeTable) {
        await (trx ? trx('qr_codes') : db.main('qr_codes'))
          .update({ code: qrCode })
          .where({ type, equipment_id: +equipmentId })
      } else {
        await await (trx ? trx('qr_codes') : db.main('qr_codes')).insert({
          code: qrCode,
          type,
          equipment_id: +equipmentId
        })
      }
    }
  } catch (error) {
    Sentry.captureException(error)
    console.log('An error occurred updating the qr code', error)
  }
}

const getUpdatedHubInfo = async (fleetId, uuid) => {
  const updatedHubInfo = await db
    .main('hubs_and_fleets')
    .leftJoin('hubs', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
    .leftJoin('ports', 'ports.hub_uuid', 'hubs.uuid')
    .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
    .leftJoin(
      'controllers',
      'controllers.controller_id',
      'hubs_and_fleets.equipment'
    )
    .options({ nestTables: true })
    .select(fleetsAndHubSelection)
    .where({
      'hubs_and_fleets.fleet_id': fleetId,
      'hubs_and_fleets.hub_uuid': uuid
    })
  return updatedHubInfo
}

// Updates the hubs table and hub-fleet join table with fleet_id
const updateHubFleet = async (uuid, data, moveBikes) => {
  const hub = await db.main('hubs').where({ uuid }).first()
  // if (!hub) throw new Error('A hub with the provided UUID does not exist')
  const hubStatus = hub && hub.status
  if (
    hubStatus &&
    ['Maintenance', 'Unavailable'].includes(hubStatus) &&
    status === 'live'
  ) {
    throw new Error(
      `You cannot move the hub to live. It's remote status is ${hubStatus}`
    )
  }

  const fleetId = data.fleet_id
  const qrCode = data.qrCode

  const hubToUpdate = await db
    .main('hubs_and_fleets')
    .select('hub_id as hubID', 'fleet_id as fleetId')
    .where({ 'hubs_and_fleets.hub_uuid': uuid, fleet_id: fleetId })
    .first()

  switch (data.integration) {
    case 'duckt':
      try {
        let {
          make = undefined,
          model = undefined,
          description = undefined,
          type = undefined,
          image = undefined,
          fleet_id = undefined, // eslint-disable-line
          integration = undefined,
          latitude = undefined,
          longitude = undefined,
          status = undefined,
          name = undefined,
          enclosureType = undefined
        } = data; // eslint-disable-line

        const { link } = image ? await uploadFile.uploadBase64ImageFile(image,
          `${uuid}-${passwordHandler.randString(10)}`,
          platformConfig.aws.s3.fleetDetails.bucket
        ) : { link: undefined }

        return await db.main.transaction(async (trx) => {
          if (hubToUpdate) {
            if (qrCode) {
              await updateQrCode(qrCode, 'hub', hub.hub_id, trx)
            }
            await trx('hubs_and_fleets')
              .update({
                status,
                make,
                model,
                description,
                type,
                image: link,
                integration
              })
              .where({ hub_uuid: uuid, fleet_id: fleet_id })
            // sometimes the update for duckt involves editing the hub
            if (name || latitude) {
              await trx('hubs')
                .update({
                  name,
                  latitude,
                  longitude
                })
                .where({ uuid: uuid })
            }
          } else {
            const registeredStation = await registerDucktStation(
              uuid,
              latitude,
              longitude,
              fleet_id
            )
            const ports = registeredStation.ports
            // perform these actions only when the hub is newly registered.
            if (registeredStation.isNewStation) {
              await trx('hubs').insert({
                uuid: uuid,
                status: 'Available',
                name,
                latitude: registeredStation.lat,
                longitude: registeredStation.lng
              })
              for (const port of ports) {
                await trx('ports').insert({
                  uuid: port.name,
                  hub_uuid: uuid,
                  status: 'Available',
                  number: port.id
                })
              }
            }
            await trx('hubs_and_fleets').insert({
              fleet_id,
              hub_uuid: uuid,
              status: hubStatus === 'Unavailable' ? 'staging' : status,
              make,
              model,
              description,
              type,
              image: link,
              integration,
              open_or_close: enclosureType || 'open'
            })
          }

          const updatedHubInfo = await getUpdatedHubInfo(fleetId, uuid)

          let data = formatFleetsAndHubsData(updatedHubInfo)[0]
          if (data && data.hubs) {
            data = Object.assign({}, data, data.hubs[0])
            delete data.hubs
          }
          if (
            data &&
            data.localHubStatus &&
            data.localHubStatus === 'staging'
          ) {
            await updateVehicleStatus(data, bikeConstants.status.inactive)
          } else if (
            data &&
            data.localHubStatus &&
            data.localHubStatus === 'live'
          ) {
            if (moveBikes) {
              await updateVehicleStatus(data, bikeConstants.status.active)
            }
          }
          return data
        })
      } catch (error) {
        Sentry.captureException(error)
        logger('Error: An error occurred updating a hub')
        throw new Error(
          `${error.message}` || 'An error occurred updating a hub'
        )
      }
    case 'kuhmute':
      if (!hub) throw new Error('A hub with the provided UUID does not exist')
      try {
        let {
          make = undefined,
          model = undefined,
          description = undefined,
          type = undefined,
          image = undefined,
          fleet_id = undefined, // eslint-disable-line
          integration = undefined,
          enclosureType = undefined,
          status = 'staging'
        } = data; // eslint-disable-line

        const { link } = image ? await uploadFile.uploadBase64ImageFile(image,
          `${uuid}-${passwordHandler.randString(10)}`,
          platformConfig.aws.s3.fleetDetails.bucket) : { link: undefined }

        return await db.main.transaction(async (trx) => {
          if (hubToUpdate) {
            if (qrCode) {
              await updateQrCode(qrCode, 'hub', hub.hub_id, trx)
            }
            await trx('hubs_and_fleets')
              .update({
                status: hubStatus === 'Unavailable' ? 'staging' : status,
                make,
                model,
                description,
                type,
                image: link,
                integration
              })
              .where({ hub_uuid: uuid, fleet_id: fleet_id })
          } else {
            await trx('hubs_and_fleets').insert({
              fleet_id,
              hub_uuid: uuid,
              status: hubStatus === 'Unavailable' ? 'staging' : status,
              make,
              model,
              description,
              type,
              image: link,
              integration,
              open_or_close: enclosureType || 'open'
            })
          }

          const updatedHubInfo = await getUpdatedHubInfo(fleetId, uuid)

          let data = formatFleetsAndHubsData(updatedHubInfo)[0]
          if (data && data.hubs) {
            data = Object.assign({}, data, data.hubs[0])
            delete data.hubs
          }
          if (
            data &&
            data.localHubStatus &&
            data.localHubStatus === 'staging'
          ) {
            await updateVehicleStatus(data, bikeConstants.status.inactive)
          } else if (
            data &&
            data.localHubStatus &&
            data.localHubStatus === 'live'
          ) {
            if (moveBikes) {
              await updateVehicleStatus(data, bikeConstants.status.active)
            }
          }
          return data
        })
      } catch (error) {
        Sentry.captureException(error)
        logger('Error: An error occurred updating a hub')
        throw new Error(error)
      }
    case 'custom':
      if (!hub) throw new Error('A hub with the provided UUID does not exist')
      try {
        let {
          make = undefined,
          model = undefined,
          description = undefined,
          type = undefined,
          image = undefined,
          fleet_id = undefined, // eslint-disable-line
          integration = undefined,
          latitude = undefined,
          longitude = undefined,
          status = undefined,
          name = undefined,
          equipmentKey = undefined,
          equipmentType = undefined,
          enclosureType = undefined
        } = data; // eslint-disable-line

        const { link } = image ? await uploadFile.uploadBase64ImageFile(image,
          `${uuid}-${passwordHandler.randString(10)}`,
          platformConfig.aws.s3.fleetDetails.bucket) : { link: undefined }

        return await db.main.transaction(async (trx) => {
          if (hubToUpdate) {
            if (qrCode) {
              await updateQrCode(qrCode, 'hub', hub.hub_id, trx)
            }
            if (equipmentType && equipmentKey) {
              if (equipmentType === 'Kisi') {
                const status = await new Kisi().fetchLock(
                  fleetId,
                  +equipmentKey
                )
                if (!status) {
                  logger('Error: Kisi device did not return status')
                } else {
                  const position = {
                    longitude:
                      longitude ||
                      status.longitude ||
                      status.place.longitude ||
                      null,
                    latitude:
                      latitude ||
                      status.latitude ||
                      status.place.latitude ||
                      null
                  }
                  if (hubToUpdate.equipment) {
                    await trx('controllers')
                      .update({
                        key: equipmentKey,
                        equipment_type: 'hub',
                        hub_id: hubToUpdate.hubID,
                        vendor: equipmentType,
                        device_type: 'iot',
                        ...position
                      })
                      .where({ controller_id: hubToUpdate.equipment })
                  } else {
                    const equipmentExists = await trx('controllers')
                      .where({
                        vendor: equipmentType,
                        key: equipmentKey
                      })
                      .first()
                    if (equipmentExists) {
                      throw new Error('The equipment Id already exists')
                    }
                    const [insertId] = await trx('controllers').insert({
                      vendor: equipmentType,
                      key: equipmentKey,
                      equipment_type: 'hub',
                      hub_id: hubToUpdate.hubID,
                      fleet_id: fleetId,
                      device_type: 'iot'
                    })
                    await trx('hubs_and_fleets')
                      .where({ hub_uuid: uuid, fleet_id: fleetId })
                      .update({ equipment: insertId })
                  }
                }
              } else if (equipmentType === 'Sas') {
                console.log('sas updates go in here')
              } else {
                // TODO: Add any other key implementation here
              }
            }
            await trx('hubs_and_fleets')
              .update({
                status,
                make,
                model,
                description,
                type,
                image: link,
                integration
              })
              .where({ hub_uuid: uuid, fleet_id: fleet_id })
            const updates = {}
            if (name) updates.name = name
            if (longitude) updates.longitude = longitude
            if (latitude) updates.latitude = latitude
            if (Object.keys(updates).length) {
              await trx('hubs').update(updates).where({ uuid: uuid })
            }
          } else {
            await trx('hubs_and_fleets').insert({
              fleet_id,
              hub_uuid: uuid,
              status: hubStatus === 'Unavailable' ? 'staging' : status,
              make,
              model,
              description,
              type,
              image: link,
              integration,
              open_or_close: enclosureType
            })
          }

          const updatedHubInfo = await getUpdatedHubInfo(fleetId, uuid)

          let data = formatFleetsAndHubsData(updatedHubInfo)[0]
          if (data && data.hubs) {
            data = Object.assign({}, data, data.hubs[0])
            delete data.image
            delete data.hubs
          }
          if (
            data &&
            data.localHubStatus &&
            data.localHubStatus === 'staging'
          ) {
            if (moveBikes) {
              await db
                .main('ports')
                .where({ hub_uuid: uuid })
                .update({ status: 'Available' })
            }
          } else if (
            data &&
            data.localHubStatus &&
            data.localHubStatus === 'live'
          ) {
            if (moveBikes) {
              await db
                .main('ports')
                .where({ hub_uuid: uuid })
                .update({ status: 'Unavailable' })
            }
          }
          return data
        })
      } catch (error) {
        Sentry.captureException(error)
        logger('Error: An error occurred updating a hub')
        throw new Error(
          `${error.message}` || 'An error occurred updating a hub'
        )
      }
    default:
      throw new Error('Hub integration not supported')
  }
}

// TODO: Add authentication :- do we need a new role?
/**
 * Return details for vehicles to 3rd party service like Kuhmute. It takes an array of bikeIds or it'll return all vehicles
 *
 * @param {*} [bikeIds=[]]
 */
const getVehicleDetails = async (qrCodes) => {
  try {
    const selection = [
      'bikes.bike_id as bikeId',
      'bikes.bike_name as bikeName',
      'bikes.bike_uuid as bikeUUID',
      'bikes.latitude as bikeLat',
      'bikes.longitude as bikeLng',
      'bikes.bike_battery_level as bikeBatteryLevel',
      'bikes.current_status as currentBikeStatus',
      'controllers.controller_id as controllerId',
      'controllers.vendor as controllerVendor',
      'controllers.key as controllerKey',
      'controllers.battery_level as controllerBatteryLevel',
      'controllers.latitude as controllerLat',
      'controllers.longitude as controllerLng'
    ]
    const vehicles = await db
      .main('bikes')
      .leftJoin('controllers', 'bikes.bike_id', '=', 'controllers.bike_id')
      .select(selection)
      .whereIn('bikes.qr_code_id', [...new Set(qrCodes)])
    return vehicles
  } catch (error) {
    Sentry.captureException(error)
    logger('Error getting vehicle details')
    console.log('Error:', error)
  }
}

// Called when a vehicle docks, change it's status to packed
/*
{
  "type": "vehicle.docked",
  "fleet": "na_est_2.fle_0b6f7376-abd5-4555-95dd-df4f70af5ea9",
  "zone": "na_est_2.zon_a05146a2-a217-42aa-95fc-f343993d91cb",
  "payload": {
      "uuid": "881"
  }
}
*/
const updateVehicleDockingStatus = async (uuid) => {
  try {
    const vehicle = await db.main('bikes').where({ bike_uuid: uuid }).first()
    let trip
    if (vehicle) {
      trip = await db
        .main('trips')
        .leftJoin('fleet_metadata', 'fleet_metadata.fleet_id', 'trips.fleet_id')
        .leftJoin('fleets', 'fleets.fleet_id', 'trips.fleet_id')
        .where((builder) => {
          builder.whereNotNull('trips.date_created').whereNull('trips.date_endtrip')
        })
        .andWhere({ bike_id: vehicle.bike_id })
        .orderBy('trip_id', 'desc')
        .first()
    }
    /*
      await db
      .main('bikes')
      .update({ current_status: 'parked' })
      .where({ bike_uuid: uuid })
      */
    let dockedToHub
    let port = await db.main('ports').where({ vehicle_uuid: uuid }).first()
    if (port) {
      dockedToHub = await db
        .main('hubs')
        .where({ uuid: port.hub_uuid })
        .first()
    }
    if (dockedToHub) {
      await db
        .main('bikes')
        .update({
          latitude: dockedToHub.latitude,
          longitude: dockedToHub.longitude
        })
        .where({ bike_id: vehicle.bike_id })
    }
    if (trip.date_endtrip) {
      // There's no active trip
      return
    }
    if (vehicle && trip) {
      let PNMessage = `Vehicle has Docked`
      let titleLocKey = 'end_your_ride'
      let bodyLocKey = 'your_vehicle_is_docked'
      let clickAction = 'docking'
      if (trip && !trip.date_endtrip && trip.smart_docking_stations_enabled) {
        PNMessage = `Vehicle has Docked. Trip ${trip.trip_id} will be ended automatically`
        // End trip if smart docking station is enabled
        // Temporary fix in case there was a delay
        if (!port) await new Promise((resolve) => setTimeout(resolve, 2000))
        port = await db.main('ports').where({ vehicle_uuid: uuid }).first()
        if (!port) logger(`Info::: the vehicle ${uuid} is not docked...`)

        const tripDetails = {
          latitude: dockedToHub && dockedToHub.latitude,
          longitude: dockedToHub && dockedToHub.longitude,
          trip_id: trip.trip_id,
          fleet_id: trip.fleet_id,
          acl: { operator_id: trip.operator_id },
          source: 'Self End',
          bike_uuid: uuid
        }
        await endTripOval(tripDetails)
        await db
          .main('trips')
          .update({ rating_shown: false })
          .where({ trip_id: trip.trip_id })
        await db
          .main('bikes')
          .update({
            latitude: dockedToHub.latitude,
            longitude: dockedToHub.longitude
          })
          .where({ bike_id: vehicle.bike_id })
        titleLocKey = 'vehicle_is_docked'
        bodyLocKey = 'your_trip_was_ended_automatically'
        clickAction = 'docked'
      }
      let fleetMetadata = await db.main('fleet_metadata').where({ fleet_id: trip.fleet_id }).first()
      let appId = '6'
      if (fleetMetadata && fleetMetadata.whitelabel_id) {
        appId = fleetMetadata.whitelabel_id
      }
      const deviceToken = trip && trip.device_token
      if (deviceToken) {
        const message = {
          notification: {
            title: 'Vehicle Docked',
            body: PNMessage,
            title_loc_key: titleLocKey,
            body_loc_key: bodyLocKey,
            sound: 'default',
            click_action: clickAction
          },
          data: {
            trip_id: trip.trip_id.toString()
          }
        }
        const options = {
          priority: 'high',
          timeToLive: 60 * 60 * 24,
          'contentAvailable': true
        }
        sendPushNotification(deviceToken, message, options, appId)
        logger(`Info::PN send for trip ${trip.trip_id.toString()}`)
      }
    }
  } catch (error) {
    Sentry.captureException(error)
    logger('🚀 ~An error occurred updating bike docking status', error)
  }
}

const updatePort = async (data) => {
  const {
    payload: { uuid, number, status, vehicle = undefined }
  } = data
  const vehicleUUID = (vehicle && vehicle.uuid) || null; // eslint-disable-line
  const valuesToUpdate = { uuid, number, status, vehicle_uuid: vehicleUUID }
  const bike = await db.main('bikes').where({ bike_uuid: vehicleUUID }).first()
  try {
    await db.main('ports').update(valuesToUpdate).where({ uuid: uuid })
    if (vehicleUUID) {
      // Means it was docking
      await updateVehicleDockingStatus(vehicleUUID)
      if (bike) {
        const bikeId = bike.bike_id
        const ioTController = await db
          .main('controllers')
          .where({ bike_id: bikeId, device_type: 'iot' })
          .first()
        try {
          if (ioTController) await lockOrUnlockBike(bikeId, 'lock')
        } catch (error) {
          Sentry.captureException(error, {data})
          logger('An error occured locking IoT on vehicle')
        }
      }
    }
  } catch (error) {
    logger('An error occurred updating port status')
    logger('Error:', error)
  }
}

const addBikeToPort = (bikes, ports, fleetId) => {
  if (bikes.length) {
    for (let bike of bikes) {
      if (bike.bikeFleetId === fleetId) {
        for (let port of ports) {
          if (port.portVehicleUUID === bike.bikeUUID) {
            port.bike = { bikeId: bike.bikeId, bikeName: bike.bikeName }
            port.bike = { bikeId: bike.bikeId, bikeName: bike.bikeName }
            port.bikeId = bike.bikeId
          }
        }
      }
    }
  }
}

const formatHubsData = (hubs) => {
  const hubsMap = {}
  const formattedData = hubs.map((hubData) => {
    return {
      ...hubData.hubs,
      ports: [hubData.ports].filter((port) => port.portId),
      bikes: [hubData.bikes].filter((bike) => bike.bikeFleetId)
    }
  })
  for (let hub of formattedData) {
    if (hubsMap[hub.hubUUID] && hub.ports.length) {
      hubsMap[hub.hubUUID].ports = [
        ...hubsMap[hub.hubUUID].ports,
        ...hub.ports
      ]
      addBikeToPort(hub.bikes, hubsMap[hub.hubUUID].ports)
    } else {
      hubsMap[hub.hubUUID] = hub
      addBikeToPort(hubsMap[hub.hubUUID].bikes, hubsMap[hub.hubUUID].ports)
    }
  }
  const updatedHubs = Object.values(hubsMap).map((hubInfo) => {
    const newData = Object.assign({}, hubInfo)
    delete newData.bikes
    const ports = [
      ...new Map(hubInfo.ports.map((port) => [port['portId'], port])).values()
    ]
    newData.ports = ports
    return newData
  })
  return updatedHubs
}

const formatFleetsAndHubsData = (fleetsWithHubs) => {
  const data = fleetsWithHubs.map((fleetsWithHubsData) => {
    return {
      ...fleetsWithHubsData.hubs_and_fleets,
      hubs: [{ ...fleetsWithHubsData.hubs }].filter((hub) => hub.hubID),
      ports: [{ ...fleetsWithHubsData.ports }].filter((port) => port.portId),
      bikes: [{ ...fleetsWithHubsData.bikes }].filter(
        (bike) => bike.bikeFleetId
      )
    }
  })

  const fleetInfoMap = {}
  for (let fleetInfo of data) {
    if (fleetInfoMap[fleetInfo.fleetId]) {
      const dataMap = fleetInfoMap[fleetInfo.fleetId]
      dataMap['fleetId'] = fleetInfo.fleetId
      dataMap['hubs'] = [...dataMap['hubs'], ...fleetInfo.hubs].map((hub) => {
        if (fleetInfo.fleetHubUUID === hub.hubUUID) {
          return Object.assign(hub, {
            localHubStatus: fleetInfo.localHubStatus,
            make: fleetInfo.make,
            model: fleetInfo.model,
            image: fleetInfo.image,
            description: fleetInfo.description,
            type: fleetInfo.type,
            integration: fleetInfo.integration,
            equipmentId: fleetInfo.equipmentId,
            enclosureType: fleetInfo.enclosureType,
            dateCreated: (hub.dateCreated.toLocaleString()).slice(0, 10)
          })
        }
        return hub
      })
      dataMap['ports'] = [...dataMap['ports'], ...fleetInfo.ports]
      addBikeToPort(fleetInfo.bikes, dataMap['ports'], fleetInfo.fleetId)
      dataMap['localHubStatus'] = fleetInfo.localHubStatus
      fleetInfoMap[fleetInfo.fleetId] = dataMap
    } else {
      const data = {}
      data['fleetId'] = fleetInfo.fleetId
      data['localHubStatus'] = fleetInfo.localHubStatus
      data['hubs'] = [...fleetInfo.hubs].map((hub) => {
        if (fleetInfo.fleetHubUUID === hub.hubUUID) {
          return Object.assign(hub, {
            localHubStatus: fleetInfo.localHubStatus,
            make: fleetInfo.make,
            model: fleetInfo.model,
            image: fleetInfo.image,
            description: fleetInfo.description,
            type: fleetInfo.type,
            integration: fleetInfo.integration,
            equipmentId: fleetInfo.equipmentId,
            enclosureType: fleetInfo.enclosureType,
            dateCreated: (hub.dateCreated.toLocaleString()).slice(0, 10)
          })
        }
        return hub
      })
      data['ports'] = [...fleetInfo.ports]
      addBikeToPort(fleetInfo.bikes, data['ports'], fleetInfo.fleetId)
      fleetInfoMap[fleetInfo.fleetId] = data
    }
  }

  const mappedFleetInfoMap = Object.values(fleetInfoMap).map((data) => {
    let hubs = [
      ...new Map(data.hubs.map((item) => [item['hubUUID'], item])).values()
    ]
    for (let port of data.ports) {
      for (let hub of hubs) {
        if (hub.hubUUID === port.portHubUUID) {
          if (hub.ports) hub.ports = [...hub.ports, port]
          else hub.ports = [port]
        }
      }
    }
    delete data.ports
    delete data.localHubStatus
    delete data.localHubStatus
    return { ...data, hubs }
  })

  return mappedFleetInfoMap
}

const getAllHubs = async (fleetId, status, exclude) => {
  const filter = {}
  if (fleetId) filter['hubs_and_fleets.fleet_id'] = fleetId
  if (status) filter['hubs_and_fleets.status'] = status

  const allHubsSelection = [
    'hubs.hub_id as hubID',
    'hubs.status as remoteHubStatus',
    'name as hubName',
    'hubs.uuid as hubUUID',
    'hubs.created_at as dateCreated',
    'hubs.latitude',
    'hubs.longitude',
    'controllers.vendor as equipmentType',
    'controllers.key as equipmentKey',
    'controllers.controller_id as controllerId',
    'controllers.make as equipmentMake',
    'controllers.model as equipmentModel',
    'controllers.bike_id as controllerBikeId',
    'hubs_and_fleets.make',
    'hubs_and_fleets.model',
    'image',
    'type',
    'description',
    'integration',
    'ports.port_id as portId',
    'ports.uuid as portUUID',
    'ports.hub_uuid as portHubUUID',
    'ports.status as portStatus',
    'ports.vehicle_uuid as portVehicleUUID',
    'ports.equipment as port_equipment_key',
    'ports.locked as portLocked',
    'ports.port_qr_code as portQRCode',
    'ports.vehicle_assign as portVehicleAssign',
    'number as portNumber',
    'charging as portChargingStatus',
    'hubs_and_fleets.fleet_id',
    'bikes.fleet_id as bikeFleetId',
    'bikes.bike_uuid as bikeUUID',
    'bikes.bike_id as bikeId'
  ]

  try {
    if (fleetId || status) {
      let hubsAssignedToFleets = await db
        .main('hubs_and_fleets')
        .leftJoin('hubs', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
        .leftJoin('ports', 'ports.hub_uuid', 'hubs_and_fleets.hub_uuid')
        .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
        .leftJoin(
          'controllers',
          'controllers.controller_id',
          'hubs_and_fleets.equipment'
        )
        .options({ nestTables: true })
        .select(fleetsAndHubSelection)
        .where(filter)
      return formatFleetsAndHubsData(hubsAssignedToFleets)
    }

    let hubs = db
      .main('hubs')
      .leftJoin('ports', 'hubs.uuid', 'ports.hub_uuid')
      .leftJoin('hubs_and_fleets', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
      .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
      .leftJoin('controllers', 'controllers.controller_id', 'hubs_and_fleets.equipment')
      .options({ nestTables: true })
      .select(allHubsSelection)

    if (exclude) {
      hubs.whereNotIn(
        'hubs.uuid',
        db.main('hubs_and_fleets').where('fleet_id', exclude).select('hub_uuid')
      )
    }

    return formatHubsData(await hubs)
  } catch (error) {
    Sentry.captureException(error, {fleetId})
    logger('An error occurred getting list of hubs')
    console.log('Error:', error)
    throw new Error('An error occurred getting list of hubs')
  }
}

const getHubByUUID = async (hubUUID, fleetId) => {
  try {
    let hubData = await db
      .main('hubs_and_fleets')
      .leftJoin('hubs', 'hubs.uuid', 'hubs_and_fleets.hub_uuid')
      .leftJoin('ports', 'ports.hub_uuid', 'hubs.uuid')
      .leftJoin('bikes', 'bikes.bike_uuid', 'ports.vehicle_uuid')
      .leftJoin(
        'controllers',
        'controllers.controller_id',
        'hubs_and_fleets.equipment'
      )
      .options({ nestTables: true })
      .select(fleetsAndHubSelection)
      .where({
        'hubs_and_fleets.fleet_id': fleetId,
        'hubs_and_fleets.hub_uuid': hubUUID
      })
    let data = formatFleetsAndHubsData(hubData)[0]
    if (data && data.hubs) {
      data = Object.assign({}, data, data.hubs[0])
      delete data.hubs
    }

    const qrCode = await db
      .main('qr_codes')
      .where({ equipment_id: data.hubID, type: 'hub' })
      .first()
    data.qrCode = qrCode ? qrCode.code : null
    data.hubQrCode = qrCode ? qrCode.code : null // for backward compatibility
    if (data.equipmentId) {
      const ctrl = await db
        .main('controllers')
        .where({ controller_id: data.equipmentId })
        .first()
      data.equipment = ctrl
    }
    if (data.ports) {
      const promises = data.ports.map(async (port) => {
        let portEquipment = {}
        if (port.portEquipmentKey) {
          portEquipment = await db.main('controllers').where({ controller_id: port.portEquipmentKey }).first()
          if (portEquipment) port.equipment = portEquipment
        }
        let lastUser
        const date = await db
          .main('trips')
          .where({ port_id: port.portId })
          .max('date_created as latestDate')
          .first()
        const trip = await db
          .main('trips')
          .where({ port_id: port.portId, date_created: date.latestDate })
          .first()
        if (trip) {
          lastUser = await db
            .users('users')
            .where({ user_id: trip.user_id })
            .first()
          lastUser.completedTrip = trip.date_endtrip
        }
        return { ...port, lastUser: lastUser }
      })
      const response = await Promise.all(promises)
      data.ports = response
    }
    if (!data.enclosureType) {
      data.image =
        'https://hubs-and-fleets-images.s3.ca-central-1.amazonaws.com/default_open.png'
    } else {
      if (!data.image) {
        if (data.enclosureType === 'open') {
          data.image =
            'https://hubs-and-fleets-images.s3.ca-central-1.amazonaws.com/default_open.png'
        } else if (data.enclosureType === 'closed') {
          data.image =
            'https://hubs-and-fleets-images.s3.ca-central-1.amazonaws.com/default_closed.png'
        }
      }
    }

    return data
  } catch (error) {
    Sentry.captureException(error, {hubUUID, fleetId})
    logger('An error occurred getting the hub details')
    console.log('Error:', error)
    throw new Error(error)
  }
}

// Get a list of hubs within a certain gps locale radius 3km(3000m)
const getAdjacentHubs = async (
  fleetId,
  status = 'Available',
  hubRange = 3000
) => {
  // TODO: Get lat and lng from somewhere

  const lat = 37.806228
  const lng = -122.435853
  try {
    const hubs = await getAllHubs(fleetId)
    const adjacentHubs = []
    const vehicleLocation = {
      latitude: parseFloat(lat),
      longitude: parseFloat(lng)
    }

    const searchRange = hubRange
    for (let hub of hubs) {
      // Hub location
      const { latitude, longitude } = hub
      const hubLocation = {
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude)
      }
      const distance = pointHandler.distanceBetweenPoints(
        vehicleLocation,
        hubLocation
      )
      if (distance <= searchRange) {
        adjacentHubs.push(hub)
      }
    }
    return adjacentHubs
  } catch (error) {
    Sentry.captureException(error, {fleetId})
    logger('Error getting list of adjacent hubs')
    console.log('Error:', error)
    throw errors.customError(
      'Failed to fetch hubs',
      responseCodes.ResourceNotFound,
      'BadRequest',
      false
    )
  }
}

// Add an adapter to the controllers table.
// The data should contain the uuid(vehicle uuid) which we can use to get any other vehicle details.
const addAdapterToVehicle = async (data) => {
  try {
    const {
      payload: { uuid, k_guid: key }
    } = data
    const vehicle = await db.main('bikes').where({ bike_uuid: uuid }).first()
    if (!vehicle) {
      throw new Error('The vehicle with the specified uuid not found')
    } else {
      const { fleet_id, bike_id } = vehicle; // eslint-disable-line
      await db.main('controllers').insert({
        device_type: 'adapter',
        vendor: 'Kumhute',
        key,
        make: 'Kuhmute',
        fleet_id,
        bike_id,
        status: 'active'
      })
    }
  } catch (error) {
    Sentry.captureException(error, {data})
    logger('Error occurred adding adpater/controller to the vehicle')
    console.log('Error:', error)
    throw errors.customError(
      'Failed to fetch hubs',
      responseCodes.ResourceNotFound,
      'BadRequest',
      false
    )
  }
}

const getEquipmentStatus = async (vendor, equipmentKey, fleetId, portId) => {
  try {
    switch (vendor.value) {
      case 'Kisi': {
        const status = await new Kisi().fetchLock(
          fleetId,
          +equipmentKey
        )
        if (!status) throw new Error('An error occurred fetching Kisi equipment status')
        else return { ...status, vendor: vendor.value }
      }
      case 'dck-mob-e': {
        const data = await new Greenriders().getStationInfo(equipmentKey)
        if (!data) throw new Error('An error occurred fetching Greenriders equipment status')
        return { ...data, vendor: vendor.value }
      }
      default:
        throw new Error('Equipment vendor not supported')
    }
  } catch (error) {
    Sentry.captureException(error, {vendor, equipmentKey, fleetId, portId})
    throw error
  }
}

const retrievePort = async (portUUID) => {
  try {
    let result = {}
    const port = await db
      .main('ports')
      .where({ 'ports.uuid': portUUID })
      .first()

    if (port) {
      result = port
      const hub = await db
        .main('hubs')
        .where({ 'hubs.uuid': port.hub_uuid })
        .leftJoin('hubs_and_fleets', 'hubs_and_fleets.hub_uuid', 'hubs.uuid')
        .first()
      if (hub) {
        delete hub.image
        result.hub = hub
      }
      if (port.equipment) {
        const ctrl = await db.main('controllers').where({ controller_id: port.equipment }).first()
        try {
          const status = await getEquipmentStatus({ value: ctrl.vendor }, ctrl.key, hub.fleet_id, port.port_id)
          port.online = !status.cached
          if (status.vendor === 'dck-mob-e') {
            if (status.LockedScooterID) {
              const vehicle = await db.main('controllers')
                .where({ key: status.LockedScooterID })
                .leftJoin('bikes', 'bikes.bike_id', 'controllers.bike_id')
                .select('bikes.bike_uuid', 'key', 'bikes.bike_id', 'controller_id').first()
              await db.main('ports').update({ vehicle_uuid: vehicle.bike_uuid }).where({ uuid: portUUID, status: 'Unavailable' })
            }
          }
        } catch (error) {
          Sentry.captureException(error, {portUUID})
          // No op. We're not using this status. An error would break the port details screen
        }
        const portStatus = await db.main('ports').where({ 'ports.uuid': portUUID }).first()
        port.current_status = portStatus.current_status
        port.vehicle_uuid = portStatus.vehicle_uuid
        port.status = portStatus.status
        result.equipment = ctrl
      }
      if (port.vehicle_uuid) {
        const bike = await db
          .main('bikes')
          .where({ 'bikes.bike_uuid': port.vehicle_uuid })
          .first()
        if (bike) {
          result.bike = bike
        }
      }
    } else throw new Error('Port not found')
    return port
  } catch (error) {
    Sentry.captureException(error, {portUUID})
    logger('Error retrieving port information', error.message)
    throw errors.customError(
      error.message || 'Failed to fetch port',
      responseCodes.ResourceNotFound,
      'ResourceNotFound',
      false
    )
  }
}

const lockVehicleToGreenriderPorts = async (port, bike) => {
  try {
    const trip = await db
      .main('trips')
      .leftJoin('fleet_metadata', 'fleet_metadata.fleet_id', 'trips.fleet_id')
      .leftJoin('fleets', 'fleets.fleet_id', 'trips.fleet_id')
      .where((builder) => {
        builder.whereNotNull('trips.date_created').whereNull('trips.date_endtrip')
      })
      .andWhere({ 'trips.bike_id': bike.bike_id })
      .orderBy('trip_id', 'desc')
      .first()
    const hub = await db.main('hubs').where({ uuid: port.hub_uuid }).first()
    if (bike) {
      await db
        .main('bikes')
        .update({
          latitude: hub.latitude,
          longitude: hub.longitude
        })
        .where({ bike_id: bike.bike_id })

      await db
        .main('controllers')
        .update({
          latitude: hub.latitude,
          longitude: hub.longitude
        })
        .where({ bike_id: bike.bike_id })
    }

    if (trip) {
      let PNMessage = `Vehicle has Docked`
      let titleLocKey = 'end_your_ride'
      let bodyLocKey = 'your_vehicle_is_docked'
      let clickAction = 'docking'
      const hub = await db.main('hubs').where({ uuid: port.hub_uuid }).first()
      await db
        .main('bikes')
        .update({
          latitude: hub.latitude,
          longitude: hub.longitude
        })
        .where({ bike_uuid: port.vehicle_uuid })
      if (trip && !trip.date_endtrip && trip.smart_docking_stations_enabled) {
        PNMessage = `Vehicle has locked on port ${port.port_id}. Trip ${trip.trip_id} will be ended automatically`
        const tripDetails = {
          latitude: hub && hub.latitude,
          longitude: hub && hub.longitude,
          trip_id: trip.trip_id,
          fleet_id: trip.fleet_id,
          acl: { operator_id: trip.operator_id },
          source: 'Self End',
          port_id: port.port_id
        }
        await endTripOval(tripDetails)
        await db
          .main('trips')
          .update({ rating_shown: false })
          .where({ trip_id: trip.trip_id })
        titleLocKey = 'vehicle_is_docked'
        bodyLocKey = 'your_trip_was_ended_automatically'
        clickAction = 'docked'
      }
      const deviceToken = trip && trip.device_token
      if (deviceToken) {
        const message = {
          notification: {
            title: 'Vehicle Locked to Port',
            body: PNMessage,
            title_loc_key: titleLocKey,
            body_loc_key: bodyLocKey,
            sound: 'default',
            click_action: clickAction
          },
          data: {
            trip_id: trip.trip_id.toString()
          }
        }

        const options = {
          priority: 'high',
          timeToLive: 60 * 60 * 24,
          'contentAvailable': true
        }
        sendPushNotification(deviceToken, message, options)
        console.log('PN Send.....')
        logger(`Info::PN send for trip ${trip.trip_id.toString()}`)
      }
    }
  } catch (error) {
    Sentry.captureException(error, {fn: 'lockVehicleToGreenriderPorts', ...port})
    logger(`🚀 ~An error occurred smart docking Greenriders port ${port.port_id}`, error)
  }
}

const unlockPort = async (port) => {
  const vendor = port.controller && port.controller.vendor
  switch (vendor) {
    case 'ParcelHive':
      const user = await bikeFleetHandler.setUpApiUser(port.controller, vendor)
      const lockerId = JSON.parse(port.controller.metadata).lockerId
      console.log('lockerId', lockerId)
      const parcelHiveApiClient = new ParcelHiveApiClient({
        accessToken: user.accessToken,
        lockerId: JSON.parse(port.controller.metadata).lockerId,
        boxId: JSON.parse(port.controller.metadata).boxId
      })
      const unlockResponse = await parcelHiveApiClient.open(port.controller)
      return unlockResponse
    default:
      break
  }
}

module.exports = {
  createHub,
  updateHub,
  getVehicleDetails,
  updateVehicleDockingStatus,
  updatePort,
  getAllHubs,
  getHubByUUID,
  updateHubFleet,
  getAdjacentHubs,
  addAdapterToVehicle,
  retrievePort,
  getEquipmentStatus,
  lockVehicleToGreenriderPorts,
  unlockPort
}
