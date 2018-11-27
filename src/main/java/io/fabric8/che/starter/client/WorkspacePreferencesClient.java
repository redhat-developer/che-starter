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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.github.GitHubClient;
import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.model.WorkspacePreferences;
import io.fabric8.che.starter.model.github.GitHubUserInfo;
import io.fabric8.che.starter.util.CheServerUrlProvider;

@Component
public class WorkspacePreferencesClient {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspacePreferencesClient.class);

    @Autowired
    GitHubClient gitHubClient;

    @Autowired
    CheServerUrlProvider cheServerUrlProvider;

    public void setCommitterInfo(final String gitHubToken, final String keycloakToken) {
        GitHubUserInfo userInfo = gitHubClient.getUserInfo(gitHubToken);
        WorkspacePreferences preferences = getPreferences(userInfo);
        setCommitterInfo(keycloakToken, preferences);
    }

    public void setCommitterInfo(final String keycloakToken, final WorkspacePreferences preferences) {
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WorkspacePreferences> entity = new HttpEntity<>(preferences, headers);
        template.exchange(CheRestEndpoints.UPDATE_PREFERENCES.generateUrl(cheServerUrlProvider.getUrl(keycloakToken)), HttpMethod.PUT, entity, String.class);
    }

    public WorkspacePreferences getCommitterInfo(final String keycloakToken) {
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<WorkspacePreferences> response = template.exchange(
                CheRestEndpoints.GET_PREFERENCES.generateUrl(cheServerUrlProvider.getUrl(keycloakToken)), HttpMethod.GET, entity,
                WorkspacePreferences.class);
        return response.getBody();
    }

    public WorkspacePreferences getPreferences(final GitHubUserInfo userInfo) {
        String name = userInfo.getName();
        String email = userInfo.getEmail();

        if (StringUtils.isBlank(name)) {
            String login = userInfo.getLogin();
            LOG.warn("'name' field is blank, using 'login' {} as committer name in 'Git' preferences", login);
            name = login;
        }

        LOG.info("Committer name: {}", name);
        LOG.info("Committer email: {}", email);

        WorkspacePreferences preferences = new WorkspacePreferences();
        preferences.setCommitterName(name);
        preferences.setCommitterEmail(email);
        return preferences;
    }

}
