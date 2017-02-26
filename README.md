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

1. http://che-starter.os.ci.centos.org/

2. http://che-starter.demo.almighty.io/

Testing with Minishift
----------------------
che-starter can be tested locally against Minishift:

- Get [Minishift](https://github.com/minishift/minishift#installation), create an OpenShift cluster (`minishift start`), open the web console (`minishift console`) and read the instructions to install the OpenShift CLI (help->Command Line Tools)

- Install [gofabric8](https://github.com/fabric8io/gofabric8#gofabric8-installer)

- Update `/etc/hosts` with a line that associates minishift IP address (`minishift ip`) and the hostname `che.openshift.mini`

- Run che-starter locally and follow the instructions for deploying che server via fabric8-online template:

```bash
    oc login -u openshift-dev -p devel
    oc new-project che

    oc login -u system:admin
    oc adm policy add-scc-to-user privileged -z che
    oc adm policy add-cluster-role-to-user cluster-admin system:serviceaccount:che:che

    oc login -u openshift-dev -p devel
    export ONLINE_VERSION=1.0.32
    oc apply -f http://central.maven.org/maven2/io/fabric8/online/apps/che/$ONLINE_VERSION/che-$ONLINE_VERSION-openshift.yml
    oc expose service che-host --hostname=che.openshift.mini

    oc login -u system:admin
    gofabric8 volumes
````

- After deployment Che will be available on http://che.openshift.mini/

- Once deployed, you can send requests from che-starter against Minishift. In order to obtain token use the following command:

```bash
    oc login -u openshift-dev -p devel
    oc whoami -t
````

License
-------
EPL 1.0, See [LICENSE](LICENSE.txt) file.

[License Maven Plugin](http://www.mojohaus.org/license-maven-plugin/) is used for license management. In order to update headers in source files run the following command: 

```bash
    $ mvn license:update-file-header
````