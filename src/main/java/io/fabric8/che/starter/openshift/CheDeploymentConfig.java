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

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.openshift.api.model.DeploymentCondition;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.ClientDeployableScalableResource;

@Component
final class CheDeploymentConfig {

    @Value("${che.openshift.deploymentconfig}")
    private String deploymentConfigName;

    @Value("${che.openshift.start.timeout}")
    private String startTimeout;

    public void deployCheIfSuspended(OpenShiftClient client, String namespace) {
        final ClientDeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig> deploymentConfig = getDeploymentConfig(client, namespace);
        if (!isDeploymentAvailable(client, namespace)) {
            deploymentConfig.scale(1, false);
            waitUntilDeploymentConfigIsAvailable(client, namespace);
        }
    }

    private ClientDeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig> getDeploymentConfig(
            OpenShiftClient client, String namespace) {
        return client.deploymentConfigs().inNamespace(namespace).withName(deploymentConfigName);
    }

    private void waitUntilDeploymentConfigIsAvailable(final OpenShiftClient client, String namespace) {
        final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);

        final Runnable readinessPoller = new Runnable() {
            public void run() {
                try {
                    if (isDeploymentAvailable(client, namespace)) {
                        queue.put(true);
                        return;
                    } else {
                        queue.put(false);
                        return;
                    }
                } catch (Throwable t) {
                    try {
                        if (queue.isEmpty()) {
                            queue.put(false);
                        }
                        return;
                    } catch (InterruptedException e) {
                    }
                }
            }
        };

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> poller = executor.scheduleWithFixedDelay(readinessPoller, 0, 500, TimeUnit.MILLISECONDS);
        executor.schedule(new Runnable() {

            @Override
            public void run() {
                poller.cancel(true);
            }
        }, Integer.valueOf(startTimeout), TimeUnit.SECONDS);
        try {
            while (!waitUntilReady(queue)) {
            }
        } finally {
            if (!poller.isDone()) {
                poller.cancel(true);
            }
            executor.shutdown();
        }
    }

    private boolean isDeploymentAvailable(OpenShiftClient client, String namespace) {
        DeploymentConfig deploymentConfig = getDeploymentConfig(client, namespace).get();
        if (deploymentConfig == null) {
            return false;
        }
        List<DeploymentCondition> conditions = deploymentConfig.getStatus().getConditions();
        if (!conditions.isEmpty()) {
            for (DeploymentCondition condition : conditions) {
                if (condition.getType().equals("Available") && condition.getStatus().equals("True")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean waitUntilReady(BlockingQueue<Object> queue) {
        try {
            Object obj = queue.poll(2, TimeUnit.SECONDS);
            if (obj == null) {
                return true;
            }
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            }
            return false;
        } catch (InterruptedException ex) {
            return true;
        }
    }
}
