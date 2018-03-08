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

    @Async
    public void deleteAllProjectsAndWorkspace(String workspaceName, String keycloakToken) throws WorkspaceNotFound {
        Workspace runningWorkspace = workspaceClient.getStartedWorkspace(keycloakToken);

        Workspace workspaceToDelete = workspaceClient.startWorkspace(workspaceName, keycloakToken);
        workspaceClient.waitUntilWorkspaceIsRunning(workspaceToDelete, keycloakToken);
        workspaceToDelete = workspaceClient.getWorkspaceById(workspaceToDelete.getId(), keycloakToken);

        List<Project> projectsToDelete = workspaceToDelete.getConfig().getProjects();
        if (projectsToDelete != null && !projectsToDelete.isEmpty()) {
            for (Project project : projectsToDelete) {
                deleteProject(workspaceToDelete, project.getName(), keycloakToken);
            }
        }

        workspaceClient.stopWorkspace(workspaceToDelete, keycloakToken);
        workspaceClient.waitUntilWorkspaceIsStopped(workspaceToDelete, keycloakToken);
        workspaceClient.deleteWorkspace(workspaceToDelete.getId(), keycloakToken);

        if (runningWorkspace != null && !runningWorkspace.getConfig().getName().equals(workspaceName)) {
            workspaceClient.startWorkspace(runningWorkspace.getConfig().getName(), keycloakToken);

        }
    }

    /**
     * Delete a project from workspace. Workspace must be running to delete a
     * project.
     * 
     * @param workspaceName
     * @param projectName
     * @param keycloakToken
     */
    public void deleteProject(Workspace workspace, String projectName, String keycloakToken) {
        String wsAgentUrl = getWsAgentUrl(workspace);

        String deleteProjectURL = CheRestEndpoints.DELETE_PROJECT.generateUrl(wsAgentUrl, projectName);
        LOG.info("Deleting project {}", projectName);
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        template.delete(deleteProjectURL);
    }

    private String getWsAgentUrl(final Workspace workspace) {
        return workspace.getRuntime().getDevMachine().getRuntime().getServers().get("4401/tcp").getUrl();
    }

}
