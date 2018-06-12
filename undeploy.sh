#!/bin/bash

echo y| cf d -r admin-portal
echo y| cf d -r customer-portal
echo y| cf d -r order-service
echo y| cf d -r menu-service
echo y| cf d -r zipkin

cf ds -f bckservice-db
cf ds -f circuit-breaker
cf ds -f config-server
cf ds -f event-bus
cf ds -f service-registry
cf ds -f zipkin-db
