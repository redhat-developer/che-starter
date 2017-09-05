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
    private static final String TEST_PROJECT_SHORT_NAME = "test-project";
    private static final String TEST_PROJECT_LONG_NAME = "training-courses-management-system";

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
        String workspaceNameForShortNamedProject = workspaceHelper.generateName(TEST_PROJECT_SHORT_NAME);
        assertTrue(workspaceNameForShortNamedProject.startsWith(TEST_PROJECT_SHORT_NAME));
        assertTrue(workspaceNameForShortNamedProject.matches("[a-zA-Z0-9][-_.a-zA-Z0-9]{1,18}[a-zA-Z0-9]"));

        String workspaceNameForLongNamedProject = workspaceHelper.generateName(TEST_PROJECT_LONG_NAME);
        assertEquals(workspaceNameForLongNamedProject.length(), WorkspaceHelper.RANDOM_POSTFIX_LENGTH);
        assertTrue(workspaceNameForLongNamedProject.matches("[a-zA-Z0-9][-_.a-zA-Z0-9]{1,18}[a-zA-Z0-9]"));
    }

}
