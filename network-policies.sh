#!/bin/bash
cf add-network-policy admin-portal --destination-app menu-service --protocol tcp --port 8443
cf add-network-policy admin-portal --destination-app order-service --protocol tcp --port 8443
cf add-network-policy customer-portal --destination-app menu-service --protocol tcp --port 8443
cf add-network-policy customer-portal --destination-app order-service --protocol tcp --port 8443
cf add-network-policy order-service --destination-app menu-service --protocol tcp --port 8443
