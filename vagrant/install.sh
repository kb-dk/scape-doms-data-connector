#!/bin/sh


SCRIPT_DIR=$(dirname $(readlink -f $0))
source $SCRIPT_DIR/setenv.sh

tomcat6-instance-create $TOMCAT
cp /target/scape-doms-data-connector-*.war $HOME/$TOMCAT/webapps/scape-doms-data-connector.war
mkdir -p $HOME/$TOMCAT/conf/Catalina/localhost
cp /vagrant/vagrant/context.xml $HOME/$TOMCAT/conf/Catalina/localhost/context.xml.default
