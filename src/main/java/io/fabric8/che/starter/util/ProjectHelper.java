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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ProjectHelper {
    private static final String GIT_EXTENSION = ".git";
    private static final String GIT_PROTOCOL = "git@";
    private static final String HTTPS_PROTOCOL = "https://";

    public String getProjectNameFromGitRepository(final String repositoryUrl)
            throws URISyntaxException, MalformedURLException {
        String httpsRepositoryUrl = changeProtocolToHttpsIfNeeded(repositoryUrl);
        URL url = new URL(httpsRepositoryUrl);
        URI uri = url.toURI();
        String path = uri.getPath();
        return getProjectNameFromPath(path);
    }

    private String changeProtocolToHttpsIfNeeded(final String repositoryUrl) {
        if (repositoryUrl.startsWith(GIT_PROTOCOL)) {
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
