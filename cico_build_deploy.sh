#!/bin/bash
yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel rh-maven33

sed -i '/OPTIONS=.*/c\OPTIONS="--selinux-enabled --log-driver=journald --insecure-registry registry.ci.centos.org:5000"' /etc/sysconfig/docker
systemctl start docker

scl enable rh-maven33 'mvn clean verify'

if [ $? -eq 0 ]; then

  docker build -t rhche/che-starter:latest .

  if [ $? -ne 0 ]; then
    echo 'Docker Build Failed'
    exit 2
  fi

  docker tag rhche/che-server:latest registry.ci.centos.org:5000/almighty/che-starter:latest
  docker push registry.ci.centos.org:5000/almighty/che-starter:latest

else
  echo 'Build Failed!'
  exit 1
fi
