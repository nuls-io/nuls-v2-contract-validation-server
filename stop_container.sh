#!/bin/bash

cid=`cat ./cid`

echo "container id is $cid"

docker exec -d ${cid} /root/ct-server/bin/stop.sh

docker stop ${cid}

echo "stopped"

exit 0