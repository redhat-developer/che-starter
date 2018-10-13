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

import org.junit.Ignore;
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
    private static final String SPACE_ID = "12db65ef-b5ed-4acb-bdab-yy4463171df3";

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
        Workspace workspace = client.createWorkspace(osioUserToken, JAVA_CENTOS_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION, SPACE_ID);
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

    @Ignore("Not possible to start workspaces on free-stg https://github.com/openshiftio/openshift.io/issues/2273")
    @Test
    public void createAndStartWorkspace() throws IOException, StackNotFoundException, WorkspaceNotFound, URISyntaxException {
        Workspace workspace = client.createWorkspace(osioUserToken, JAVA_CENTOS_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION, SPACE_ID);
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
        String statusRunning = client.getWorkspaceStatus(id, osioUserToken);
        assertEquals(statusRunning, WorkspaceStatus.RUNNING.toString());

        client.stopWorkspace(workspace, osioUserToken);
        client.waitUntilWorkspaceIsStopped(workspace, osioUserToken);
        String statusStopped = client.getWorkspaceStatus(id, osioUserToken);
        assertEquals(statusStopped, WorkspaceStatus.STOPPED.toString());

        deleteWorkspaceById(createdWorkspace.getId());
    }

    @Ignore("Not possible to start workspaces on free-stg https://github.com/openshiftio/openshift.io/issues/2273")
    @Test
    public void createAndStartWorkspaceWhenThereIsAlreadyOneRunning() throws IOException, StackNotFoundException, WorkspaceNotFound, URISyntaxException {
        // Creating and starting first workspace
        Workspace firstWorkspace = client.createWorkspace(osioUserToken, JAVA_CENTOS_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION, SPACE_ID);
        String firstWorkspaceName = firstWorkspace.getConfig().getName();
        String firstWorkspaceId = firstWorkspace.getId();

        assertNotNull("First Workspace name can not be null", firstWorkspaceName);
        assertNotNull("First Workspace id can not be null", firstWorkspaceId);
        LOG.info("First Workspace '{}' with id '{}' has been created", firstWorkspaceName, firstWorkspaceId);

        // Check that workspace has been really created
        Workspace createdFirstWorkspace = client.listWorkspaces(osioUserToken).stream().filter(w -> w.getId().equals(firstWorkspaceId)).findFirst().get();
        assertNotNull(createdFirstWorkspace);
        assertEquals(firstWorkspaceName, createdFirstWorkspace.getConfig().getName());

        // Waiting till the first workspace is running
        client.startWorkspace(firstWorkspaceName, osioUserToken);
        client.waitUntilWorkspaceIsRunning(createdFirstWorkspace, osioUserToken);
        String firstStatusRunning = client.getWorkspaceStatus(firstWorkspaceId, osioUserToken);
        assertEquals(firstStatusRunning, WorkspaceStatus.RUNNING.toString());

        // Creating and starting second workspace
        Workspace secondWorkspace = client.createWorkspace(osioUserToken, WILDFLY_SWARM_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION, SPACE_ID);
        String secondWorkspaceName = secondWorkspace.getConfig().getName();
        String secondWorkspaceId = secondWorkspace.getId();

        assertNotNull("Second Workspace name can not be null", secondWorkspaceName);
        assertNotNull("Second Workspace id can not be null", secondWorkspaceId);
        LOG.info("Second Workspace '{}' with id '{}' has been created", secondWorkspaceName, secondWorkspaceId);

        // Waiting till the second workspace is running
        client.startWorkspace(secondWorkspaceName, osioUserToken);
        client.waitUntilWorkspaceIsRunning(secondWorkspace, osioUserToken);

        // Checking workspace statuses - first should be stopped / second is running
        String firstStatusStopped = client.getWorkspaceStatus(firstWorkspaceId, osioUserToken);
        String secondStatusRunning = client.getWorkspaceStatus(secondWorkspaceId, osioUserToken);
        assertEquals(firstStatusStopped, WorkspaceStatus.STOPPED.toString());
        assertEquals(secondStatusRunning, WorkspaceStatus.RUNNING.toString());

        // Stopping second workspace
        client.stopWorkspace(secondWorkspace, osioUserToken);
        client.waitUntilWorkspaceIsStopped(secondWorkspace, osioUserToken);
        String secondStatusStopped = client.getWorkspaceStatus(firstWorkspaceId, osioUserToken);
        assertEquals(secondStatusStopped, WorkspaceStatus.STOPPED.toString());

        // Deleting two workspaces
        deleteWorkspaceById(firstWorkspaceId);
        deleteWorkspaceById(secondWorkspaceId);

    }

    @Test
    public void getWorkspaceByIdAndDelete() throws StackNotFoundException, IOException, URISyntaxException, WorkspaceNotFound {
        Workspace workspace = client.createWorkspace(osioUserToken, VERTX_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION, SPACE_ID);
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
        Workspace workspace = client.createWorkspace(osioUserToken, WILDFLY_SWARM_STACK_ID, GITHUB_REPO, BRANCH, DESCRIPTION, SPACE_ID);
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
