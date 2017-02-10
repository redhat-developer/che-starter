# How to deploy che-starter on OpenShift

```sh
cd che-starter

docker build -t rhche/che-starter:nightly .

cd openshift-template

oc create -f che_starter_template.json

oc new-app --template=eclipse-che-starter
```
