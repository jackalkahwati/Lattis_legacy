const express = require('express')
const router = express.Router()
const platform = require('@velo-labs/platform')
const logger = platform.logger
const integrationHandler = require('./../model_handlers/integration-handler')
const _ = require('underscore')
const db = require('../db')
const responseCodes = platform.responseCodes
const errors = platform.errors
const jsonResponse = platform.jsonResponse
const { generateToken } = require('../utils/auth')
const axios = require('axios')
const { updateVehicleDockingStatus, lockVehicleToGreenriderPorts } = require('../model_handlers/integration-handler')
const { lockOrUnlockBike } = require('../model_handlers/bike-fleet-handler')
const { v4: uuidv4 } = require('uuid')
const { Kisi } = require('../utils/kisi')
const { updateKisiLockStatus, updateSegwayBikeStatus, segwayApiClientForUs, segwayApiClientForEu } = require('./../model_handlers/bike-fleet-handler')
const Greenriders = require('../utils/greenriders')
const { SegwayApiClient } = require('../utils/segway-api')
const sendPushNotification = require('../utils/firebase-messaging')
const { uploadBase64ImageFile } = require('../utils/file-upload')
const passwordHandler = require('../utils/password-handler')

const greenriders = new Greenriders()

const platformConfig = platform.config

const { GPS_TRACKING_SERVICE: GPS_SERVICE_URL, DUCKT_SENDER_IP } = process.env

const duckTHandler = async (req) => {
  try {
    console.log('DuckT Request::', req.body)
    const {
      status,
      port,
      vehicle
    } = req.body
    const bikeId = +vehicle
    const bike = await db.main('bikes').where({ bike_id: bikeId }).first()
    const ioTController = await db.main('controllers').where({ bike_id: bikeId, device_type: 'iot' }).first()
    if (status === 'LOCKED') {
      logger('DuckT Vehicle Locked')
      await db.main('ports').update({ vehicle_uuid: bike.bike_uuid }).where({ uuid: port })
      try {
        if (ioTController) await lockOrUnlockBike(bikeId, 'lock')
      } catch (error) {
        logger('An error occured locking IoT on bike after DuckT lock')
      }
      await updateVehicleDockingStatus(bike.bike_uuid)
    } else if (status === 'UNLOCKED') {
      logger('DuckT Vehicle Unlocked')
      await db.main('ports').update({ vehicle_uuid: null, current_status: 'parked' }).where({ uuid: port })
    }
  } catch (error) {
    logger('Error:::', JSON.stringify(error))
  }
}

const formatAndSendNotificationMessage = async (portId, action) => {
  const latestTrip = await db
    .main('trips')
    .where({ port_id: portId })
    .orderBy('date_created', 'DESC')
    .first()
  if (latestTrip && !latestTrip.date_endtrip && action) {
    const message = {
      notification: {
        title: 'Greenriders Station',
        body: 'Port status changed',
        title_loc_key: action,
        body_loc_key: action,
        sound: 'default',
        click_action: action
      },
      data: {
        trip_id: `${latestTrip.trip_id}`
      }
    }
    const options = {
      contentAvailable: true,
      priority: 'high'
    }
    await sendPushNotification(latestTrip.device_token, message, options)
    logger(`Info::PN send for ${action} for trip ${latestTrip.trip_id}`)
  }
}

