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

import org.apache.commons.lang3.StringUtils;

import io.fabric8.che.starter.exception.InvalidKeycloakTokenFormatException;

public class KeycloakTokenValidator {
    private static final String BEARER_PREFIX = "Bearer ";

    public static void validate(final String keycloakToken) throws InvalidKeycloakTokenFormatException {
        if (!isValid(keycloakToken)) {
            throw new InvalidKeycloakTokenFormatException("Keycloak token must have 'Bearer ' prefix");
        }
    }

    private static boolean isValid(final String keycloakToken) {
        return (StringUtils.isNotBlank(keycloakToken) && keycloakToken.startsWith(BEARER_PREFIX));
    }
}
