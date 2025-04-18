#!/bin/bash

# Try every 15 seconds
maxRetries=40
retryInterval=15
echo "Waiting for the deployment to be ready for testing"
retry=0
deployment=$1
until [ ${retry} -ge ${maxRetries} ]
do
    status=`aws deploy get-deployment --deployment-id ${deployment} --region ca-central-1 | jq '.deploymentInfo.status'`
    echo "::debug::The deployment is currently in the status $status"
    if [ ${status} == "\"Succeeded"\" ];then
        echo "::debug::The deploym ${deployment} Succeeded"
        exit 0
    elif [ ${status} == "\"Failed"\" ];then
        echo "::debug::The deploym ${deployment} Failed"
        exit 1
    fi
    retry=$[${retry}+1]
    echo "::debug::Retrying [${retry}/${maxRetries}] in ${retryInterval}(s) "
    sleep ${retryInterval}
done
        
if [ ${retry} -ge ${maxRetries} ]; then
    echo "::warning::Deployment Ready Check Timed Out after ${maxRetries} attempts"
fi
