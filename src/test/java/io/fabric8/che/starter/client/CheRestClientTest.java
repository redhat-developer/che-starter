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

import static org.testng.Assert.assertFalse;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import io.fabric8.che.starter.client.CheRestClient;
import io.fabric8.che.starter.model.Workspace;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CheRestClientTest {

    private static final Logger LOG = LogManager.getLogger(CheRestClientTest.class);

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private CheRestClient client;

    @Test
    public void listWorkspaces() {
        List<Workspace> workspaces = this.client.listWorkspaces(cheServerURL);
        LOG.info("Number of workspaces: {}", workspaces.size());
        workspaces.forEach(w -> LOG.info("workspace ID: {}", w.getId()));
        assertFalse(workspaces.isEmpty());
    }

    @Test(expected = HttpClientErrorException.class)
    public void createAndStartWorkspace() {
        client.createAndStartWorkspace(cheServerURL);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stopAllWorskpaces() {
        client.stopAllWorkspaces();
    }

    @Test
    public void stopWorskpace() {
        List<Workspace> workspaces = client.listWorkspaces(cheServerURL);
        if (!workspaces.isEmpty()) {
            List<Workspace> runningWorkspaces = workspaces.stream().filter(w -> w.getStatus().equals("RUNNING"))
                    .collect(Collectors.toList());
            if (!runningWorkspaces.isEmpty()) {
                client.stopWorkspace(cheServerURL, runningWorkspaces.get(0).getId());
            }
        }
    }

}
