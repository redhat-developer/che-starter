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
yum -y install centos-release-scl java-1.8.0-openjdk-devel git patch bzip2 docker
yum -y install rh-maven33

sed -i '/OPTIONS=.*/c\OPTIONS="--selinux-enabled --log-driver=journald --insecure-registry registry.ci.centos.org:5000"' /etc/sysconfig/docker
systemctl start docker

scl enable rh-maven33 'mvn clean verify'

if [ $? -eq 0 ]; then

  export PROJECT_VERSION = `mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`

  docker build --build-arg VERSION=${PROJECT_VERSION} -t rhche/che-starter:nightly .

  if [ $? -ne 0 ]; then
    echo 'Docker Build Failed'
    exit 2
  fi

  docker login -u rhchebot -p $RHCHEBOT_DOCKER_HUB_PASSWORD -e noreply@redhat.com
  docker push rhche/che-starter:nightly

  docker tag rhche/che-starter:nightly registry.ci.centos.org:5000/almighty/che-starter:nightly
  docker push registry.ci.centos.org:5000/almighty/che-starter:nightly

else
  echo 'Build Failed!'
  exit 1
fi
