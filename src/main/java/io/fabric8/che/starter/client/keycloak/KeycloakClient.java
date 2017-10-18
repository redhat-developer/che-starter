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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.che.starter.exception.KeycloakException;

@Component
public class KeycloakClient {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakClient.class);
    private static final String ACCESS_TOKEN = "access_token";

    @Value("${OSO_ADMIN_TOKEN:#{null}}")
    private String openShiftAdminToken;

    @Value("${OPENSHIFT_TOKEN_URL:https://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/openshift-v3/token}")
    private String openShiftTokenUrl;

    @Value("${GITHUB_TOKEN_URL:https://auth.prod-preview.openshift.io/api/token?for=https://github.com}")
    private String gitHubTokenUrl;

    public String getOpenShiftToken(String keycloakToken) throws JsonProcessingException, IOException, KeycloakException {
        if (StringUtils.isNotBlank(openShiftAdminToken)) {
            LOG.info("Using OpenShift admin token");
            return openShiftAdminToken;
        }
        LOG.info("OpenShift token url - {}", openShiftTokenUrl);
        // {"access_token":"token","expires_in":86400,"scope":"user:full","token_type":"Bearer"}
        String token = getAccessToken(openShiftTokenUrl, keycloakToken);
        return token;
    }

    public String getGitHubToken(String keycloakToken) throws KeycloakException, JsonProcessingException, IOException {
        LOG.info("GitHub token url - {}", gitHubTokenUrl);
        // {"access_token":"token","scope":"admin:repo_hook,gist,read:org,repo,user","token_type":"bearer"}
        String token = getAccessToken(gitHubTokenUrl, keycloakToken);
        return token;
    }

    private String getAccessToken(String endpoint, String keycloakToken) throws KeycloakException, JsonProcessingException, IOException {
        String responseBody = getResponseBody(endpoint, keycloakToken);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(responseBody);
        JsonNode accessToken = json.get(ACCESS_TOKEN);
        if (accessToken == null) {
            throw new KeycloakException("Unable to obtain token from endpoint: " + endpoint);
        }
        return accessToken.asText();
    }

    private String getResponseBody(String endpoint, String keycloakToken) {
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        ResponseEntity<String> response = template.exchange(endpoint.toString(), HttpMethod.GET, null, String.class);
        return response.getBody();
    }

}
