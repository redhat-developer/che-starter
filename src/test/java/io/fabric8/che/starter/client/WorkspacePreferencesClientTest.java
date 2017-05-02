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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.WorkspacePreferences;

public class WorkspacePreferencesClientTest extends TestConfig {
    private static final String KEYCLOAK_TOKEN = null;
    private static final String COMMITTER_NAME = "Ilya Buziuk";
    private static final String COMMITTER_EMAIL = "ilyabuziuk@gmail.com";

    @Value("${che.server.url}")
    String cheServerUrl;

    @Autowired
    WorkspacePreferencesClient client;

    @Test
    public void setCommitterInfo() throws Exception {
        client.setCommiterInfo(cheServerUrl, KEYCLOAK_TOKEN, getTestPreferences());
        checkCommitterInfoSetCorrectly();
    }

    private void checkCommitterInfoSetCorrectly() {
        WorkspacePreferences committerInfo = client.getCommitterInfo(cheServerUrl, KEYCLOAK_TOKEN);

        assertNotNull(committerInfo);
        assertEquals(committerInfo.getCommiterName(), COMMITTER_NAME);
        assertEquals(committerInfo.getCommiterEmail(), COMMITTER_EMAIL);
    }

    private WorkspacePreferences getTestPreferences() {
        WorkspacePreferences preferences = new WorkspacePreferences();
        preferences.setCommitterName(COMMITTER_NAME);
        preferences.setCommitterEmail(COMMITTER_EMAIL);
        return preferences;
    }

}
