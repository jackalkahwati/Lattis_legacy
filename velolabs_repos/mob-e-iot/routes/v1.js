const express = require("express");
const router = express.Router();

router.use("/scooter", require("../modules/v1/scooters/scooter.routes"));
router.use("/auth", require("../modules/v1/users/auth.routes"));
router.use("/devices", require("../modules/v1/stations/station.routes"));

module.exports = router;
