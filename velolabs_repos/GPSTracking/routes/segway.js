const express = require("express");
const router = express.Router();
const { formatResponse, logger, isOutofGeofence, calcCrowDistance, checkAndCreateTheftTicket } = require("../utils");
const { BikeAndControllerModel, TripModel, TicketModel } = require("../database/models");
const moment = require("moment");
const { SegwayApiClient } = require("../integrations");

const segwayApiClientForUs = new SegwayApiClient({
  baseUrl: process.env.SEGWAY_API_URL,
  clientId: process.env.SEGWAY_CLIENT_ID,
  clientSecret: process.env.SEGWAY_CLIENT_SECRET
});

const segwayApiClientForEu = new SegwayApiClient({
  baseUrl: process.env.SEGWAY_EU_API_URL,
  clientId: process.env.SEGWAY_EU_CLIENT_ID,
  clientSecret: process.env.SEGWAY_EU_CLIENT_SECRET
});

router.post("/location", async (req, res) => {
  try {
    const locationData = req.body;
    const vehicle = await BikeAndControllerModel.findOne({
      controller_key: locationData.iotCode
    }).lean();
    if (!vehicle) return res.json({ code: 0 });
    const latitude = locationData.latitude;
    const longitude = locationData.longitude;
    const gpsUtcTime = locationData.gpsUtcTime;
    const steps = latitude && longitude && [+latitude, +longitude, moment().unix()];

    const position = { latitude, longitude, gpsUtcTime };
    if (position) {
      BikeAndControllerModel.updateOne({ controller_key: locationData.iotCode }, { position })
        .then(() => { /* bike details updated */ })
        .catch(error => {
          logger.error("Error updating IoT::", error);
        });
    }

    const bikeOnTrip = await BikeAndControllerModel.findOne({ status: "on_trip", controller_key: locationData.iotCode, current_trip: { $ne: null }, vendor: { $in: ["Segway", "Segway IoT EU"] } })
      .populate({
        path: "fleet",
        model: "FleetMeta"
      }).populate({
        path: "trip",
        model: "Trip"
      }).exec();

    if (bikeOnTrip) {
      let apiClient;
      if (bikeOnTrip.vendor === "Segway") apiClient = segwayApiClientForUs;
      if (bikeOnTrip.vendor === "Segway IoT EU") apiClient = segwayApiClientForEu;
      await updateSegwayTrips(locationData, bikeOnTrip, apiClient, steps);
    } else {
      if (vehicle.mainStatus === "active") { // Only create tickets for live vehicles
        // If vehicle not on trip check if it's 100m away from lastValidLocation
        const lastValidLocation = vehicle.lastValidLocation;
        if (lastValidLocation && steps) {
          const distanceBetween = calcCrowDistance([steps[0], steps[1]], [lastValidLocation.latitude, lastValidLocation.longitude]);
          if (distanceBetween > 100) {
            checkAndCreateTheftTicket(vehicle, TicketModel);
          }
        }
      }
    }
    return res.json({ code: 0 });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error with segway location push",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/alert", async (req, res) => {
  try {
    console.log("🚀 ~ Alert push::", JSON.stringify(req.body));
    const alertData = req.body;
    const vehicle = await BikeAndControllerModel.findOne({
      controller_key: alertData.iotCode
    }).lean();

    if (+alertData.code === 1 || +alertData.code === 3) {// Illegal movement or dismantling, Create a theft ticket
      if (vehicle.mainStatus === "active") { // Only create tickets for live vehicles
        checkAndCreateTheftTicket(vehicle, TicketModel);
      }
    }
    return res.json({ code: 0 });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error with segway alert push",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/fault", async (req, res) => {
  try {
    console.log("🚀 ~ Fault push", JSON.stringify(req.body));
    return res.json({ code: 0 });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error with segway fault push",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

const updateSegwayTrips = async (locationData, bike, client, steps) => {
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
        if (outOfGeo && client && locationData.iotCode) await client.alertOutOfGeofence(locationData.iotCode);
      }
    }
  } catch (error) {
    logger.error("Error::", error.message || error);
  }
};

router.post("/status", async (req, res) => {
  try {
    const statusData = req.body;
    const vehicle = await BikeAndControllerModel.findOne({
      controller_key: statusData.iotCode
    }).exec();
    if (!vehicle) return res.json({ code: 0 });
    // Find ongoing trips with tracking enabled(doNotTrackTrip is false)
    if (statusData) {
      const updateData = {
        battery: statusData.powerPercent,
        meta: {
          iotCode: statusData.iotCode,
          platformCode: statusData.platformCode,
          statusUtcTime: +statusData.statusUtcTime,
          online: statusData.online === "true",
          locked: statusData.locked === "true",
          lockVoltage: +statusData.lockVoltage,
          networkSignal: +statusData.networkSignal,
          charging: statusData.charging === "true",
          speedMode: +statusData.speedMode,
          odometer: +statusData.odometer,
          remainingRange: +statusData.remainingRange,
          totalRidingSecs: +statusData.totalRidingSecs,
        }
      };
      BikeAndControllerModel.updateOne({ controller_key: statusData.iotCode }, updateData)
        .then(() => { /* bike details updated */ })
        .catch(error => {
          logger.error("Error updating IoT::", error);
        });
    }
    return res.json({ code: 0 });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error with segway status push",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

module.exports = { router, segwayApiClientForUs, segwayApiClientForEu };