#!/bin/bash
set -euxo pipefail

APP_HOME=/opt/lattis-iot-apis-nodejs
cd $APP_HOME && pm2 start ecosystem.config.js
