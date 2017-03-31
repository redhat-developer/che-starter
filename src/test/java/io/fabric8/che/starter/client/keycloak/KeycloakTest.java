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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.KeycloakException;

@Ignore("Valid keycloak token must be provided")
public class KeycloakTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(KeycloakTest.class);
    private static final String AUTH_HEADER = "Bearer <ACCESS_TOKEN>";

    @Autowired
    KeycloakClient keycloakClient;

    @Test
    public void getGitHubToken() throws KeycloakException {
        String gitHubToken = keycloakClient.getGitHubToken(AUTH_HEADER);
        LOG.info("GitHub Token: {}", gitHubToken);
    }

    @Test
    public void getOpenShiftToken() throws JsonProcessingException, IOException, KeycloakException {
        String openShiftToken = keycloakClient.getOpenShiftToken(AUTH_HEADER);
        LOG.info("OpenShift Token: {}", openShiftToken);
    }

}
