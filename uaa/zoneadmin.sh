#!/bin/bash
set -e
# vars to define
# UAA_ENDPOINT eg uaa.systemdomain.com
# ADMIN_CLIENT_ID eg admin
# ADMIN_CLIENT_SECRET get this from ops manager
# IDENTITY_ZONE_ID this is the guid of the identity zone, it’s the first guid in the URI for any page in the Pivotal SSO UI
# ZONEADMIN_CLIENT_ID pick a name for the admin client in the zone
# ZONEADMIN_CLIENT_SECRET

source set-env.sh

uaac target $UAA_ENDPOINT --skip-ssl-validation
uaac token client get $ADMIN_CLIENT_ID -s $ADMIN_CLIENT_SECRET
uaac curl /oauth/clients -k -H "Content-type:application/json" -H "X-Identity-Zone-Id:$IDENTITY_ZONE_ID" -X POST -d "{\"client_id\":\"$ZONEADMIN_CLIENT_ID\",\"client_secret\":\"$ZONEADMIN_CLIENT_SECRET\",\"scope\":[\"uaa.none\"],\"resource_ids\":[\"none\"],\"authorities\":[\"uaa.admin\",\"clients.read\",\"clients.write\",\"scim.read\",\"scim.write\",\"scim.userids\",\"clients.secret\"],\"authorized_grant_types\":[\"client_credentials\"]}"
