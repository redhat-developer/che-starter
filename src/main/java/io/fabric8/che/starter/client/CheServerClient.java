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

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.model.server.CheServerInfo;
import io.fabric8.che.starter.multi.tenant.MultiTenantToggle;
import io.fabric8.che.starter.multi.tenant.TenantUpdater;
import io.fabric8.che.starter.openshift.CheDeploymentConfig;
import io.fabric8.che.starter.openshift.CheServerRouteChecker;
import io.fabric8.che.starter.util.CheServerHelper;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class CheServerClient {

    @Autowired
    private CheDeploymentConfig cheDeploymentConfig;
    
    @Autowired
    private MultiTenantToggle toggle;

    @Autowired
    private CheServerRouteChecker cheServerRouteChecker;

    @Autowired
    private TenantUpdater tenanUpdater;

    public CheServerInfo getCheServerInfo(OpenShiftClient client, String namespace, String requestURL, String keycloakToken) {
        if (toggle.isMultiTenant(keycloakToken)) {
            if (cheDeploymentConfig.deploymentExists(client, namespace)) {
                // user is supposed to be multi-tenant but still has single-tenant che-server in the namespace,
                // so tenant needs to be updated
                tenanUpdater.update(keycloakToken);
                // wait 10 seconds to make sure that che deployment would be deleted
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return CheServerHelper.generateCheServerInfo(false, requestURL);
            } else {
                // multi-tenant che-server is supposed to be always accessible
                return CheServerHelper.generateCheServerInfo(true, requestURL);
            }
        }

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
    public void startCheServer(OpenShiftClient client, String namespace, String keycloakToken) throws RouteNotFoundException {
        if (toggle.isMultiTenant(keycloakToken)) {
            return; // che-starter is not supposed to start multi-tenant che-server
        }
        cheDeploymentConfig.deployCheIfSuspended(client, namespace);
    }

}
