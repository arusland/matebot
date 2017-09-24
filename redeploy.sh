#!/bin/bash

sdir="$(dirname $0)"
pid=$(ps -eaf | awk '$10=="./matebot.jar" {print $2}' | head -1)

echo "Script dir=$sdir"
echo "pid=$pid"

cd $sdir

git reset --hard HEAD
git pull

mvn clean package -DskipTests

if [ "$pid" != "" ]; then
  echo "Killing process $pid..."
  kill -9 $pid
fi

rm ./dist/*.jar
cp ./matebot-core/target/matebot-core-*-jar-with-dependencies.jar ./dist/matebot.jar

cd $sdir/dist

java -jar ./matebot.jar &
