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

import org.springframework.stereotype.Component;

import io.fabric8.che.starter.exception.MultiTenantMigrationException;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class MigrationCongigMap {
    private static final String REQUEST_ID = "request-id";
    private static final String MIGRATION_CONFIG_MAP_NAME = "migration";

    public boolean exists(final OpenShiftClient client, final String namespace) {
        return getConfigMap(client, namespace) != null;
    }

    public String getRequestId(final OpenShiftClient client, final String namespace) throws MultiTenantMigrationException {
        ConfigMap cm = getConfigMap(client, namespace);
        if (cm == null) {
            throw new MultiTenantMigrationException(MIGRATION_CONFIG_MAP_NAME + " config map does not exist");
        }
        return cm.getData().get(REQUEST_ID);
    }

    private ConfigMap getConfigMap(final OpenShiftClient client, final String namespace) {
        return client.configMaps().inNamespace(namespace).withName(MIGRATION_CONFIG_MAP_NAME).get();
    }
}
