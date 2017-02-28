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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
final class CheServerPod {
    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";

    @Value("${che.openshift.project}")
    private String project;

    @Value("${che.openshift.pod}")
    private String podName;

    public void startPodIfNeeded(OpenShiftClient client) {
        Pod pod = client.pods().inNamespace(project).withName(podName).get();
        String status = pod.getStatus().getPhase();
        if (!status.equals(OPENSHIFT_POD_STATUS_RUNNING)) {
            // start pod
        }
    }
}
