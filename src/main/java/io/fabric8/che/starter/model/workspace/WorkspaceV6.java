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

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceV6 {

    private String id;
    private String status;
    // Runtime is returned only on get workspace by composite key, in collection call, it is not
    private WorkspaceRuntime runtime;
    private WorkspaceConfigV6 config;
    private Map<String, String> links;

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

    public WorkspaceConfigV6 getConfig() {
        return config;
    }

    public void setConfig(WorkspaceConfigV6 config) {
        this.config = config;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public WorkspaceRuntime getRuntime() {
        return runtime;
    }

    public void setRuntime(WorkspaceRuntime runtime) {
        this.runtime = runtime;
    }

}
