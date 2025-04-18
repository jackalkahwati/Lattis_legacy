/** @format */

const isInsidePolygon = require("point-in-polygon");
const GeofenceModel = require("../database/models/geofence");
const logger = require("../utils/logger").logger;

/**
 * check if point is inside geofence defined by circle
 *
 * @param {*} currentLocation Array [latitude, longitude]
 * @param {*} circle Object e.g {'center': {'latitude': 45.49407318666883, 'longitude': -73.61138788210224}, 'radius': 4.649803617592324}
 * @returns
 */
const isInsideCircle = (currentLocation, circle) => {
  const {
    center: { latitude, longitude },
    radius
  } = circle;
  const ky = 40000 / 360; // kilometers per degree for latitude
  const kx = Math.cos((Math.PI * latitude) / 180.0) * ky; // kilometres per degree for longitude
  const dx = Math.abs(longitude - currentLocation[1]) * kx;
  const dy = Math.abs(latitude - currentLocation[0]) * ky;
  return Math.sqrt(dx * dx + dy * dy) <= radius.value;
};

const isOutofGeofence = async (fleetId, currentLocation) => {
  try {
    // An array of geofences
    const geofences = await GeofenceModel.find({ fleet_id: fleetId }).exec();
    const values = [];
    for (const geo of geofences) {
      const geometry = geo.geometry;
      let points;
      if (geometry.shape === "polygon") {
        points = geometry.points.map((point) => [point.latitude, point.longitude]);
        values.push(isInsidePolygon(currentLocation, points));
      } else if (geometry.shape === "rectangle") {
        const [bottomLeftLng, bottomLeftLtd, topRightLng, topRightLtd] = geometry.bbox;
        // convert bound box to polygon
        points = [
          [bottomLeftLtd, bottomLeftLng],
          [bottomLeftLtd, topRightLng],
          [topRightLtd, topRightLng],
          [topRightLtd, bottomLeftLng]
        ];

        values.push(isInsidePolygon(currentLocation, points));
      } else if (geometry.shape === "circle") {
        const { units, value } = geometry.radius;
        let distanceInKm = value;
        if (units === "miles") distanceInKm = value / 0.62137;
        const circle = {
          center: geometry.center,
          radius: { ...geometry.radius, value: distanceInKm }
        };
        const insideCircle = isInsideCircle(currentLocation, circle);
        values.push(insideCircle);
      }
    }
    if (!values.length) return false;
    const outOfGeo = values.every((val) => val === false); // If all are false, then it's outside all geofences for that fleet
    return outOfGeo;
  } catch (error) {
    logger.error("Error::", error.message || error);
  }
};

module.exports = { isOutofGeofence };
