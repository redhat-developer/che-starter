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
package io.fabric8.planner.che.starter.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.planner.che.starter.client.CheRestClient;
import io.fabric8.planner.che.starter.client.IdGenerator;
import io.fabric8.planner.che.starter.model.Workspace;

@CrossOrigin
@RestController
@RequestMapping("/workspace")
public class WorkspaceConroller {

    private static final Logger LOG = LogManager.getLogger(WorkspaceConroller.class);

    @Autowired
    CheRestClient cheRestClient;

    @Autowired
    IdGenerator generator;

    @PostMapping
    public Workspace create() {
        return new Workspace();
    }

    @GetMapping
    public List<Workspace> list() {
        return cheRestClient.listWorkspaces();
    }

    @DeleteMapping("/{id}/runtime")
    public Workspace stop(@PathVariable String id) {
        return new Workspace();
    }
    
    @DeleteMapping("/all")
    public void stopAll() {
        cheRestClient.stopAllWorkspaces();
    }

}
