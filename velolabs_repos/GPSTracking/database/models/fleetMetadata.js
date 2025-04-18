/** @format */

const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const fleetMetadataSchema = new Schema(
  {
    fleet_id: {
      type: Number,
      required: true,
      unique: true
    },
    geofenseEnabled: {
      type: Boolean,
      default: true
    },
    doNotTrackTrips: {
      type: Boolean,
      default: false
    },
    meta: {
      type: Map
    }
  },
  {
    timestamps: true
  }
);

fleetMetadataSchema.set("toObject", { virtuals: true });
fleetMetadataSchema.set("toJSON", { virtuals: true });

const FleetMetadataModel = mongoose.model("FleetMeta", fleetMetadataSchema, "fleetMetadata");

module.exports = FleetMetadataModel;
