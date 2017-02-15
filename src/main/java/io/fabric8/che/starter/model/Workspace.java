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
    private String status;
    private WorkspaceConfig config;
    private List<WorkspaceLink> links;

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

}
