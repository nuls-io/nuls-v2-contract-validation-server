#!/bin/bash

serverdir=$(cd `dirname $0`; pwd)

cid=`docker run -it -d -p 15151:15151 -v ${serverdir}/:/root/ct-server contract_validation`

echo "container id is $cid"

echo ${cid} > cid

docker exec -d ${cid} /root/ct-server/bin/start.sh

echo "started"

exit 0