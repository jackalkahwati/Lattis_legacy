const express = require("express");
const scooterController = require("./scooter.controller");
const { checkValidToken } = require("../../../middlewares/auth.middleware");

const router = express.Router();

router.post(
  "/createorupdatescooter",
  checkValidToken,
  scooterController.createOrUpdateScooter
);

module.exports = router;
