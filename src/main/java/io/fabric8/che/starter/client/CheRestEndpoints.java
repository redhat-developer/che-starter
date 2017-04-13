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
package io.fabric8.che.starter.client;

public enum CheRestEndpoints {
    CREATE_WORKSPACE    ("/api/workspace"),
    START_WORKSPACE     ("/api/workspace/{id}/runtime"),
    CHECK_WORKSPACE     ("/api/workspace/{id}/check"),
    DELETE_WORKSPACE    ("/api/workspace/{id}"),
    GET_WORKSPACE_BY_ID ("/api/workspace/{id}"),
    LIST_WORKSPACES     ("/api/workspace"),
    STOP_WORKSPACE      ("/api/workspace/{id}/runtime"),
    LIST_STACKS         ("/api/stack?maxItems=1000"),
    CREATE_PROJECT      ("/project/batch"),
    DELETE_PROJECT      ("/project/{id}"),
    SET_OAUTH_TOKEN     ("/wsmaster/api/oauth/token?oauth_provider={provider}"),
    UPDATE_PREFERENCES  ("/wsmaster/api/preferences");

    private final String endpoint;

    private CheRestEndpoints(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String generateUrl(String cheServerUrl) {
        return cheServerUrl + endpoint;
    }

    public String generateUrl(String cheServerUrl, String id) {
        return cheServerUrl + endpoint.replace("{id}", id);
    }
    
    @Override
    public String toString() {
        return endpoint;
    }
}
