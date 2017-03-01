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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.util.WorkspaceHelper;

public class WorkspaceClientTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(WorkspaceClientTest.class);
    private static final String GITHUB_REPO = "https://github.com/che-samples/console-java-simple";
    private static final String BRANCH = "master";
    private static final String STACK_ID = "java-default";

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private WorkspaceClient client;
    
    @Autowired
    private WorkspaceHelper workspaceHelper;

    @Test
    public void listWorkspaces() {
        List<Workspace> workspaces = this.client.listWorkspaces(cheServerURL);
        LOG.info("Number of workspaces: {}", workspaces.size());
        workspaces.forEach(w -> LOG.info("workspace ID: {}", w.getId()));
    }

    @Ignore("Workspace create / delete API is not supported on 'demo.che.ci.centos.org'")
    @Test
    public void createAndDeleteWorkspace() throws IOException {
        Workspace workspace = client.createWorkspace(cheServerURL, workspaceHelper.generateName(), STACK_ID, GITHUB_REPO, BRANCH);
        LOG.info("Workspace URL: {}",workspace.getWorkspaceIdeUrl());
        client.deleteWorkspace(cheServerURL, workspace.getId());
    }

    @Ignore("Workspace stop API is not supported on 'demo.che.ci.centos.org'")
    @Test
    public void stopWorskpace() {
        List<Workspace> workspaces = client.listWorkspaces(cheServerURL);
        if (!workspaces.isEmpty()) {
            List<Workspace> runningWorkspaces = workspaces.stream().filter(w -> w.getStatus().equals("RUNNING"))
                    .collect(Collectors.toList());
            if (!runningWorkspaces.isEmpty()) {
                client.stopWorkspace(cheServerURL, runningWorkspaces.get(0).getId());
            }
        }
    }

}
