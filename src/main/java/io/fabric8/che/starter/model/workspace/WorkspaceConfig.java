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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceConfig {
	
	private List<WorkspaceCommand> commands;
	// there could go list of projects
	private String defaultEnv;
	private String description;
	private WorkspaceEnvironments environments;
    private String name;
    private List<WorkspaceLink> links;

    public WorkspaceConfig() {
    	commands = new ArrayList<WorkspaceCommand>();
    	links = new ArrayList<WorkspaceLink>();
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

	public String getDefaultEnv() {
		return defaultEnv;
	}

	public void setDefaultEnv(String defaultEnv) {
		this.defaultEnv = defaultEnv;
	}

	public WorkspaceEnvironments getEnvironments() {
		return environments;
	}

	public void setEnvironments(WorkspaceEnvironments environments) {
		this.environments = environments;
	}

	public List<WorkspaceLink> getLinks() {
		return links;
	}

	public void setLinks(List<WorkspaceLink> links) {
		this.links = links;
	}

}