router.post('/greenriders', async (req, res) => {
  try {
    if (!(_.has(req.headers, 'x-api-client') && _.has(req.headers, 'x-api-key'))) {
      logger('Error: some required header params are missing(client, x-api-key)')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const { 'x-api-client': client, 'x-api-key': apiKey } = req.headers
    await authorizeClient({ client, apiKey })
    const { body } = req
    const type = body.type
    let status
    let message
    let port
    if (type === 'scooter.info') {
      const scooterId = body.scooterId
      const scooterInfo = {
        'Cmd': 'ScooterInfo',
        'ScooterId': scooterId
      }
      const controller = await db.main('controllers').where({ key: scooterId }).first()
      if (!controller) return res.status(404).json({ msg: 'The scooter is not registered in our system', error: true })
      const qrCode = await db.main('qr_codes').where({ type: 'bike', equipment_id: controller.bike_id }).first()
      scooterInfo.QRCOde = qrCode || null
      if (['Segway IoT EU', 'Segway'].includes(controller.vendor)) {
        let segwayApi
        if (controller.vendor === 'Segway') {
          segwayApi = segwayApiClientForUs
        } else if (controller.vendor === 'Segway IoT EU') {
          segwayApi = segwayApiClientForEu
        }
        updateSegwayBikeStatus(segwayApi, controller.bike_id, controller)
        const status = await segwayApi.getVehicleInformation(controller.key)
        scooterInfo.BatLevel = status.powerPercent || null
      }
      if (controller.bike_id && !scooterInfo.BatLevel) {
        const bike = await db.main('bikes').where({ bike_id: controller.bike_id }).first()
        scooterInfo.BatLevel = bike.bike_battery_level || controller.battery_level || 0
      }

      return res.json(scooterInfo)
    }

    if (type === 'station.info') {
      const station = body.data
      const location = { latitude: station.Latitude, longitude: station.Longitude }
      if (station.LockedScooterID) {
        const bikeCtrl = await db.main('controllers').where({ key: station.LockedScooterID }).first()
        if (bikeCtrl && bikeCtrl.bike_id) {
          const bike = await db.main('bikes').where({ bike_id: bikeCtrl.bike_id }).first()
          db.main('bikes').update(location).where({ bike_id: bikeCtrl.bike_id })
          const stationCtrl = await db.main('controllers').where({ key: station.StationUID }).first()
          if (stationCtrl) {
            const portData = await db.main('ports').where({ equipment: stationCtrl.controller_id }).first()
            if (port) {
              await db.main('ports').update({ vehicle_uuid: bike.bike_uuid, status: 'Unavailable' }).where({ port_id: port.port_id })
              db.main('hubs').update(location).where({ uuid: portData.hub_uuid }) // Update hub coordinates
            }
            await lockVehicleToGreenriderPorts(portData, bike)
          }
        }
      } else if (!station.LockedScooterID) {
        const stationCtrl = await db.main('controllers').where({ key: station.StationUID }).first()
        if (stationCtrl) await db.main('ports').update({ status: 'Available', vehicle_uuid: null, current_status: 'parked' }).where({ port_id: stationCtrl.port_id })
        const portData = await db.main('ports').where({ equipment: stationCtrl.controller_id }).first()
        db.main('hubs').update(location).where({ uuid: portData.hub_uuid }) // Update hub coordinates
      }
      return res.json({ msg: 'Request Acknowledged' })
    }

    const opData = body && body.data && body.data.Attributes
    if (!opData) {
      return res.status(400).json({ message: 'Missing body data' })
    }
    const stationUID = opData.StationUID
    const stationCtrl = await db.main('controllers').where({ key: stationUID }).first()
    if (stationCtrl) {
      port = await db.main('ports').where({equipment: stationCtrl.controller_id}).first()
    } else {
      return res.json({ status: status || 'OK', data: req.body, message: message || 'Data delivered' })
    }
    switch (type) {
      case 'station.lock': {
        const vehicleController = await db.main('controllers').where({ key: opData.LockedScooterID }).first()
        if (!opData.LockedScooterID) {
          await db.main('ports').update({ status: 'Available', current_status: 'parked', vehicle_uuid: null }).where({port_id: port.port_id})
          return
        }
        if (opData.LockedScooterID && !vehicleController) {
          await db.main('ports').update({ status: 'Unavailable', vehicle_uuid: null }).where({port_id: port.port_id})
          return
        }
        if (['Segway', 'Segway IoT EU'].includes(vehicleController.vendor)) { // In case it's a Segway attached
          if (vehicleController.vendor === 'Segway') {
            await SegwayApiClient.lock(opData.LockedScooterID)
          } else if (vehicleController.vendor === 'Segway IoT EU') {
            await segwayApiClientForEu.lock(opData.LockedScooterID)
          }
        }
        let bike
        if (stationCtrl) {
          bike = await db.main('bikes').where({ bike_id: vehicleController.bike_id }).first()
          if (port && bike) await db.main('ports').update({ vehicle_uuid: bike.bike_uuid, status: 'Unavailable' }).where({port_id: port.port_id})
        }
        if (stationCtrl && port && (port.current_trip_id || bike.current_trip_id)) await lockVehicleToGreenriderPorts(port, bike)
        break
      }
      case 'station.unlock':
        const vehicleController = await db.main('controllers').where({ key: opData.LockedScooterID }).first()
        if (!opData.LockedScooterID) {
          await db.main('ports').update({ status: 'Available', current_status: 'parked', vehicle_uuid: null }).where({port_id: port.port_id})
          return
        }
        if (opData.LockedScooterID && !vehicleController) {
          await db.main('ports').update({ status: 'Unavailable', current_status: 'parked', vehicle_uuid: null }).where({port_id: port.port_id})
          return
        }
        if (['Segway', 'Segway IoT EU'].includes(vehicleController.vendor)) { // In case it's a Segway attached
          if (vehicleController.vendor === 'Segway') {
            await SegwayApiClient.unlock(opData.LockedScooterID)
          } else if (vehicleController.vendor === 'Segway IoT EU') {
            await segwayApiClientForEu.unlock(opData.LockedScooterID)
          }
        }
        if (port) await db.main('ports').update({ vehicle_uuid: null, status: 'Available', current_status: 'parked' }).where({port_id: port.port_id})
        await formatAndSendNotificationMessage(port.port_id, 'port_unlocked')
        break
      default:
        status = 'Failed'
        message = 'Invalid Type'
        break
    }
    res.json({ status: status || 'OK', data: req.body, message: message || 'Data delivered' })
  } catch (error) {
    console.log('An error occured::', JSON.stringify(error))
    res.json({ status: 'OK', data: req.body, message: 'Request received' })
  }
})

// Hook endpoint to get Kuhmute events. We'll use a single endpoint and use the event param to identify the next action
router.post('/webhooks', async (req, res) => {
  try {
    let originIps = req.headers['x-forwarded-for']
    let isOriginIpsValid = originIps && Array.isArray(originIps.split(',')) && originIps.split(',').includes(DUCKT_SENDER_IP)
    if (isOriginIpsValid) {
      // This logic is for DuckT events based on the source IP address.
      duckTHandler(req)
      return res.json({ 'msg': 'Handled...' })
    }
    const { type } = req.body
    if (!(_.has(req.headers, 'x-api-client') && _.has(req.headers, 'x-api-key'))) {
      logger('Error: some required header params are missing(client, x-api-key)')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const { 'x-api-client': client, 'x-api-key': apiKey } = req.headers
    await authorizeClient({ client, apiKey })

    /* Event types
    ● hub.update
    ● hub.create
    ● port.update
    ● vehicle.update
    ● vehicle.create
    ● Vehicle.maintenance
    ● vehicle.docked
  */
    switch (type) {
      case 'hub.create':
        logger('Creating hub:::', JSON.stringify(req.body))
        await integrationHandler.createHub(req.body)
        res.end()
        break

      case 'hub.update':
        logger('Updating hub:::', JSON.stringify(req.body))
        await integrationHandler.updateHub(req.body)
        res.end()
        break

      /*       case 'vehicle.docked':
      logger('Updating vehicle status to parked:::', JSON.stringify(req.body))
      await integrationHandler.updateVehicleDockingStatus(req.body)
      res.end()
      break */

      case 'port.update':
        logger('Updating port status:::', JSON.stringify(req.body))
        await integrationHandler.updatePort(req.body)
        res.end()
        break

      case 'vehicle.create':
        logger('Adding adapter to vehicle:::', JSON.stringify(req.body))
        await integrationHandler.addAdapterToVehicle(req.body)
        res.end()
        break

      default:
        logger(`Hook for event ${type} not implemented`)
    }
  } catch (error) {
    logger(`Error handling hook events`)
    res.status(500).json({ 'error': error.message || 'An error occurred handling the event' })
  }
})

const authorizeClient = async (data) => {
  const { client, apiKey } = data
  const clientAPIData = await db.users('api_keys').where({ 'issued_to': client, key: apiKey }).first()
  if (!clientAPIData) throw new Error('Invalid credentials')
  else return clientAPIData
}

// Allow access to vehicles by third party
// TODO: Auth :- do we need an extra role or another more gereral access for external integration
router.get('/vehicles', async (req, res) => {
  try {
    if (!(_.has(req.headers, 'x-api-client') && _.has(req.headers, 'x-api-key'))) {
      logger('Error: some required header params are missing(client, x-api-key)')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const { 'x-api-client': client, 'x-api-key': apiKey } = req.headers
    await authorizeClient({ client, apiKey })
    const qrCodes = req.body.qrCodes
    if (Array.isArray(qrCodes) && qrCodes.length) {
      const qrCodes = req.body.qrCodes
      const vehicleDetails = await integrationHandler.getVehicleDetails(qrCodes)
      res.send(vehicleDetails)
    } else {
      logger('Error: vehicle  qrCodes were not provided')
      jsonResponse(res, responseCodes.BadRequest, 'Error: Vehicle  qrCodes were not provided', null)
    }
  } catch (error) {
    console.log('error', error)
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), { 'error': error.message })
  }
})

router.get('/hubs/:uuid', async (req, res) => {
  try {
    if (!(_.has(req.query, 'fleet_id') && _.has(req.params, 'uuid'))) {
      logger('Error: some required parameters are missing')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }
    const data = await integrationHandler.getHubByUUID(req.params.uuid, req.query.fleet_id)
    jsonResponse(res, responseCodes.OK, errors.noError(), { data })
  } catch (error) {
    logger('An error occurred retrieving the hub')
    jsonResponse(res, responseCodes.InternalServer, error.message, null)
  }
})

router.put('/hubs/:uuid', async (req, res) => {
  try {
    if (!(_.has(req.body, 'fleet_id') && _.has(req.params, 'uuid'))) {
      logger('Error: some required parameters are missing(uuid, fleet_id)')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
      return
    }
    const moveBikesToLive = +req.query.moveBikes || 0
    const data = await integrationHandler.updateHubFleet(req.params.uuid, req.body, !!moveBikesToLive)
    jsonResponse(res, responseCodes.OK, errors.noError(), { data })
  } catch (error) {
    logger('An error occurred updating hubs')
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.formatErrorForWire(error), { error: error.message || null })
  }
})

