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
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.workspace.Workspace;

public class WorkspaceClientTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceClientTest.class);
    private static final String GITHUB_REPO = "https://github.com/che-samples/console-java-simple";
    private static final String BRANCH = "master";
    private static final String STACK_ID = "java-centos";
    private static final String DESCRIPTION = GITHUB_REPO + "#" + BRANCH + "#" + "WI1345";
    private static final String KEYCLOAK_TOKEN = null;

    @Autowired
    private WorkspaceClient client;

    @Test
    public void listWorkspaces() {
        List<Workspace> workspaces = this.client.listWorkspaces(KEYCLOAK_TOKEN);
        LOG.info("Number of workspaces: {}", workspaces.size());
        workspaces.forEach(w -> LOG.info("workspace ID: {}", w.getId()));
    }

    @Test
    public void createAndDeleteWorkspace() throws IOException, StackNotFoundException, WorkspaceNotFound, URISyntaxException {
        Workspace workspace = client.createWorkspace(KEYCLOAK_TOKEN, STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION);
        String name = workspace.getConfig().getName();
        String id = workspace.getId();

        assertNotNull("Workspace name can not be null", name);
        assertNotNull("Workspace id can not be null", id);
        LOG.info("Workspace '{}' with id '{}' has been created", name, id);

        // Check that workspace has been really created
        Workspace createdWorkspace = client.listWorkspaces(KEYCLOAK_TOKEN).stream().filter(w -> w.getId().equals(id)).findFirst().get();
        assertNotNull(createdWorkspace);
        assertEquals(name, createdWorkspace.getConfig().getName());

        // Deleting workspace
        client.deleteWorkspace(id, KEYCLOAK_TOKEN);
        // Checking that workspaces has was really deleted
        Workspace workspaceThatShouldNotExist = client.listWorkspaces(KEYCLOAK_TOKEN).stream().filter(w -> w.getId().equals(id)).findFirst().orElse(null);
        assertNull(workspaceThatShouldNotExist);
    }

    @Test
    public void stopWorskpace() {
        List<Workspace> workspaces = client.listWorkspaces(KEYCLOAK_TOKEN);

        if (!workspaces.isEmpty()) {
            LOG.info("Number of workspaces: {}", workspaces.size());
            List<Workspace> runningWorkspaces = workspaces.stream().filter(w -> w.getStatus().equals("RUNNING"))
                    .collect(Collectors.toList());
            if (!runningWorkspaces.isEmpty()) {
                LOG.info("Number of running workspaces: {}", runningWorkspaces.size());
                client.stopWorkspace(runningWorkspaces.get(0), KEYCLOAK_TOKEN);
                client.waitUntilWorkspaceIsStopped(runningWorkspaces.get(0), KEYCLOAK_TOKEN);
            }
        }
    }

}
