/** @format */

const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const geofencesSchema = new Schema(
  {
    fleet_id: {
      type: Number,
      required: true
    },
    geofence_id: {
      type: Number,
      required: true
    },
    name: String,
    status: {
      type: Boolean,
      dafault: false
    },
    geometry: {
      type: Object,
      required: true
    }
  },
  {
    timestamps: true
  }
);

geofencesSchema.set("toObject", { virtuals: true });
geofencesSchema.set("toJSON", { virtuals: true });

const GeofenceModel = mongoose.model("Geofence", geofencesSchema, "geofences");

module.exports = GeofenceModel;
