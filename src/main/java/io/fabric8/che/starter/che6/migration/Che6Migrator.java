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
package io.fabric8.che.starter.che6.migration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.client.WorkspaceClient;
import io.fabric8.che.starter.client.keycloak.KeycloakTokenParser;

@Component
public class Che6Migrator {
    private static final Logger LOG = LoggerFactory.getLogger(Che6Migrator.class);

    @Value("${CHE_TENANT_MAINTAINER_URL:http://che-tenant-maintainer:8080/che5ToChe6}")
    private String cheTenantMaintainerUrl;

    @Autowired
    KeycloakTokenParser keycoakTokenParser;

    @Autowired
    Che6MigrationMap che6MigrationMap;
    
    @Autowired
    WorkspaceClient workspaceClient;

    @Async
    public void migrateWorkspaces(final String keycloakToken, final String namespace) throws JsonProcessingException, IOException {
        String identityId = keycoakTokenParser.getIdentityId(keycloakToken);
        LOG.info("User '{}' should be migrated to che 6", identityId);
        LOG.info("Stopping all workspaces before migration");
        workspaceClient.stopWorkspaces(keycloakToken);

        RestTemplate template = new Che6MigratorRestTemplate(keycloakToken, namespace);
        ResponseEntity<Che6MigrationStatus> response = template.exchange(cheTenantMaintainerUrl, HttpMethod.GET, null, Che6MigrationStatus.class);

        Che6MigrationStatus status = response.getBody();
        LOG.info("Status code: {}", status.getCode());
        LOG.info("Status message: {}", status.getMessage());
        LOG.info("Status detals: {}", status.getDetails());

        che6MigrationMap.get().put(identityId, isSuccessful(status));
    }
    
    private boolean isSuccessful(final Che6MigrationStatus status) {
        return status.getCode() == 0 ? true : false;
    }
}
