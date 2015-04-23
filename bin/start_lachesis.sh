#!/bin/bash
# Start Lachesis service
BASEDIR=$(dirname $0)/..
CONFIG=${LACHESIS_CONFIG:-$BASEDIR/conf/default.conf}
JARFILE=$BASEDIR/soda-fountain-jetty/target/scala-2.11/lachesis-assembly-*.jar
if [ ! -e $JARFILE ]; then
  cd $BASEDIR && sbt assembly
fi
java -Dconfig.file=$CONFIG -jar $JARFILE &