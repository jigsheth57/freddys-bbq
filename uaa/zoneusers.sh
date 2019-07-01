#!/bin/bash
# set -e

# vars to define
# ZONE_ENDPOINT eg freddys-bbq.login.systemdomain.com
# ZONEADMIN_CLIENT_ID
# ZONEADMIN_CLIENT_SECRET

source set-env.sh

uaac target $ZONE_ENDPOINT --skip-ssl-validation
uaac token client get $ZONEADMIN_CLIENT_ID -s $ZONEADMIN_CLIENT_SECRET
uaac user add frank --email frank@whitehouse.gov --given_name Frank --family_name Underwood -p password
uaac user add freddy --email freddy@freddysbbq.com --given_name Freddy --family_name Hayes -p password
uaac group add menu.read
uaac group add menu.write
uaac group add order.admin
uaac group add order.me
uaac group add admin.read
uaac group add admin.write
uaac member add menu.read frank
uaac member add menu.read freddy
uaac member add menu.write freddy
uaac member add order.admin freddy
uaac member add order.me frank
uaac member add admin.read freddy
uaac member add admin.write freddy

