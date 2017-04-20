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

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

public class KeycloakRestTemplate extends RestTemplate {

    public KeycloakRestTemplate(final String keycloakToken) {
        if (StringUtils.isNotBlank(keycloakToken)) {
            this.setInterceptors(Collections.singletonList(new KeycloakInterceptor(keycloakToken)));
        }
    }

}
