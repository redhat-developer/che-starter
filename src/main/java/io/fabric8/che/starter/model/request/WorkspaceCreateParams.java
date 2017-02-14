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

    private String masterURL;
    private String userToken;
    private String name;
    private String stack;
    private String repo;
    private String branch;

    public String getMasterURL() {
        return masterURL;
    }

    public void setMasterURL(String masterURL) {
        this.masterURL = masterURL;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
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

}
