#!/bin/bash

set -euxo pipefail

APP_HOME=/opt/lattis-iot-apis-nodejs
SSM_PATH=/env/prod/mob-e-iot/
REGION=ap-south-1

aws ssm get-parameters-by-path --with-decryption \
      --query 'Parameters[*].{Name:Name,Value:Value}' \
      --path "$SSM_PATH" --region $REGION |  \
      jq -r 'map("\(.Name | sub("'$SSM_PATH'";""))=\(.Value)") | join("\n")' > $APP_HOME/.env

chown -R ubuntu:ubuntu $APP_HOME
