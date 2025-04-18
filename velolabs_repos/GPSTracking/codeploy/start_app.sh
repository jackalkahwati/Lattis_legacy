#!/bin/bash
set -euxo pipefail

APP_HOME=/opt/gpstracking
cd $APP_HOME && pm2 start ecosystem.config.js
