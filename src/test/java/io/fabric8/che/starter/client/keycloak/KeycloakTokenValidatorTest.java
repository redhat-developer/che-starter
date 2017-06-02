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

import org.junit.Test;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.KeycloakException;

public class KeycloakTokenValidatorTest extends TestConfig {
    private static final String VALID_TOKEN = "Bearer token";
    private static final String INVALID_TOKEN = "token";


    @Test
    public void processValidToken() throws KeycloakException {
        KeycloakTokenValidator.validate(VALID_TOKEN);
    }

    @Test(expected = KeycloakException.class)
    public void processInvalidToken() throws KeycloakException {
        KeycloakTokenValidator.validate(INVALID_TOKEN);
    }
}
