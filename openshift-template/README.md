# How to deploy che-starter on OpenShift

```sh
cd che-starter

# build the image
docker build -t rhche/che-starter:nightly .

oc create -f che_starter_template.json

oc new-app --template=eclipse-che-starter
```
