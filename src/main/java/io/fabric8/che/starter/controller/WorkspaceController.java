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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.CheRestClient;
import io.fabric8.che.starter.client.Generator;
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.model.response.WorkspaceInfo;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    private static final Logger LOG = LogManager.getLogger(WorkspaceController.class);

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    CheRestClient cheRestClient;

    @Autowired
    Generator generator; 

    @ApiOperation(value = "Create and start a new workspace. Stop all other workspaces (only one workspace can be running at a time). If a workspace with the imported project already exists, just start it")
    @PostMapping
    public WorkspaceInfo create(@RequestBody String masterUrl, @RequestBody String userToken, @RequestBody WorkspaceCreateParams parameters) throws IOException {
        LOG.info("OpenShift MasterURL: {}", masterUrl);
        return cheRestClient.createWorkspace(cheServerURL, parameters.getName(), parameters.getStack(), parameters.getRepo(), parameters.getBranch());
    }

    @ApiIgnore
    @GetMapping
    public List<WorkspaceInfo> list() {
        return cheRestClient.listWorkspaces(cheServerURL);
    }

    @ApiIgnore
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        cheRestClient.deleteWorkspace(cheServerURL, id);
    }

    @ApiIgnore
    @DeleteMapping("/{id}/runtime")
    public void stop(@PathVariable String id) {
        cheRestClient.stopWorkspace(cheServerURL, id);
    }

    @ApiIgnore
    @DeleteMapping("/all")
    public void stopAll() {
        cheRestClient.stopAllWorkspaces();
    }

}