router.post('/hubs/', async (req, res) => {
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
      // eslint-disable-next-line camelcase
      qr_code = undefined,
      portsNumber = undefined,
      enclosureType = undefined,
      equipmentKey = undefined,
      lockerId = undefined,
      boxId = undefined
    } = req.body

    const hubUUID = `hub_${uuidv4()}`
    const { link } = image ? await uploadBase64ImageFile(image,
      `${hubUUID}-${passwordHandler.randString(10)}`,
      platformConfig.aws.s3.fleetDetails.bucket) : { link: undefined }

    await db.main.transaction(async (trx) => {
      const [inserted] = await trx('hubs').insert({
        name,
        latitude,
        longitude,
        uuid: hubUUID,
        status: 'Available'
      })

      let controller
      if (!lockerId && equipmentKey) {
        controller = await trx('controllers').insert({
          latitude,
          longitude,
          fleet_id,
          key: equipmentKey,
          device_type: 'lock',
          vendor: 'Sas'
        })
      } else if (lockerId && boxId && equipmentKey) {
        controller = await trx('controllers').insert({
          latitude,
          longitude,
          fleet_id,
          key: equipmentKey,
          device_type: 'iot',
          vendor: 'ParcelHive',
          metadata: JSON.stringify({lockerId: lockerId, boxId: boxId})
        })
      }
      await trx('qr_codes').insert({
        code: qr_code,
        type: 'hub',
        equipment_id: inserted
      })
      await trx('hubs_and_fleets').insert({
        fleet_id,
        status,
        make,
        model,
        image: link,
        type,
        description,
        integration,
        qr_code,
        hub_id: inserted,
        hub_uuid: hubUUID,
        number_of_ports: portsNumber,
        open_or_close: enclosureType,
        equipment: controller ? [controller] : null
      })
      for (let i = 1; i <= portsNumber; i++) {
        await trx('ports').insert({
          uuid: `port_${uuidv4()}`, hub_uuid: hubUUID, status: 'Available', current_status: 'parked', number: i
        })
      }
    })

    const hub = await db.main('hubs').where({ uuid: hubUUID }).first()
    jsonResponse(res, responseCodes.OK, errors.noError(), hub)
  } catch (error) {
    logger('An error occurred adding a new hubs')
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.formatErrorForWire(error), { error: error.message || null })
  }
})

