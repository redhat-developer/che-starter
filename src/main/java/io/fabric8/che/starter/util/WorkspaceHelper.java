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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceFileToOpen;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;
import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class WorkspaceHelper {

    private static final Logger LOG = LogManager.getLogger(WorkspaceHelper.class);

    public static final String WORKSPACE_START_IDE_URL = "ide start url";
    public static final String WORKSPACE_IDE_URL = "ide url";
    public static final String HTTP_METHOD_PATCH = "PATCH";

    public List<Workspace> filterByRepository(final List<Workspace> workspaces, final String repository) {
        return workspaces.stream().filter(w -> {
            List<Project> projects = w.getConfig().getProjects();
            if (projects != null && !projects.isEmpty()) {
                for (Project project : projects) {
                    if (project.getSource() != null && project.getSource().getLocation() != null) {
                        if (repository.equals(project.getSource().getLocation())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * @param workspaces
     * @param requestURL
     * @see https://github.com/redhat-developer/che-starter/issues/133
     */
    public void addWorkspaceStartLink(final List<Workspace> workspaces, final String requestURL) {
        for (Workspace w : workspaces) {
            addWorkspaceStartLink(w, requestURL);
        }
    }

    public void addWorkspaceStartLink(final Workspace workspace, final String requestURL) {
        String workspaceId = workspace.getId();
        String href = generateHrefForWorkspaceStartLink(requestURL, workspaceId);
        href = appendOpenFileAction(workspace, href);

        WorkspaceLink startingLink = new WorkspaceLink();
        startingLink.setHref(href);
        startingLink.setMethod(HTTP_METHOD_PATCH);
        startingLink.setRel(WORKSPACE_START_IDE_URL);

        workspace.getLinks().add(startingLink);
    }

    protected String appendOpenFileAction(final Workspace workspace, String href) {
        try {
            List<WorkspaceFileToOpen> filesToOpenList = workspace.getFilesToOpen();
            if (filesToOpenList == null || filesToOpenList.size() <= 0) {
                return href;
            }

            UriComponentsBuilder uribuilder = UriComponentsBuilder.fromUriString(href);
            for (WorkspaceFileToOpen fileToOpen : filesToOpenList) {
                uribuilder.queryParam("action", URLEncoder.encode("openFile:file=" + fileToOpen.getFilePath() + ";line="
                                                                      + fileToOpen.getLine(), UTF_8.toString()));
            }
            return uribuilder.build().toUriString();

        } catch (UnsupportedEncodingException e) {
            LOG.error("couldn't encode openfiles actions url parameters with UTF-8 ... ignoring", e);
        }
        return href;
    }

    public WorkspaceLink getWorkspaceIdeLink(final Workspace workspace) {
        List<WorkspaceLink> links = workspace.getLinks();
        return links.stream().filter(link -> WORKSPACE_IDE_URL.equals(link.getRel())).findFirst().get();
    }

    /**
     * Che workspace id is used as OpenShift service / deployment config name
     * and must match the regex [a-z]([-a-z0-9]*[a-z0-9]) e.g.
     * "q5iuhkwjvw1w9emg"
     *
     * @return randomly generated workspace id
     */
    public String generateId() {
        return RandomStringUtils.random(16, true, true).toLowerCase();
    }

    public String generateName() {
        return RandomStringUtils.random(8, true, true).toLowerCase();
    }

    private String generateHrefForWorkspaceStartLink(final String requestURL, final String workspaceId) {
        return requestURL + "/" + workspaceId;
    }

}
