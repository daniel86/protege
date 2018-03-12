#!/bin/bash

DIR=`dirname "$(readlink -f "$0")"`
echo $DIR
cd $DIR
cd protege-desktop/target/
cd protege-5.2.1-SNAPSHOT-platform-independent
cd Protege-5.2.1-SNAPSHOT 

./run.sh
