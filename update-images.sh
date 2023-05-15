git pull
docker rmi contract_validation
docker rmi docker.nuls.io/repository/docker/contract_validation
mvn clean -DskipTests=true package
./make_image.sh
docker tag contract_validation:latest docker.nuls.io/repository/docker/contract_validation:latest
docker push docker.nuls.io/repository/docker/contract_validation:latest