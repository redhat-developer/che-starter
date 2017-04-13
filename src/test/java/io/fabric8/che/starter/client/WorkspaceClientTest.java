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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.workspace.Workspace;

public class WorkspaceClientTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(WorkspaceClientTest.class);
    private static final String GITHUB_REPO = "https://github.com/che-samples/console-java-simple";
    private static final String BRANCH = "master";
    private static final String STACK_ID = "java-default";
    private static final String DESCRIPTION = GITHUB_REPO + "#" + BRANCH + "#" + "WI1345";

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private WorkspaceClient client;
    
    @Test
    public void listWorkspaces() {
        List<Workspace> workspaces = this.client.listWorkspaces(cheServerURL);
        LOG.info("Number of workspaces: {}", workspaces.size());
        workspaces.forEach(w -> LOG.info("workspace ID: {}", w.getId()));
    }

    
    @Test
    @Ignore("Ignored due to issue with running a workspace on remote OS test instance")
    public void createAndDeleteWorkspace() throws IOException, StackNotFoundException, WorkspaceNotFound {
        Workspace workspace = client.createWorkspace(cheServerURL, null, STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION);
        client.waitUntilWorkspaceIsRunning(cheServerURL, workspace);
        client.stopWorkspace(cheServerURL, workspace.getId());
        client.waitUntilWorkspaceIsStopped(cheServerURL, workspace);
        client.deleteWorkspace(cheServerURL, workspace.getId());
    }

    @Test
    public void stopWorskpace() {
        List<Workspace> workspaces = client.listWorkspaces(cheServerURL);
        if (!workspaces.isEmpty()) {
            List<Workspace> runningWorkspaces = workspaces.stream().filter(w -> w.getStatus().equals("RUNNING"))
                    .collect(Collectors.toList());
            if (!runningWorkspaces.isEmpty()) {
                client.stopWorkspace(cheServerURL, runningWorkspaces.get(0).getId());
            }
        }
    }

}
