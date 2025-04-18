/** @format */

const express = require("express");
const router = express.Router();
const {
  formatResponse,
  logger,
  calcCrowDistance,
  checkAndCreateTheftTicket
} = require("../utils");
const { BikeAndControllerModel, TripModel, TicketModel } = require("../database/models");
const moment = require("moment");

const updateEdgeTrips = async (bike, steps) => {
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
    }
  } catch (error) {
    logger.error("Error::", error.message || error);
  }
};

router.post("/location", async (req, res) => {
  try {
    const locationData = req.body;
    console.log(
      "🚀 ~ file: edge.js ~ line 37 ~ router.post ~ locationData",
      locationData
    );
    const location = locationData.location || locationData.waypoint;
    const controllerKey = locationData["veh-id"];
    const vehicle = await BikeAndControllerModel.findOne({
      controller_key: controllerKey
    }).lean();
    if (!vehicle) {
      res.status(404);
      return res.json({ error: "Couldn't find vehicle with that key", key: controllerKey });
    }
    const latitude = location.lat;
    const longitude = location.lon;
    const gpsUtcTime = location.updated;
    const steps = latitude && longitude && [+latitude, +longitude, moment().unix()];
    const position = { latitude, longitude, gpsUtcTime };
    if (position) {
      BikeAndControllerModel.updateOne({ controller_key: controllerKey }, { position })
        .then(() => {
          /* bike details updated */
        })
        .catch((error) => {
          logger.error("Error updating IoT::", error);
        });
    }

    const bikeOnTrip = await BikeAndControllerModel.findOne({
      status: "on_trip",
      controller_key: controllerKey,
      current_trip: { $ne: null },
      vendor: { $in: ["Edge"] }
    })
      .populate({
        path: "fleet",
        model: "FleetMeta"
      })
      .populate({
        path: "trip",
        model: "Trip"
      })
      .exec();

    if (bikeOnTrip) {
      await updateEdgeTrips(bikeOnTrip, steps);
    } else {
      if (vehicle.mainStatus === "active") {
        // Only create tickets for live vehicles
        // If vehicle not on trip check if it's 100m away from lastValidLocation
        const lastValidLocation = vehicle.lastValidLocation;
        if (lastValidLocation && steps) {
          const distanceBetween = calcCrowDistance(
            [steps[0], steps[1]],
            [lastValidLocation.latitude, lastValidLocation.longitude]
          );
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
      error.message || "An error with edge location push",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/power", async (req, res) => {
  try {
    const powerData = req.body;
    if(!powerData.power.update || !powerData.power.update.batteries) {
      return res.json({ message: "No battery update" });
    }
    const batteryData = powerData.power.update.batteries;
    const controllerKey = powerData["veh-id"];
    const vehicle = await BikeAndControllerModel.findOne({
      controller_key: controllerKey
    }).lean();
    if (!vehicle) {
      res.status(404);
      return res.json({ error: "Couldn't find vehicle with that key", key: controllerKey });
    }
    const battery = batteryData[0];
    const batteryLevel = battery.percent;
    if (batteryLevel !== undefined && batteryLevel !== null) {
      BikeAndControllerModel.updateOne({ controller_key: controllerKey }, { battery: batteryLevel })
        .then(() => {
          /* bike details updated */
        })
        .catch((error) => {
          logger.error("Error updating IoT::", error);
        });
    }
    return res.json({ code: 0 });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error with edge power push",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/trip", (req, res) => {
  console.log("🚀 ~ file: edge.js ~ line 107 ~ router.post ~ req", req.body);
  res.json("Trip event received");
});

router.post("/lock", (req, res) => {
  console.log("🚀 ~ file: edge.js ~ line 112 ~ router.post ~ req", req.body);
  res.json("Lock event received");
});

router.post("/error", (req, res) => {
  console.log("🚀 ~ file: edge.js ~ line 117 ~ router.post ~ req", req.body);
  res.json("Error event received");
});

module.exports = router;
