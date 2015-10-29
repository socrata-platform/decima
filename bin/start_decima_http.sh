#!/bin/bash
# Start Decima service

BASEDIR=$(dirname $0)/..
if [ -z "$DECIMA_CONFIG" ]; then
  echo "Using default config, to set a config file use $DECIMA_CONFIG."
else
  JAVA_ARGS="-Dconfig.file=$DECIMA_CONFIG"
fi

if [ -z "$LOG_CONFIG" ]; then
  echo "Using default log file, set a log config file with $LOG_CONFIG."
else
  JAVA_ARGS="$JAVA_ARGS -Dlogback.configurationFile=$LOG_CONFIG"
fi

JARFILE=$BASEDIR/decima-http/target/scala-2.11/decima-http-assembly-*.jar

if [ ! -e $JARFILE ]; then
  cd $BASEDIR && sbt assembly
fi

java ${JAVA_ARGS} -jar $JARFILE
