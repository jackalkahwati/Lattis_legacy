#!/bin/bash

set -euxo pipefail

APP_HOME=/opt/oval

if [[ "$DEPLOYMENT_GROUP_NAME" == *"dev"* ]]
then
    SSM_PATH=/env/dev/oval/
    REGION=ca-central-1
else
    SSM_PATH=/env/prod/oval/
    REGION=ca-central-1
fi

aws ssm get-parameters-by-path --with-decryption \
      --query 'Parameters[*].{Name:Name,Value:Value}' \
      --path "$SSM_PATH" --region $REGION |  \
      jq -r 'map("\(.Name | sub("'$SSM_PATH'";""))=\(.Value)") | join("\n")' > $APP_HOME/.env

chown -R oval:oval $APP_HOME
