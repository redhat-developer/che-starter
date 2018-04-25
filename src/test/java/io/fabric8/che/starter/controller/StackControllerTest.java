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
package io.fabric8.che.starter.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.fabric8.che.starter.TestBase;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = { "OPENSHIFT_TOKEN_URL=http://localhost:33333/keycloak/token/openshift",
        "GITHUB_TOKEN_URL=http://localhost:33333/keycloak/token/github",
        "CHE_SERVER_URL=http://localhost:33333"})
public class StackControllerTest extends TestBase {

    private static final String STACK_ENDPOINT = "/stack";

    @Autowired
    private MockMvc mockMvc;

    @BeforeClass
    public static void setUp() throws IOException {
        VertxServer.getInstance().startVertxServer();
    }

    @AfterClass
    public static void destroy() throws Exception {
        VertxServer.getInstance().stopVertxServer();
    }

    @Test
    public void testGetStacks() throws Exception {
        mockMvc.perform(get(STACK_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN).param("masterUrl", VERTX_SERVER)
                .param("namespace", NAMESPACE)).andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$[1].id", is("java-default"))).andExpect(jsonPath("$[0].id", is("vert.x")));
    }

    @Test
    public void testGetStacksWithWrongToken() throws Exception {
        mockMvc.perform(get(STACK_ENDPOINT).header("Authorization", "badtoken").param("masterUrl", VERTX_SERVER)
                .param("namespace", NAMESPACE)).andExpect(status().is(400));
    }

    /*
     * Deprecated - namespace is not required anymore for multi-tenant che server
     */
    @Deprecated
    @Test
    public void testGetStacksWithWrongNamespace() throws Exception {
        mockMvc.perform(get(STACK_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN).param("masterUrl", VERTX_SERVER)
                .param("namespace", "noexisting")).andExpect(status().is(200));
    }

    /*
     * Deprecated - masterURL is not required anymore for multi-tenant che server
     */
    @Deprecated
    @Test
    public void testGetStacksWithWrongMasterURL() throws Exception {
        mockMvc.perform(get(STACK_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
                .param("masterUrl", "http://i.do.not.exist").param("namespace", NAMESPACE)).andExpect(status().is(200));
    }
}
