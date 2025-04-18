/** @format */

const express = require("express");
const router = express.Router();
const GeofenceModel = require("../database/models/geofence");
const FleetMetadataModel = require("../database/models/fleetMetadata");
const { formatResponse } = require("../utils");

router.post("/", async (req, res) => {
  try {
    const geofence = req.body.geofence;
    const geoInstance = new GeofenceModel({
      ...geofence,
      geometry: JSON.parse(geofence.geometry)
    });
    await FleetMetadataModel.findOneAndUpdate({ fleet_id: geofence.fleet_id }, { geofenseEnabled: true }, { upsert: true, new: true });
    const geoData = await geoInstance.save();
    return res.json(formatResponse(req, null, geoData));
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred saving geofence data",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.delete("/:geofenceId", async (req, res) => {
  try {
    const geofenceId = req.params.geofenceId;
    await GeofenceModel.findOneAndDelete({ geofence_id: geofenceId });
    res.json({ error: null, message: "Geofence update successfull", payload: null });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred deleting geofence data",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.patch("/:geofenceId", async (req, res) => {
  try {
    const geofenceId = req.params.geofenceId;
    const geoUpdates = req.body.geoUpdates;
    const updated = await GeofenceModel.findOneAndUpdate({ geofence_id: geofenceId }, geoUpdates, {
      new: true
    });
    res.json({ error: null, message: "Geofence update successfull", payload: updated });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred updating geofence data",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

router.patch("/status/:fleetId", async (req, res) => {
  try {
    const fleetId = req.params.fleetId;
    const status = req.body.status;
    await GeofenceModel.updateMany({ fleet_id: fleetId }, { status }, {
      new: true
    });
    const data = await FleetMetadataModel.findOneAndUpdate({ fleet_id: fleetId }, { fleet_id: fleetId, geofenseEnabled: status }, { upsert: true, new: true });
    res.json({ error: null, message: "Fleet Geofence status update successfull", payload: data });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred updating fleet geofence status",
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

module.exports = router;
