const router = require("express").Router();
const { BikeAndControllerModel, TripModel } = require("../database/models");
const { isOutofGeofence, logger, checkSourceIpAddress } = require("../utils");
const axios = require("axios");
const { DASHBOARD_URL, DASHBOARD_API_KEY } = process.env;

const config = { headers: {
  "x-api-key": DASHBOARD_API_KEY,
  "x-api-client": "GPSTrackingService"
} };

const notificationTypes = {
  NetworkRegistration: "1007",
  LockOpened: "1001",
  LockClosed: "1002",
  GPSReported: "1003",
  MultipleGPSReported: "1023",
  HeartBeatReceived: "1006"
};

const sendNotificationRequest = async (body) => {
  await axios.post(`${ DASHBOARD_URL }/api/sentinel`, body, config);
};

router.post("/", async (req, res) => {
  try {
    const isSourceIpAddressAllowed = checkSourceIpAddress(req);
    if (!isSourceIpAddressAllowed) {
      res.status(400).json({ message: "Invalid source IP address" });
    }
    const notificationBody = req.body;
    const notificationType = notificationBody.notificationType;
    switch (notificationType) {
      case notificationTypes.NetworkRegistration: {
        console.log("Network Registration::", notificationBody);
        break;
      }
      case notificationTypes.GPSReported: {
        const message = notificationBody.message;
        const position = { latitude: message.latitude, longitude: message.longitude };
        await sendNotificationRequest(notificationBody);
        await BikeAndControllerModel.updateOne({ controller_key: message.lockId }, { position });
        break;
      }
      case notificationTypes.MultipleGPSReported: {
        const message = notificationBody.message;
        const gpsList = message.gpsList;
        if (!gpsList.length) return res.json({ message: "Empty gps list" });
        const lockOnTrip = await BikeAndControllerModel.findOne({ status: "on_trip", controller_key: message.lockId, current_trip: { $ne: null }, vendor: { $in: ["Sentinel"] } })
          .populate({
            path: "fleet",
            model: "FleetMeta"
          }).populate({
            path: "trip",
            model: "Trip"
          }).exec();
        const steps = gpsList.map(gps => {
          return [gps.latitude, gps.longitude, gps.time];
        });
        await TripModel.findOneAndUpdate(
          {
            trip_id: lockOnTrip.current_trip
          },
          {
            $push: {
              steps: steps
            }
          },
          { new: true }
        );
        const fleet = lockOnTrip.fleet;
        if (fleet && fleet.geofenseEnabled) {
          const outOfGeo = await isOutofGeofence(fleet.fleet_id, [steps[0], steps[1]]);
          if (outOfGeo) {
            // TODO: Implement out of geofence alert
          }
        }
        break;
      }
      case notificationTypes.HeartBeatReceived:
      case notificationTypes.LockOpened:
      case notificationTypes.LockClosed: 
        await sendNotificationRequest(notificationBody);
        break;
      default:
        return res.json({ message: "Unknown notification type" });
    }
    res.json({ message: "Data Received" });
  } catch (error) {
    logger.error("Error::", error);
  }
});

module.exports = router;
