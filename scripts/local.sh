#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

# this step builds our artifact
lein do clean, uberjar

# the path where the artifact is
jarfile=$root/target/uberjar/*-standalone.jar

mv $jarfile $root/app.jar

rm -rf $root/target
