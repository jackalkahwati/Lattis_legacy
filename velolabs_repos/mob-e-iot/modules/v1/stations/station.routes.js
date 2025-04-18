const express = require("express");
const stationController = require("./station.controller");
const { checkValidToken } = require("../../../middlewares/auth.middleware");
const {
  checkScooterIdValidOrNot,
} = require("../../../middlewares/scooterId.middleware");

const router = express.Router();

router.get("/stations", checkValidToken, stationController.getAllStations);

router.get(
  "/stationsinfo/:StationUID",
  checkValidToken,
  stationController.getStation
);

router.get("/getInfo/:StationUID", checkValidToken, stationController.getInfo);

router.put(
  "/deviceunlock",
  checkValidToken,
  checkScooterIdValidOrNot,
  stationController.unlockDevice
);
router.put(
  "/devicelock",
  checkValidToken,
  checkScooterIdValidOrNot,
  stationController.lockDevice
);

router.post(
  "/registerorupdatestation",
  checkValidToken,
  stationController.registerOrUpdateStation
);

router.post("/reset", checkValidToken, stationController.resetDockingStation);

router.post(
  "/setmaintenance",
  checkValidToken,
  stationController.setMaintenance
);

router.post("/setbooking", checkValidToken, stationController.setBooking);

module.exports = router;
