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

To tell it which application properties file to use (located in the src/main/resources directory) specify the spring.profiles.active parameter, like so:

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
There are two available profiles, local and test. To select which profile to use, add the -Dspring.profiles.active property to the command line:

```bash
    $ java -Dspring.profiles.active=local -jar target/che-starter-1.0-SNAPSHOT.jar
````

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

- http://che-starter.prod-preview.openshift.io/

Testing with Minishift
----------------------
che-starter can be tested locally against Minishift. The instructions for running Minishift can be found in the [How to run Che on OpenShift](https://github.com/redhat-developer/rh-che#how-to-run-che-on-openshift) document.

Executing Requests
------------------
Many of the services available via the Swagger interface require setting masterUrl, namespace and Authorization token.

- The masterUrl value is the Openshift REST API endpoint. If running against Minishift, execute the following to open the minishift console:

```bash
    minishift console
````
- Copy the URL excluding the path component, this will be used as the masterUrl.  For example, a console address of https://192.168.42.64:8443/console/ would mean the masterUrl value is https://192.168.42.64:8443/.

- The namespace is the project name in Openshift in which the Che server is deployed (usually "eclipse-che").  Logging in as the developer user (described next) will list the user's projects.

- In order to obtain the Authorization token use the following command:

```bash
    oc login -u developer -p developer
    oc whoami -t
````

The output of this command (which looks like a random string of characters and numbers) is the authorization token.

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


