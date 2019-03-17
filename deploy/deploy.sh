#!/usr/bin/env bash

echo "Stopping old app..."
docker stop matebot

echo "Removing old app..."
docker rm matebot

NOW=$(date +"%Y-%m-%d-%H-%M-%S")
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Script dir - $SCRIPT_DIR"

echo "Backing up old app to /home/backup/matebot-$NOW.."
mv /home/matebot /home/backup/matebot-$NOW

mkdir /home/matebot

echo "Moving new app..."
mv $SCRIPT_DIR/* /home/matebot
cp ~/.matebot/application.properties /home/matebot/application.properties

cd /home/matebot

echo "Disk space info"

df -H

echo "Building docker image..."

docker build -t matebot .

sh run.sh
