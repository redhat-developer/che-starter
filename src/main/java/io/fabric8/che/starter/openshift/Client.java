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

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class Client {

    public OpenShiftClient get(String masterUrl, String token) {
         Config config = new ConfigBuilder().withMasterUrl(masterUrl).withOauthToken(token).build();
         return new DefaultOpenShiftClient(config);
    }

    public OpenShiftClient get(String masterUrl, String username, String password) {
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).withUsername(username).withPassword(password).build();
        return new DefaultOpenShiftClient(config);
    }

}
