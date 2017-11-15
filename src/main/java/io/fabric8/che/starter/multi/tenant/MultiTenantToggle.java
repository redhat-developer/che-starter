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

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.client.keycloak.KeycloakTokenParser;
import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.util.UnleashConfig;

@Component
public class MultiTenantToggle {
    private static final Logger LOG = LoggerFactory.getLogger(MultiTenantToggle.class);

    private static final String APP_NAME = "che-starter";
    private static final String FEATURE_NAME = "deploy.che-multi-tenant";

    @Value("${HOSTNAME:che-starter-host}")
    private String hostname;

    @Value("${TOGGLE_URL:http://f8toggles/api}")
    private String unleashAPI;

    @Autowired
    KeycloakTokenParser keycloakTokenParser;

    private Unleash unleash;

    @PostConstruct
    public void initUnleash() {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(hostname)
                .unleashAPI(unleashAPI)
                .build();

        this.unleash = new DefaultUnleash(config);
    }

    public boolean isMultiTenant(final String keycloakToken) {
        try {
            UnleashContext context = getContext(keycloakToken);
            return unleash.isEnabled(FEATURE_NAME, context);
        } catch (IOException e) {
            LOG.error("Unable to get UnleashContext from the Keycloak token", e);
            return false;
        }
    }

    private UnleashContext getContext(final String keycloakToken) throws JsonProcessingException, IOException {
        String identityId = keycloakTokenParser.getIdentityId(keycloakToken);
        String sessionState = keycloakTokenParser.getSessionState(keycloakToken);
        LOG.info("Identity ID: {}", identityId);
        LOG.info("Session State: {}", sessionState);
        return UnleashContext.builder().userId(identityId).sessionId(sessionState).build();
    }

}
