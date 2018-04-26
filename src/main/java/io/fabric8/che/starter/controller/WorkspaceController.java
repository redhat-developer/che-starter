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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.client.ProjectClient;
import io.fabric8.che.starter.client.WorkspaceClient;
import io.fabric8.che.starter.client.WorkspacePreferencesClient;
import io.fabric8.che.starter.client.github.GitHubClient;
import io.fabric8.che.starter.client.github.GitHubTokenProvider;
import io.fabric8.che.starter.client.keycloak.KeycloakTokenValidator;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.exception.KeycloakException;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.util.WorkspaceHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class WorkspaceController {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceController.class);

    @Autowired
    WorkspaceClient workspaceClient;

    @Autowired
    ProjectClient projectClient;

    @Autowired
    GitHubTokenProvider keycloakClient;

    @Autowired
    GitHubClient tokenClient;

    @Autowired
    WorkspaceHelper workspaceHelper;

    @Autowired
    WorkspacePreferencesClient workspacePreferencesClient;

    @ApiOperation(value = "List workspaces per git repository. If repository parameter is not specified return all workspaces")
    @GetMapping("/workspace")
    public List<Workspace> list(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestParam(required = false) String repository,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken,
            HttpServletRequest request)
            throws JsonProcessingException, IOException, KeycloakException {

        KeycloakTokenValidator.validate(keycloakToken);
        String requestURL = request.getRequestURL().toString();
        return listWorkspaces(repository, requestURL, keycloakToken);
    }

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time)")
    @PostMapping("/workspace")
    public Workspace create(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestBody WorkspaceCreateParams params,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws IOException, URISyntaxException, StackNotFoundException,
            GitHubOAthTokenException, KeycloakException, WorkspaceNotFound {
        KeycloakTokenValidator.validate(keycloakToken);
        String gitHubToken = keycloakClient.getGitHubToken(keycloakToken);
        return createWorkspace(gitHubToken, keycloakToken, params);
    }

    @ApiOperation(value = "Start an existing workspace. Stop all other workspaces (only one workspace can be running at a time)")
    @PatchMapping("/workspace/{name}")
    public Workspace startExisting(@PathVariable String name, @RequestParam String masterUrl,
            @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws IOException, URISyntaxException, StackNotFoundException,
            GitHubOAthTokenException, KeycloakException, WorkspaceNotFound {

        KeycloakTokenValidator.validate(keycloakToken);
        String gitHubToken = keycloakClient.getGitHubToken(keycloakToken);
        Workspace workspace = workspaceClient.startWorkspace(name, keycloakToken);
        setGitHubOAthTokenAndCommitterInfo(gitHubToken, keycloakToken);
        return workspace;
    }

    @ApiOperation(value = "Delete an existing workspace by name")
    @DeleteMapping("/workspace/{name}")
    public void deleteExistingWorkspace(@PathVariable String name, @RequestParam String masterUrl,
            @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws JsonProcessingException, IOException, KeycloakException, WorkspaceNotFound {

        KeycloakTokenValidator.validate(keycloakToken);
        Workspace workspace = workspaceClient.getWorkspaceByName(name, keycloakToken);
        String id = workspace.getId();
        LOG.info("Deleting workspace name: {}, id: {}", name, id);
        workspaceClient.stopWorkspace(workspace, keycloakToken);
        workspaceClient.waitUntilWorkspaceIsStopped(workspace, keycloakToken);
        workspaceClient.deleteWorkspace(id, keycloakToken);
        LOG.info("workspace '{}' has been succesfully deleted", name);
    }

    /**
     * Deletes a workspace by its name. If there are projects on a workspace,
     * remove those at first, then delete a workspace. If different workspace is
     * running, during this process it is stopped and after successful deletion
     * it gets started again.
     * 
     * @param workspaceName
     * @param keycloakToken
     * @throws WorkspaceNotFound
     */

    public void deleteWorkspace(String workspaceName, String keycloakToken) throws WorkspaceNotFound {
        projectClient.deleteAllProjectsAndWorkspace(workspaceName, keycloakToken);
    }

    /**
     * Create workspace from specified params.
     * 
     * @param gitHubOAuthToken
     * @param keycloakToken
     * @param params
     * @return create Workspace
     * @throws WorkspaceNotFound
     */
    public Workspace createWorkspace(String gitHubOAuthToken, String keycloakToken, WorkspaceCreateParams params)
            throws URISyntaxException, IOException, StackNotFoundException,
            GitHubOAthTokenException, WorkspaceNotFound {

        Workspace workspace = createWorkspaceFromParams(gitHubOAuthToken, keycloakToken, params);
        String workspaceName = workspace.getConfig().getName();
        workspaceClient.startWorkspaceAsync(workspaceName, keycloakToken);
        return workspace;
    }

    /**
     * Creates a new workspace from params
     */
    private Workspace createWorkspaceFromParams(String gitHubToken, String keycloakToken, WorkspaceCreateParams params) throws StackNotFoundException, IOException, GitHubOAthTokenException, URISyntaxException {
        Workspace workspace = workspaceClient.createWorkspace(keycloakToken, params.getStackId(), params.getRepo(), params.getBranch(), params.getDescription());
        setGitHubOAthTokenAndCommitterInfo(gitHubToken, keycloakToken);
        return workspace;
    }

    private void setGitHubOAthTokenAndCommitterInfo(String gitHubToken, String keycloakToken)
            throws IOException, GitHubOAthTokenException {
        if (!StringUtils.isBlank(gitHubToken)) {
            tokenClient.setGitHubOAuthToken(gitHubToken, keycloakToken);
            try {
                workspacePreferencesClient.setCommitterInfo(gitHubToken, keycloakToken);
            } catch (Exception e) {
                LOG.warn("Unable to set committer info in Che Git preferences");
            }
        }
    }

    public List<Workspace> listWorkspaces(final String repository, final String requestUrl, final String keycloakToken) {
        List<Workspace> workspaces;
        try {
            if (!StringUtils.isBlank(repository)) {
                LOG.info("Fetching workspaces for repositoriy: {}", repository);
                workspaces = workspaceClient.listWorkspacesPerRepository(repository, keycloakToken);
            } else {
                workspaces = workspaceClient.listWorkspaces(keycloakToken);
            }
            workspaceHelper.addWorkspaceStartLink(workspaces, requestUrl);
        } catch (RestClientException e) {
            throw new RestClientException(
                    "Error while getting the list of workspaces", e);
        }
        return workspaces;
    }
}
