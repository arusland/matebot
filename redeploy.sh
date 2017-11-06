#!/bin/bash

sdir="$(dirname $0)"

echo "Script dir=$sdir"

cd $sdir

git reset --hard HEAD
git pull

mvn clean package -DskipTests
OUT=$?

if [ $OUT != 0 ]; then
   echo "Project rebuild failed: $OUT!"
   exit $OUT
fi

echo "Killing all started matebot instances..."
pgrep -a -f matebot.jar | awk '{print $1;}' | while read -r a; do kill -9 $a; done

rm ./dist/*.jar
cp ./matebot-core/target/matebot-core-*-jar-with-dependencies.jar ./dist/matebot.jar

cd $sdir/dist

java -DstartReason=redeploy -jar ./matebot.jar &
