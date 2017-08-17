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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.workspace.Workspace;

@Component
public class ProjectClient {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectClient.class);

    @Autowired
    private WorkspaceClient workspaceClient;

    /**
     * Delete a project from workspace. Workspace must be running to delete a
     * project.
     * 
     * @param cheServerURL
     * @param workspaceName
     * @param projectName
     */
    public void deleteProject(String cheServerURL, Workspace workspace, String projectName, String keycloakToken) {
        String wsAgentUrl = getWsAgentUrl(workspace);

        String deleteProjectURL = CheRestEndpoints.DELETE_PROJECT.generateUrl(wsAgentUrl, projectName);
        LOG.info("Deleting project {}", projectName);
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        template.delete(deleteProjectURL);
    }

    @Async
    public void deleteAllProjectsAndWorkspace(String cheServerURL, String workspaceName, String masterUrl, String namespace, String openShiftToken, 
            String keycloakToken) throws WorkspaceNotFound {
        Workspace runningWorkspace = workspaceClient.getStartedWorkspace(cheServerURL, keycloakToken);

        Workspace workspaceToDelete = workspaceClient.startWorkspace(cheServerURL, workspaceName, masterUrl, namespace, openShiftToken, keycloakToken);
        workspaceClient.waitUntilWorkspaceIsRunning(cheServerURL, workspaceToDelete, keycloakToken);
        workspaceToDelete = workspaceClient.getWorkspaceById(cheServerURL, workspaceToDelete.getId(), keycloakToken);

        List<Project> projectsToDelete = workspaceToDelete.getConfig().getProjects();
        if (projectsToDelete != null && !projectsToDelete.isEmpty()) {
            for (Project project : projectsToDelete) {
                deleteProject(cheServerURL, workspaceToDelete, project.getName(), keycloakToken);
            }
        }

        workspaceClient.stopWorkspace(cheServerURL, workspaceToDelete, keycloakToken);
        workspaceClient.waitUntilWorkspaceIsStopped(masterUrl, namespace, openShiftToken, cheServerURL, workspaceToDelete, keycloakToken);

        workspaceClient.deleteWorkspace(cheServerURL, workspaceToDelete.getId(), keycloakToken);

        if (runningWorkspace != null && !runningWorkspace.getConfig().getName().equals(workspaceName)) {
            workspaceClient.startWorkspace(cheServerURL, runningWorkspace.getConfig().getName(), masterUrl, namespace, openShiftToken, keycloakToken);

        }
    }

    private String getWsAgentUrl(final Workspace workspace) {
        return workspace.getRuntime().getDevMachine().getRuntime().getServers().get("4401/tcp").getUrl();
    }

}
