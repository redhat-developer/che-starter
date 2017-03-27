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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.model.GitHubUserInfo;

public class GitHubTokenClientTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(GitHubTokenClientTest.class);
    private static final String GIT_HUB_TOKEN = "GIT_HUB_TOKEN";

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private GitHubClient client;

    @Test(expected = GitHubOAthTokenException.class)
    public void setGitHubToken() throws GitHubOAthTokenException, IOException {
        client.setGitHubOAuthToken(cheServerURL, GIT_HUB_TOKEN);
    }

    @Ignore("Valid GitHub token must be provided")
    @Test
    public void getUserInfo() {
        GitHubUserInfo userInfo = client.getUserInfo(GIT_HUB_TOKEN);
        LOG.info(userInfo.getLogin());
        LOG.info(userInfo.getName());
        LOG.info(userInfo.getEmail());
    }

}
