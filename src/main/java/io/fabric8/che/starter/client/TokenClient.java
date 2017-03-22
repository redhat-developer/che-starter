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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.template.TokenTemplate;

@Component
public class TokenClient {
    private static final Logger LOG = LogManager.getLogger(TokenClient.class);

    @Autowired
    private TokenTemplate tokenTemplate;

    /**
     * Uploads the Github oAuth token to the workspace so that the developer can send push requests
     *  
     * @param cheServerURL
     * @param token
     * @throws IOException
     * @throws GitHubOAthTokenException 
     */
    public void setGitHubOAuthToken(String cheServerURL, String token) 
            throws IOException, GitHubOAthTokenException {
        String url = cheServerURL + CheRestEndpoints.SET_OAUTH_TOKEN.getEndpoint().replace("{provider}", "github");
        String jsonTemplate = tokenTemplate.createRequest().setToken(token).getJSON();

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(jsonTemplate, headers);
        
        try {
            template.postForLocation(url, entity);
        } catch (Exception e) {
            LOG.error("Error setting GitHub oAuth token on Che Server: " + cheServerURL, e);
            throw new GitHubOAthTokenException(e);
        }
    }
}
