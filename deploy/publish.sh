#!/bin/bash

cp ../matebot-core/target/matebot-core-*-jar-with-dependencies.jar ./matebot.jar
cp ../matebot-ctl/target/matebot-ctl-*-jar-with-dependencies.jar ./matebotctl.jar

if [ -z "$MATEBOT_HOST"]; then
	echo "Env variable not defined: MATEBOT_HOST"
	exit 2
fi

echo "Copying files to remote $MATEBOT_HOST..."

NOW=$(date +"%Y-%m-%d-%H-%M-%S")
ssh root@$MATEBOT_HOST 'mkdir /tmp/'$NOW
scp -r ./* root@$MATEBOT_HOST:/tmp/$NOW
ssh root@$MATEBOT_HOST 'sh /tmp/'$NOW/deploy.sh
