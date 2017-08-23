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
package io.fabric8.che.starter.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.model.server.CheServerInfo;
import io.fabric8.che.starter.openshift.CheDeploymentConfig;
import io.fabric8.che.starter.openshift.CheServerRouteChecker;
import io.fabric8.che.starter.util.CheServerHelper;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class CheServerClient {

    @Autowired
    CheDeploymentConfig cheDeploymentConfig;

    @Autowired
    private CheServerRouteChecker cheServerRouteChecker;

    public CheServerInfo getCheServerInfo(OpenShiftClient client, String namespace, String requestURL, String keycloakToken) {
        boolean isCheServerReadyToHandleRequests;
        boolean isDeploymentAvailable = cheDeploymentConfig.isDeploymentAvailable(client, namespace);

        if (isDeploymentAvailable) {
            isCheServerReadyToHandleRequests = cheServerRouteChecker.isRouteAccessible(client, namespace, keycloakToken);
        } else {
            isCheServerReadyToHandleRequests = false;
        }

        CheServerInfo info = CheServerHelper.generateCheServerInfo(isCheServerReadyToHandleRequests, requestURL);
        return info;
    }

    @Async
    public void startCheServer(OpenShiftClient client, String namespace) throws RouteNotFoundException {
        cheDeploymentConfig.deployCheIfSuspended(client, namespace);
    }

}
