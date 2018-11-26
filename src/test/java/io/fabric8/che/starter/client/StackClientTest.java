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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.stack.Stack;
import io.fabric8.che.starter.model.stack.StackProjectMapping;
import io.fabric8.che.starter.model.workspace.WorkspaceEnvironment;

public class StackClientTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(StackClientTest.class);
    private static final String VERTX_STACK_ID = "vert.x";
    private static final String JAVA_CENTOS_STACK_ID = "java-centos";
    private static final String SPRING_BOOT_STACK_ID = "spring-boot";
    private static final String WILDFLY_SWARM_STACK_ID = "wildfly-swarm";
    private static final String NODE_JS_STACK_ID = "nodejs-centos";
    private static final String NON_EXISTING_STACK_ID = "non-existing-stack-id";

    @Autowired
    private StackClient client;

    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Test
    public void listStacks() {
        List<Stack> stacks = client.listStacks(osioUserToken);
        LOG.info("Number of stacks: {}", stacks.size());
        assertTrue(!stacks.isEmpty());
    }

    @Test
    public void vertxStackExists() {
        List<Stack> stacks = client.listStacks(osioUserToken);
        Stack stack = stacks.stream().filter(s -> VERTX_STACK_ID.equals(s.getId())).findFirst().get();
        assertNotNull(VERTX_STACK_ID + " not Found", stack);
        LOG.info("vertx stack is found: {}", stack.getId());
        assertEquals(VERTX_STACK_ID, stack.getId());
    }

    @Test
    public void getVertxStack() throws StackNotFoundException {
        Stack vertx = client.getStack(VERTX_STACK_ID, osioUserToken);
        LOG.info("'vert.x' stack is found: {}", vertx);
    }

    @Test
    public void getJavaCentosStack() throws StackNotFoundException {
        Stack javaCentos = client.getStack(JAVA_CENTOS_STACK_ID, osioUserToken);
        LOG.info("'java-centos' stack is found: {}", javaCentos);
    }

    @Test
    public void getSpringBootStack() throws StackNotFoundException {
        Stack springBoot = client.getStack(SPRING_BOOT_STACK_ID, osioUserToken);
        LOG.info("'spring-boot' stack is found: {}", springBoot);
    }

    @Test
    public void getWildflySwarmStack() throws StackNotFoundException {
        Stack wildflySwarm = client.getStack(WILDFLY_SWARM_STACK_ID, osioUserToken);
        LOG.info("'wildfly-swarm' stack is found: {}", wildflySwarm);
    }

    @Test
    public void getNodeJsStack() throws StackNotFoundException {
        Stack nodeJs = client.getStack(NODE_JS_STACK_ID, osioUserToken);
        LOG.info("'nodejs4' stack is found: {}", nodeJs);
    }

    @Test
    public void getProjectTypeForVertxStack() {
        String projectType = client.getProjectTypeByStackId(VERTX_STACK_ID);
        assertEquals("maven", projectType);
    }

    @Test
    public void getProjectTypeForNonExistingStack() {
        String projectType = client.getProjectTypeByStackId(NON_EXISTING_STACK_ID);
        assertEquals(StackProjectMapping.BLANK_PROJECT_TYPE, projectType);
    }

    @Test(expected = StackNotFoundException.class)
    public void getNonExistingStackImage() throws StackNotFoundException {
        client.getStack(NON_EXISTING_STACK_ID, osioUserToken);
    }

    @Test
    public void defaultEnvironmentHasAgentsOrInstallers() throws StackNotFoundException {
        Stack vertxStack = client.getStack(VERTX_STACK_ID, osioUserToken);
        WorkspaceEnvironment env = vertxStack.getWorkspaceConfig().getEnvironments().get("default");
        env.getMachines().forEach((key, value) -> {
            assertTrue(value.getAgents() != null || value.getInstallers() != null);
        });
    }
}
