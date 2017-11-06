#!/bin/bash

sdir="$(dirname $0)"

echo "Killing all started matebot instances..."
pgrep -a -f matebot.jar | awk '{print $1;}' | while read -r a; do kill -9 $a; done

cd $sdir

if [ -z "$1" ]
then
    MB_START_REASON=restart
else
    MB_START_REASON=$1
fi

echo reason: "$MB_START_REASON"

java -Dfile.encoding=UTF-8 -DstartReason=$MB_START_REASON -jar ./matebot.jar &
