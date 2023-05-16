#!/bin/bash
docker rmi contract_validation:latest
docker build -t contract_validation .

exit 0