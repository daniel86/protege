#!/bin/bash

DIR=`dirname "$(readlink -f "$0")"`
echo $DIR
cd $DIR
cd protege-desktop/target/
cd protege-5.6.0-beta-1-SNAPSHOT-platform-independent/Protege-5.6.0-beta-1-SNAPSHOT/

./run.sh
