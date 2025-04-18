const express = require("express");
const router = express.Router();
const { formatResponse, logger } = require("../utils");
const { FleetMetadataModel } = require("../database/models");

const integrationTypesMap = {
"geotab": "geotabInfo"
};

router.post("/", async (req, res) => {
  try {
    const { info, type, fleetId } = req.body;
    if(!fleetId) return res.status(400).send(formatResponse(req, true, "Fleet ID is required"));
    if(info && type) {
      const metaKey = integrationTypesMap[type];
      const infoKey = `meta.${metaKey}`;
      await FleetMetadataModel.findOneAndUpdate({ fleet_id: fleetId }, { $set: { [infoKey]: info } }, { upsert: true, new: true });
    }
    res.json({ msg: "Integration added succcessfully" });
  } catch (error) {
    const errorMessage = formatResponse(
      req,
      true,
      error.message || "An error occurred creating a new integration",
      null,
      error
    );
    logger.error(errorMessage);
    res.status(500).json(errorMessage);
  }
});

module.exports = router;