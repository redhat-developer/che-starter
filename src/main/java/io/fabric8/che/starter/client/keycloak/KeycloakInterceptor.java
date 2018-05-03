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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

class KeycloakInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakInterceptor.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "req_id";
    private static final String BEARER_PREFIX = "Bearer ";
    private String keycloakToken;

    public KeycloakInterceptor(String keycloakToken) {
        this.keycloakToken = keycloakToken.startsWith(BEARER_PREFIX) ? keycloakToken : BEARER_PREFIX + keycloakToken;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add(AUTHORIZATION_HEADER, keycloakToken);
        headers.add(REQUEST_ID_HEADER, getRequestId());
        return execution.execute(request, body);
    }

    private String getRequestId() {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        LOG.debug("'X-Request-Id' sent {}", requestId);
        return requestId;
    }
}
