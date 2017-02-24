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

import io.fabric8.che.starter.model.Workspace;

@Component
public class WorkspaceHelper {
    private static final String REPO_BRANCH_DELIMITER = "#";

    public String getDescription(final String repo, final String branch) {
        return repo + REPO_BRANCH_DELIMITER + branch;
    }

    public List<Workspace> filterByRepository(final List<Workspace> workspaces, final String repository) {
        return workspaces.stream().filter(w -> {
            String description = w.getDescription();
            return description != null && description.split(REPO_BRANCH_DELIMITER)[0].equals(repository);
        }).collect(Collectors.toList());
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

}
