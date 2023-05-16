#!/bin/bash

cid=`cat ./cid`

echo "container id is $cid"

docker exec -d ${cid} /root/ct-server/bin/stop.sh

docker stop ${cid}
docker rm ${cid}
echo "stopped"
docker rmi contract_validation
exit 0