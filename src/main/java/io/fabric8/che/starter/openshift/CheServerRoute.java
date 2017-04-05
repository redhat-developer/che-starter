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

import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public final class CheServerRoute {
    private static final Logger LOG = LogManager.getLogger(CheServerRoute.class);

    @Value("${che.openshift.route}")
    private String cheRoute;

    @Value("${che-host.openshift.route}")
    private String cheHostRoute;

    public String getUrl(final OpenShiftClient client, final String namespace) throws RouteNotFoundException {
        Route route = getRouteByName(client, namespace, cheRoute);
        if (route == null) {
            LOG.warn("Route '" + cheRoute + "' not found. Trying to get '" + cheHostRoute + "' route");
            route = getRouteByName(client, namespace, cheHostRoute);
        }
        if (route != null) {
            String host = route.getSpec().getHost();
            String protocol = route.getSpec().getPort().getTargetPort().getStrVal();
            LOG.info("Host '{}' has been found", host);
            LOG.info("Route protocol '{}'", protocol);
            return protocol + "://" + host;
        }
        throw new RouteNotFoundException(
                "Routes '" + cheRoute + "'/'" + cheHostRoute + "' not found in '" + namespace + "' namespace");
    }

    private Route getRouteByName(final OpenShiftClient client, final String namespace, final String routeName) {
        return client.routes().inNamespace(namespace).withName(routeName).get();
    }

}
