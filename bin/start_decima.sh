#!/bin/bash
# Start Decima service
BASEDIR=$(dirname $0)/..
CONFIG=${DECIMA_CONFIG:-$BASEDIR/conf/default.conf}
JARFILE=$BASEDIR/target/scala-2.11/lachesis-assembly-*.jar
if [ ! -e $JARFILE ]; then
  cd $BASEDIR && sbt assembly
fi
java -Dconfig.file=$CONFIG -jar $JARFILE &
