#!/bin/bash

sdir="$(dirname $0)"

cd $sdir

java -jar ./matebotctl.jar &
