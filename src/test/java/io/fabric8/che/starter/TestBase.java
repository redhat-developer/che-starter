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
package io.fabric8.che.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.che.starter.model.request.WorkspaceCreateParams;

public class TestBase {

    public static final String VERTX_SERVER = "http://localhost:33333";
    public static final String NAMESPACE = "eclipse-che";
    public static final String OPENSHIFT_TOKEN = "openshifttoken";
    public static final String KEYCLOAK_TOKEN = "Bearer keycloaktoken";
    public static final String GITHUB_TOKEN = "githubtoken";

    public static final String WORKSPACE_IDE_URL = "http://localhost:33333/che/vertxworkspace";

    protected static final String PROJECT_GIT_REPO = "https://github.com/mlabuda/vertx-with-che.git";
    protected static final String PROJECT_GIT_BRANCH = "master";
    protected static final String WORKSPACE_DESCRIPTION = "https://github.com/mlabuda/vertx-with-che.git#master#WI13";
    protected static final String PROJECT_ID = "/vertx-with-che";
    protected static final String WORKSPACE_STACK_ID = "vert.x";
    protected static final String WORKSPACE_NAME = "chevertxwsid13";

    public String getCreateWorkspaceRequestBody(WorkspaceCreateParams workspaceParams) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(workspaceParams);
    }

    public WorkspaceCreateParams initWorkspaceCreateParams() {
        WorkspaceCreateParams workspaceParams = new WorkspaceCreateParams();
        workspaceParams.setRepo(PROJECT_GIT_REPO);
        workspaceParams.setBranch(PROJECT_GIT_BRANCH);
        workspaceParams.setStackId(WORKSPACE_STACK_ID);
        workspaceParams.setDescription(WORKSPACE_DESCRIPTION);
        return workspaceParams;
    }
}
