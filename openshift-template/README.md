# How to deploy che-starter on Minishift

```sh
minishift start

eval $(minishift docker-env)


docker build -t rhche/che-starter:nightly .

cd openshift-template

oc create -f che-starter.app.yaml

oc new-app --template=che-starter
```
