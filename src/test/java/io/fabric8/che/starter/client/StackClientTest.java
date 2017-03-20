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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.model.Stack;

@Ignore("'demo.che.ci.centos.org' is down")
public class StackClientTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(StackClientTest.class);

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    private StackClient client;

    @Test
    public void listStacks() {
        List<Stack> stacks = client.listStacks(cheServerURL, null);
        LOG.info("Number of stacks: {}", stacks.size());
        assertTrue(!stacks.isEmpty());
    }

}
