const fs = require("fs");
const path = require("path");

const KEY = fs.readFileSync(
  path.join(__dirname, "..", "certificates", process.env.KEYPATH_FILENAME)
);
const CERT = fs.readFileSync(
  path.join(__dirname, "..", "certificates", process.env.CERTPATH_FILENAME)
);
const CA = fs.readFileSync(
  path.join(__dirname, "..", "certificates", process.env.CAPATH_FILENAME)
);
const HOST = process.env.AWS_IOT_HOST;

const option = {
  host: HOST,
  key: KEY,
  cert: CERT,
  ca: CA,
  port: process.env.AWS_IOT_PORT,
};

module.exports = { option };
