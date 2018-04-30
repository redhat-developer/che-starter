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
package io.fabric8.che.starter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.server.CheServerInfo;
import io.fabric8.che.starter.model.server.CheServerLink;

public class CheServerHelperTest extends TestConfig {
    private static final boolean IS_RUNNING = true;
    private static final boolean IS_MULTI_TENANT = false;
    private static final boolean IS_CLUSTER_FULL = false;
    private static final String REQUEST_URL = "http://localhost:10000/server";

    @Test
    public void checkCheServerStatusLink() {
        CheServerInfo info = CheServerHelper.generateCheServerInfo(IS_RUNNING, REQUEST_URL, IS_MULTI_TENANT, IS_CLUSTER_FULL);
        List<CheServerLink> links = info.getLinks();

        assertTrue(info.isRunning());
        assertFalse(links.isEmpty());
        assertFalse(info.isMultiTenant());
        assertFalse(info.isClusterFull());

        CheServerLink statusLink = links.stream()
                .filter(link -> CheServerHelper.CHE_SERVER_STATUS_URL.equals(link.getRel())).findFirst().get();

        assertEquals(REQUEST_URL, statusLink.getHref());
    }
}
