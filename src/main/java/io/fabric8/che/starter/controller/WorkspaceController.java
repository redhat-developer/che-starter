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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.WorkspaceClient;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.che.starter.util.ProjectHelper;
import io.fabric8.che.starter.util.WorkspaceHelper;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {
    private static final Logger LOG = LogManager.getLogger(WorkspaceController.class);

    @Autowired
    OpenShiftClientWrapper clientWrapper;

    @Autowired
    WorkspaceClient workspaceClient;

    @Autowired
    ProjectHelper projectHelper;

    @Autowired
    WorkspaceHelper workspaceHelper;

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping
    public Workspace create(@RequestParam String masterUrl, @RequestParam String namespace, @RequestBody WorkspaceCreateParams params,
            @RequestHeader("Authorization") String token) throws IOException, URISyntaxException, RouteNotFoundException {
        String cheServerUrl = clientWrapper.getCheServerUrl(masterUrl, namespace, token);

        String projectName = projectHelper.getProjectNameFromGitRepository(params.getRepo());

        List<Workspace> workspaces = workspaceClient.listWorkspaces(cheServerUrl);

        String description = workspaceHelper.getDescription(params.getRepo(), params.getBranch());

        for (Workspace ws : workspaces) {
            String wsDescription = ws.getDescription();
            if (wsDescription != null && wsDescription.equals(description)) {
                // Before we can create a project, we must start the new workspace.  First check it's not already running
                if (!WorkspaceClient.WORKSPACE_STATUS_RUNNING.equals(ws.getStatus()) && 
                        !WorkspaceClient.WORKSPACE_STATUS_STARTING.equals(ws.getStatus())) {
                    workspaceClient.startWorkspace(cheServerUrl, ws.getId());
                }
                return ws;
            }
        }

        // Create the workspace
        Workspace workspaceInfo = workspaceClient.createWorkspace(cheServerUrl, params.getName(), params.getStack(),
                params.getRepo(), params.getBranch());

        // Create the project - this is an async call
        workspaceClient.createProject(cheServerUrl, workspaceInfo.getId(), projectName, params.getRepo(),
                params.getBranch());
        
        return workspaceInfo;
    }

    @ApiOperation(value = "List workspaces per git repository. If repository parameter is not specified return all workspaces")
    @GetMapping
    public List<Workspace> list(@RequestParam String masterUrl, @RequestParam String namespace, @RequestParam(required = false) String repository,
            @RequestHeader("Authorization") String token) throws RouteNotFoundException {
        String cheServerUrl = clientWrapper.getCheServerUrl(masterUrl, namespace, token);
        if (!StringUtils.isEmpty(repository)) {
            LOG.info("Fetching workspaces for repositoriy: {}", repository);
            return workspaceClient.listWorkspacesPerRepository(cheServerUrl, repository);
        }
        return workspaceClient.listWorkspaces(cheServerUrl);
    }

}
