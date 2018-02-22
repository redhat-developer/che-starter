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
cat jenkins-env | grep -e KEYCLOAK_TOKEN > inherit-env
. inherit-env

yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel curl
yum -y install rh-maven33

# installing jq via curl since 'No package jq available' for yum
curl -LO https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64
mv jq-linux64 /usr/bin/jq
chmod +x /usr/bin/jq

# Keycloak token provided by `che_functional_tests_credentials_wrapper` from `openshiftio-cico-jobs` is a refresh token. 
# Obtaining osio user token
AUTH_RESPONSE=$(curl -H "Content-Type: application/json" -X POST -d '{"refresh_token":"'$KEYCLOAK_TOKEN'"}' https://auth.prod-preview.openshift.io/api/token/refresh)

# `OSIO_USER_TOKEN` is used for che-starter integration tests which are run against prod-preview
export OSIO_USER_TOKEN=$(echo $AUTH_RESPONSE | jq --raw-output ".token | .access_token")

scl enable rh-maven33 'mvn clean verify -B'

if [ $? -ne 0 ]; then
    echo 'Build Failed!'
    exit 1
fi
