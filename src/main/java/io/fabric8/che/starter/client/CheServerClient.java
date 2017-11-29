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

import io.fabric8.che.starter.exception.MultiTenantMigrationException;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.model.server.CheServerInfo;
import io.fabric8.che.starter.multi.tenant.MigrationCongigMap;
import io.fabric8.che.starter.multi.tenant.MigrationPod;
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

    @Autowired
    MigrationCongigMap migrationCongigMap;

    @Autowired
    MigrationPod migrationPod;

    public CheServerInfo getCheServerInfo(OpenShiftClient client, String namespace, String requestURL,
            String keycloakToken) throws MultiTenantMigrationException {
        if (toggle.isMultiTenant(keycloakToken)) {
            return getCheServerInfoForMultiTenant(client, namespace, requestURL, keycloakToken);
        } else {
            return getCheServerInfoForSingleTenant(client, namespace, requestURL, keycloakToken);
        }
    }

    @Async
    public void startCheServer(OpenShiftClient client, String namespace, String keycloakToken)
            throws RouteNotFoundException {
        if (toggle.isMultiTenant(keycloakToken)) {
            return; // che-starter is not supposed to start multi-tenant che-server
        }
        cheDeploymentConfig.deployCheIfSuspended(client, namespace);
    }

    private CheServerInfo getCheServerInfoForSingleTenant(OpenShiftClient client, String namespace, String requestURL,
            String keycloakToken) {
        boolean isCheServerReadyToHandleRequests;
        boolean isDeploymentAvailable = cheDeploymentConfig.isDeploymentAvailable(client, namespace);

        if (isDeploymentAvailable) {
            isCheServerReadyToHandleRequests = cheServerRouteChecker.isRouteAccessible(client, namespace, keycloakToken);
        } else {
            isCheServerReadyToHandleRequests = false;
        }

        CheServerInfo info = CheServerHelper.generateCheServerInfo(isCheServerReadyToHandleRequests, requestURL, false);
        return info;
    }

    private CheServerInfo getCheServerInfoForMultiTenant(OpenShiftClient client, String namespace, String requestURL,
            String keycloakToken) {
        if (cheDeploymentConfig.deploymentExists(client, namespace) && !migrationCongigMap.exists(client, namespace)) {
            // user is supposed to be multi-tenant but still has single-tenant che-server in *-che namespace,
            // and does not have 'migration' cm, so update tenant must be called
            tenanUpdater.update(keycloakToken);
            // wait 10 seconds to be sure that 'migration' cm would be created - indication that migration has started
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // migration has just started
            return CheServerHelper.generateCheServerInfo(false, requestURL, true);
        } else if (migrationPod.exists(client, namespace)
                && (!migrationPod.isReady(client, namespace) || migrationPod.isRunning(client, namespace))) {
            // migration is in progress
            return CheServerHelper.generateCheServerInfo(false, requestURL, true);
        } else if (migrationPod.isTerminated(client, namespace)) {
            // migration has been already done
            return CheServerHelper.generateCheServerInfo(true, requestURL, true);
        } else {
            // Should only happen if migration pod have been removed manually
            return CheServerHelper.generateCheServerInfo(true, requestURL, true);
        }
    }

}
