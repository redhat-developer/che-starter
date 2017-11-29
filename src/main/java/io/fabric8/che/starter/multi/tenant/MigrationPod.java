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
package io.fabric8.che.starter.multi.tenant;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.ContainerStateRunning;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class MigrationPod {
    private static final Logger LOG = LoggerFactory.getLogger(MigrationPod.class);
    private static final String MIGRATION_POD_LABEL = "migration";
    private static final String REQUEST_ID_ANNOTATION = "request-id";

    @Autowired
    MigrationCongigMap migrationCongigMap;

    public boolean exists(final OpenShiftClient client, final String namespace) {
        Pod pod = getPod(client, namespace);
        return (pod != null);
    }

    public boolean isReady(final OpenShiftClient client, final String namespace) {
        Pod pod = getPod(client, namespace);
        if (pod != null) {
            return pod.getStatus().getContainerStatuses().get(0).getReady();
        }
        return false;
    }

    public boolean isRunning(final OpenShiftClient client, final String namespace) {
        Pod pod = getPod(client, namespace);
        if (pod != null) {
            ContainerStateRunning running = pod.getStatus().getContainerStatuses().get(0).getState().getRunning();
            return (running != null);
        }
        return false;
    }

    public boolean isTerminated(final OpenShiftClient client, final String namespace) {
        Pod pod = getPod(client, namespace);
        if (pod != null) {
            ContainerStateTerminated terminated = pod.getStatus().getContainerStatuses().get(0).getState().getTerminated();
            return (terminated != null);
        }
        return false;
    }

    private Pod getPod(OpenShiftClient client, String namespace) {
        try {
            Pod migrationPod = null;
            String requestId = migrationCongigMap.getRequestId(client, namespace);
            List<Pod> pods = client.pods().inNamespace(namespace).withLabel(MIGRATION_POD_LABEL).list().getItems();
            for (Pod pod : pods) {
                String requestIdAnnotaion = pod.getMetadata().getAnnotations().get(REQUEST_ID_ANNOTATION);
                if (requestId.equals(requestIdAnnotaion)) {
                    migrationPod = pod;
                    break;
                }
            }
            return migrationPod;
        } catch (Exception e) {
            LOG.error("Error occured during migration to multi-tenant Che server", e);
            return null;
        }
    }

}
