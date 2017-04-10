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
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceEnvironments {

    @JsonProperty("default")
    private WorkspaceEnvironment defaultEnv;

    public WorkspaceEnvironment getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(WorkspaceEnvironment defaultEnv) {
        this.defaultEnv = defaultEnv;
    }
}
