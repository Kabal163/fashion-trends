#!/usr/bin/env bash
jarName=$1
java -jar jarName \
 -Dorg.asynchttpclient.webSocketMaxFrameSize=512000 \
 -Djava.util.logging.config.file=logging.properties