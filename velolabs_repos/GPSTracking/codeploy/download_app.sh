#!/bin/bash

set -euxo pipefail

APP_HOME=/opt/gpstracking

if [[ "$DEPLOYMENT_GROUP_NAME" == *"dev"* ]]
then
    SSM_PATH=/env/dev/gpstracking/
    REGION=ca-central-1
else
    SSM_PATH=/env/prod/gpstracking/
    REGION=ca-central-1
fi

aws ssm get-parameters-by-path --with-decryption \
      --query 'Parameters[*].{Name:Name,Value:Value}' \
      --path "$SSM_PATH" --region $REGION |  \
      jq -r 'map("\(.Name | sub("'$SSM_PATH'";""))=\(.Value)") | join("\n")' > $APP_HOME/.env

chown -R gpstracking:gpstracking $APP_HOME
