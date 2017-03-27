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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.model.GitHubUserInfo;
import io.fabric8.che.starter.model.WorspacePreferences;

@Component
public class WorkspacePreferencesClient {
    private static final Logger LOG = LogManager.getLogger(WorkspacePreferencesClient.class);

    @Autowired
    GitHubClient client;

    public String setCommiterInfo(final String gitHubToken, final String cheServerUrl) {
        GitHubUserInfo userInfo = client.getUserInfo(gitHubToken);
        WorspacePreferences preferences = getPreferences(userInfo);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WorspacePreferences> entity = new HttpEntity<WorspacePreferences>(preferences, headers);
        ResponseEntity<String> response = template.exchange(CheRestEndpoints.UPDATE_PREFERENCES.generateUrl(cheServerUrl), HttpMethod.PUT, entity, String.class);
        return response.getBody();
    }

    private WorspacePreferences getPreferences(final GitHubUserInfo userInfo) {
        String name = userInfo.getName();
        String email = userInfo.getEmail();

        LOG.info("Commiter name: {}", name);
        LOG.info("Commiter email: {}", email);

        WorspacePreferences preferences = new WorspacePreferences();
        preferences.setCommiterName(name);
        preferences.setCommiterEmail(email);
        return preferences;
    }

}
