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

import io.fabric8.che.starter.model.DevMachine;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceRuntime {
    private DevMachine devMachine;

    public DevMachine getDevMachine() {
        return devMachine;
    }

    public void setDevMachine(DevMachine devMachine) {
        this.devMachine = devMachine;
    }
}
