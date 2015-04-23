#!/bin/bash

# Run lachesis migrations.
# See README file for options - but command can be migrate / undo / redo
BASEDIR=$(dirname $0)/..
if [ -z "$LACHESIS_CONFIG" ]; then
    CONFIG="$BASEDIR/conf/default.conf"
    echo "Using default configuration at: $CONFIG"
else
    echo "Using configuration file specified in LACHESIS_CONFIG environment variable"
    CONFIG=$LACHESIS_CONFIG
fi
#TODO: Fix JARFILE
JARFILE=$BASEDIR/soda-fountain-jetty/target/scala-2.1l/lachesis-assembly-*.jar
if [ ! -e $JARFILE ]; then
  cd $BASEDIR && sbt assembly
fi
COMMAND=${1:-migrate}
echo Running MigrateSchema $COMMAND $2...
java -Dconfig.file=$CONFIG -cp $JARFILE com.socrata.lachesis.db.MigrateSchema $COMMAND $2
