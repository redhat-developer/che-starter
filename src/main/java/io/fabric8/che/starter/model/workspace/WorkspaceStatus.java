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

public enum WorkspaceStatus {
    RUNNING("RUNNING"),
    STARTING("STARTING"),
    STOPPED("STOPPED"),
    STOPPING("STOPPING");

    private final String status;

    private WorkspaceStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
