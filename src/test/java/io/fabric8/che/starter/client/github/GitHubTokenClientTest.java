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

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.model.github.GitHubEmail;
import io.fabric8.che.starter.model.github.GitHubUserInfo;

public class GitHubTokenClientTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubTokenClientTest.class);
    private static final String GIT_HUB_TOKEN = "dummy_token";

    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Autowired
    private GitHubClient client;

    @Test
    public void setGitHubToken() throws GitHubOAthTokenException, IOException {
        client.setGitHubOAuthToken(GIT_HUB_TOKEN, osioUserToken);
    }

    @Ignore("Valid GitHub token must be provided")
    @Test
    public void getUserInfo() {
        GitHubUserInfo userInfo = client.getUserInfo(GIT_HUB_TOKEN);
        LOG.info("Committer Name {}", userInfo.getName());
        LOG.info("Committer Email {}", userInfo.getEmail());
    }

    @Ignore("Valid GitHub token must be provided")
    @Test
    public void getPrimaryEmail() {
        GitHubEmail primaryEmail = client.getPrimaryEmail(GIT_HUB_TOKEN);
        String email = primaryEmail.getEmail();
        LOG.info("Primary email {}", email);
    }

}
