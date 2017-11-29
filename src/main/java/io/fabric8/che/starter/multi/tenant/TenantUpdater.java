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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;

@Component
public class TenantUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(TenantUpdater.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "req_id";

    @Value("${UPDATE_TENANT_ENDPOINT:https://api.openshift.io/api/user/services}")
    private String updateTenantEndpoint;

    @Async
    public void update(final String keycloakToken) {
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set(REQUEST_ID_HEADER, getRequestId());
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        template.setRequestFactory(requestFactory);
        ResponseEntity<String> response = template.exchange(updateTenantEndpoint, HttpMethod.PATCH, entity, String.class);
        LOG.info("User tenant has been updated. Status code {}", response.getStatusCode());
    }

    private String getRequestId() {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        return StringUtils.isNotBlank (requestId) ? requestId : RandomStringUtils.random(16, true, true).toLowerCase();
    }


}
