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
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceStatus;

public class WorkspaceClientTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceClientTest.class);
    private static final String GITHUB_REPO = "https://github.com/che-samples/console-java-simple";
    private static final String BRANCH = "master";
    private static final String DESCRIPTION = GITHUB_REPO + "#" + BRANCH + "#" + "WI1345";
    private static final String JAVA_CENTOS_STACK_ID = "java-centos";
    private static final String VERTX_STACK_ID = "vert.x";
    private static final String WILDFLY_SWARM_STACK_ID = "wildfly-swarm";

    @Autowired
    private WorkspaceClient client;

    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Test
    public void listWorkspaces() {
        List<Workspace> workspaces = this.client.listWorkspaces(osioUserToken);
        LOG.info("Number of workspaces: {}", workspaces.size());
        workspaces.forEach(w -> LOG.info("workspace ID: {}", w.getId()));
    }

    @Test
    public void createAndDeleteWorkspace() throws IOException, StackNotFoundException, WorkspaceNotFound, URISyntaxException {
        Workspace workspace = client.createWorkspace(osioUserToken, JAVA_CENTOS_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION);
        String name = workspace.getConfig().getName();
        String id = workspace.getId();

        assertNotNull("Workspace name can not be null", name);
        assertNotNull("Workspace id can not be null", id);
        LOG.info("Workspace '{}' with id '{}' has been created", name, id);

        // Check that workspace has been really created
        Workspace createdWorkspace = client.listWorkspaces(osioUserToken).stream().filter(w -> w.getId().equals(id)).findFirst().get();
        assertNotNull(createdWorkspace);
        assertEquals(name, createdWorkspace.getConfig().getName());

        deleteWorkspaceById(createdWorkspace.getId());
    }

    @Test
    public void createAndStartWorkspace() throws IOException, StackNotFoundException, WorkspaceNotFound, URISyntaxException {
        Workspace workspace = client.createWorkspace(osioUserToken, JAVA_CENTOS_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION);
        String name = workspace.getConfig().getName();
        String id = workspace.getId();

        assertNotNull("Workspace name can not be null", name);
        assertNotNull("Workspace id can not be null", id);
        LOG.info("Workspace '{}' with id '{}' has been created", name, id);

        // Check that workspace has been really created
        Workspace createdWorkspace = client.listWorkspaces(osioUserToken).stream().filter(w -> w.getId().equals(id)).findFirst().get();
        assertNotNull(createdWorkspace);
        assertEquals(name, createdWorkspace.getConfig().getName());

        client.startWorkspace(name, osioUserToken);
        client.waitUntilWorkspaceIsRunning(createdWorkspace, osioUserToken);
        WorkspaceStatus statusRunning = client.getWorkspaceStatus(id, osioUserToken);
        assertEquals(statusRunning.getWorkspaceStatus(), "RUNNING");

        client.stopWorkspace(workspace, osioUserToken);
        client.waitUntilWorkspaceIsStopped(workspace, osioUserToken);
        WorkspaceStatus statusStopped = client.getWorkspaceStatus(id, osioUserToken);
        assertEquals(statusStopped.getWorkspaceStatus(), "STOPPED");

        deleteWorkspaceById(createdWorkspace.getId());
    }

    @Test
    public void getWorkspaceByIdAndDelete() throws StackNotFoundException, IOException, URISyntaxException, WorkspaceNotFound {
        Workspace workspace = client.createWorkspace(osioUserToken, VERTX_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION);
        String name = workspace.getConfig().getName();
        String id = workspace.getId();

        assertNotNull("Workspace name can not be null", name);
        assertNotNull("Workspace id can not be null", id);
        LOG.info("Workspace '{}' with id '{}' has been created", name, id);

        Workspace workspaceById = client.getWorkspaceById(id, osioUserToken);
        assertNotNull(workspaceById);
        assertEquals(name, workspaceById.getConfig().getName());

        deleteWorkspaceById(workspaceById.getId());
    }

    @Test
    public void getWorkspaceByNameAndDelete() throws StackNotFoundException, IOException, URISyntaxException, WorkspaceNotFound {
        Workspace workspace = client.createWorkspace(osioUserToken, WILDFLY_SWARM_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION);
        String name = workspace.getConfig().getName();
        String id = workspace.getId();

        assertNotNull("Workspace name can not be null", name);
        assertNotNull("Workspace id can not be null", id);
        LOG.info("Workspace '{}' with id '{}' has been created", name, id);

        Workspace workspaceByName = client.getWorkspaceByName(name, osioUserToken);
        assertNotNull(workspaceByName);
        assertEquals(name, workspaceByName.getConfig().getName());

        deleteWorkspaceById(workspaceByName.getId());
    }

    @Test
    public void stopWorskpace() {
        List<Workspace> workspaces = client.listWorkspaces(osioUserToken);
        if (!workspaces.isEmpty()) {
            LOG.info("Number of workspaces: {}", workspaces.size());
            List<Workspace> runningWorkspaces = workspaces.stream().filter(w -> w.getStatus().equals("RUNNING"))
                    .collect(Collectors.toList());
            if (!runningWorkspaces.isEmpty()) {
                LOG.info("Number of running workspaces: {}", runningWorkspaces.size());
                client.stopWorkspace(runningWorkspaces.get(0), osioUserToken);
                client.waitUntilWorkspaceIsStopped(runningWorkspaces.get(0), osioUserToken);
            }
        }
    }

    private void deleteWorkspaceById(final String id) throws WorkspaceNotFound {
        client.deleteWorkspace(id, osioUserToken);
        Workspace workspaceThatShouldNotExist = client.listWorkspaces(osioUserToken).stream().filter(w -> w.getId().equals(id)).findFirst().orElse(null);
        assertNull(workspaceThatShouldNotExist);
        LOG.info("Workspace with id '{}' has been deleted", id);
    }

}
