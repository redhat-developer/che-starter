# che-starter

This service provides REST endpoints for managing Che server instances for users.

Building
--------
mvn clean install

Running
-------
java -jar target/che-starter-1.0-SNAPSHOT.jar


Debugging
---------
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -jar target/che-starter-1.0-SNAPSHOT.jar 


Once the service is running, refer to the REST API documentation here:

http://localhost:10000/swagger-ui.html
