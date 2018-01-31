#!/bin/bash
if [ "$1" == "" ]
then
	read msg
else
	msg=$1
fi
git stash && git pull && git stash apply && git add * && git commit -m $msg && git push