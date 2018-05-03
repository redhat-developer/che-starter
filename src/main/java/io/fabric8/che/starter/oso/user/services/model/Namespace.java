/*-
 * #%L
 * che-starter
 * %%
 * Copyright (C) 2018 Red Hat, Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package io.fabric8.che.starter.oso.user.services.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Namespace {
    private String name;

    @JsonProperty("cluster-app-domain")
    private String clusterAppDomain;
    
    @JsonProperty("cluster-capacity-exhausted")
    private boolean clusterCapacityExhausted;

    public String getClusterAppDomain() {
        return clusterAppDomain;
    }

    public void setClusterAppDomain(String clusterAppDomain) {
        this.clusterAppDomain = clusterAppDomain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClusterCapacityExhausted() {
        return clusterCapacityExhausted;
    }

    public void setClusterCapacityExhausted(boolean clusterCapacityExhausted) {
        this.clusterCapacityExhausted = clusterCapacityExhausted;
    }
}
