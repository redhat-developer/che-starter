How to deploy che-starter on Minishift ?
========================================

* Start Minishift with the `minishift start` command.

* Use the minishift `docker-env` command to export the environment variables that are required to reuse the daemon:

```bash
$ eval $(minishift docker-env)
````
You should now be able to use docker on the command line of your host, talking to the docker daemon inside the Minishift VM.

* Build `rhche/che-starter` Docker image:

```bash
cd che-starter
mvn clean package
docker build -t rhche/che-starter .
````

* Login to Minishift and create a new project: 

```bash
oc login -u developer -p developer
oc new-project che-starter
````

* Deploy che-starter via templates from `openshift-template` folder:

```bash
oc process -f che-starter.app.yaml | oc apply -f -
````
