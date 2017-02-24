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
package io.fabric8.che.starter.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workspace {

    private String id;
    private String name;
    private String description;
    private String login;
    private String status;
    private String repository;
    private String branch;
    private WorkspaceConfig config;
    private List<WorkspaceLink> links;
    private WorkspaceRuntime runtime;
    private String location;
    private String workspaceIdeUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWorkspaceIdeUrl() {
        return workspaceIdeUrl;
    }

    public void setWorkspaceIdeUrl(String workspaceIdeUrl) {
        this.workspaceIdeUrl = workspaceIdeUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public WorkspaceConfig getConfig() {
        return config;
    }

    public void setConfig(WorkspaceConfig config) {
        this.config = config;
    }

    public List<WorkspaceLink> getLinks() {
        return links;
    }

    public void setLinks(List<WorkspaceLink> links) {
        this.links = links;
    }

    public WorkspaceRuntime getRuntime() {
        return runtime;
    }

    public void setRuntime(WorkspaceRuntime runtime) {
        this.runtime = runtime;
    }
}
