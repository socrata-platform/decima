#!/bin/bash
# Build a docker container

BASEDIR=$(dirname $0)/..

PROJECT="$1"
if [ $PROJECT == "" ]; then
  echo "Must provide a valid project name: decima-http or decima-poller"
  exit 1
fi

JARFILE=$BASEDIR/$PROJECT/target/scala-2.11/$PROJECT-assembly-*.jar

if [ ! -e $JARFILE ]; then
  cd $BASEDIR && sbt assembly
fi

cp $JARFILE $PROJECT/docker/$PROJECT-assembly.jar
docker build -t $PROJECT $PROJECT/docker
