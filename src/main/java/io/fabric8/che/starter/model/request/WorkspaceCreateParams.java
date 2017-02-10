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

import io.swagger.annotations.ApiImplicitParam;

/**
 * This DTO is used to manage the workspace creation parameters coming from a
 * client calling the workspace create method.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceCreateParams {

    private String name;
    private String repo;
    private String branch;

    // @ApiImplicitParam(name = "name", value = "Workspace Name", required =
    // false, dataType = "string", paramType = "path")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // @ApiImplicitParam(name = "repo", value = "Git repository URL", required =
    // true, dataType = "string", paramType = "path")
    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    // @ApiImplicitParam(name = "branch", value = "Git branch", required = true,
    // dataType = "string", paramType = "path")
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

}