router.get('/hubs', async (req, res) => {
  try {
    const fleetId = req.query.fleet_id
    const status = req.query.status
    const exclude = req.query.exclude_fleet
    const type = req.query.type
    let data
    if (type && type === 'adjacent') {
      data = await integrationHandler.getAdjacentHubs(fleetId)
    } else data = await integrationHandler.getAllHubs(fleetId, status, exclude)
    jsonResponse(res, responseCodes.OK, errors.noError(), { data })
  } catch (error) {
    logger('An error occurred retrieving hubs')
    console.log('error', error)
    jsonResponse(res, responseCodes.InternalServer, errors.internalServer(true), null)
  }
})

// Get status about the bike
router.get('/grow/:bikeId', async (req, res) => {
  try {
    if (!_.has(req.params, 'bikeId')) {
      logger('Error: some required parameters are missing')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }
    const controller = await db.main('controllers').where({ 'bike_id': req.params.bikeId }).first()
    if (!controller) throw new Error('No controller for the bike Id')
    const token = generateToken()
    const response = await axios.get(`${GPS_SERVICE_URL}/vehicles/${controller.key}`, {
      headers: {
        'x-access-token': token
      }
    })
    jsonResponse(res, responseCodes.OK, errors.noError(), response.data)
  } catch (error) {
    logger(`An error occurred getting GROW vehicle data`)
    jsonResponse(res, responseCodes.InternalServer, error.message || errors.internalServer(true), null)
  }
})

