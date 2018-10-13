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
package io.fabric8.che.starter.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This DTO is used to manage the workspace creation parameters coming from a
 * client calling the workspace create method.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceCreateParams {
    private String stackId;
    private String repo;
    private String branch;
    private String description;
    // openshift.io specific parameters
    private String spaceId;

    public String getStackId() {
        return stackId;
    }

    public void setStackId(String stackId) {
        this.stackId = stackId;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

}
