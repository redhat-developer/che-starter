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
package io.fabric8.che.starter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;

public class WorkspaceHelperTest extends TestConfig {
    private static final String REQUEST_URL = "http://localhost:10000/workspace";
    private static final String TEST_PROJECT_NAME = "test-project";

    @Autowired
    WorkspaceHelper workspaceHelper;

    @Test
    public void checkStartWorkspaceLink() {
        Workspace workspace = new Workspace();

        String workspaceId = workspaceHelper.generateId();
        workspace.setId(workspaceId);
        workspace.setLinks(new ArrayList<>());

        workspaceHelper.addWorkspaceStartLink(workspace, REQUEST_URL);

        List<WorkspaceLink> links = workspace.getLinks();

        assertEquals(links.size(), 1);

        WorkspaceLink workspaceLink = links.get(0);

        assertEquals(WorkspaceHelper.HTTP_METHOD_PATCH, workspaceLink.getMethod());
        assertEquals(WorkspaceHelper.WORKSPACE_START_IDE_URL, workspaceLink.getRel());
        assertEquals(REQUEST_URL + "/" + workspaceId, workspaceLink.getHref());
    }

    @Test
    public void generateWorkspaceName() {
        String workspaceName = workspaceHelper.generateName(TEST_PROJECT_NAME);
        String[] split = workspaceName.split("-");
        String randomPostfix = split[split.length - 1];
        assertEquals(randomPostfix.length(), WorkspaceHelper.RANDOM_POSTFIX_LENGTH);
        assertTrue(randomPostfix.matches("^[a-z0-9]+$"));
    }

}
