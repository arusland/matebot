#!/usr/bin/env bash

echo "Run matebot app..."

docker run --restart=always -d -v /etc/matebot/logs:/app/logs -v /etc/matebot/data:/app/data --name matebot matebot
