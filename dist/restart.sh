#!/bin/bash

sdir="$(dirname $0)"

echo "Killing all started matebot instances..."
pgrep -a -f matebot.jar | awk '{print $1;}' | while read -r a; do kill -9 $a; done

cd $sdir

if [ "" == "$MB_START_REASON" ]; then
    MB_START_REASON=restart
fi

java -DstartReason=$MB_START_REASON -jar ./matebot.jar &
