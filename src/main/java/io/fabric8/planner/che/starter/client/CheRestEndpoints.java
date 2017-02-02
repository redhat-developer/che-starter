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
package io.fabric8.planner.che.starter.client;

public enum CheRestEndpoints {
    CREATE_WORKSPACE("http://demo.che.ci.centos.org/api/workspace"),
    LIST_WORKSPACES ("http://demo.che.ci.centos.org/api/workspace");

    private final String endpoint;

    private CheRestEndpoints(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return endpoint;
    }

}