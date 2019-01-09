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
package io.fabric8.che.starter.model.workspace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.che.starter.model.project.Project;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfig {

    private List<WorkspaceCommand> commands;
    private String defaultEnv;
    private String description;
    private Map<String, WorkspaceEnvironment> environments;
    private String name;
    private List<WorkspaceLink> links;
    private List<Project> projects;
    private Map<String, String> attributes;

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

    public List<WorkspaceCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<WorkspaceCommand> commands) {
        this.commands = commands;
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
    }

    public Map<String, WorkspaceEnvironment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Map<String, WorkspaceEnvironment> environments) {
        this.environments = environments;
    }

    public List<WorkspaceLink> getLinks() {
        return links;
    }

    public void setLinks(List<WorkspaceLink> links) {
        this.links = links;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
