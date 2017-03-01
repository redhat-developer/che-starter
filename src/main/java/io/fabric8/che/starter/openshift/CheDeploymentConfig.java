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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.ClientDeployableScalableResource;

@Component
final class CheDeploymentConfig {

	@Value("${che.openshift.project}")
	private String project;

	@Value("${che.openshift.deploymentconfig}")
	private String deploymentConfigName;

	@Value("${che.openshift.start.timeout}")
	private String startTimeout;

	public void startPodIfNeeded(OpenShiftClient client) {
		final ClientDeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig> deploymentConfig = client
				.deploymentConfigs().inNamespace(project).withName(deploymentConfigName);
		if (deploymentConfig.get().getSpec().getReplicas().intValue() != 1) {
			deploymentConfig.scale(1, true);
			waitUntilDeploymentConfigIsScaled(client);
		}
		// TODO Process exception
	}

	private ClientDeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig> getDeploymentConfig(
			OpenShiftClient client) {
		return client.deploymentConfigs().inNamespace(project).withName(deploymentConfigName);
	}

	private void waitUntilDeploymentConfigIsScaled(final OpenShiftClient client) {
		final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);

		final Runnable deploymentPoller = new Runnable() {
			public void run() {
				try {
					DeploymentConfig deploymentConfig = getDeploymentConfig(client).get();
					if (deploymentConfig != null) {
						if (deploymentConfig.getStatus().getReadyReplicas().intValue() != 1) {
							queue.put(true);
						}
					} else {
						queue.put(true);
					}
				} catch (Throwable t) {
					return;
				}

			}
		};

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture poller = executor.scheduleWithFixedDelay(deploymentPoller, 0, 1000, TimeUnit.MILLISECONDS);
		try {
			Utils.waitUntilReady(queue, Integer.valueOf(startTimeout) * 1000, TimeUnit.MILLISECONDS);
		} finally {
			poller.cancel(true);
			executor.shutdown();
		}
	}
}
