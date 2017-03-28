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
package io.fabric8.che.starter.model.stack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.fabric8.che.starter.model.workspace.WorkspaceConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stack {
    private String id;
    private String name;
    private String description;
    private WorkspaceConfig workspaceConfig;
    private StackSource source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public StackSource getSource() {
    	return source;
    }
    
    public void setSource(StackSource source) {
    	this.source = source;
    }

	public WorkspaceConfig getWorkspaceConfig() {
		return workspaceConfig;
	}

	public void setWorkspaceConfig(WorkspaceConfig workspaceConfig) {
		this.workspaceConfig = workspaceConfig;
	}
}
