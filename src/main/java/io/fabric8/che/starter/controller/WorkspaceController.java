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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.CheRestClient;
import io.fabric8.che.starter.client.Generator;
import io.fabric8.che.starter.model.response.WorkspaceResponse;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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

    @PostMapping
    public WorkspaceResponse create() throws IOException {
    	return cheRestClient.createWorkspace(cheServerURL);
    }

    @ApiOperation(value = "List the user's workspaces")
    @ApiImplicitParams({
        
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success")
    })
    @GetMapping
    public List<WorkspaceResponse> list() {
        return cheRestClient.listWorkspaces(cheServerURL);
    }

    @DeleteMapping("/{id}/runtime")
    public void stop(@PathVariable String id) {
        cheRestClient.stopWorkspace(cheServerURL, id);
    }
    
    @DeleteMapping("/all")
    public void stopAll() {
        cheRestClient.stopAllWorkspaces();
    }

}
