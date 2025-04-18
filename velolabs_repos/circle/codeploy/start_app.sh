#!/bin/bash
set -euxo pipefail

APP_HOME=/opt/circle
if [[ "$DEPLOYMENT_GROUP_NAME" == *"dev"* ]]
then
	pm2 start "$APP_HOME/circle.dev.sh"
else
	pm2 start "$APP_HOME/circle.sh"
fi 
