/** @format */

const express = require("express");
const router = express.Router();
const { TripModel, BikeAndControllerModel, FleetMetadataModel } = require("../database/models");
const { formatResponse, logger, getTripAddress } = require("../utils");
const { client, executeCommand } = require("../integrations/grow");
const { GeotabApi, ActonAPI, OmniAPI } = require("../integrations");
const moment = require("moment");
const util = require("util");
const getTripAddressAsync = util.promisify(getTripAddress);
const { segwayApiClientForUs, segwayApiClientForEu } = require("./segway");

const checkSteps = (steps) => {
  let stepsData = steps;
  if (typeof steps === "string") stepsData = JSON.parse(steps);
  else if (Array.isArray(steps)) stepsData = steps;
  if (stepsData && stepsData.length) {
    if (Array.isArray(stepsData[0])) return stepsData[0];
  }
  return stepsData;
};

router.get("/", async (req, res) => {
  try {
    const tripsIds = req.query.trips.split(",").map((id) => +id);
    const tripsInfo = await TripModel.find({ trip_id: { $in: tripsIds } }).exec();
    return res.json(formatResponse(req, null, null, tripsInfo));
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || `An error occurred all trips info ${ req.params.trips }`,
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/start-trip", async (req, res) => {
  try {
    const { tripDetails } = req.body;
    console.log(`<>Starting trip:: ${tripDetails.trip_id} with bike ${tripDetails.bike_id}`);
    const {
      bike_id,
      start_address,
      device_token,
      steps,
      trip_id,
      controller_id,
      key,
      device_type,
      vendor,
      make,
      fleet_id,
      bike_name,
      doNotTrackTrip: doNotTrackTrips,
      meta
    } = tripDetails;
    const geofenceEnabled = !!(meta && meta.geofenceEnabled);
    delete meta.geofenceEnabled;
    await FleetMetadataModel.findOneAndUpdate({ fleet_id }, { doNotTrackTrips, meta, geofenceEnabled }, { upsert: true, new: true });
    let stepData = checkSteps(steps);
    let startTripAddress = start_address.trim();
    if(!startTripAddress) {
      startTripAddress = await getTripAddressAsync(stepData[0], stepData[1]) || "Unnamed Road";
    }
    const tripInfo = {
      trip_id,
      bike_id,
      steps: [stepData],
      start_address: startTripAddress,
      date_created: moment().unix(),
      device_token,
      fleet_id,
      doNotTrackTrip: doNotTrackTrips
    };
    const bikeAndControllerInfo = {
      bike_id,
      controller_id,
      controller_key: key,
      device_type,
      vendor,
      make,
      fleet_id,
      bike_name,
      status: "on_trip",
      current_trip: trip_id
    };
    if(vendor === "Geotab IoT") {
      const client = new GeotabApi(fleet_id);
      const geotabInfo = await client.getDeviceData(key.replace(/-/g,""));
      bikeAndControllerInfo.geotabInfo = geotabInfo;
      bikeAndControllerInfo.controller_key = bikeAndControllerInfo.controller_key.replace(/-/g,"");
    }
    const isTripAvailable = await TripModel.findOne({ trip_id });
    if (isTripAvailable) {
      console.log(`<<The trip already exists. Updating steps will mess things up>>: ${trip_id}`);
      return res.json({ msg: `Trip already exists ${trip_id}` });
    }
    const tripInstance = new TripModel(tripInfo);
    const isBikeSaved = await BikeAndControllerModel.findOne({
      bike_id
    }).exec();
    if (!isBikeSaved) {
      const bikeAndControllerInstance = new BikeAndControllerModel({ ...bikeAndControllerInfo, _id: bike_id });
      await bikeAndControllerInstance.save();
      // Subscribe to heartbeat for new Grow devices
      if (vendor === "Grow") client.subscribe(`s/h/17/${ key }`);
    } else {
      await BikeAndControllerModel.findOneAndUpdate({ bike_id }, bikeAndControllerInfo);
    }
    const trip = await tripInstance.save();
    return res.json(formatResponse(req, null, null, trip));
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred starting trip",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/end-trip", async (req, res) => {
  try {
    const {
      tripDetails: {
        trip_id = undefined,
        // date_endtrip = undefined,
        // steps = undefined
      }
    } = req.body;
    console.log(`🚀 ~ End trip request for trip> ${trip_id}`);
    if (!trip_id) {
      return res.status(400).json({
        error: true,
        message: "trip_id, date_endtrip and steps are required"
      });
    }
    const trip = await TripModel.findOne({ trip_id }).populate({
      path: "bikeAndController",
      model: "BikesAndController"
    }).exec();
    if(!trip) {
      res.statusMessage = `The trip ${trip_id} does not exist`;
      return res.status(400).json(`The trip ${trip_id} does not exist`);
    }
    const bike = trip.bikeAndController;
    let endPosition;
    if(["Segway", "Segway IoT EU"].includes(bike.vendor)) {
      let apiClient; 
      if(bike.vendor === "Segway") apiClient = segwayApiClientForUs;
      if(bike.vendor === "Segway IoT EU") apiClient = segwayApiClientForEu;
      const vehicleInfo = await apiClient.getVehicleInformation(bike.controller_key);
      endPosition = vehicleInfo && [+vehicleInfo.latitude, +vehicleInfo.longitude, moment().unix()];
    }

    if(["ACTON", "Omni"].includes(bike.vendor)) {
      let clientAPI;
      if (bike.vendor === "ACTON") clientAPI = ActonAPI;
      else if (bike.vendor === "Omni") clientAPI = OmniAPI;
      const vehicleInfo = await clientAPI.getCurrentStatus(bike.controller_key);
      endPosition = vehicleInfo && [+vehicleInfo.lat, +vehicleInfo.lon, Date.parse(vehicleInfo.gpsTimestamp)/1000];
    }
    // Turn off Grow alarm if ride ended out of geofence
    if( bike && bike.vendor === "Grow" && bike.growAlarmOn) {
      console.log(`<>Out of geo, alarm on, ending ride for ${bike.controller_key}`);
      executeCommand(`s/c/${ bike.controller_key }`, "50,0");
      await BikeAndControllerModel.updateOne({ controller_key: bike.controller_key }, { growAlarmOn: false });
    }
    const tripSteps = [...trip.steps];
    const lastStep = tripSteps.pop();
    const endAddress = (endPosition && endPosition.length) ? await getTripAddressAsync(endPosition[0], endPosition[1]) : await getTripAddressAsync(lastStep[0], lastStep[1]);
    let updatedTrip = await TripModel.findOneAndUpdate(
      {
        trip_id
      },
      {
        $set: {
          end_address: endAddress || `Unnamed Road ${endPosition[0]}, ${endPosition[1]}`,
          date_endtrip: moment().unix()
        },
        toVersion: null
      },
      { new: true }
    );
    if(endPosition && endPosition.length) {
      updatedTrip = await TripModel.findOneAndUpdate(
        {
          trip_id
        },
        {
          $push: {
            steps: [endPosition]
          }
        },
        { new: true }
      );
    }
    let lastValidLocation;
    if(endPosition) {
      lastValidLocation = {
        latitude: endPosition[0],
        longitude: endPosition[1],
        timestamp: moment().unix()
      };
    } else {
      lastValidLocation = {
        latitude: lastStep[0],
        longitude: lastStep[1],
        timestamp: moment().unix()
      };
    }
    await BikeAndControllerModel.findOneAndUpdate({ bike_id: bike.bike_id }, { status: "parked", current_trip: null, lastValidLocation });
    console.log(`🚀 ~ End trip request for trip> ${trip_id} successfull`);
    res.json(formatResponse(req, null, null, updatedTrip));
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred ending trip",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.get("/:tripId", async (req, res) => {
  try {
    const tripId = req.params.tripId;
    const trip = await TripModel.findOne({ trip_id: tripId }).exec();
    if (!trip) {
      logger.error(formatResponse(req, true, `Trip with ID ${ tripId } not found`));
      return res
        .status(404)
        .json(formatResponse(req, true, `Trip with ID ${ tripId } not found`));
    }
    return res.json(formatResponse(req, null, null, trip));
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || `An error occurred getting trip ${ req.params.tripId }`,
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

module.exports = router;
