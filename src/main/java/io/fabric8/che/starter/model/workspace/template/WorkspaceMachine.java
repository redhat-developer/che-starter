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
public class WorkspaceMachine {
    
    private List<WorkspaceServer> servers;
    private List<String> agents;
    private Map<String,String> attributes;
    
    public WorkspaceMachine() {
        servers = new ArrayList<WorkspaceServer>();
        agents = new ArrayList<String>();
        attributes = new HashMap<String,String>();
    }
    
    public List<WorkspaceServer> getServers() {
        return servers;
    }
    
    public void setServers(List<WorkspaceServer> servers) {
        this.servers = servers;
    }
    
    public List<String> getAgents() {
        return agents;
    }
    
    public void setAgents(List<String> agents) {
        this.agents = agents;
    }
    
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
