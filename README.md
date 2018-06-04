
[![Build Status](https://ci.centos.org/buildStatus/icon?job=devtools-che-starter-build-che-credentials-master)](https://ci.centos.org/job/devtools-che-starter-build-che-credentials-master)

che-starter
===========

REST endpoints for managing [Eclipse Che](http://www.eclipse.org/che/) workspaces.

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

To tell it which application properties file to use (located in the `src/main/resources` directory) specify the `spring.profiles.active` parameter, like so:

```bash
    $ java -Dspring.profiles.active=local -jar target/che-starter-1.0-SNAPSHOT.jar 
````

Once the service is running, it would be available with [Swagger](http://swagger.io/) documentation on [http://localhost:10000/](http://localhost:10000/)

* Docker container:

```bash
docker build -t rhche/che-starter .
docker run -p 10000:10000 -t rhche/che-starter
````

Profiles
--------
There are two available profiles, local and test. To select which profile to use, add the `-Dspring.profiles.active` property to the command line:

```bash
    $ java -Dspring.profiles.active=local -jar target/che-starter-1.0-SNAPSHOT.jar
````

Debugging
---------
There are several ways for debugging the project:

* From Eclipse IDE you can simply right-click on `Application.java` -> **Debug As..** -> **Java Application**

* Running the project in the debug mode from the command line: 

```bash
    $ java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -Dspring.profiles.active=local -jar target/che-starter-1.0-SNAPSHOT.jar 
````

Integration test
----------------
Integration tests are running as part of the build against multi-tenant Che server deployed on prod-preview osd.
In order to successfully run those tests locally `OSIO_USER_TOKEN` env var must be set before building the project:

```bash
    $ export OSIO_USER_TOKEN=<OSIO_PROD_PREVIEW_USER_TOKEN>
````

Tests can be skipped via `-DskipTests` mvn command line argument.

CI Jobs
-------
1. https://ci.centos.org/job/devtools-che-starter-prcheck/

2. https://ci.centos.org/job/devtools-che-starter-build-che-credentials-master/

On success, the second job will push the che-starter image to [Docker Hub](https://hub.docker.com/r/rhche/che-starter/). Another copy is pushed to the local CentOS CI registry. The CentOS CI Registry hosted image can then be used by other components in the CentOS CI services, either as triggers or as a point of integration.

Live preview
------------

- http://che-starter-dsaas-preview.b6ff.rh-idev.openshiftapps.com

Testing with Minishift
----------------------
che-starter can be tested locally against Minishift. The instructions for running Minishift can be found in the [How to deploy che-starter on Minishift ?](https://github.com/redhat-developer/che-starter/tree/master/openshift-template) document.

Code Conventions
----------------
- Indent using spaces only
- New line in the end

[Apache Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is used for validating the code conventions. All conventions can be found in the [checkstyle.xml](https://github.com/redhat-developer/che-starter/blob/master/src/main/resources/checkstyle.xml)

License
-------
EPL 1.0, See [LICENSE](LICENSE.txt) file.

[License Maven Plugin](http://www.mojohaus.org/license-maven-plugin/) is used for license management. In order to update headers in source files run the following command: 

```bash
    $ mvn license:update-file-header
````
