git pull
docker rmi contract_validation
docker rmi docker.nuls.io/repository/docker/contract_validation
mvn clean -DskipTests=true package
cd target && mkdir contract-validation-server && mv contract-validation-server.tar.gz contract-validation-server/
cd contract-validation-server && tar -xzf contract-validation-server.tar.gz
./make_image.sh
docker tag contract_validation:latest docker.nuls.io/repository/docker/contract_validation:latest
docker push docker.nuls.io/repository/docker/contract_validation:latest