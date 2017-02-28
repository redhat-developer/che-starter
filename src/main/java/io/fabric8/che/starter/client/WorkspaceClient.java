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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.model.DevMachineServer;
import io.fabric8.che.starter.model.Project;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.model.WorkspaceLink;
import io.fabric8.che.starter.model.WorkspaceStatus;
import io.fabric8.che.starter.template.ProjectTemplate;
import io.fabric8.che.starter.template.WorkspaceTemplate;
import io.fabric8.che.starter.util.WorkspaceHelper;

@Component
public class WorkspaceClient {
    private static final Logger LOG = LogManager.getLogger(WorkspaceClient.class);

    public static final String WORKSPACE_LINK_IDE_URL = "ide url";
    public static final String WORKSPACE_LINK_START_WORKSPACE = "start workspace";
    public static final String WORKSPACE_STATUS_RUNNING = "RUNNING";
    public static final String WORKSPACE_STATUS_STARTING = "STARTING";
    public static final long WORKSPACE_START_TIMEOUT_MS = 60000;

    @Autowired
    private WorkspaceTemplate workspaceTemplate;

    @Autowired
    private WorkspaceHelper workspaceHelper;

    @Autowired
    private ProjectTemplate projectTemplate;

    public List<Workspace> listWorkspaces(String cheServerUrl) {
        String url = CheRestEndpoints.LIST_WORKSPACES.generateUrl(cheServerUrl);
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<Workspace>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Workspace>>() {
                });

        List<Workspace> workspaces = response.getBody();
        for (Workspace workspace : workspaces) {
            workspace.setName(workspace.getConfig().getName());
            workspace.setDescription(workspace.getConfig().getDescription());

            for (WorkspaceLink link : workspace.getLinks()) {
                if (WORKSPACE_LINK_IDE_URL.equals(link.getRel())) {
                    workspace.setWorkspaceIdeUrl(link.getHref());
                    break;
                }
            }
        }
        return workspaces;
    }

    public List<Workspace> listWorkspacesPerRepository(String cheServerUrl, String repository) {
        List<Workspace> workspaces = listWorkspaces(cheServerUrl);
        return workspaceHelper.filterByRepository(workspaces, repository);
    }

    public Workspace createWorkspace(String cheServerUrl, String name, String stack, String repo, String branch)
            throws IOException {
        // The first step is to create the workspace
        String url = CheRestEndpoints.CREATE_WORKSPACE.generateUrl(cheServerUrl);
        String jsonTemplate = workspaceTemplate.createRequest().
                                                setName(name).
                                                setStack(stack).
                                                setDescription(workspaceHelper.getDescription(repo, branch)).
                                                getJSON();

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(jsonTemplate, headers);

        ResponseEntity<Workspace> workspaceResponse = template.exchange(url, HttpMethod.POST, entity, Workspace.class);
        Workspace workspace = workspaceResponse.getBody();

        LOG.info("Workspace has been created: {}", workspace);

        workspace.setName(workspace.getConfig().getName());
        workspace.setDescription(workspace.getConfig().getDescription());

        for (WorkspaceLink link : workspace.getLinks()) {
            if (WORKSPACE_LINK_IDE_URL.equals(link.getRel())) {
                workspace.setWorkspaceIdeUrl(link.getHref());
                break;
            }
        }

        return workspace;
    }

    @Async
    public void createProject(String cheServerURL, String workspaceId, String name, String repo, String branch)
            throws IOException {

        // Before we can create a project, we must start the new workspace
        startWorkspace(cheServerURL, workspaceId);

        // Poll until the workspace is started
        WorkspaceStatus status = checkWorkspace(cheServerURL, workspaceId);
        long currentTime = System.currentTimeMillis();
        while (!WORKSPACE_STATUS_RUNNING.equals(status.getWorkspaceStatus())
                && System.currentTimeMillis() < (currentTime + WORKSPACE_START_TIMEOUT_MS)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("Error while polling for workspace status", e);
                break;
            }
            status = checkWorkspace(cheServerURL, workspaceId);
        }

        Workspace workspace = getWorkspaceByKey(cheServerURL, workspaceId);

        DevMachineServer server = workspace.getRuntime().getDevMachine().getRuntime().getServers().get("4401/tcp");

        // Next we create a new project within the workspace
        String url = CheRestEndpoints.CREATE_PROJECT.generateUrl(server.getUrl(), workspaceId);
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
            LOG.info("There seems to have been a problem creating project {}", name);
        }
    }

    public Workspace getWorkspaceByKey(String cheServerUrl, String workspaceId) {
        String url = CheRestEndpoints.GET_WORKSPACE_BY_ID.generateUrl(cheServerUrl, workspaceId);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        return template.exchange(url, HttpMethod.GET, entity, Workspace.class).getBody();
    }

    public void deleteWorkspace(String cheServerUrl, String workspaceId) {
        String url = CheRestEndpoints.DELETE_WORKSPACE.generateUrl(cheServerUrl, workspaceId);
        RestTemplate template = new RestTemplate();
        template.delete(url);
    }

    public void startWorkspace(String cheServerUrl, String workspaceId) {
        String url = CheRestEndpoints.START_WORKSPACE.generateUrl(cheServerUrl, workspaceId);
        RestTemplate template = new RestTemplate();
        template.postForLocation(url, null);
    }

    public WorkspaceStatus checkWorkspace(String cheServerUrl, String workspaceId) {
        String url = CheRestEndpoints.CHECK_WORKSPACE.generateUrl(cheServerUrl, workspaceId);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<WorkspaceStatus> status = template.exchange(url, HttpMethod.GET, entity, WorkspaceStatus.class);
        return status.getBody();
    }

    public void stopWorkspace(String cheServerUrl, String id) {
        String url = CheRestEndpoints.STOP_WORKSPACE.generateUrl(cheServerUrl, id);
        RestTemplate template = new RestTemplate();
        template.delete(url);
    }

}
