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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
final class CheServerRoute {
    private static final Logger LOG = LogManager.getLogger(CheServerRoute.class);

    @Value("${che.openshift.route}")
    private String routeName;

    public String getUrl(OpenShiftClient client, String namespace) {
        Route route = client.routes().inNamespace(namespace).withName(routeName).get();
        String host = route.getSpec().getHost();
        LOG.info("Router host {}: ", host);
        return  "http://" + host;
    }

}
