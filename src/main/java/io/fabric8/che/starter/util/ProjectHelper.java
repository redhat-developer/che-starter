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

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class ProjectHelper {

    public String getProjectNameFromGitRepository(String repositoryUrl)
            throws URISyntaxException, MalformedURLException {
        URL url = new URL(repositoryUrl);
        URI uri = url.toURI();
        String path = uri.getPath();
        String projectName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        return projectName;
    }

    public String generateName() {
        return RandomStringUtils.random(8, true, true).toLowerCase();
    }

}
