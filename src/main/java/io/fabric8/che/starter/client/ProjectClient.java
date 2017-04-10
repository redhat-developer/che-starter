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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.exception.ProjectCreationException;
import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.project.Source;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceState;
import io.fabric8.che.starter.model.workspace.WorkspaceStatus;

@Component
public class ProjectClient {
    private static final Logger LOG = LogManager.getLogger(ProjectClient.class);

    @Value("${che.workspace.start.timeout}")
    private long workspaceStartTimeout;

    @Autowired
    private WorkspaceClient workspaceClient;

    @Autowired
    private StackClient stackClient;

    @Async
    public void createProject(String cheServerURL, String workspaceId, String name, String repo, String branch, String stack)
            throws IOException, ProjectCreationException {

        // Before we can create a project, we must start the new workspace
        workspaceClient.startWorkspace(cheServerURL, workspaceId);

        // Poll until the workspace is started
        WorkspaceStatus status = workspaceClient.getWorkspaceStatus(cheServerURL, workspaceId);
        long currentTime = System.currentTimeMillis();
        while (!WorkspaceState.RUNNING.toString().equals(status.getWorkspaceStatus())
                && System.currentTimeMillis() < (currentTime + workspaceStartTimeout)) {
            try {
                Thread.sleep(1000);
                LOG.info("Polling workspace '{}' status...", workspaceId);
            } catch (InterruptedException e) {
                LOG.error("Error while polling for workspace status", e);
                break;
            }
            status = workspaceClient.getWorkspaceStatus(cheServerURL, workspaceId);
        }
        LOG.info("Workspace '{}' is running", workspaceId);

        Workspace workspace = workspaceClient.getWorkspace(cheServerURL, workspaceId);

        String wsAgentUrl = getWsAgentUrl(workspace);

        // Next we create a new project against workspace agent API Url
        String url = CheRestEndpoints.CREATE_PROJECT.generateUrl(wsAgentUrl);
        LOG.info("Creating project against workspace agent URL: {}", url);

        String projectType = stackClient.getProjectTypeByStackId(stack);
        LOG.info("Project type: {}", projectType);
  
        Project project = initProject(name, repo, branch, projectType);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Project[]> entity = new HttpEntity<Project[]>(new Project[] {project}, headers);

        ResponseEntity<Project[]> response = template.exchange(url, HttpMethod.POST, entity, Project[].class);

        if (response.getBody().length > 0) {
            Project p = response.getBody()[0];
            LOG.info("Successfully created project {}", p.getName());
        } else {
            LOG.info("Error occurred while creating project {}", name);
            throw new ProjectCreationException("Error occurred while creating project " + name);
        }
    }

    private String getWsAgentUrl (final Workspace workspace) {
        return workspace.getRuntime().getDevMachine().getRuntime().getServers().get("4401/tcp").getUrl();
    }

    private Project initProject(String name, String repo, String branch, String projectType) {
        Project project = new Project();
        project.setName(name);
        Source source = new Source();
        Map<String,String> sourceParams = source.getParameters();
        sourceParams.put("branch", branch);
        sourceParams.put("keepVcs", "true");
        source.setType("git");
        source.setLocation(repo);
        project.setSource(source);
        project.setProjectType(projectType);
        project.setType(projectType);
        project.setDescription("Created via che-starter API");
        project.setPath("/" + name);
        List<String> mixins = new ArrayList<>();
        mixins.add("git");
        project.setMixins(mixins);
        return project;
    }
}
