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
package io.fabric8.che.starter.client.github;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.exception.KeycloakException;
import io.fabric8.che.starter.model.github.GitHubEmail;
import io.fabric8.che.starter.model.github.GitHubUserInfo;

public class GitHubTokenClientTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubTokenClientTest.class);

    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Autowired
    private GitHubClient client;

    @Autowired
    GitHubTokenProvider gitHubTokenProvider;

    private String gitHubToken;

    @PostConstruct
    public void init() throws JsonProcessingException, KeycloakException, IOException {
        this.gitHubToken = gitHubTokenProvider.getGitHubToken("Bearer " + osioUserToken);
    }

    @Test
    public void setGitHubToken() throws GitHubOAthTokenException, IOException {
        client.setGitHubOAuthToken(gitHubToken, osioUserToken);
    }

    @Test
    public void getUserInfo() {
        GitHubUserInfo userInfo = client.getUserInfo(gitHubToken);
        assertNotNull(userInfo);
        assertNotNull(userInfo.getEmail());
        if (StringUtils.isBlank(userInfo.getName())) {
            String login = userInfo.getLogin();
            LOG.warn("'name' field is blank, using 'login' '{}' instead", login);
            assertNotNull(login);
        }
    }

    @Test
    public void getPrimaryEmail() {
        GitHubEmail primaryEmail = client.getPrimaryEmail(gitHubToken);
        String email = primaryEmail.getEmail();
        assertNotNull(email);
    }

}
