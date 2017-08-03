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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.ProjectCreationException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.project.Source;
import io.fabric8.che.starter.model.workspace.Workspace;

@Component
public class ProjectClient {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectClient.class);

    @Autowired
    private StackClient stackClient;
    
    @Autowired
    private WorkspaceClient workspaceClient;

    /**
     * Starts a workspace, wait for it and import project
     */
    @Async
    public void createProject(String cheServerURL, Workspace workspace, String projectName, String repo, String branch, String stack, 
            String masterUrl, String namespace, String openShiftToken, String keycloakToken)
            throws IOException, ProjectCreationException, WorkspaceNotFound, URISyntaxException, MalformedURLException {

        workspaceClient.startWorkspace(cheServerURL, workspace.getConfig().getName(), masterUrl, namespace, openShiftToken, keycloakToken);
        workspaceClient.waitUntilWorkspaceIsRunning(cheServerURL, workspace, keycloakToken);
        Workspace startedWorkspace = workspaceClient.getWorkspaceById(cheServerURL, workspace.getId(), keycloakToken);

        String wsAgentUrl = getWsAgentUrl(startedWorkspace);

        // Next we create a new project against workspace agent API Url
        String url = CheRestEndpoints.CREATE_PROJECT.generateUrl(wsAgentUrl);
        LOG.info("Creating project against workspace agent URL: {}", url);

        String projectType = stackClient.getProjectTypeByStackId(stack);

        LOG.info("Stack: {}", stack);
        LOG.info("Project type: {}", projectType);

        try {
            Project project = createProject(projectName, repo, branch, projectType, url, keycloakToken);
            LOG.info("Successfully created project {}", project.getName());
        } catch (Exception e) {
            LOG.info("Error occurred while creating project {}", projectName);
            throw new ProjectCreationException("Error occurred while creating project " + projectName, e);
        }
    }

    private Project createProject(String projectName, String repo, String branch, String projectType, String url,
            String keycloakToken) {
        Project project = initProject(projectName, repo, branch, projectType);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Project[]> entity = new HttpEntity<Project[]>(new Project[] { project }, headers);
        ResponseEntity<Project[]> response = template.exchange(url, HttpMethod.POST, entity, Project[].class);

        return response.getBody()[0];
    }

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

    private Project initProject(String name, String repo, String branch, String projectType) {
        Project project = new Project();
        project.setName(name);
        Source source = new Source();
        Map<String, String> sourceParams = source.getParameters();
        sourceParams.put("branch", branch);
        sourceParams.put("keepVcs", "true");
        source.setType("git");
        source.setLocation(repo);
        project.setSource(source);
        project.setProjectType(projectType);
        project.setType(projectType);
        project.setDescription("Created via che-starter API");
        project.setPath("/" + name);
        List<String> mixins = new ArrayList<>();
        mixins.add("git");
        project.setMixins(mixins);
        return project;
    }
}
