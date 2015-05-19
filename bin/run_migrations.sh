#!/bin/bash

# Run decima migrations.
# See README file for options - but command can be migrate / undo / redo

BASEDIR=$(dirname $0)/..

if [ -z "$DECIMA_CONFIG" ]; then
    echo "Using default configuration, to set a config file use $DECIMA_CONFIG."
else
    echo "Using configuration file specified in DECIMA_CONFIG environment variable"
    JAVA_ARGS="-Dconfig.file=$DECIMA_CONFIG"
fi

JARFILE=$BASEDIR/target/scala-2.11/decima-assembly-*.jar

if [ ! -e $JARFILE ]; then
  cd ${BASEDIR} && sbt assembly
fi

COMMAND=${1:-migrate}

echo Running MigrateSchema $COMMAND $2...
java ${JAVA_ARGS} -cp ${JARFILE} com.socrata.decima.MigrateSchema ${COMMAND} $2
