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

echo "Installing certificate into $CHE_STARTER_HOME/InstallCert/ directory"

# Import the certificate
cd $CHE_STARTER_HOME/InstallCert/

echo "Import the remote certificate from ${OSO_ADDRESS}"
java InstallCert $OSO_ADDRESS << ANSWERS
1
ANSWERS

echo "Export the certificate into the keystore for ${OSO_DOMAIN_NAME}"
keytool -exportcert -alias $OSO_DOMAIN_NAME-1 -keystore jssecacerts -storepass changeit -file $KUBERNETES_CERTS_CA_FILE

cd $CHE_STARTER_HOME/


exec java -Djava.security.egd=file:/dev/./urandom -jar ${CHE_STARTER_HOME}/app.jar $@
exit $?
