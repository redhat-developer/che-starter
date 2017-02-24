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

import io.fabric8.che.starter.client.CheRestClient;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.openshift.Client;
import io.fabric8.che.starter.openshift.Router;
import io.fabric8.che.starter.util.Generator;
import io.fabric8.che.starter.util.ProjectHelper;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {
    private static final Logger LOG = LogManager.getLogger(WorkspaceController.class);

    @Autowired
    Client client;

    @Autowired
    Router router;

    @Autowired
    CheRestClient cheRestClient;
    
    @Autowired
    ProjectHelper projectHelper;

    @Autowired
    Generator generator;

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping
    public Workspace create(@RequestParam String masterUrl, @RequestBody WorkspaceCreateParams params,
            @RequestHeader("Authorization") String token) throws IOException, URISyntaxException {
        String cheServerUrl = getCheServerUrl(masterUrl, token);

        String projectName = projectHelper.getProjectNameFromGitRepository(params.getRepo());

        List<Workspace> workspaces = cheRestClient.listWorkspaces(cheServerUrl);

        String workspaceLocator = cheRestClient.workspaceLocatorKey(params.getRepo(), params.getBranch());

        for (Workspace ws : workspaces) {
            if (ws.getDescription().equals(workspaceLocator)) {
                // Before we can create a project, we must start the new workspace.  First check it's not already running
                if (!CheRestClient.WORKSPACE_STATUS_RUNNING.equals(ws.getStatus()) && 
                        !CheRestClient.WORKSPACE_STATUS_STARTING.equals(ws.getStatus())) {               
                    cheRestClient.startWorkspace(cheServerUrl, ws.getId());
                }
                
                return ws;
            }
        }

        Workspace workspaceInfo = cheRestClient.createWorkspace(cheServerUrl, params.getName(), params.getStack(),
                params.getRepo(), params.getBranch());

        cheRestClient.createProject(cheServerUrl, workspaceInfo.getId(), projectName, params.getRepo(),
                params.getBranch());
        
        return workspaceInfo;
    }

    @ApiOperation(value = "List workspaces")
    @GetMapping
    public List<Workspace> list(@RequestParam String masterUrl, @RequestParam(required = false) String repository,
            @RequestHeader("Authorization") String token) {
        String cheServerUrl = getCheServerUrl(masterUrl, token);
        if (!StringUtils.isEmpty(repository)) {
            LOG.info("Fetching workspaces for repositoriy: {}", repository);
            return cheRestClient.listWorkspacesPerRepository(cheServerUrl, repository);
        }
        return cheRestClient.listWorkspaces(cheServerUrl);
    }

    private String getCheServerUrl(String masterUrl, String token) {
        OpenShiftClient openShiftClient = client.get(masterUrl, token);
        return router.getUrl(openShiftClient);
    }

}
