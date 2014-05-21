#!/usr/bin/env bash

apt-get update > /dev/null
apt-get install -y zip unzip

apt-get install -y python-software-properties
add-apt-repository ppa:webupd8team/java
apt-get update > /dev/null

#echo debconf shared/accepted-oracle-license-v1-1 select true |  sudo debconf-set-selections
#echo debconf shared/accepted-oracle-license-v1-1 seen true |  sudo debconf-set-selections
#apt-get install -y oracle-java7-installer oracle-java7-set-default
apt-get install -y openjdk-7-jdk

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/


sudo apt-get install -y tomcat6-user

echo "192.168.50.2 doms-testbed" >> /etc/hosts
echo "192.168.50.4 domsgui-testbed" >> /etc/hosts
echo "192.168.50.6 doms-scape-testbed" >> /etc/hosts
