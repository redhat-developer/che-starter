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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceFileToOpen;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;

public class WorkspaceHelperTest extends TestConfig {
    private static final String REQUEST_URL = "http://localhost:10000/workspace";

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
    public void checkStartWorkspaceLinkWith1FileToOpen() {
        // having that
        Workspace workspace = new Workspace();
        String workspaceId = workspaceHelper.generateId();
        workspace.setId(workspaceId);
        workspace.setLinks(new ArrayList<>());

        List<WorkspaceFileToOpen> list = new ArrayList<WorkspaceFileToOpen>();
        list.add(new WorkspaceFileToOpen().withFilePath("myProject/pom.xml")//
                                          .withLine(50));
        workspace.setFilesToOpen(list);

        workspaceHelper.addWorkspaceStartLink(workspace, REQUEST_URL);

        List<WorkspaceLink> links = workspace.getLinks();
        assertEquals(links.size(), 1);

        WorkspaceLink workspaceLink = links.get(0);

        assertEquals(WorkspaceHelper.HTTP_METHOD_PATCH, workspaceLink.getMethod());
        assertEquals(WorkspaceHelper.WORKSPACE_START_IDE_URL, workspaceLink.getRel());
        assertEquals(//
        REQUEST_URL + "/" //
            + workspaceId //
            + "?" //
            + "action=openFile%3Afile%3DmyProject%2Fpom.xml%3Bline%3D50" //
        , workspaceLink.getHref());
    }

    @Test
    public void checkStartWorkspaceLinkWith2FilesToOpen() {
        Workspace workspace = new Workspace();
        String workspaceId = workspaceHelper.generateId();
        workspace.setId(workspaceId);
        workspace.setLinks(new ArrayList<>());

        List<WorkspaceFileToOpen> list = new ArrayList<WorkspaceFileToOpen>();
        list.add(new WorkspaceFileToOpen().withFilePath("myProject/pom.xml")//
                                          .withLine(50));
        list.add(new WorkspaceFileToOpen().withFilePath("myProject/package.json")//
                                          .withLine(60));
        workspace.setFilesToOpen(list);

        workspaceHelper.addWorkspaceStartLink(workspace, REQUEST_URL);

        List<WorkspaceLink> links = workspace.getLinks();
        assertEquals(links.size(), 1);

        WorkspaceLink workspaceLink = links.get(0);

        assertEquals(WorkspaceHelper.HTTP_METHOD_PATCH, workspaceLink.getMethod());
        assertEquals(WorkspaceHelper.WORKSPACE_START_IDE_URL, workspaceLink.getRel());
        assertEquals(//
        REQUEST_URL + "/" //
            + workspaceId //
            + "?" //
            + "action=openFile%3Afile%3DmyProject%2Fpom.xml%3Bline%3D50" //
            + "&" //
            + "action=openFile%3Afile%3DmyProject%2Fpackage.json%3Bline%3D60"
        , workspaceLink.getHref());
    }

}
