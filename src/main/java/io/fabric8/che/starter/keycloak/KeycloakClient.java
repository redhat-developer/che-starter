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
package io.fabric8.che.starter.keycloak;

import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.util.UrlHelper;

@Component
public class KeycloakClient implements TokenReceiver {
    private static final Logger LOG = LogManager.getLogger(KeycloakClient.class);
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SCOPE = "scope";

    @Override
    public String getOpenShiftToken(String authHeader) {
        return getToken(KeycloakEndpoint.GET_OPENSHIFT_TOKEN, authHeader);
    }

    @Override
    public String getGitHubToken(String authHeader) {
        return getToken(KeycloakEndpoint.GET_GITHUB_TOKEN, authHeader);
    }

    private String getToken(KeycloakEndpoint endpoint, String authHeader) {
        // access_token=token&scope=scope
        String responseBody = getResponseBody(endpoint, authHeader);
        Map<String, String> parameter = UrlHelper.splitQuery(responseBody);
        String token = parameter.get(ACCESS_TOKEN);
        LOG.info("Token: {}", token);
        String skope = parameter.get(SCOPE);
        LOG.info("Skope: {}", skope);
        return token;
    }

    private String getResponseBody(KeycloakEndpoint endpoint, String authHeader) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<String> response = template.exchange(endpoint.toString(), HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

}
