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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;

@Component
public class WorkspaceHelper {
    public static final String WORKSPACE_START_IDE_URL = "ide start url";
    public static final String WORKSPACE_IDE_URL = "ide url";
    public static final String HTTP_METHOD_PATCH = "PATCH";
    public static final int RANDOM_POSTFIX_LENGTH = 5;

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

        WorkspaceLink startingLink = new WorkspaceLink();
        startingLink.setHref(href);
        startingLink.setMethod(HTTP_METHOD_PATCH);
        startingLink.setRel(WORKSPACE_START_IDE_URL);

        workspace.getLinks().add(startingLink);
    }

    public WorkspaceLink getWorkspaceIdeLink(final Workspace workspace) {
        List<WorkspaceLink> links = workspace.getLinks();
        return links.stream().filter(link -> WORKSPACE_IDE_URL.equals(link.getRel())).findFirst().orElse(null);
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

    /**
     * Generates workspace name based on projectName with random postfix e.g. 'che-starter-f8zbl2is'
     * @param projectName
     * @return workspace name
     * @see https://github.com/openshiftio/openshift.io/issues/446
     * @see https://github.com/eclipse/che/issues/6130 
     */
    public String generateName(final String projectName) {
        String randomPostfix = RandomStringUtils.random(RANDOM_POSTFIX_LENGTH, true, true).toLowerCase();
        String workspaceName = projectName + "-" + randomPostfix;
        // hot fix for workspace validator - currently max length for workspace name is 100 chars
        return (workspaceName.length() <= 100) ? workspaceName : randomPostfix;
    }

    private String generateHrefForWorkspaceStartLink(final String requestURL, final String workspaceId) {
        return requestURL + "/" + workspaceId;
    }

}
