#!/bin/bash
cat jenkins-env | grep PASS > inherit-env
. inherit-env

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel rh-maven33

sed -i '/OPTIONS=.*/c\OPTIONS="--selinux-enabled --log-driver=journald --insecure-registry registry.ci.centos.org:5000"' /etc/sysconfig/docker
systemctl start docker

scl enable rh-maven33 'mvn clean verify'

if [ $? -eq 0 ]; then

  docker build -t rhche/che-starter:nightly .

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
