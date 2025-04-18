/** @format */

const express = require("express");
const router = express.Router();
const { GrowModel, FleetMetadataModel, BikeAndControllerModel } = require("../database/models");
const { formatResponse, logger, controllerInfoMap } = require("../utils");
const { GeotabApi } = require("../integrations");
const { segwayApiClientForUs, segwayApiClientForEu } = require("./segway");
const { ActonAPI, OmniAPI } = require("../integrations");
const moment = require("moment");
const { executeCommand } = require("../integrations/grow");

router.get("/:vehicleId", async (req, res) => {
  try {
    const vehicleId = req.params.vehicleId;
    const vehicle = await GrowModel.findById(vehicleId).exec();
    if (!vehicle) {
      logger.error(formatResponse(req, true, `vehicle ID ${ vehicleId } not found`));
      return res
        .status(404)
        .json(formatResponse(req, true, `Vehicle with ID ${ vehicleId } not found`));
    }
    return res.json(formatResponse(req, null, null, vehicle));
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred getting vehicle details",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

const getBikePosition = async (bikeId) => {
  try {
    const vehicle = await BikeAndControllerModel.findOne({ bike_id: bikeId });
    if (!vehicle) throw new Error(`The bike with id  ${ bikeId } was not found`);
    const vendor = vehicle.vendor;
    let currentPosition;
    if (vendor) {
      switch (vendor) {
        case "Segway":
        case "Segway IoT EU": {
          let apiClient;
          if (vendor === "Segway") apiClient = segwayApiClientForUs;
          if (vendor === "Segway IoT EU") apiClient = segwayApiClientForEu;
          const vehicleInfo = await apiClient.getVehicleInformation(vehicle.controller_key);
          currentPosition = vehicleInfo && { latitude: +vehicleInfo.latitude, longitude: +vehicleInfo.longitude, timestamp: moment().unix() };
          break;
        }
        case "ACTON":
        case "Omni": {
          let clientAPI;
          if (vendor === "ACTON") clientAPI = ActonAPI;
          else if (vendor === "Omni") clientAPI = OmniAPI;
          const vehicleInfo = await clientAPI.getCurrentStatus(vehicle.controller_key);
          currentPosition = vehicleInfo && { latitude: +vehicleInfo.lat, longitude: +vehicleInfo.lon, timestamp: moment().unix() };
          break;
        }
        case "Grow": {
          // Send command to return current GPS location
          executeCommand(`s/c/${ vehicle.controller_key }`, "201");
          break;
        }
        default:
          throw new Error(`${ vendor } not supported yet`);
      }

      return currentPosition;
    }
  } catch (error) {
    throw new Error(`Error:: ${ error.message }`);
  }
};

router.patch("/", async (req, res) => {
  try {
    // Update vehicle status
    const { values, filter } = req.body;
    let updates = { status: values.current_status, mainStatus: values.status };
    let bikeId;
    if (filter && filter.bike_id && Array.isArray(filter.bike_id)) {
      bikeId = +filter.bike_id[0];
    } else {
      bikeId = +filter.bike_id;
    }
    if (values.status === "active") {
      const currentPosition = await getBikePosition(bikeId);
      if (currentPosition) updates = { ...updates, currentPosition, lastValidLocation: currentPosition };
    }
    await BikeAndControllerModel.findOneAndUpdate({ bike_id: bikeId }, updates, { upsert: true, new: true });
    res.json({ message: "Vehicle updated successfully" });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occured updating vehicle status",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.post("/", async (req, res) => {
  try {
    // create vehicles and controller and fleet if not exist
    const info = req.body.info;
    if (info.bikeAndCtrl) {
      const bikeAndCtrl = info.bikeAndCtrl;
      await BikeAndControllerModel.findOneAndUpdate({ bike_id: bikeAndCtrl.bike_id }, bikeAndCtrl, { upsert: true, new: true });
      if (info.fleet) {
        const fleet = info.fleet;
        const metaInfo = fleet.meta ? { ...fleet.meta } : undefined;
        delete fleet.meta;

        const controllerMetaKey = controllerInfoMap[metaInfo.integrationType];
        const infoKey = `meta.${ controllerMetaKey }`;
        await FleetMetadataModel.findOneAndUpdate({ fleet_id: fleet.fleet_id }, { ...fleet, $set: { [infoKey]: metaInfo.info } }, { upsert: true, new: true });
      }
      if (bikeAndCtrl.vendor === "Geotab IoT") {
        const client = new GeotabApi(bikeAndCtrl.fleet_id);
        const geotabInfo = await client.getDeviceData(bikeAndCtrl.controller_key.replace(/-/g, ""));
        await BikeAndControllerModel.findOneAndUpdate({ bike_id: bikeAndCtrl.bike_id }, { geotabInfo });
      }
    }
    res.json({ message: "Bike, Ctrl and fleet created/updated" });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occured creating vehicle/fleet status",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

module.exports = router;
