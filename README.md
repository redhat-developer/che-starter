che-starter
===========

REST endpoints for managing [Eclipse Che](http://www.eclipse.org/che/) servers and workspaces.

Building from command line
--------------------------
[Apache Maven](https://maven.apache.org/) is used for building the project: 

```bash
    $ mvn clean verify
````

Running
-------
There are several ways for running the project:

* [Spring Boot Maven Plugin](http://docs.spring.io/spring-boot/docs/current/maven-plugin/index.html):

```bash
    $ mvn spring-boot:run
````

* From Eclipse IDE you can simply right-click on `Application.java` -> **Run As..** -> **Java Application**

* From command line:

```bash
    $ java -jar target/che-starter-1.0-SNAPSHOT.jar
````

Once the service is running, it would be available with [Swagger](http://swagger.io/) documentation on [http://localhost:10000/](http://localhost:10000/)

Debugging
---------
There are several ways for debugging the project:

* From Eclipse IDE you can simply right-click on `Application.java` -> **Debug As..** -> **Java Application**

* Running the project in the debug mode from the command line: 

```bash
    $ java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -jar target/che-starter-1.0-SNAPSHOT.jar 
````

CI Jobs
-------
1. https://ci.centos.org/job/devtools-che-starter/

2. https://ci.centos.org/job/devtools-che-starter-build-master/

On success, the second job will push the che-starter image to [Docker Hub](https://hub.docker.com/r/rhche/). Another copy is pushed to the local CentOS CI registry. The CentOS CI Registry hosted image can then be used by other components in the CentOS CI services, either as triggers or as a point of integration.

Live preview
------------

1. http://che-starter.os.ci.centos.org/

2. http://che-starter.demo.almighty.io/


License
-------
EPL 1.0, See [LICENSE](LICENSE.txt) file.

[License Maven Plugin](http://www.mojohaus.org/license-maven-plugin/) is used for license management. In order to update headers in source files run the following command: 

```bash
    $ mvn license:update-file-header
````