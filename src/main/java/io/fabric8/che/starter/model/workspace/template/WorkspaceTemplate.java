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
package io.fabric8.che.starter.model.workspace.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceTemplate {

    private Map<String,WorkspaceEnvironment> environments;
    private String defaultEnv;
    private String name;
    private String description;
    private List<WorkspaceCommand> commands;
    private List<WorkspaceProject> projects;
    private List<WorkspaceLink> links;
    
    public WorkspaceTemplate() {
        environments = new HashMap<String,WorkspaceEnvironment>();
        commands = new ArrayList<WorkspaceCommand>();
        projects = new ArrayList<WorkspaceProject>();
        links = new ArrayList<WorkspaceLink>();
    }
    
    public Map<String, WorkspaceEnvironment> getEnvironments() {
        return environments;
    }
    
    public void setEnvironments(Map<String, WorkspaceEnvironment> environments) {
        this.environments = environments;
    }
    
    public String getDefaultEnv() {
        return defaultEnv;
    }
    
    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
    }
    
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
    
    public List<WorkspaceProject> getProjects() {
        return projects;
    }
    
    public void setProjects(List<WorkspaceProject> projects) {
        this.projects = projects;
    }
    
    public List<WorkspaceLink> getLinks() {
        return links;
    }
    
    public void setLinks(List<WorkspaceLink> links) {
        this.links = links;
    }

}
