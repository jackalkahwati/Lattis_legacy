/** @format */

const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const tripSchema = new Schema(
  {
    trip_id: {
      type: Number,
      required: [true, "trip_id is required"],
      unique: true
    },
    bike_id: {
      type: Number,
      required: [true, "bike_id is required"]
    },
    steps: [],
    start_address: {
      type: String,
      required: true
    },
    end_address: {
      type: String,
      default: null
    },
    date_created: {
      type: Number,
      required: true
    },
    date_endtrip: {
      type: Number,
      default: null
    },
    device_token: {
      type: String,
      default: null
    },
    fleet_id: {
      type: Number,
      required: true
    },
    manualEndTrip: {
      type: Boolean,
      default: false
    },
    doNotTrackTrip: {
      type: Boolean,
      default: false
    },
    toVersion: { // Only used for Goetab trips
      type: String,
      default: null
    }
  },
  {
    timestamps: true
  }
);

tripSchema.virtual("bikeAndController", {
  ref: "BikesAndController",
  localField: "bike_id",
  foreignField: "bike_id",
  justOne: true
});

tripSchema.set("toObject", { virtuals: true });
tripSchema.set("toJSON", { virtuals: true });

const TripModel = mongoose.model("Trip", tripSchema, "trips");

module.exports = TripModel;
