#!/bin/bash

script_dir="$(dirname $0)"
url=https://github.com/arusland/matebot.git

echo "Getting current commit of $url"

cur_commit="$(git ls-remote $url master | cut -f 1)"
filename="$script_dir/$(echo "$url" | sha1sum | awk '{print $1}')"

if [ -f "$filename" ]
then
	echo "$filename found."
	last_commit="$(cat $filename)"
else
	echo "$filename not found."
	last_commit=""
fi

echo cur_commit=$cur_commit
echo last_commit=$last_commit
echo filename=$filename

if [ "$cur_commit" != "$last_commit" ]; then
  echo "We have new commit!"
  echo "$cur_commit">$filename

  cmd=$script_dir/redeploy.sh
  echo "Executing command $cmd...."

  sh $cmd  
fi