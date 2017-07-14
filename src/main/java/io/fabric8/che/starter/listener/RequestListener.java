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
package io.fabric8.che.starter.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.che.starter.client.keycloak.KeycloakTokenParser;

@WebListener
public class RequestListener implements ServletRequestListener {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REQUEST_ID_MDC_KEY = "req_id";
    private static final String IDENTITY_ID_MDC_KEY = "identity_id";
    private static final String UNKNOWN_IDENTITY_ID = "Unknown";

    @Autowired
    KeycloakTokenParser keycloakTokenParser;

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        requestId = (StringUtils.isBlank(requestId)) ? generateRequestId() : requestId;

        String keycloakToken = request.getHeader(AUTHORIZATION_HEADER);
        String identityId = getIdentityId(keycloakToken);

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        MDC.put(IDENTITY_ID_MDC_KEY, identityId);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        MDC.clear();
    }

    private String getIdentityId(final String keycloakToken) {
        String identityId;
        if (StringUtils.isBlank(keycloakToken)) {
            identityId = UNKNOWN_IDENTITY_ID;
        } else {
            try {
                identityId = keycloakTokenParser.getIdentityId(keycloakToken);
            } catch (Exception e) {
                identityId = UNKNOWN_IDENTITY_ID;
            }
        }
        return identityId;
    }

    private String generateRequestId() {
        return RandomStringUtils.random(16, true, true).toLowerCase();
    }

}
