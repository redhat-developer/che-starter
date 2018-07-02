#!/bin/bash

###
# #%L
# che-starter
# %%
# Copyright (C) 2017 Red Hat, Inc.
# %%
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# #L%
###

eval "$(./env-toolkit load -f jenkins-env.json \
          DEVSHIFT_TAG_LEN \
          RHCHEBOT_DOCKER_HUB_PASSWORD  \
          GIT_COMMIT \
          KEYCLOAK_TOKEN  \
          QUAY_USERNAME  \
          QUAY_PASSWORD)"

yum -y update
yum -y install epel-release
yum -y install centos-release-scl java-1.8.0-openjdk-devel docker curl jq
yum -y install rh-maven33

# TARGET variable gives ability to switch context for building rhel based images, default is "centos"
# If CI slave is configured with TARGET="rhel" RHEL based images should be generated then.
TARGET=${TARGET:-"centos"}

# Keycloak token provided by `che_functional_tests_credentials_wrapper` from `openshiftio-cico-jobs` is a refresh token.
# Obtaining osio user token
AUTH_RESPONSE=$(curl -H "Content-Type: application/json" -X POST -d '{"refresh_token":"'$KEYCLOAK_TOKEN'"}' https://auth.prod-preview.openshift.io/api/token/refresh)

# `OSIO_USER_TOKEN` is used for che-starter integration tests which are run against prod-preview
export OSIO_USER_TOKEN=$(echo $AUTH_RESPONSE | jq --raw-output ".token | .access_token")

systemctl start docker

scl enable rh-maven33 'mvn clean verify -B'

if [ $? -eq 0 ]; then

  export PROJECT_VERSION=`mvn -o help:evaluate -Dexpression=project.version | grep -e '^[[:digit:]]'`

  if [ $TARGET == "rhel" ]; then
    DOCKERFILE="Dockerfile.rhel"
    IMAGE_URL="quay.io/openshiftio/rhel-almighty-che-starter"
  else
    DOCKERFILE="Dockerfile"
    IMAGE_URL="quay.io/openshiftio/almighty-che-starter"
  fi

  if [ -n "${QUAY_USERNAME}" -a -n "${QUAY_PASSWORD}" ]; then
    docker login -u ${QUAY_USERNAME} -p ${QUAY_PASSWORD} quay.io
  else
    echo "Could not login, missing credentials for the registry"
  fi

  docker build -t rhche/che-starter:latest -f ${DOCKERFILE} .

  if [ $? -ne 0 ]; then
    echo 'Docker Build Failed!'
    exit 2
  fi

  TAG=$(echo $GIT_COMMIT | cut -c1-${DEVSHIFT_TAG_LEN})

  #push to docker.io ONLY if not RHEL
  if [ $TARGET != "rhel" ]; then
    docker login -u rhchebot -p $RHCHEBOT_DOCKER_HUB_PASSWORD -e noreply@redhat.com
    docker tag rhche/che-starter:latest rhche/che-starter:$TAG
    docker push rhche/che-starter:latest
    docker push rhche/che-starter:$TAG
  fi

  docker tag rhche/che-starter:latest ${IMAGE_URL}:$TAG
  docker push ${IMAGE_URL}:$TAG

  docker tag rhche/che-starter:latest ${IMAGE_URL}:latest
  docker push ${IMAGE_URL}:latest

else
  echo 'Build Failed!'
  exit 1
fi
