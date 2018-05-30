#!/bin/bash

mvn clean install package -DskipTests=true
cf t
echo -n "Validate the space & org, you are currently logged in before continuing!"
read
cf cs p-mysql 100mb zipkin-db
cf cs p-rabbitmq standard config-event-bus
cf cs p-service-registry standard service-registry
echo "Checking status of the Spring Cloud Service Instances!"
until [ `cf service service-registry | grep -c "succeeded"` -eq 1  ]
do
  echo -n "."
  sleep 5s
done
echo
echo "Service instances created. Pushing all required applications."

cf p