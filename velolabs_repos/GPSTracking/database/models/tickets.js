/** @format */

const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const ticketSchema = new Schema(
  {
    fleet_id: {
      type: Number,
      required: true
    },
    bike_id: {
      type: Number,
      required: true
    },
    ticket_id: {
      type: Number
    },
    category: {
      type: String,
      enum: ["potential_theft", "low_battery", "depleted_battery"],
      required: true
    },
    resolved: {
      type: Boolean,
      default: false
    },
    status: {
      type: String,
    },
    date_resolved: {
      type: Date
    }
  },
  {
    timestamps: true
  }
);

ticketSchema.set("toObject", { virtuals: true });
ticketSchema.set("toJSON", { virtuals: true });

const TicketModel = mongoose.model("Ticket", ticketSchema, "ticket");

module.exports = TicketModel;
