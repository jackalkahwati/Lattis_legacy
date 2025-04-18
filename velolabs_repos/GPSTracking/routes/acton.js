/** @format */

const express = require("express");
const router = express.Router();
const { formatResponse, logger, isOutofGeofence, calcCrowDistance, checkAndCreateTheftTicket } = require("../utils");
const { BikeAndControllerModel, TripModel, TicketModel } = require("../database/models");
const moment = require("moment");
const { ActonAPI, OmniAPI } = require("../integrations");
const { userDBConnection } = require("../database/mysql");

const updateActonTrips = async (data, bike, steps) => {
  try {
    if (steps && steps.length) {
      await TripModel.findOneAndUpdate(
        {
          trip_id: bike.current_trip
        },
        {
          $push: {
            steps: [steps]
          }
        },
        { new: true }
      );
      const fleet = bike.fleet;
      if (fleet && fleet.geofenseEnabled) {
        const outOfGeo = await isOutofGeofence(fleet.fleet_id, [steps[0], steps[1]]);
        if (outOfGeo) {
          let client;
          if (bike.vendor === "ACTON") client = ActonAPI;
          else if (bike.vendor === "Omni") client = OmniAPI;
          client.locateVehicle(data.IMEI);
        }
      }
    }
  } catch (error) {
    logger.error("Error::", error.message || error);
  }
};
// Receive push data from ACTON
router.post("/", async (req, res) => {
  try {
    const apiKey = req.headers["x-api-key"];
    console.log("🚀 ~ file: acton.js ~ line 44 ~ router.post ~ apiKey", apiKey);
    if (!apiKey) {
      return res.status(401).json({ msg: "A valid API key is required" });
    }
    userDBConnection.execute(
      "SELECT * FROM `api_keys` WHERE `key` = ?",
      [apiKey],
      (err, results) => {
        console.log("🚀 ~ file: acton.js ~ line 52 ~ router.post ~ results", results);
        if (err) {
          console.log("🚀 ~ Error acton:::", err);
          logger.error("Error verifying API key:::", err);
          return res.status(500).json({ msg: "Error verifying API key" });
        } else {
          if (!results || (results && !results.length)) {
            logger.error(`API key ${ apiKey } not found`);
            return res.status(401).json({ msg: "A valid API key is required" });
          }
        }
      }
    );
    let data = req.body[0]; // Will be at index 0
    console.log("🚀 ~ file: acton.js ~ line 65 ~ router.post ~ data", data);
    res.status(200).json({ "msg": "Request received" });
    if (!data) return;
    const vehicle = await BikeAndControllerModel.findOne({
      controller_key: data.IMEI
    }).lean();
    if (!vehicle) {
      logger.info(`Vehicle ${ data.IMEI } not registered`);
      return;
    }

    const latitude = data.lat;
    const longitude = data.lon;
    const gpsUtcTime = data.gpsTimestamp;
    const steps = latitude && longitude && [+latitude, +longitude, moment().unix()];
    const extendedData = data.extendedData;

    const position = { latitude, longitude, gpsUtcTime: Date.parse(gpsUtcTime) / 1000 };
    if (position) {
      BikeAndControllerModel.updateOne({ controller_key: data.IMEI }, {
        position, batteryIoT: data.iotBattery,
        battery: extendedData.scooterBattery,
        meta: data
      }).then(() => { /* bike details updated */ })
        .catch(error => {
          logger.error("Error updating IoT::", error);
        });
    }

    // Update ACTON/Omni bike on trip
    const bikeOnTrip = await BikeAndControllerModel.findOne({ status: "on_trip", controller_key: data.IMEI, current_trip: { $ne: null }, vendor: { $in: ["ACTON", "Omni"] } })
      .populate({
        path: "fleet",
        model: "FleetMeta"
      }).populate({
        path: "trip",
        model: "Trip"
      }).exec();

    if (bikeOnTrip) {
      await updateActonTrips(data, bikeOnTrip, steps);
    } else {
      if (vehicle.mainStatus === "active") { // Only create tickets for live vehicles
        // check and create theft ticket if vehicle not on trip and 100m away
        const lastValidLocation = vehicle.lastValidLocation;
        if (lastValidLocation && steps) {
          const distanceBetween = calcCrowDistance([steps[0], steps[1]], [lastValidLocation.latitude, lastValidLocation.longitude]);
          if (distanceBetween > 100) {
            checkAndCreateTheftTicket(vehicle, TicketModel);
          }
        }
      }
    }
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || `An error resolving the ticket ${ req.params.ticketId }`,
      null,
      error
    );
    logger.error(errorMessage);
    res.json({ "msg": "Something is not right..." });
  }
});

module.exports = router;
