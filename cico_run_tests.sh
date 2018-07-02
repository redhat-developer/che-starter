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

eval "$(./env-toolkit load -f jenkins-env.json KEYCLOAK_TOKEN)"

yum -y update
yum -y install epel-release
yum -y install centos-release-scl java-1.8.0-openjdk-devel curl jq
yum -y install rh-maven33

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
