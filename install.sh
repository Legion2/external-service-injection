#!/bin/bash
set -e

absPath=$(readlink -f "$0")
absDir=$(dirname "$absPath")

mvn "$@" clean install -f "$absDir/framework"
mvn "$@" clean install -f "$absDir/demo/messaging-adapter-api"
mvn "$@" clean install -f "$absDir/demo/calculator-api"
mvn "$@" clean package -f "$absDir/demo/TOSCA-service/tosca-mqtt-service-adapter"
mvn "$@" clean package -f "$absDir/demo/TOSCA-service/tosca-amqp-service-adapter"
mvn "$@" clean package -f "$absDir/demo/TOSCA-service/tosca-calculator-adapter"
mvn "$@" clean install -f "$absDir/demo/demo-message-consumer"
mvn "$@" clean install -f "$absDir/demo/demo-calculator"
mvn "$@" clean package -f "$absDir/demo/demo-servlet"
