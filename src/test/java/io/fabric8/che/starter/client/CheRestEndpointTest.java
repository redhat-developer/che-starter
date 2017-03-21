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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import static org.junit.Assert.assertEquals;

public class CheRestEndpointTest extends TestConfig {
    private static final String WORKSPACE_ID = "quwieuqoweuqo";

    @Value("${che.server.url}")
    String cheServerUrl;

    @Test
    public void generateUrlsFromEndpoints() {
        String createWorkspceUrl = CheRestEndpoints.CREATE_WORKSPACE.generateUrl(cheServerUrl);
        assertEquals(createWorkspceUrl, cheServerUrl + "/api/workspace");
        String deleteWorkspaceUrl = CheRestEndpoints.DELETE_WORKSPACE.generateUrl(cheServerUrl, WORKSPACE_ID);
        assertEquals(deleteWorkspaceUrl, cheServerUrl + "/api/workspace/" + WORKSPACE_ID);
    }

}
