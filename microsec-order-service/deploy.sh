#!/bin/bash

mvn clean install package -DskipTests=true
cf t
echo -n "Validate the space & org, you are currently logged in before continuing!"
read
cf cs p-identity auth sso
cf cs p-config-server standard config-server -c '{"git":{"uri":"https://github.com/jigsheth57/config-repo"}}'
cf cs p-service-registry standard service-registry
cf cs p-mysql 100mb mysql
cf cs p-rabbitmq standard config-event-bus
echo "Checking status of the Spring Cloud Service Instances!"
until [ `cf service config-server | grep -c "succeeded"` -eq 1  ]
do
  echo -n "."
  sleep 5s
done
until [ `cf service service-registry | grep -c "succeeded"` -eq 1  ]
do
  echo -n "."
  sleep 5s
done
echo
echo "Service instances created. Pushing all required applications."

cf p
