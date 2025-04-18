const cpus = require('os').cpus().length
const SET_CPUS = process.env.SET_CPUS
let recommendedCPU = -1

module.exports = {
  apps: [{
    name: 'lattis-iot-apis-nodejs',
    script: './server.js',
    instances: recommendedCPU,
    exec_mode: 'cluster'
  }]
}
