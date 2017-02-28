/*-
 * #%L
 * che-starter
 * %%
 * Copyright (C) 2017 Red Hat, Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package io.fabric8.che.starter.openshift;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.openshift.client.OpenShiftClient;

public class RouterTest extends TestConfig {

    @Autowired
    CheServerRouter router;

    @Autowired
    OpenShiftClientWrapper client;

    @Value("${che.openshift.username}")
    private String username;

    @Value("${che.openshift.password}")
    private String password;

    @Value("${che.openshift.endpoint}")
    private String endpoint;

    @Ignore("Test is run against local minishift cluster and requires additional setup")
    @Test
    public void testRouteURL() {
        OpenShiftClient openShiftClient = client.get(endpoint, username, password);
        router.getUrl(openShiftClient);
    }

}
