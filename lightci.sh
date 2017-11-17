#!/bin/bash

script_dir="$(dirname $0)"
url=https://github.com/arusland/matebot.git

echo "Getting current commit of $url"

cur_commit="$(git ls-remote $url master | cut -f 1)"
filename="$script_dir/$(echo "$url" | sha1sum | awk '{print $1}')"
cur_time="$(date -R)"

if [ -f "$filename" ]
then
	echo "$cur_time - $filename found."
	last_commit="$(cat $filename)"
else
	echo "$cur_time - $filename not found.">>lightci.log
	last_commit=""
fi

echo cur_commit=$cur_commit
echo last_commit=$last_commit
echo filename=$filename

if [[ "$cur_commit" != "" && "$cur_commit" != "$last_commit" ]]; then
  cur_time="$(date -R)"
  echo "We have new commit!"
  echo "$cur_commit">$filename
  echo "$cur_time - cur_commit=$cur_commit, last_commit=$last_commit">>lightci.log

  cmd=$script_dir/redeploy.sh
  echo "Executing command $cmd...."

  sh $cmd
fi
