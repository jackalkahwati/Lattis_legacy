/** @format */

const formatResponse = require("./response");
const { generateToken, verifyToken, authententicateRequest } = require("./auth");
const { expressLogger, logger } = require("./logger");
const { getLatestTrip, calcCrowDistance, _getTripAddress, getLastValidLocation, checkAndCreateTheftTicket } = require("./general");
const { isOutofGeofence } = require("./geofence");
const { computePercentage } = require("./geotabBatteryPercentage");
const { checkSourceIpAddress } = require("./whitelistIP");

const controllerInfoMap = {
  "Segway": "segwayInfo",
  "Segway IoT EU": "SegwayEUInfo",
  "Grow": "growInfo",
  "Geotab IoT": "geotabInfo",
  "ACTON": "actonInfo",
  "Omni IoT": "omniIoTInfo",
  "Teltonika": "teltonikaInfo",
  "Sentinel": "Sentinel",
  "Okai": "Okai"
};

module.exports = {
  formatResponse,
  generateToken,
  verifyToken,
  authententicateRequest,
  logger,
  expressLogger,
  getLatestTrip,
  calcCrowDistance,
  isOutofGeofence,
  getTripAddress: _getTripAddress,
  getLastValidLocation,
  checkAndCreateTheftTicket,
  computePercentage,
  controllerInfoMap,
  checkSourceIpAddress
};
