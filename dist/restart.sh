#!/bin/bash

sdir="$(dirname $0)"

echo "Killing all started matebot instances..."
pgrep -a -f matebot.jar | awk '{print $1;}' | while read -r a; do kill -9 $a; done

cd $sdir

java -DstartReason=restart -jar ./matebot.jar &
