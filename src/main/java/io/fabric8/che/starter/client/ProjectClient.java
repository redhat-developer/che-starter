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
import io.fabric8.che.starter.model.DevMachineServer;
import io.fabric8.che.starter.model.Project;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceStatus;
import io.fabric8.che.starter.template.ProjectTemplate;

@Component
public class ProjectClient {
    private static final Logger LOG = LogManager.getLogger(ProjectClient.class);

    @Value("${che.workspace.start.timeout}")
    private long workspaceStartTimeout;

    @Autowired
    private ProjectTemplate projectTemplate;

    @Autowired
    WorkspaceClient workspaceClient;

    @Async
    public void createProject(String cheServerURL, String workspaceId, String name, String repo, String branch)
            throws IOException, ProjectCreationException {

        // Before we can create a project, we must start the new workspace
        workspaceClient.startWorkspace(cheServerURL, workspaceId);

        // Poll until the workspace is started
        WorkspaceStatus status = workspaceClient.getWorkspaceStatus(cheServerURL, workspaceId);
        long currentTime = System.currentTimeMillis();
        while (!WorkspaceClient.WORKSPACE_STATUS_RUNNING.equals(status.getWorkspaceStatus())
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

        String jsonTemplate = projectTemplate.createRequest().setName(name).setRepo(repo).setBranch(branch).getJSON();

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(jsonTemplate, headers);

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

}
