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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;

public class GitHubTokenClientTest extends TestConfig {
    private static final String GITHUB_TOKEN = "e72e16c7e42f292c6912e7710c838347ae178b4q";
   
    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private TokenClient client;

    @Test(expected=GitHubOAthTokenException.class)
    public void setGitHubToken() throws GitHubOAthTokenException, IOException {
        client.setGitHubOAuthToken(cheServerURL, GITHUB_TOKEN);
    }

}
