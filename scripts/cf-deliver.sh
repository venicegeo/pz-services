#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein

chmod 700 $root/lein

# this step builds our artifact
$root/lein do clean, uberjar

# the path where the artifact is
jarfile=$root/target/uberjar/*-standalone.jar

mv $jarfile $root/app.jar

rm -rf $root/target
