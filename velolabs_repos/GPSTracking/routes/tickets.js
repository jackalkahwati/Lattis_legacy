/** @format */

const express = require("express");
const router = express.Router();
const { TicketModel } = require("../database/models");
const { formatResponse } = require("../utils");

router.patch("/resolve/:ticketId", async (req, res) => {
  try {
    const ticketId = req.params.ticketId;
    const ticket = await TicketModel.findOneAndUpdate({ ticket_id: ticketId }, { resolved: true }, { new: true });
    console.log("🚀 ~ Resolved ticket::", ticket);
    return res.json(formatResponse(req, null, null, ticket));
  } catch (error) {
    console.log("🚀Error ticket resolution::", error);
    const errorMessage = formatResponse(
      req,
      true,
      error.message || `An error resolving the ticket ${ req.params.ticketId }`,
      null,
      error
    );
    res.status(500).json(errorMessage);
  }
});

module.exports = router;
