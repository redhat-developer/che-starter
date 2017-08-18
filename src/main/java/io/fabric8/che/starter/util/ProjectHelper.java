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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.project.Source;

@Component
public class ProjectHelper {
    private static final String GIT_EXTENSION = ".git";
    private static final String GIT_PROTOCOL = "git@";
    private static final String HTTPS_PROTOCOL = "https://";

    public Project initProject(String name, String repo, String branch, String projectType) {
        Project project = new Project();
        project.setName(name);
        Source source = new Source();
        Map<String, String> sourceParams = source.getParameters();
        sourceParams.put("branch", branch);
        sourceParams.put("keepVcs", "true");
        source.setType("git");
        source.setLocation(repo);
        project.setSource(source);
        project.setType(projectType);
        project.setDescription("Created via che-starter API");
        project.setPath("/" + name);
        List<String> mixins = new ArrayList<>();
        mixins.add("git");
        project.setMixins(mixins);
        return project;
    }

    public String getProjectNameFromGitRepository(final String repositoryUrl)
            throws URISyntaxException, MalformedURLException {
        String httpsRepositoryUrl = changeProtocolToHttpsIfNeeded(repositoryUrl);
        URL url = new URL(httpsRepositoryUrl);
        URI uri = url.toURI();
        String path = uri.getPath();
        return getProjectNameFromPath(path);
    }

    private String changeProtocolToHttpsIfNeeded(final String repositoryUrl) {
        if (!StringUtils.isBlank(repositoryUrl) && repositoryUrl.startsWith(GIT_PROTOCOL)) {
            return StringUtils.replaceOnce(repositoryUrl.replace(":", "/"), GIT_PROTOCOL, HTTPS_PROTOCOL);
        }
        return repositoryUrl;
    }

    private String getProjectNameFromPath(final String path) {
        String projectName;
        String normalizedPath = removeTrailingSlash(path);
        if (path.endsWith(GIT_EXTENSION)) {
            projectName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1, normalizedPath.lastIndexOf(GIT_EXTENSION));
         } else {
            projectName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1, normalizedPath.length());
         }
         return projectName;
    }

    private String removeTrailingSlash(final String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
