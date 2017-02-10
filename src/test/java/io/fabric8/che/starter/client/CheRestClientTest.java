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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.che.Stack;
import io.fabric8.che.starter.model.response.WorkspaceInfo;

import static org.junit.Assert.*;

public class CheRestClientTest extends TestConfig {

    private static final Logger LOG = LogManager.getLogger(CheRestClientTest.class);

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private CheRestClient client;

    @Test
    public void listWorkspaces() {
        List<WorkspaceInfo> workspaces = this.client.listWorkspaces(cheServerURL);
        LOG.info("Number of workspaces: {}", workspaces.size());
        workspaces.forEach(w -> LOG.info("workspace ID: {}", w.getId()));
    }

    @Test
    public void createWorkspace() throws IOException {
        client.createWorkspace(cheServerURL);
    }

    @Test
    public void stopWorskpace() {
        List<WorkspaceInfo> workspaces = client.listWorkspaces(cheServerURL);
        if (!workspaces.isEmpty()) {
            List<WorkspaceInfo> runningWorkspaces = workspaces.stream().filter(w -> w.getStatus().equals("RUNNING"))
                    .collect(Collectors.toList());
            if (!runningWorkspaces.isEmpty()) {
                client.stopWorkspace(cheServerURL, runningWorkspaces.get(0).getId());
            }
        }
    }

    @Test
    public void listStacks() {
        List<Stack> stacks = client.listStacks(cheServerURL);
        assertFalse(stacks.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stopAllWorskpaces() {
        client.stopAllWorkspaces();
    }

}
