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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.client.GitHubClient;
import io.fabric8.che.starter.client.ProjectClient;
import io.fabric8.che.starter.client.WorkspaceClient;
import io.fabric8.che.starter.client.WorkspacePreferencesClient;
import io.fabric8.che.starter.client.keycloak.KeycloakClient;
import io.fabric8.che.starter.client.keycloak.KeycloakTokenValidator;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.exception.KeycloakException;
import io.fabric8.che.starter.exception.ProjectCreationException;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.che.starter.util.ProjectHelper;
import io.fabric8.che.starter.util.WorkspaceHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class WorkspaceController {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceController.class);

    @Autowired
    OpenShiftClientWrapper openShiftClientWrapper;

    @Autowired
    WorkspaceClient workspaceClient;

    @Autowired
    ProjectClient projectClient;

    @Autowired
    ProjectHelper projectHelper;
    
    @Autowired
    KeycloakClient keycloakClient;

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
            throws RouteNotFoundException, JsonProcessingException, IOException, KeycloakException {

        KeycloakTokenValidator.validate(keycloakToken);
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        String requestURL = request.getRequestURL().toString();
        return listWorkspaces(masterUrl, namespace, openShiftToken, repository, requestURL, keycloakToken);
    }

    @ApiOperation(value = "List workspaces per git repository. If repository parameter is not specified return all workspaces")
    @GetMapping("/workspace/oso")
    public List<Workspace> listOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestParam(required = false) String repository,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken,
            HttpServletRequest request) throws RouteNotFoundException, JsonProcessingException, IOException {

        String requestURL = request.getRequestURL().toString();
        return listWorkspaces(masterUrl, namespace, openShiftToken, repository, requestURL, null);
    }

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping("/workspace")
    public Workspace create(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestBody WorkspaceCreateParams params,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws IOException, URISyntaxException, RouteNotFoundException, StackNotFoundException,
            GitHubOAthTokenException, ProjectCreationException, KeycloakException, WorkspaceNotFound {

        KeycloakTokenValidator.validate(keycloakToken);
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        String gitHubOAuthToken = keycloakClient.getGitHubToken(keycloakToken);
        return createWorkspace(masterUrl, namespace, openShiftToken, gitHubOAuthToken, keycloakToken, params);
    }

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping("/workspace/oso")
    public Workspace createOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @RequestBody WorkspaceCreateParams params,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken)
            throws IOException, URISyntaxException, RouteNotFoundException, StackNotFoundException,
            GitHubOAthTokenException, ProjectCreationException, WorkspaceNotFound {

        return createWorkspace(masterUrl, namespace, openShiftToken, null, null, params);
    }

    @ApiOperation(value = "Delete an existing workspace")
    @DeleteMapping("/workspace/{name}")
    public void deleteExistingWorkspace(@PathVariable String name, @RequestParam String masterUrl,
            @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws JsonProcessingException, IOException, KeycloakException, RouteNotFoundException, WorkspaceNotFound {

        KeycloakTokenValidator.validate(keycloakToken);
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        deleteWorkspace(masterUrl, namespace, openShiftToken, name, keycloakToken);
    }

    @ApiOperation(value = "Delete an existing workspace")
    @DeleteMapping("/workspace/oso/{name}")
    public void deleteExistingWorkspaceOnOpenShift(@PathVariable String name, @RequestParam String masterUrl,
            @RequestParam String namespace,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken)
            throws JsonProcessingException, IOException, KeycloakException, RouteNotFoundException, WorkspaceNotFound {

        deleteWorkspace(masterUrl, namespace, openShiftToken, name, null);
    }

    /**
     * Deletes a workspace by its name. If there are projects on a workspace,
     * remove those at first, then delete a workspace. If different workspace is
     * running, during this process it is stopped and after successful deletion
     * it gets started again.
     * 
     * @param masterURL
     *            master URL
     * @param namespace
     *            namespace
     * @param openShiftToken
     *            OpenShift token
     * @param workspaceName
     *            workspace name
     * @throws RouteNotFoundException
     * @throws WorkspaceNotFound
     */

    public void deleteWorkspace(String masterUrl, String namespace, String openShiftToken, String workspaceName, String keycloakToken)
            throws RouteNotFoundException, WorkspaceNotFound {
        String cheServerURL = openShiftClientWrapper.getCheServerUrl(masterUrl, namespace, openShiftToken);

        projectClient.deleteAllProjectsAndWorkspace(cheServerURL, workspaceName, masterUrl, namespace, openShiftToken, keycloakToken);
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
     * @throws WorkspaceNotFound
     */
    public Workspace createWorkspace(String masterUrl, String namespace, String openShiftToken,
            String gitHubOAuthToken, String keycloakToken, WorkspaceCreateParams params)
            throws RouteNotFoundException, URISyntaxException, IOException, StackNotFoundException,
            GitHubOAthTokenException, ProjectCreationException, WorkspaceNotFound {

        String cheServerURL = openShiftClientWrapper.getCheServerUrl(masterUrl, namespace, openShiftToken);
        Workspace workspace = null;

        String workspaceName = params.getWorkspaceName();
        if (StringUtils.isNotBlank(workspaceName)) {
            workspace = workspaceClient.startWorkspace(cheServerURL, workspaceName, masterUrl, namespace, openShiftToken, keycloakToken);
            if (StringUtils.isNotBlank(gitHubOAuthToken)) {
                tokenClient.setGitHubOAuthToken(cheServerURL, gitHubOAuthToken, keycloakToken);
            }
            return workspace;
        }

        workspace = createWorkspaceFromParams(cheServerURL, keycloakToken, gitHubOAuthToken, params);

        String projectName = projectHelper.getProjectNameFromGitRepository(params.getRepo());
        projectClient.createProject(cheServerURL, workspace, projectName, params.getRepo(), 
                params.getBranch(), params.getStackId(), masterUrl, namespace, openShiftToken, keycloakToken);

        return workspace;
    }

    /**
     * Creates a new workspace from params.
     * 
     * @param cheServerURL
     *            che server URL
     * @param keycloakToken
     *            keycloak token to authenticate against Che server
     * @param params
     *            workspace create params
     * @throws IOException
     * @throws StackNotFoundException
     * @throws GitHubOAthTokenException
     * 
     * @return created workspace
     */
    private Workspace createWorkspaceFromParams(String cheServerURL, String keycloakToken, String githubToken,
            WorkspaceCreateParams params) throws StackNotFoundException, IOException, GitHubOAthTokenException {

        Workspace workspace = workspaceClient.createWorkspace(cheServerURL, keycloakToken, params.getStackId(),
                params.getRepo(), params.getBranch(), params.getDescription());

        if (!StringUtils.isBlank(githubToken)) {
            tokenClient.setGitHubOAuthToken(cheServerURL, githubToken, keycloakToken);
            try {
                workspacePreferencesClient.setCommitterInfo(cheServerURL, githubToken, keycloakToken);
            } catch (HttpServerErrorException e) {
                LOG.warn("Unable to set committer info in Che Git preferences");
            }
        }

        return workspace;
    }

    public List<Workspace> listWorkspaces(final String masterURL, final String namespace, final String openShiftToken,
            final String repository, final String requestUrl, final String keycloakToken) throws RouteNotFoundException {
        String cheServerURL = openShiftClientWrapper.getCheServerUrl(masterURL, namespace, openShiftToken);

        List<Workspace> workspaces;
        if (!StringUtils.isBlank(repository)) {
            LOG.info("Fetching workspaces for repositoriy: {}", repository);
            workspaces = workspaceClient.listWorkspacesPerRepository(cheServerURL, repository, keycloakToken);
        } else {
            workspaces = workspaceClient.listWorkspaces(cheServerURL, keycloakToken);
        }
        workspaceHelper.addWorkspaceStartLink(workspaces, requestUrl);
        return workspaces;
    }
}
