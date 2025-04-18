/** @format */

const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const growSchema = new Schema(
  {
    _id: String,
    conexion: Number,
    timestamp: String,
    "position.latitude": Schema.Types.Decimal128,
    "position.longitude": Schema.Types.Decimal128,
    "position.altitude": String,
    "position.speed": String,
    "position.hdop": Schema.Types.Decimal128,
    batiot: String,
    batsco: String,
    batext: String,
    speedsco: String,
    coderr: String,
    rssi: String,
    "4g2g": String,
    qr: String,
    boolean: String
  },
  {
    timestamps: true
  }
);

growSchema.set("toObject", { virtuals: true });
growSchema.set("toJSON", { virtuals: true });

const GrowModel = mongoose.model("Grow", growSchema, "grow");

module.exports = GrowModel;
