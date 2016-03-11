#!/bin/bash -ex

pushd `dirname $0`/.. > /dev/null
root=$(pwd -P)
popd > /dev/null

export PATH=$PATH:$root/ci

! type lein >/dev/null 2>&1 && source $root/ci/lein.sh

lein do clean, test
