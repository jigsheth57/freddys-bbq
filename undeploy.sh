#!/bin/bash

echo y| cf d -r admin-portal-bbq
echo y| cf d -r customer-portal-bbq
echo y| cf d -r order-service-bbq
echo y| cf d -r menu-service-bbq
echo y| cf d -r zipkin-bbq

cf ds -f bckservice-db
cf ds -f circuit-breaker
cf ds -f config-server
cf ds -f event-bus
cf ds -f service-registry
cf ds -f zipkin-db
