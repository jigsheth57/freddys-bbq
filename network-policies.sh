#!/bin/bash
cf add-network-policy admin-portal-bbq --destination-app menu-service-bbq --protocol tcp --port 61001
cf add-network-policy admin-portal-bbq --destination-app order-service-bbq --protocol tcp --port 61001
cf add-network-policy customer-portal-bbq --destination-app menu-service-bbq --protocol tcp --port 61001
cf add-network-policy customer-portal-bbq --destination-app order-service-bbq --protocol tcp --port 61001
cf add-network-policy order-service-bbq --destination-app menu-service-bbq --protocol tcp --port 61001

# cf add-network-policy admin-portal-bbq --destination-app menu-service-bbq --protocol tcp --port 8080
# cf add-network-policy admin-portal-bbq --destination-app order-service-bbq --protocol tcp --port 8080
# cf add-network-policy customer-portal-bbq --destination-app menu-service-bbq --protocol tcp --port 8080
# cf add-network-policy customer-portal-bbq --destination-app order-service-bbq --protocol tcp --port 8080
# cf add-network-policy order-service-bbq --destination-app menu-service-bbq --protocol tcp --port 8080
