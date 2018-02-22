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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.KeycloakException;

public class GitHubTokenProviderTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubTokenProviderTest.class);
    
    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Autowired
    GitHubTokenProvider gitHubTokenProvider;

    @Test
    public void getGitHubToken() throws KeycloakException, JsonProcessingException, IOException {
        String gitHubToken = gitHubTokenProvider.getGitHubToken("Bearer " + osioUserToken);
        assertNotNull(gitHubToken);
        LOG.info("GitHub Token has been successfully obtained");
    }

}
