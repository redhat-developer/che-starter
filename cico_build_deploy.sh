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
cat jenkins-env | grep -e RHCHEBOT_DOCKER_HUB_PASSWORD -e GIT > inherit-env
. inherit-env

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel docker
yum -y install rh-maven33

systemctl start docker

scl enable rh-maven33 'mvn clean verify -B'

if [ $? -eq 0 ]; then

  export PROJECT_VERSION=`mvn -o help:evaluate -Dexpression=project.version | grep -e '^[[:digit:]]'`

  docker build -t rhche/che-starter:latest .

  if [ $? -ne 0 ]; then
    echo 'Docker Build Failed!'
    exit 2
  fi

  TAG=$(echo $GIT_COMMIT | cut -c1-7)

  docker login -u rhchebot -p $RHCHEBOT_DOCKER_HUB_PASSWORD -e noreply@redhat.com

  docker tag rhche/che-starter:latest rhche/che-starter:$TAG
  docker push rhche/che-starter:latest
  docker push rhche/che-starter:$TAG

  docker tag rhche/che-starter:latest registry.devshift.net/almighty/che-starter:$TAG
  docker push registry.devshift.net/almighty/che-starter:$TAG

  docker tag rhche/che-starter:latest registry.devshift.net/almighty/che-starter:latest
  docker push registry.devshift.net/almighty/che-starter:latest

else
  echo 'Build Failed!'
  exit 1
fi
