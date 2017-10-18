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
package io.fabric8.che.starter.client.keycloak;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.KeycloakException;

@Ignore("Valid keycloak token must be provided")
public class KeycloakTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakTest.class);
    private static final String KEYCLOAK_TOKEN = "Bearer <KEYCLOAK TOKEN>";

    @Autowired
    KeycloakClient keycloakClient;

    @Test
    public void getGitHubToken() throws KeycloakException, JsonProcessingException, IOException {
        String gitHubToken = keycloakClient.getGitHubToken(KEYCLOAK_TOKEN);
        LOG.info("GitHub Token: {}", gitHubToken);
    }

    @Test
    public void getOpenShiftToken() throws JsonProcessingException, IOException, KeycloakException {
        String openShiftToken = keycloakClient.getOpenShiftToken(KEYCLOAK_TOKEN);
        LOG.info("OpenShift Token: {}", openShiftToken);
    }

}
