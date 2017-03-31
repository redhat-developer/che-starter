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
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.che.starter.exception.KeycloakException;
import io.fabric8.che.starter.util.UrlHelper;

@Component
public class KeycloakClient {
    private static final String ACCESS_TOKEN = "access_token";

    @Value("${OPENSHIFT_TOKEN_URL:http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/openshift-v3/token}")
    private String openShiftTokenUrl;

    @Value("${GITHUB_TOKEN_URL:http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token}")
    private String gitHubTokenUrl;

    public String getOpenShiftToken(String keycloakToken) throws JsonProcessingException, IOException, KeycloakException {
        // {"access_token":"token","expires_in":86400,"scope":"user:full","token_type":"Bearer"}
        String responseBody = getResponseBody(openShiftTokenUrl, keycloakToken);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        JsonNode accessToken = json.get(ACCESS_TOKEN);
        if (accessToken == null) {
            throw new KeycloakException("Unable to obtain OpenShift token");
        }
        return accessToken.asText();
    }

    public String getGitHubToken(String keycloakToken) throws KeycloakException {
        // access_token=token&scope=scope
        String responseBody = getResponseBody(gitHubTokenUrl, keycloakToken);
        Map<String, String> parameter = UrlHelper.splitQuery(responseBody);
        String token = parameter.get(ACCESS_TOKEN);
        if (token == null) {
            throw new KeycloakException("Unable to obtain GitHub token");
        }
        return token;
    }

    private String getResponseBody(String endpoint, String keycloakToken) {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(Collections.singletonList(new KeycloakInterceptor(keycloakToken)));
        ResponseEntity<String> response = template.exchange(endpoint.toString(), HttpMethod.GET, null, String.class);
        return response.getBody();
    }

}
