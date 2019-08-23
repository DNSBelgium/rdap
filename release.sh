#!/usr/bin/env bash

if [ -z $1 ]
then
  echo "Releasing new RDAP patch version"
  ./gradlew release -Prelease.useAutomaticVersion=true
else
  echo "Releasing RDAP-$1"
  ./gradlew release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=$1
fi
