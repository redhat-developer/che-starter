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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.WorkspacePreferences;
import io.fabric8.che.starter.model.github.GitHubUserInfo;

public class WorkspacePreferencesClientTest extends TestConfig {
    private static final String KEYCLOAK_TOKEN = null;
    private static final String GITHUB_LOGIN = "johndoe";
    private static final String GITHUB_EMAIL = "johndoe@gmail.com";

    private String committerName;
    private String committerEmail;
    private GitHubUserInfo gitHubUserInfo;

    @Value("${che.server.url}")
    String cheServerUrl;

    @Autowired
    WorkspacePreferencesClient client;

    @PostConstruct
    public void init() {
        this.committerName = generateCommitterName();
        this.committerEmail = generateCommitterEmail();

        this.gitHubUserInfo = new GitHubUserInfo();
        this.gitHubUserInfo.setEmail(GITHUB_EMAIL);
        this.gitHubUserInfo.setLogin(GITHUB_LOGIN);
    }

    @Test
    public void setCommitterInfo() throws Exception {
        client.setCommitterInfo(cheServerUrl, KEYCLOAK_TOKEN, getTestPreferences());
        checkCommitterInfoSetCorrectly();
    }

    /**
     * @see <a href=
     *      "https://github.com/redhat-developer/che-starter/issues/205"> Need
     *      to use `login` as committer name if `name` is blank</a>
     */
    @Test
    public void getCommitterInfoWhenNameIsBlank() {
        WorkspacePreferences preferences = client.getPreferences(gitHubUserInfo);
        assertEquals(GITHUB_EMAIL, preferences.getCommiterEmail());
        assertEquals(GITHUB_LOGIN, preferences.getCommiterName());
    }

    private void checkCommitterInfoSetCorrectly() {
        WorkspacePreferences committerInfo = client.getCommitterInfo(cheServerUrl, KEYCLOAK_TOKEN);

        assertNotNull(committerInfo);
        assertEquals(committerInfo.getCommiterName(), committerName);
        assertEquals(committerInfo.getCommiterEmail(), committerEmail);
    }

    private WorkspacePreferences getTestPreferences() {
        WorkspacePreferences preferences = new WorkspacePreferences();
        preferences.setCommitterName(committerName);
        preferences.setCommitterEmail(committerEmail);
        return preferences;
    }

    private String generateCommitterEmail() {
        return RandomStringUtils.random(10, true, true) + "@redhat.com";
    }

    private String generateCommitterName() {
        return "John " + RandomStringUtils.random(10, true, true);
    }

}