router.get('/ports/:uuid', async (req, res) => {
  try {
    if (!_.has(req.params, 'uuid')) {
      logger('Error: some required parameters are missing')
      jsonResponse(res, responseCodes.BadRequest, errors.missingParameter(true), null)
    }
    const port = await integrationHandler.retrievePort(req.params.uuid)
    let qrCodeInfo
    if (port) {
      qrCodeInfo = await db.main('qr_codes').where({ equipment_id: port.port_id, 'type': 'port' }).first()
      if (qrCodeInfo) {
        port.port_qr_code = qrCodeInfo.code
      }
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), port)
  } catch (error) {
    logger(`An error occurred getting port information`)
    jsonResponse(res, responseCodes.InternalServer, error.message || { message: 'An error occurred getting port information' }, null)
  }
})

router.put('/ports/:uuid', async (req, res) => {
  try {
    const { body } = req
    const { operation, vendor, equipmentVendor, portQrCode, equipmentKey, fleetId, equipmentId, equipmentType, portId, lockerId, boxId } = body
    if (operation === 'updatingPort') {
      const isQRCodeSet = await db.main('qr_codes').where({ code: portQrCode }).first()

      // Upsert operation
      if (isQRCodeSet && isQRCodeSet.equipment_id !== +portId) { // this equipment_id refers to the port Id not controller
        throw new Error('The QR code already exists')
      } else if (!isQRCodeSet) {
        const isPortInQrCodeTable = await db.main('qr_codes').where({ equipment_id: portId, 'type': 'port' }).first()
        if (isPortInQrCodeTable) await db.main('qr_codes').update({ code: portQrCode }).where({ type: equipmentType, equipment_id: +portId })
        else await db.main('qr_codes').insert({ code: portQrCode, type: equipmentType, equipment_id: +portId })
      }
    } else {
      if (['addingEquipment', 'updatingEquipment'].includes(operation)) {
        if (!vendor || !equipmentKey) throw new Error('Vendor or equipment key is missing')
      }
      let position
      if (vendor === 'Kisi') {
        const status = await integrationHandler.getEquipmentStatus(equipmentVendor, equipmentKey, fleetId, portId)
        if (!status) throw new Error('Failed to get equipment status')
        position = {
          longitude: status.longitude ||
            status.place.longitude ||
            null,
          latitude: status.latitude ||
            status.place.latitude ||
            null
        }
      }

      let metadata = ''
      if (vendor === 'ParcelHive') {
        metadata = JSON.stringify({lockerId: lockerId, boxId: boxId})
      }

      if (operation === 'addingEquipment') {
        if (vendor === 'dck-mob-e') {
          const clientId = `${equipmentKey}-${body.password}`
          await new Greenriders().registerNewStation(equipmentKey, body.password, clientId)
          await new Greenriders().getStationInfo(equipmentKey)
        }
        // Only update DB if requests to Greenriders IoT server are successful
        const [insertId] = await db.main('controllers').insert({ vendor: vendor, key: equipmentKey, equipment_type: 'port', port_id: portId, fleet_id: fleetId, device_type: 'iot', ...position, metadata: metadata })
        // Todo: Update port lock status too
        const updates = { equipment: insertId }
        await db.main('ports').where({ uuid: req.params.uuid }).update(updates)
      } else if (operation === 'editingEquipment') {
        await db.main('controllers').where({ controller_id: equipmentId }).update({ key: equipmentKey, equipment_type: 'port', port_id: portId, vendor: vendor, fleet_id: fleetId, device_type: 'iot', ...position, metadata: metadata })
        const updates = {}

        if (!_.isEmpty()) {
          await db.main('ports').where({ port_id: portId }).update(updates)
        }
      }
      return jsonResponse(res, responseCodes.OK, errors.noError(), { message: `${operation} successful. Lock is online` })
    }
    jsonResponse(res, responseCodes.OK, errors.noError(), { message: `${operation} successful` })
  } catch (error) {
    if (error.code && error.code === 'ER_DUP_ENTRY') {
      return jsonResponse(res, responseCodes.InternalServer, errors.customError('The equipment is already assigned to a different device', responseCodes.InternalServer, 'DuplicateKey', true), null)
    }
    logger(`An error occurred updating port information`)
    return jsonResponse(res, responseCodes.InternalServer, errors.customError(error.message || 'An error occurred updating port information', responseCodes.InternalServer, 'DuplicateKey', true), null)
  }
})

