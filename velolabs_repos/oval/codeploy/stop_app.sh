#!/bin/bash
set -euxo pipefail

APP_HOME=/opt/oval
cd $APP_HOME && pm2 stop ecosystem.config.js
