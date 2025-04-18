/** @format */

const mqtt = require("mqtt");
const express = require("express");
const growRouter = express.Router();
const { BikeAndControllerModel, TripModel, FleetMetadataModel, TicketModel } = require("../database/models");
const { logger, isOutofGeofence, calcCrowDistance, checkAndCreateTheftTicket } = require("../utils");
const moment = require("moment");

const { GROW_SERVICE_URL, GROW_USERNAME, GROW_PASSWORD } = process.env;

const client = mqtt.connect(GROW_SERVICE_URL, {
  username: GROW_USERNAME,
  password: GROW_PASSWORD
});

const updateGrowSettings = async (trip, deviceId, heartbeatData, growAlarmOn) => {
  try {
    const newSteps = [
      heartbeatData["position.latitude"],
      heartbeatData["position.longitude"],
      moment().unix()
    ];
    if (newSteps[0] === 0 || newSteps[1] === 0) {
      console.log(`🚀 ~ Device ${ deviceId } might not be responding ${ newSteps }. Coordinates not logged...`);
      return;
    }
    if (heartbeatData) {
      await TripModel.updateOne(
        {
          trip_id: trip.trip_id
        },
        {
          $push: {
            steps: [newSteps]
          }
        }
      );
    }
    const fleetGeoStatus = await FleetMetadataModel.findOne({ fleet_id: trip.fleet_id }).exec();
    if (fleetGeoStatus && fleetGeoStatus.geofenseEnabled) {
      const outOfGeo = await isOutofGeofence(trip.fleet_id, [newSteps[0], newSteps[1]]);
      if (outOfGeo && !growAlarmOn) {
        executeCommand(`s/c/${ deviceId }`, "57,1");
        executeCommand(`s/c/${ deviceId }`, "50,1");
        await BikeAndControllerModel.updateOne({ controller_key: deviceId }, { growAlarmOn: true });
      }
      if (!outOfGeo && growAlarmOn) {
        executeCommand(`s/c/${ deviceId }`, "50,0");
        await BikeAndControllerModel.updateOne({ controller_key: deviceId }, { growAlarmOn: false });
      }
    }
  } catch (error) {
    logger.error("Error::", error.message || error);
  }
};

growRouter.post("/heartbeat", async (req, res) => {
  try {
    const { topic, message: heartbeatData } = req.body;
    res.json({ msg: `Data received for device ${topic}` });
    const currentLocation = {
      latitude: heartbeatData["position.latitude"],
      longitude: heartbeatData["position.longitude"],
    };
    const deviceId = topic.split("/").slice(-1)[0];
    const updateData = {
      position: currentLocation,
      battery: heartbeatData.batsco,
      meta: {
        conexion: heartbeatData.conexion,
        timestamp: heartbeatData.timestamp,
        altitude: heartbeatData["position.altitude"],
        speed: heartbeatData["position.speed"],
        hdop: heartbeatData["position.hdop"],
        batiot: heartbeatData.batiot,
        batsco: heartbeatData.batsco,
        batext: heartbeatData.batext,
        speedsco: heartbeatData.speedsco,
        coderr: heartbeatData.coderr,
        rssi: heartbeatData.rssi,
        "4g2g": heartbeatData["4g2g"],
        qr: heartbeatData.qr,
        boolean: heartbeatData.boolean,
        ...currentLocation
      }
    };

    const bike = await BikeAndControllerModel.findOneAndUpdate({ controller_key: deviceId }, updateData, { new: true }).lean();
    const fleetId = bike && bike.fleet_id;
    const currentTrip = bike && bike.current_trip;
    const tripData = { trip_id: currentTrip, fleet_id: fleetId };
    const growAlarmOn = !!(bike && bike.growAlarmOn);
    if (currentTrip) {
      await updateGrowSettings(tripData, deviceId, heartbeatData, growAlarmOn);
    } else if (!currentTrip && bike.mainStatus === "active") {
      const lastValidLocation = bike.lastValidLocation;
      const steps = [currentLocation.latitude, currentLocation.longitude];
      const distanceBetween = calcCrowDistance([steps[0], steps[1]], [lastValidLocation.latitude, lastValidLocation.longitude]);
      if (distanceBetween > 100) {
        checkAndCreateTheftTicket(bike, TicketModel);
      }
    }
  } catch (error) {
    logger.error(error);
  }
});

// Create ticket for scooters when they receive 'Burglar alarm' || '30hkm+ alarm' messages
growRouter.post("/create-theft-ticket", async (req, res) => {
  try {
    console.log("<Theft ticket body>", req.body);
    const { topic } = req.body;
    res.json({ msg: `Theft data received for ${topic}` });
    const deviceId = topic.split("/").slice(-1)[0];
    const bike = await BikeAndControllerModel.findOne({ controller_key: deviceId }).lean();
    const currentTrip = bike && bike.current_trip;
    if(!currentTrip && bike.mainStatus === "active") {
      checkAndCreateTheftTicket(bike, TicketModel);
    }
  } catch (error) {
    logger.error(error);
  }
});

growRouter.put("/alarm-status", async (req, res) => {
  try {
    const { topic, message } = req.body;
    res.json({ msg: `Alarm status update received for ${topic}` });
    const deviceId = topic.split("/").slice(-1)[0];
    await BikeAndControllerModel.updateOne({ controller_key: deviceId }, { growAlarmOn: message });
  } catch (error) {
    logger.error(error);
  }
});

const getCoordinates = (data) => {
  const splitData = data.split(",");
  const gpsInfo = [...splitData.slice(3, 5)];
  const gpSObject = {};
  for (let key of gpsInfo) {
    let removeQuotes = key.replace(/"/g, "");
    removeQuotes = removeQuotes.split(":");
    gpSObject[removeQuotes[0].toString()] = +removeQuotes[1].trim();
  }
  return { latitude: gpSObject["position.latitude"], longitude: gpSObject["position.longitude"], timestamp: moment().unix() };
};

// Update the lastValidLocation when the vehicle goes live
growRouter.post("/send-to-live", async (req, res) => {
  try {
    console.log("<Sending vehicle to live>", req.body);
    const { topic, message } = req.body;
    res.json({ msg: `Send to live data received for ${topic}` });
    const deviceId = topic.split("/").slice(-1)[0];
    const coordinates = getCoordinates(message);
    await BikeAndControllerModel.updateOne({ controller_key: deviceId }, { lastValidLocation: coordinates });
  } catch (error) {
    logger.error(error);
  }
});

const getDeviceIds = async () => {
  try {
    const growVehicles = await BikeAndControllerModel.find({
      vendor: "Grow"
    }).exec();
    const vehicles = growVehicles.map((device) => device.controller_key);
    return vehicles;
  } catch (error) {
    logger.error(error);
  }
};

client.on("connect", async () => {
  try {
    const deviceIds = await getDeviceIds();
    for (let deviceId of deviceIds) {
      client.subscribe(`s/h/17/${ deviceId }`);
      client.subscribe(`s/a/17/${ deviceId }`);
    }
  } catch (error) {
    logger.error(error);
  }
});

const executeCommand = async (topic, command) => {
  try {
    client.publish(topic, command, (error) => {
      if (error) {
        logger.error(
          `An error occurred executing the command ${ command } on topic ${ topic }`
        );
        throw new Error(error);
      } else {
        logger.info(`Command ${ command } to topic ${ topic } executed successfully`);
      }
    });
  } catch (error) {
    logger.error({
      source: "Grow Heartbeat",
      message: error.message || "An error occurred updating Grow device from heartbeat",
      trace: error
    });
  }
};

module.exports = {
  executeCommand,
  client,
  growRouter
};
