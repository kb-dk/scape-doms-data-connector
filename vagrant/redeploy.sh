#!/bin/bash

SCRIPT_DIR=$(dirname $(readlink -f $0))
source $SCRIPT_DIR/setenv.sh

cp /target/scape-doms-data-connector-*.war $HOME/$TOMCAT/webapps/scape-doms-data-connector.war
