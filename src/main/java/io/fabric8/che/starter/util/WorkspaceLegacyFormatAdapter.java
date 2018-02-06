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

import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;
import io.fabric8.che.starter.model.workspace.WorkspaceV6;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkspaceLegacyFormatAdapter {

    private static final String RUNTIME = "/runtime";
    private static final String SNAPSHOT = "/snapshot";

    /**
     * Takes the new format and returns the legacy format with all the necessary
     * data and links fabricated from what is returned by new che server
     * @param workspaceV6 Che 6 compatible Workspace response model
     * @return Che 5 compatible Workspace response model
     */
    public static Workspace getWorkspaceLegacyFormat(final WorkspaceV6 workspaceV6) {
        Workspace response = new Workspace();
        response.setConfig(workspaceV6.getConfig());
        response.setId(workspaceV6.getId());
        response.setRuntime(workspaceV6.getRuntime());
        response.setStatus(workspaceV6.getStatus());
        response.setLinks(convertLinksToLegacy(workspaceV6));
        return response;
    }

    /**
     * Takes <self>, <ide> and <channel> links, adding or subtracting necessary constants to get
     * all the links used by Che 5 server
     * As new links are returned as a <Map><String, String></Map>, this function iterates through
     * the keys and fabricates all the legacy data from sources that are present
     * @param workspaceV6 Che 6 Workspace response model
     * @return <List><WorkspaceLink></List> Arraylist of legacy formated links
     */
    private static List<WorkspaceLink> convertLinksToLegacy(WorkspaceV6 workspaceV6) {
        Map<String, String> links = workspaceV6.getLinks();
        List<WorkspaceLink> response = new ArrayList<>();
        links.entrySet().parallelStream().forEach(entry -> {
            switch (entry.getKey()) {
                case "self":
                    WorkspaceLink self = new WorkspaceLink();
                    WorkspaceLink start = new WorkspaceLink();
                    WorkspaceLink remove = new WorkspaceLink();
                    WorkspaceLink getAllWorkspaces = new WorkspaceLink();
                    WorkspaceLink getSnapshot = new WorkspaceLink();

                    self.setHref(entry.getValue());
                    self.setRel("self link");
                    self.setMethod(HttpMethod.GET.name().toUpperCase());

                    start.setHref(entry.getValue() + RUNTIME);
                    start.setRel("start workspace");
                    start.setMethod(HttpMethod.POST.name().toUpperCase());

                    remove.setHref(entry.getValue());
                    remove.setRel("remove workspace");
                    remove.setMethod(HttpMethod.DELETE.name().toUpperCase());

                    getAllWorkspaces.setHref(entry.getValue().substring(0, entry.getValue().length() - (workspaceV6.getId().length() + 1)));
                    getAllWorkspaces.setRel("get all user workspaces");
                    getAllWorkspaces.setMethod(HttpMethod.GET.name().toUpperCase());

                    getSnapshot.setHref(entry.getValue() + SNAPSHOT);
                    getSnapshot.setRel("get workspace snapshot");
                    getSnapshot.setMethod(HttpMethod.GET.name().toUpperCase());

                    response.add(self);
                    response.add(start);
                    response.add(remove);
                    response.add(getAllWorkspaces);
                    response.add(getSnapshot);
                    break;
                case "ide":
                    WorkspaceLink ide = new WorkspaceLink();
                    ide.setHref(entry.getValue());
                    ide.setRel("ide url");
                    ide.setMethod(HttpMethod.GET.name().toUpperCase());
                    response.add(ide);
                    break;
                case "environment/outputChannel":
                    WorkspaceLink outputChannel = new WorkspaceLink();
                    WorkspaceLink getWorkspaceEvents = new WorkspaceLink();

                    outputChannel.setHref(entry.getValue());
                    outputChannel.setRel("environment.output_channel");
                    outputChannel.setMethod(HttpMethod.GET.name().toUpperCase());

                    getWorkspaceEvents.setHref(entry.getValue());
                    getWorkspaceEvents.setRel("get workspace events channel");
                    getWorkspaceEvents.setMethod(HttpMethod.GET.name().toUpperCase());

                    response.add(outputChannel);
                    response.add(getWorkspaceEvents);
                    break;
                case "environment/statusChannel":
                    WorkspaceLink statusChannel = new WorkspaceLink();
                    statusChannel.setHref(entry.getValue());
                    statusChannel.setRel("environment.status_channel");
                    statusChannel.setMethod(HttpMethod.GET.name().toUpperCase());
                    response.add(statusChannel);
                    break;
                default: break;
            }
        });
        return response;
    }

}
