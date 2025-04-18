const express = require("express");
const router = express.Router();
const { logger } = require("../utils");
const { updateDashboardData, trackGeotabTrips, updateGeotabBattery } = require("../scheduler");

router.post("/", async (req, res) => {
  try {
    const { operation /* params */ } = req.body;
    console.log("Operation:::::", operation);
    switch (operation) {
      case "UpdateDashboardData":
        updateGeotabBattery();
        updateDashboardData();
        break;
      case "TrackGeotabTrips":
        trackGeotabTrips();
        break;
      default:
        console.log("...Unsupported operation...");
    }
    res.json({ message: "Task received successfully" });
  } catch (error) {
    logger.error(`Error processing cron task:: ${ JSON.stringify(req.body) }:`, error);
  }
});

module.exports = router;