#!/usr/bin/env bash
jarName=$1
rulesFile=$2
java -jar $jarName \
 --rules $rulesFile \
 --topic fashion \
 --client-secret $CLIENT_SECRET \
 --appId $APP_ID \
 -Dorg.asynchttpclient.webSocketMaxFrameSize=512000 \
 -Djava.util.logging.config.file=logging.properties