router.post('/ports/admin', async (req, res) => {
  const { action, vendor, key: stationUID, portId } = req.body // action can be book, reset or maintain
  if (vendor !== 'dck-mob-e') {
    throw new Error('This operation is only available for Greenriders equipment')
  }
  let data = null
  switch (action) {
    case 'book':
      data = await greenriders.bookStation(stationUID)
      break
    case 'reload':
      data = await greenriders.getStationInfo(stationUID)
      if (data.cached) throw new Error('The station seems offline')
      if (data.LockedScooterID) {
        const ctrl = await db.main('controllers').where({ key: data.LockedScooterID })
          .leftJoin('bikes', 'bikes.bike_id', 'controllers.bike_id')
          .options({ nestTables: true }).first()

        await db.main('ports').update({ status: 'Unavailable', current_status: 'on_trip', vehicle_uuid: ctrl.vehicle_uuid }).where({port_id: portId})
        if (ctrl) {
          if (ctrl.bikes) {
            ctrl.bike = ctrl.bikes
            delete ctrl.bikes
          }
          data = ctrl
        }
      } else if (!data.LockedScooterID) {
        await db.main('ports').update({ status: 'Available', current_status: 'parked', vehicle_uuid: null }).where({port_id: portId})
      }
      const port = await db.main('ports').where({ port_id: portId })
      if (!data) data = { port }
      else data = { ...data, port }
      break
    case 'reset':
      data = await greenriders.resetStation(stationUID)
      return jsonResponse(res, responseCodes.OK, errors.noError(), { message: 'Reset Operation Send', data })
    case 'maintain':
      data = await greenriders.setStationToMaintenance(stationUID)
      break
    default:
      throw new Error('Invalid admin action')
  }
  return jsonResponse(res, responseCodes.OK, errors.noError(), { message: 'Operation successful', data })
})

router.post('/ports/:uuid/unlock', async (req, res) => {
  try {
    const portUUID = req.params.uuid
    const port = await db.main('ports').where({ uuid: portUUID }).first()
    if (!port) throw new Error('Port not found')
    if (!port.equipment) throw new Error('Port does not have an equipment')
    if (port && port.equipment) {
      const controller = await db.main('controllers').where({ controller_id: port.equipment }).first()
      if (!controller) throw new Error('Controller not found')
      switch (controller.vendor) {
        case 'Kisi':
          const unlockResponse = await new Kisi().unlock(controller.fleet_id, controller.key)
          if (unlockResponse.error || !unlockResponse) throw new Error(unlockResponse.error)
          await updateKisiLockStatus(undefined, controller)
          jsonResponse(res, responseCodes.OK, errors.noError(), unlockResponse)
          return
        case 'dck-mob-e': {
          const data = await greenriders.getStationInfo(controller.key)
          if (data.cached) throw new Error('The station seems offline')
          if (!data.LockedScooterID) {
            await db.main('ports').update({ status: 'Available', current_status: 'parked', vehicle_uuid: null }).where({port_id: port.port_id})
            throw new Error('The station does not have a vehicle locked')
          } else {
            const response = await greenriders.unlockStation(controller.key, data.LockedScooterID)
            await db.main('ports').update({ current_status: 'parked', status: 'Available', vehicle_uuid: null }).where({ uuid: portUUID })
            jsonResponse(res, responseCodes.OK, errors.noError(), response)
            return
          }
        }
        case 'ParcelHive':
          port.controller = controller
          const response = await integrationHandler.unlockPort(port)
          jsonResponse(res, responseCodes.OK, errors.noError(), response)
          break
        default:
          throw new Error('Vendor not supported')
      }
    }
  } catch (error) {
    logger(`An error occurred unlocking vehicle from station`, error.message)
    return jsonResponse(res, responseCodes.InternalServer, errors.customError(error.message || 'An error occurred unlocking the port', responseCodes.InternalServer, 'UnlockingError', true), null)
  }
})

module.exports = router
