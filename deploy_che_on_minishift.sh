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

# Default OpenShift credentials
DEFAULT_CHE_OPENSHIFT_USERNAME="developer"
DEFAULT_CHE_OPENSHIFT_PASSWORD="developer"

# Default fabric8-online che template version. All template versions can be found on http://central.maven.org/maven2/io/fabric8/online/apps/che/
DEFAULT_CHE_TEMPLATE_VERSION=1.0.53

# CHE_OPENSHIFT_USERNAME and CHE_OPENSHIFT_PASSWORD variables can be used for custom credentials
CHE_OPENSHIFT_USERNAME=${CHE_OPENSHIFT_USERNAME:-${DEFAULT_CHE_OPENSHIFT_USERNAME}}
CHE_OPENSHIFT_PASSWORD=${CHE_OPENSHIFT_PASSWORD:-${DEFAULT_CHE_OPENSHIFT_PASSWORD}}

# CHE_TEMPLATE_VERSION variables can be used for specifying fabric8-online che template version 
CHE_TEMPLATE_VERSION=${CHE_TEMPLATE_VERSION:-${DEFAULT_CHE_TEMPLATE_VERSION}}

# Create OpenShift project
oc login -u ${CHE_OPENSHIFT_USERNAME} -p ${CHE_OPENSHIFT_PASSWORD}
oc new-project che

# Create a serviceaccount with privileged scc
oc login -u system:admin
oc adm policy add-scc-to-user privileged -z che

# Apply fabric8-online che template and create route by exposing 'che-host' service
oc login -u ${CHE_OPENSHIFT_USERNAME} -p ${CHE_OPENSHIFT_PASSWORD}
oc apply -f http://central.maven.org/maven2/io/fabric8/online/apps/che/$CHE_TEMPLATE_VERSION/che-$CHE_TEMPLATE_VERSION-openshift.yml
oc expose service che-host --hostname=che.openshift.mini

# 'gofabric8 volumes' is only needed for minishift and minikube. 
#  PVC (persistent volume claims) on a normal openshift or kubernetes cluster would be automatically bond to a real persistent volumes
oc login -u system:admin
gofabric8 volumes

# Reminder for updating /etc/hosts file with a line that associates minishift IP address and the hostname che.openshift.mini
echo "$(tput setaf 3)NOTE: update /etc/hosts file - $(tput setaf 2) $(minishift ip) che.openshift.mini $(tput sgr0)"
