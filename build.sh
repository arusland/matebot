#!/bin/bash

rm ./dist/*.jar
mvn clean package -DskipTests
cp ./matebot-core/target/matebot-core-*-jar-with-dependencies.jar ./dist/matebot.jar
cp ./matebot-ctl/target/matebot-ctl-*-jar-with-dependencies.jar ./dist/matebotctl.jar

