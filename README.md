# che-starter

This service provides REST endpoints for managing Che server instances for users.

## Building

mvn clean install

## Running
java -jar target/che-starter-1.0-SNAPSHOT.jar


## Debugging
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -jar target/che-starter-1.0-SNAPSHOT.jar 


Once the service is running, refer to the REST API documentation here:

http://localhost:10000/swagger-ui.html

## CI
There is a CI job at https://ci.centos.org/job/devtools-che-starter-build-master/ that runs on each merge to master in this repo.

On success, it will push the che-starter image to dockerhub at rhche/che, and another copy is pushed to the local CentOS CI registry. The CentOS CI Registry hosted image can then be used by other components in the CentOS CI services, either as triggers or as a point of integration.
