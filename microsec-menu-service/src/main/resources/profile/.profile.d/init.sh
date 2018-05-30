#!/bin/bash
echo "This is profile.d"
export JAVA_HOME=./.java-buildpack/open_jdk_jre
export PATH=$PATH:$JAVA_HOME/bin
openssl pkcs12 -export -in $CF_INSTANCE_CERT -inkey $CF_INSTANCE_KEY -out temp_pk12.p12 -passout "pass:${STORE_PASS}"
keytool -importkeystore -srckeystore temp_pk12.p12 -srcstoretype PKCS12 -srcstorepass ${STORE_PASS} -destkeystore server.jks -deststoretype pkcs12 -deststorepass ${STORE_PASS} -noprompt
mv server.jks BOOT-INF/classes/
