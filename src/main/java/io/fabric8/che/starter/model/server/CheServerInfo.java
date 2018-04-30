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
package io.fabric8.che.starter.model.server;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheServerInfo {
    private boolean isRunning;
    private List<CheServerLink> links;
    private boolean isMultiTenant;
    private boolean isClusterFull;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public List<CheServerLink> getLinks() {
        return links;
    }

    public void setLinks(List<CheServerLink> links) {
        this.links = links;
    }

    public boolean isMultiTenant() {
        return isMultiTenant;
    }

    public void setMultiTenant(boolean isMultiTenant) {
        this.isMultiTenant = isMultiTenant;
    }

    public boolean isClusterFull() {
        return isClusterFull;
    }

    public void setClusterFull(boolean isClusterFull) {
        this.isClusterFull = isClusterFull;
    }

}
