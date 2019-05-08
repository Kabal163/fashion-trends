#!/usr/bin/env bash
jarName=$1
rulesFile=$2
java -jar $jarName \
 --rules $rulesFile \
 --topic fashion \
 --client_secret $CLIENT_SECRET \
 --app_id $APP_ID \
 -Dorg.asynchttpclient.webSocketMaxFrameSize=512000 \
 -Djava.util.logging.config.file=logging.properties