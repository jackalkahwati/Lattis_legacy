const requestIp = require("request-ip");

const SENTINEL_NOTIFICATIN_IPS = ["8.209.103.113", "8.209.80.190"];

const checkSourceIpAddress = (req) => {
  const clientIp = requestIp.getClientIp(req);
  return SENTINEL_NOTIFICATIN_IPS.includes(clientIp);
};

module.exports = { checkSourceIpAddress };
