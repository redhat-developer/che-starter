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
package io.fabric8.che.starter.client;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.model.GitHubUserInfo;
import io.fabric8.che.starter.model.Token;

@Component
public class GitHubClient {
    private static final Logger LOG = LogManager.getLogger(GitHubClient.class);
    
    @Value("${GITHUB_USER_URL:https://api.github.com/user}")
    private String GIT_HUB_USER_ENDPOINT;

    /**
     * Uploads the Github oAuth token to the workspace so that the developer can send push requests
     *  
     * @param cheServerURL
     * @param gitHubToken
     * @throws IOException
     * @throws GitHubOAthTokenException 
     */
    public void setGitHubOAuthToken(final String cheServerURL, final String gitHubToken, final String keycloakToken)
            throws IOException, GitHubOAthTokenException {
        String url = cheServerURL + CheRestEndpoints.SET_OAUTH_TOKEN.getEndpoint().replace("{provider}", "github");

        Token token = new Token();
        token.setToken(gitHubToken);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Token> entity = new HttpEntity<Token>(token, headers);

        try {
            template.postForLocation(url, entity);
        } catch (Exception e) {
            LOG.error("Error setting GitHub OAuth token on Che Server: {}", cheServerURL);
            throw new GitHubOAthTokenException("Error setting GitHub OAuth token", e);
        }
    }

    public GitHubUserInfo getUserInfo(final String gitHubToken) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + gitHubToken);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<GitHubUserInfo> response = template.exchange(GIT_HUB_USER_ENDPOINT, HttpMethod.GET, entity, GitHubUserInfo.class);
        return response.getBody();
    }
}
