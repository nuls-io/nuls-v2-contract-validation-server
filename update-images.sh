git pull
mvn clean -DskipTests=true package
cd target && mkdir contract-validation-server && mv contract-validation-server.tar.gz contract-validation-server/
cd contract-validation-server && tar -xzf contract-validation-server.tar.gz
pwd
./make_image.sh
./stop_container.sh
./start_container.sh