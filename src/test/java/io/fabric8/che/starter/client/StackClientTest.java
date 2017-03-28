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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.stack.Stack;
import io.fabric8.che.starter.model.stack.StackProjectMapping;

public class StackClientTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(StackClientTest.class);
    private static final String VERTX_STACK_ID = "vert.x";
    private static final String NON_EXISTING_STACK_ID = "non-existing-stack-id";
    private static final String KEYCLOAK_TOKEN = null;

    @Value("${che.server.url}")
    String cheServerUrl;

    @Autowired
    private StackClient client;

    @Test
    public void listStacks() {
        List<Stack> stacks = client.listStacks(cheServerUrl, KEYCLOAK_TOKEN);
        LOG.info("Number of stacks: {}", stacks.size());
        assertTrue(!stacks.isEmpty());
    }

    @Test
    public void vertxStackExists() {
        List<Stack> stacks = client.listStacks(cheServerUrl, KEYCLOAK_TOKEN);
        Stack stack = stacks.stream().filter(s -> VERTX_STACK_ID.equals(s.getId())).findFirst().get();
        assertNotNull(VERTX_STACK_ID + " not Found", stack);
        LOG.info("vertx stack is found: {}", stack.getId());
        assertEquals(VERTX_STACK_ID, stack.getId());
    }

    @Test
    public void getVertxStack() throws StackNotFoundException {
        Stack vertxStack = client.getStack(cheServerUrl, VERTX_STACK_ID, KEYCLOAK_TOKEN);
        LOG.info("vertx stack is found: {}", vertxStack);
    }

    @Test
    public void getProjectTypeForVertxStack() {
        String projectType = client.getProjectTypeByStackId(VERTX_STACK_ID);
        assertEquals(projectType, "maven");
    }

    @Test
    public void getProjectTypeForNonExistingStack() {
        String projectType = client.getProjectTypeByStackId(NON_EXISTING_STACK_ID);
        assertEquals(projectType, StackProjectMapping.BLANK_PROJECT_TYPE);
    }

    @Test(expected = StackNotFoundException.class)
    public void getNonExistingStackImage() throws StackNotFoundException {
        client.getStack(cheServerUrl, NON_EXISTING_STACK_ID, KEYCLOAK_TOKEN);
    }

}
