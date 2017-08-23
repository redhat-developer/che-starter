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

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.client.CheRestEndpoints;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class CheServerRouteChecker {
    private static final Logger LOG = LoggerFactory.getLogger(CheServerRouteChecker.class);

    @Autowired
    private CheServerRoute route;

    @Value("${che.openshift.start.timeout}")
    private String startTimeout;

    /**
     * @see <a href="https://bugzilla.redhat.com/show_bug.cgi?id=1481603">The endpoints value always be delayed</a>
     * 
     * @param client
     * @param namespace
     * @throws RouteNotFoundException
     */
    public void waitUntilRouteIsAccessible(final OpenShiftClient client, final String namespace, final String keycloakToken) throws RouteNotFoundException {
        long start = System.currentTimeMillis();
        long end = start + Long.valueOf(startTimeout);
        while (System.currentTimeMillis() < end) {
            if (isRouteAccessible(client, namespace, keycloakToken)) {
                break;
            }
        }
    }

    public boolean isRouteAccessible(final OpenShiftClient client, final String namespace, final String keycloakToken) {
        try {
            String routeURL = route.getUrl(client, namespace);
            String listWorkspacesURL = CheRestEndpoints.LIST_WORKSPACES.generateUrl(routeURL);
            URL url = new URL(listWorkspacesURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (StringUtils.isNotBlank(keycloakToken)) {
                connection.setRequestProperty("Authorization", keycloakToken);
            }
            int statusCode = connection.getResponseCode();
            boolean isRouteAccessible = isValid(statusCode);
            LOG.info("Che server endpoint '{}' is accessible: {}", listWorkspacesURL, isRouteAccessible);
            return isRouteAccessible;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValid(int statusCode) {
        return (200 == statusCode || 403 == statusCode || 302 == statusCode);
    }

}
