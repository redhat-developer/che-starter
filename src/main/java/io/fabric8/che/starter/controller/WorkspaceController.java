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
package io.fabric8.che.starter.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.client.ProjectClient;
import io.fabric8.che.starter.client.GitHubClient;
import io.fabric8.che.starter.client.WorkspaceClient;
import io.fabric8.che.starter.client.keycloak.KeycloakClient;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.exception.ProjectCreationException;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;
import io.fabric8.che.starter.model.workspace.WorkspaceState;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.che.starter.util.ProjectHelper;
import io.fabric8.che.starter.util.WorkspaceHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class WorkspaceController {
    private static final Logger LOG = LogManager.getLogger(WorkspaceController.class);

    @Autowired
    OpenShiftClientWrapper openShiftClientWrapper;

    @Autowired
    WorkspaceClient workspaceClient;

    @Autowired
    ProjectClient projectClient;

    @Autowired
    KeycloakClient keycloakClient;

    @Autowired
    GitHubClient tokenClient;

    @Autowired
    ProjectHelper projectHelper;

    @Autowired
    WorkspaceHelper workspaceHelper;

    @ApiOperation(value = "List workspaces per git repository. If repository parameter is not specified return all workspaces")
    @GetMapping("/workspace")
    public List<Workspace> list(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestParam(required = false) String repository,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws RouteNotFoundException, JsonProcessingException, IOException {

        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        return listWorkspaces(masterUrl, namespace, openShiftToken, repository);
    }

    @ApiOperation(value = "List workspaces per git repository. If repository parameter is not specified return all workspaces")
    @GetMapping("/workspace/oso")
    public List<Workspace> listOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestParam(required = false) String repository,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken)
            throws RouteNotFoundException, JsonProcessingException, IOException {

        return listWorkspaces(masterUrl, namespace, openShiftToken, repository);
    }

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping("/workspace")
    public WorkspaceLink create(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestBody WorkspaceCreateParams params,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws IOException, URISyntaxException, RouteNotFoundException, StackNotFoundException, GitHubOAthTokenException, ProjectCreationException {

        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        String gitHubOAuthToken = keycloakClient.getGitHubToken(keycloakToken);
        return createWorkspace(masterUrl, namespace, openShiftToken, gitHubOAuthToken, keycloakToken, params);
    }

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping("/workspace/oso")
    public WorkspaceLink createOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestBody WorkspaceCreateParams params,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken)
            throws IOException, URISyntaxException, RouteNotFoundException, StackNotFoundException, GitHubOAthTokenException, ProjectCreationException {

        return createWorkspace(masterUrl, namespace, openShiftToken, null, null, params);
    }

    /**
     * Create workspace from specified params.
     * 
     * @param masterUrl
     * @param namespace
     * @param openShiftToken
     * @param gitHubOAuthToken
     * @param keycloakToken
     * @param params
     * @return create Workspace
     */
    public WorkspaceLink createWorkspace(String masterUrl, String namespace, String openShiftToken, String gitHubOAuthToken,
            String keycloakToken, WorkspaceCreateParams params) throws RouteNotFoundException, URISyntaxException,
            IOException, StackNotFoundException, GitHubOAthTokenException, ProjectCreationException {

        String cheServerUrl = openShiftClientWrapper.getCheServerUrl(masterUrl, namespace, openShiftToken);
        String projectName = projectHelper.getProjectNameFromGitRepository(params.getRepo());

        List<Workspace> workspaces = workspaceClient.listWorkspaces(cheServerUrl);

        String description = workspaceHelper.getDescription(params.getRepo(), params.getBranch());

        for (Workspace ws : workspaces) {
            String wsDescription = ws.getConfig().getDescription();
            if (wsDescription != null && wsDescription.equals(description)) {
                // Before we can create a project, we must start the new workspace.  First check it's not already running
                if (!WorkspaceState.RUNNING.toString().equals(ws.getStatus()) && 
                        !WorkspaceState.STARTING.toString().equals(ws.getStatus())) {
                    workspaceClient.startWorkspace(cheServerUrl, ws.getId());
                }
                return workspaceHelper.getWorkspaceIdeLink(ws);
            }
        }

        // Create the workspace
        Workspace workspace = workspaceClient.createWorkspace(cheServerUrl, keycloakToken, params.getName(), params.getStack(),
                params.getRepo(), params.getBranch());
        
        // Set the GitHub oAuth token if it is available
        if (!StringUtils.isBlank(gitHubOAuthToken)) {
            tokenClient.setGitHubOAuthToken(cheServerUrl, gitHubOAuthToken);
        }

        // Create the project - this is an async call
        projectClient.createProject(cheServerUrl, workspace.getId(), projectName, params.getRepo(),
                params.getBranch(), params.getStack());

        return workspaceHelper.getWorkspaceIdeLink(workspace);
    }

    public List<Workspace> listWorkspaces(String masterUrl, String namespace, String openShiftToken, String repository)
            throws RouteNotFoundException {
        String cheServerUrl = openShiftClientWrapper.getCheServerUrl(masterUrl, namespace, openShiftToken);
        if (!StringUtils.isBlank(repository)) {
            LOG.info("Fetching workspaces for repositoriy: {}", repository);
            return workspaceClient.listWorkspacesPerRepository(cheServerUrl, repository);
        }
        return workspaceClient.listWorkspaces(cheServerUrl);
    }
}
