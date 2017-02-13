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
yum -y update
yum -y install centos-release-scl java-1.8.0-openjdk-devel
yum -y install rh-maven33

scl enable rh-maven33 'mvn clean verify -B'

if [ $? -ne 0 ]; then
    echo 'Build Failed!'
    exit 1
fi
