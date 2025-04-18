const cpus = require("os").cpus().length;
require("dotenv").config();
const SET_CPUS = process.env.SET_CPUS;
let recommendedCPU = -1;
if(SET_CPUS && typeof parseInt(SET_CPUS) === "number") {
  if(SET_CPUS >= cpus) recommendedCPU = -1;
  else recommendedCPU = SET_CPUS;
}

module.exports = {
  apps: [{
    name: "GPSService",
    script: "./app.js",
    instances: recommendedCPU,
    exec_mode: "cluster"
  }]
};
