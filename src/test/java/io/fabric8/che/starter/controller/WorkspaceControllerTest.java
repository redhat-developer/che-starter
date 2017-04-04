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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import io.fabric8.che.starter.model.request.WorkspaceCreateParams;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = { "OPENSHIFT_TOKEN_URL=http://localhost:33333/keycloak/token/openshift",
		"GITHUB_TOKEN_URL=http://localhost:33333/keycloak/token/github" })
public class WorkspaceControllerTest extends TestBase {
	
	private static final String WORKSPACE_ENDPOINT = "/workspace";
	private static final String WORKSPACE_OSO_ENDPOINT = "/workspace/oso";
	
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
	public void getWorkspacesTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN).param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE)).andExpect(jsonPath("$[0].id", is("chevertxwsid13")))
				.andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$[0].status", is("RUNNING")));
	}

	@Test
	public void getWorkspacesOSOTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.param("masterUrl", VERTX_SERVER).param("namespace", NAMESPACE))
				.andExpect(jsonPath("$[0].id", is("chevertxwsid13"))).andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$[0].status", is("RUNNING")));
	}

	@Test
	public void getWorkspaceWithWrongTokenTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_ENDPOINT).header("Authorization", "wrongkeycloaktoken")
				.param("masterUrl", VERTX_SERVER).param("namespace", NAMESPACE)).andExpect(status().is(401));
	}

	@Test
	public void getWorkspaceOSOWithWrongTokenTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_OSO_ENDPOINT).header("Authorization", "wrongopenshifttoken")
				.param("masterUrl", VERTX_SERVER).param("namespace", NAMESPACE)).andExpect(status().is(401));
	}

	@Test
	public void getWorkspacesWithWrongNamespaceTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN).param("masterUrl", VERTX_SERVER)
				.param("namespace", "noexisting")).andExpect(status().is(401));
	}

	@Test
	public void getWorkspacesOSOWithWrongNamespaceTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.param("masterUrl", VERTX_SERVER).param("namespace", "noexisting")).andExpect(status().is(401));
	}

	@Test
	public void getWorkspacesWithWrongMasterURLTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
				.param("masterUrl", "http://i.do.not.exist").param("namespace", NAMESPACE)).andExpect(status().is(401));
	}

	@Test
	public void getWorkspacesOSOWithWrongMasterURLTest() throws Exception {
		mockMvc.perform(get(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.param("masterUrl", "http://i.do.not.exist").param("namespace", NAMESPACE)).andExpect(status().is(401));
	}

	@Test
	public void createWorkspaceTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$.href", is(WORKSPACE_IDE_URL)));
	}

	@Test
	public void createWorkspaceOSOTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$.href", is(WORKSPACE_IDE_URL)));
	}

	@Test
	public void createWorkspaceWithWrongTokenTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_ENDPOINT).header("Authorization", "wrongkeycloakttoken")
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is(401));
	}

	@Test
	public void createWorkspaceOSOWithWrongTokenTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_OSO_ENDPOINT).header("Authorization", "wrongopenshifttoken")
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is(401));
	}

	@Test
	public void createWorkspaceWithWrongNamespaceTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", "idonotexists").content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is(401));
	}

	@Test
	public void createWorkspaceOSOWithWrongNamespaceTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", "idonotexists").content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is(401));
	}

	@Test
	public void createWorkspaceWithWrongMasterURLTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", "http://i.do.not.exist")
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is(401));
	}

	@Test
	public void createWorkspaceOSOWithWrongMasterURLTest() throws Exception {
		mockMvc.perform(post(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", "http://i.do.not.exist")
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(initWorkspaceCreateParams())))
				.andExpect(status().is(401));
	}
	
	@Test
	public void createWorkspaceWithWrongRepoParamTest() throws Exception {
		WorkspaceCreateParams workspaceParams = initWorkspaceCreateParams();
		workspaceParams.setRepo("incorrecturl");
		workspaceParams.setDescription("incorrecturl#master");
		mockMvc.perform(post(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(workspaceParams)))
				.andExpect(status().is(400));
	}

	@Test
	public void createWorkspaceOSOWithWrongRepoParamTest() throws Exception {
		WorkspaceCreateParams workspaceParams = initWorkspaceCreateParams();
		workspaceParams.setRepo("incorrecturl");
		workspaceParams.setDescription("incorrecturl#master");
		mockMvc.perform(post(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(workspaceParams)))
				.andExpect(status().is(400));
	}
	
	@Test
	public void createWorkspaceWithNonexistingStackParamTest() throws Exception {
		WorkspaceCreateParams workspaceParams = initWorkspaceCreateParams();
		workspaceParams.setStack("nada");
		// Need to also modify description, which is ID of a workspace to avoid getting already existing WS
		workspaceParams.setBranch("custom");
		workspaceParams.setDescription(PROJECT_GIT_REPO + "#custom");
		mockMvc.perform(post(WORKSPACE_ENDPOINT).header("Authorization", KEYCLOAK_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(workspaceParams)))
				.andExpect(status().is(404));
	}

	@Test
	public void createWorkspaceOSOWithNonexistingStackParamTest() throws Exception {
		WorkspaceCreateParams workspaceParams = initWorkspaceCreateParams();
		workspaceParams.setStack("nada");
		// Need to also modify description, which is ID of a workspace to avoid getting already existing WS
		workspaceParams.setBranch("custom");
		workspaceParams.setDescription(PROJECT_GIT_REPO + "#custom");
		mockMvc.perform(post(WORKSPACE_OSO_ENDPOINT).header("Authorization", OPENSHIFT_TOKEN)
				.header("Content-Type", "application/json").param("masterUrl", VERTX_SERVER)
				.param("namespace", NAMESPACE).content(getCreateWorkspaceRequestBody(workspaceParams)))
				.andExpect(status().is(404));
	}
}
