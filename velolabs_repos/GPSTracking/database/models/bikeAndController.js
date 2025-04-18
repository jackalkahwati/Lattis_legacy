/** @format */

const { Schema } = require("../db");
const mongoose = require("mongoose");

const bikeAndControllerSchema = new Schema(
  {
    _id: Number,
    bike_id: {
      type: Number,
      required: [true, "bike_id is required"],
      unique: true,
    },
    bike_name: {
      type: String
    },
    mainStatus: {
      type: String,
      enum: ["active", "inactive"],
    },
    status: {
      type: String,
      enum: ["parked", "on_trip", "controller_assigned", "lock_not_assigned"],
      required: true,
      default: "parked"
    },
    controller_id: {
      type: Number,
      required: true,
    },
    fleet_id: {
      type: Number,
      required: true,
    },
    controller_key: {
      type: String,
      required: true,
    },
    device_type: {
      type: String,
    },
    vendor: {
      type: String,
      required: true,
    },
    make: {
      type: String,
      default: null,
    },
    position: {
      type: Map,
      of: Number
    },
    battery: {
      type: Number
    },
    batteryIoT: {
      type: Number
    },
    current_trip: { // Id of current trip if on trip else null
      type: Number,
      default: null
    },
    geotabInfo: {
      type: Map,
      default: null
    },
    meta: {
      type: Map
    },
    growAlarmOn: {
      type: Boolean,
      default: false,
    },
    lastValidLocation: { // Last position vehicle went live or ended ride
      type: Map,
      of: Number
    },
  },
  {
    timestamps: true,
  }
);

bikeAndControllerSchema.virtual("fleet", {
  ref: "FleetMeta",
  localField: "fleet_id",
  foreignField: "fleet_id",
  justOne: true
});

bikeAndControllerSchema.virtual("bikeAndController", {
  ref: "BikesAndController",
  localField: "bike_id",
  foreignField: "bike_id",
  justOne: true
});

bikeAndControllerSchema.virtual("trip", {
  ref: "Trip",
  localField: "current_trip",
  foreignField: "trip_id",
  justOne: true
});

bikeAndControllerSchema.set("toObject", { virtuals: true });
bikeAndControllerSchema.set("toJSON", { virtuals: true });

const BikeAndControllerModel = mongoose.model(
  "BikesAndController",
  bikeAndControllerSchema,
  "bikesAndControllers"
);

module.exports = BikeAndControllerModel;
