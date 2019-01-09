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

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceMachineV6 {
    private List<String> installers;
    private WorkspaceMachineAttribute attributes;
    private Map<String, WorkspaceMachineServers> servers;
    private Map<String, Map<String, String>> volumes;

    public List<String> getInstallers() {
        return installers;
    }

    public void setInstallers(List<String> installers) {
        this.installers = installers;
    }

    public WorkspaceMachineAttribute getAttributes() {
        return attributes;
    }

    public void setAttributes(WorkspaceMachineAttribute attributes) {
        this.attributes = attributes;
    }

    public Map<String, WorkspaceMachineServers> getServers() {
        return servers;
    }

    public void setServers(Map<String, WorkspaceMachineServers> servers) {
        this.servers = servers;
    }

    public Map<String, Map<String, String>> getVolumes() {
        return volumes;
    }

    public void setVolumes(Map<String, Map<String, String>> volumes) {
        this.volumes = volumes;
    }

}
