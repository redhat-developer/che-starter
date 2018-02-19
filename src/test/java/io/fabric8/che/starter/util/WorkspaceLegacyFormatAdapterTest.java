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

import com.google.gson.Gson;
import io.fabric8.che.starter.model.project.Attribute;
import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.project.Source;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceCommand;
import io.fabric8.che.starter.model.workspace.WorkspaceCommandAttributes;
import io.fabric8.che.starter.model.workspace.WorkspaceConfig;
import io.fabric8.che.starter.model.workspace.WorkspaceEnvironment;
import io.fabric8.che.starter.model.workspace.WorkspaceLink;
import io.fabric8.che.starter.model.workspace.WorkspaceMachine;
import io.fabric8.che.starter.model.workspace.WorkspaceMachineAttribute;
import io.fabric8.che.starter.model.workspace.WorkspaceRecipe;
import io.fabric8.che.starter.model.workspace.WorkspaceV6;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Repeat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceLegacyFormatAdapterTest {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceLegacyFormatAdapterTest.class);
    private static Workspace v5CorrectFull;

    @Before
    public void setUp() {
        v5CorrectFull = new Workspace();
        WorkspaceConfig v5CorrectFullConfig = new WorkspaceConfig();
        List<WorkspaceLink> v5CorrectFullLinks = new ArrayList<>();

        /*========================*
         * WORKSPACE ENVIRONMENTS *
         *========================*/
        List<String> v5CorrectFullEnvironmentDefaultMachineArguments = new ArrayList<>();
        v5CorrectFullEnvironmentDefaultMachineArguments.add("com.redhat.bayesian.lsp");
        v5CorrectFullEnvironmentDefaultMachineArguments.add("org.eclipse.che.ws-agent");
        v5CorrectFullEnvironmentDefaultMachineArguments.add("org.eclipse.che.terminal");
        v5CorrectFullEnvironmentDefaultMachineArguments.add("org.eclipse.che.exec");

        WorkspaceMachineAttribute v5CorrectFullMachineAttribute = new WorkspaceMachineAttribute();
        v5CorrectFullMachineAttribute.setMemoryLimitBytes("2147483648");

        WorkspaceMachine v5CorrectFullEnvironmentDefaultMachine = new WorkspaceMachine();
        v5CorrectFullEnvironmentDefaultMachine.setAgents(v5CorrectFullEnvironmentDefaultMachineArguments);
        v5CorrectFullEnvironmentDefaultMachine.setAttributes(v5CorrectFullMachineAttribute);

        Map<String, WorkspaceMachine> v5CorrectFullEnvironmentDefaultMachines = new HashMap<>();
        v5CorrectFullEnvironmentDefaultMachines.put("dev-machine", v5CorrectFullEnvironmentDefaultMachine);

        WorkspaceRecipe v5CorrectFullEnvironmentDefaultRecipe = new WorkspaceRecipe();
        v5CorrectFullEnvironmentDefaultRecipe.setType("dockerimage");
        v5CorrectFullEnvironmentDefaultRecipe.setLocation("registry.devshift.net/che/vertx");

        WorkspaceEnvironment v5CorrectFullConfigEnvironmentDefault = new WorkspaceEnvironment();
        v5CorrectFullConfigEnvironmentDefault.setMachines(v5CorrectFullEnvironmentDefaultMachines);
        v5CorrectFullConfigEnvironmentDefault.setRecipe(v5CorrectFullEnvironmentDefaultRecipe);

        Map<String, WorkspaceEnvironment> v5CorrectFullConfigEnvironments = new HashMap<>();
        v5CorrectFullConfigEnvironments.put("default", v5CorrectFullConfigEnvironmentDefault);

        /*====================*
         * WORKSPACE PROJECTS *
         *====================*/
        Map<String, String> v5CorrectFullConfigProjectSourceParameters = new HashMap<>();
        v5CorrectFullConfigProjectSourceParameters.put("branch", "master");
        v5CorrectFullConfigProjectSourceParameters.put("keepVcs", "true");

        Source v5CorrectFullConfigProjectSource = new Source();
        v5CorrectFullConfigProjectSource.setLocation("https://github.com/openshiftio-vertx-boosters/vertx-http-booster");
        v5CorrectFullConfigProjectSource.setType("git");
        v5CorrectFullConfigProjectSource.setParameters(v5CorrectFullConfigProjectSourceParameters);

        Project v5CorrectFullConfigProject = new Project();
        v5CorrectFullConfigProject.setLinks(new ArrayList<>());
        v5CorrectFullConfigProject.setName("vertx-http-booster");
        v5CorrectFullConfigProject.setAttributes(new Attribute());
        v5CorrectFullConfigProject.setType("maven");
        v5CorrectFullConfigProject.setSource(v5CorrectFullConfigProjectSource);
        v5CorrectFullConfigProject.setPath("/vertx-http-booster");
        v5CorrectFullConfigProject.setDescription("Created via che-starter API");
        v5CorrectFullConfigProject.setMixins(Collections.arrayToList(new String[]{"git"}));

        List<Project> v5CorrectFullConfigProjects = new ArrayList<>();
        v5CorrectFullConfigProjects.add(v5CorrectFullConfigProject);

        /*====================*
         * WORKSPACE COMMANDS *
         *====================*/
        WorkspaceCommand debug = new WorkspaceCommand();
        WorkspaceCommand run = new WorkspaceCommand();
        WorkspaceCommand build = new WorkspaceCommand();

        WorkspaceCommandAttributes debugAttributes = new WorkspaceCommandAttributes();
        WorkspaceCommandAttributes runAttributes = new WorkspaceCommandAttributes();
        WorkspaceCommandAttributes buildAttributes = new WorkspaceCommandAttributes();

        debugAttributes.setGoal("Build");
        runAttributes.setPreviewUrl("http://${server.port.8080}");
        runAttributes.setGoal("Run");
        buildAttributes.setPreviewUrl("http://${server.port.8080}");
        buildAttributes.setGoal("Debug");

        debug.setCommandLine("scl enable rh-maven33 \u0027mvn compile vertx:debug -f ${current.project.path}\u0027");
        debug.setName("debug");
        debug.setAttributes(debugAttributes);
        debug.setType("custom");
        run.setCommandLine("scl enable rh-maven33 \u0027mvn compile vertx:run -f ${current.project.path}\u0027");
        run.setName("run");
        run.setAttributes(runAttributes);
        run.setType("custom");
        build.setCommandLine("scl enable rh-maven33 \u0027mvn clean install -f ${current.project.path}\u0027");
        build.setName("build");
        build.setAttributes(buildAttributes);
        build.setType("mvn");

        List<WorkspaceCommand> v5CorrectFullConfigCommands = new ArrayList<>();
        v5CorrectFullConfigCommands.add(debug);
        v5CorrectFullConfigCommands.add(run);
        v5CorrectFullConfigCommands.add(build);

        v5CorrectFullConfig.setDefaultEnv("default");
        v5CorrectFullConfig.setEnvironments(v5CorrectFullConfigEnvironments);
        v5CorrectFullConfig.setProjects(v5CorrectFullConfigProjects);
        v5CorrectFullConfig.setName("tfiaq");
        v5CorrectFullConfig.setDescription("mycustomdescription");
        v5CorrectFullConfig.setCommands(v5CorrectFullConfigCommands);
        v5CorrectFullConfig.setLinks(new ArrayList<>());

        /*=================*
         * WORKSPACE LINKS *
         *=================*/
        WorkspaceLink selfLink = new WorkspaceLink();
        WorkspaceLink startWorkspace = new WorkspaceLink();
        WorkspaceLink removeWorkspace = new WorkspaceLink();
        WorkspaceLink getAllWorkspaces = new WorkspaceLink();
        WorkspaceLink getWorkspaceSnapshot = new WorkspaceLink();
        WorkspaceLink ideUrl = new WorkspaceLink();
        WorkspaceLink getWorkspaceEventsChannel = new WorkspaceLink();
        WorkspaceLink getWorkspaceOutputChannel = new WorkspaceLink();
        WorkspaceLink getWorkspaceStatusChannel = new WorkspaceLink();

        selfLink.setHref("https://che.openshift.io/wsmaster/api/workspace/workspace3ca9wjqq5qm66skq");
        selfLink.setRel("self link");
        selfLink.setMethod("GET");
        startWorkspace.setHref("https://che.openshift.io/wsmaster/api/workspace/workspace3ca9wjqq5qm66skq/runtime");
        startWorkspace.setRel("start workspace");
        startWorkspace.setMethod("POST");
        removeWorkspace.setHref("https://che.openshift.io/wsmaster/api/workspace/workspace3ca9wjqq5qm66skq");
        removeWorkspace.setRel("remove workspace");
        removeWorkspace.setMethod("DELETE");
        getAllWorkspaces.setHref("https://che.openshift.io/wsmaster/api/workspace");
        getAllWorkspaces.setRel("get all user workspaces");
        getAllWorkspaces.setMethod("GET");
        getWorkspaceSnapshot.setHref("https://che.openshift.io/wsmaster/api/workspace/workspace3ca9wjqq5qm66skq/snapshot");
        getWorkspaceSnapshot.setRel("get workspace snapshot");
        getWorkspaceSnapshot.setMethod("GET");
        ideUrl.setHref("https://che.openshift.io/tdancs/tfiaq");
        ideUrl.setRel("ide url");
        ideUrl.setMethod("GET");
        getWorkspaceEventsChannel.setHref("wss://che.openshift.io/wsmaster/api/ws");
        getWorkspaceEventsChannel.setRel("get workspace events channel");
        getWorkspaceEventsChannel.setMethod("GET");
        getWorkspaceOutputChannel.setHref("wss://che.openshift.io/wsmaster/api/ws");
        getWorkspaceOutputChannel.setRel("environment.output_channel");
        getWorkspaceOutputChannel.setMethod("GET");
        getWorkspaceStatusChannel.setHref("wss://che.openshift.io/wsmaster/api/ws");
        getWorkspaceStatusChannel.setRel("environment.status_channel");
        getWorkspaceStatusChannel.setMethod("GET");

        v5CorrectFullLinks.add(selfLink);
        v5CorrectFullLinks.add(startWorkspace);
        v5CorrectFullLinks.add(removeWorkspace);
        v5CorrectFullLinks.add(getAllWorkspaces);
        v5CorrectFullLinks.add(getWorkspaceSnapshot);
        v5CorrectFullLinks.add(ideUrl);
        v5CorrectFullLinks.add(getWorkspaceEventsChannel);
        v5CorrectFullLinks.add(getWorkspaceOutputChannel);
        v5CorrectFullLinks.add(getWorkspaceStatusChannel);

        v5CorrectFull.setId("workspace3ca9wjqq5qm66skq");
        v5CorrectFull.setStatus("STOPPED");
        v5CorrectFull.setConfig(v5CorrectFullConfig);
        v5CorrectFull.setLinks(v5CorrectFullLinks);
    }
    
    @Test
    public void getWorkspaceLegacyFormat() throws IOException {
        Gson gson = new Gson();
        WorkspaceComparator comparator = new WorkspaceComparator();
        String workspaceV5Json, workspaceV6Json;

        InputStream workspaceV5JsonStream = getClass().getClassLoader().getResourceAsStream("workspacev5.json");
        Assert.assertNotNull("ClassLoader failed to load test resource: workspacev5.json", workspaceV5JsonStream);
        InputStream workspaceV6JsonStream = getClass().getClassLoader().getResourceAsStream("workspacev6.json");
        Assert.assertNotNull("ClassLoader failed to load test resource: workspacev6.json", workspaceV6JsonStream);
        workspaceV5Json = IOUtils.toString(workspaceV5JsonStream, StandardCharsets.UTF_8);
        Assert.assertNotNull("IOUtils failed to load resource, WorkspaceV5 JSON String not present", workspaceV5Json);
        workspaceV6Json = IOUtils.toString(workspaceV6JsonStream, StandardCharsets.UTF_8);
        Assert.assertNotNull("IOUtils failed to load resource, WorkspaceV6 JSON String not present", workspaceV6Json);

        Workspace workspaceV5JsonDeserialized = gson.fromJson(workspaceV5Json, Workspace.class);
        Assert.assertNotNull("Gson failed to deserialize workspaceV5, object is null",workspaceV5JsonDeserialized);
        WorkspaceV6 workspaceV6JsonDeserialized = gson.fromJson(workspaceV6Json, WorkspaceV6.class);
        Assert.assertNotNull("Gson failed to deserialize workspaceV6, object is null",workspaceV6JsonDeserialized);
        Workspace workspaceV6JsonConverted = WorkspaceLegacyFormatAdapter.getWorkspaceLegacyFormat(workspaceV6JsonDeserialized);
        Assert.assertNotNull("WorkspaceV6 failed to convert to legacy", workspaceV6JsonConverted);

        int workspacesEqual = comparator.compare(workspaceV5JsonDeserialized,workspaceV6JsonConverted);
        Assert.assertTrue(workspacesEqual == 0);
    }

}
