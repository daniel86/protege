#!/bin/bash

mvn install -Dmaven.test.skip=true
mvn package -Dmaven.test.skip=true

