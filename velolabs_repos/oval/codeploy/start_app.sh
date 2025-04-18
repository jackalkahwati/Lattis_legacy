#!/bin/bash
set -euxo pipefail

APP_HOME=/opt/oval
cd $APP_HOME && pm2 start ecosystem.config.js
