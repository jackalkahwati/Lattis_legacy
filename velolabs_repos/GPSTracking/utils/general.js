/** @format */
const { logger } = require("./logger");
const platform = require("@velo-labs/platform");
const mapBoxHandler = platform.mapBoxHandler;
const googleMapsHandler = platform.googleMapsHandler;
const axios = require("axios");

const { DASHBOARD_URL, DASHBOARD_API_KEY } = process.env;

const config = { headers: {
  "x-api-key": DASHBOARD_API_KEY,
  "x-api-client": "GPSTrackingService"
} };

// Get the latest completed trip
const getLatestTrip = async (bikeId, TripModel) => {
  try {
    const latest = await TripModel.findOne({
      date_endtrip: { $ne: null },
      bike_id: bikeId
    }).sort({
      createdAt: -1
    });
    if(!latest) return null;
    const lastGPSLocation =
      Array.isArray(latest.steps) && latest.steps.length && latest.steps.slice(-1)[0];
    return lastGPSLocation;
  } catch (error) {
    logger.error(error);
  }
};

const getLastValidLocation = async (bikeId, BikeAndControllerModel) => {
  try {
    const vehicle = await BikeAndControllerModel.findOne({ bike_id: bikeId }).lean();
    return vehicle ?  vehicle.lastValidLocation: null;
  } catch (error) {
    logger.error(error);
    throw new Error(`An error occurred getting latValidLocation for vehicle ${bikeId}: ${error.message}`);
  }
};

// Converts numeric degrees to radians
const toRad = (value) => {
  return (value * Math.PI) / 180;
};

/**
 * Get distance btn two points
 *
 * @param {*} pointA An array of [latitude, longitude, time]
 * @param {*} pointB An array of [latitude, longitude, time]
 * @returns distance between 2 points in kms
 */
const calcCrowDistance = (pointA, pointB) => {
  const R = 6371; // km
  const dLat = toRad(pointB[0] - pointA[0]);
  const dLon = toRad(pointB[1] - pointA[1]);
  const lat1 = toRad(pointA[0]);
  const lat2 = toRad(pointB[0]);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const d = R * c;
  return d * 1000; // Return distance in metres
};

/**
 * To get trip start and end address
 *
 * @param {Number} latitude - latitude of the location to get address
 * @param {Number} longitude - longitude of the location to get address
 * @param {Function} callback
 * @private
 */
 const _getTripAddress = (latitude, longitude, callback) => {
  if (!latitude || !longitude) {
    logger(`latitude or longitude is invalid with values lat: ${latitude} and long: ${longitude}`);
    return callback("Missing latitude or longitufe", null);
  }
  mapBoxHandler.getAddressInformationForCoordinate(
    {
      latitude: latitude,
      longitude: longitude
    },
    (error, address) => {
      if (error) {
        logger.error("Error: making trip-address query to MapBox");
        return callback("Error: making trip-address query to MapBox", null);
      }
      const locationAddress = address
        ? (!!address.address && !!address.street)
          ? address.address + " " + address.street
          : ""
        : "";
      if (!(locationAddress.trim().length)) {
        googleMapsHandler.getAddressInformationForCoordinate({
          latitude: latitude,
          longitude: longitude
        }, (error, addressInfo) => {
          if (error) {
            logger.error("Error: Failed to get address from google maps API with error: ", error);
            return callback("Error: Failed to get address from google maps API with error: ", null);
          }
          callback(null, addressInfo.street ? addressInfo.street : "Unnamed Road");
        });
      } else {
        return callback(null, locationAddress);
      }
    }
  );
};

const checkAndCreateTheftTicket = async (bike, TicketModel) => {
  try {
    const { bike_id: bikeId, fleet_id: fleetId } = bike;
    const checkPendingTheftTicket = await TicketModel.findOne({ bike_id: bikeId, resolved: false, category: "potential_theft" }).exec();
    if (!checkPendingTheftTicket) {
      const data = {
        category: "potential_theft",
        fleet_id: fleetId,
        customer_id: 11111111,
        operator_id: 11111111,
        bike_id: bikeId,
        notes: `A potential theft was reported on bike ${ bike.name || bikeId } `
      };
      const response = await axios.post(`${ DASHBOARD_URL }/api/gpsService/create-ticket`, data, config);
      const payload = response.data.payload;
      const ticketInstance = new TicketModel({
        fleet_id: fleetId,
        ticket_id: payload && payload.insertId,
        bike_id: bikeId,
        category: "potential_theft",
      });
      await ticketInstance.save();
    }
  } catch (error) {
    logger.error("An error occurred creating the theft ticket");
    const err = (error.response && error.response.data)  || error.message;
    logger.error(err);
  }
};

module.exports = { getLatestTrip, calcCrowDistance, _getTripAddress, getLastValidLocation, checkAndCreateTheftTicket };
