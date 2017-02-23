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
cat jenkins-env | grep RHCHEBOT_DOCKER_HUB_PASSWORD > inherit-env
. inherit-env

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel docker
yum -y install rh-maven33

systemctl start docker

scl enable rh-maven33 'mvn clean verify -B'

if [ $? -eq 0 ]; then

  export PROJECT_VERSION=`mvn -o help:evaluate -Dexpression=project.version | grep -e '^[[:digit:]]'`

  docker build -t rhche/che-starter:nightly .

  if [ $? -ne 0 ]; then
    echo 'Docker Build Failed!'
    exit 2
  fi

  docker login -u rhchebot -p $RHCHEBOT_DOCKER_HUB_PASSWORD -e noreply@redhat.com
  docker push rhche/che-starter:nightly

  docker tag rhche/che-starter:nightly 8.43.84.245.xip.io/che/che-starter:nightly
  docker push 8.43.84.245.xip.io/che/che-starter:nightly

else
  echo 'Build Failed!'
  exit 1
fi
