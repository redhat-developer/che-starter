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
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.model.DevMachineServer;
import io.fabric8.che.starter.model.Project;
import io.fabric8.che.starter.model.ProjectTemplate;
import io.fabric8.che.starter.model.Stack;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.model.WorkspaceLink;
import io.fabric8.che.starter.model.WorkspaceStatus;
import io.fabric8.che.starter.model.WorkspaceTemplate;
import io.fabric8.che.starter.model.response.WorkspaceInfo;

@Service
public class CheRestClient {
    private static final Logger LOG = LogManager.getLogger(CheRestClient.class);

    public static final String WORKSPACE_LINK_IDE_URL = "ide url";
    public static final String WORKSPACE_LINK_START_WORKSPACE = "start workspace";
    public static final String WORKSPACE_STATUS_RUNNING = "RUNNING";

    public static final long WORKSPACE_START_TIMEOUT_MS = 60000;

    @Autowired
    WorkspaceTemplate workspaceTemplate;

    @Autowired
    ProjectTemplate projectTemplate;

    public List<WorkspaceInfo> listWorkspaces(String cheServerURL) {
        String url = generateURL(cheServerURL, CheRestEndpoints.LIST_WORKSPACES);
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<WorkspaceInfo>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<WorkspaceInfo>>() {
                });
        return response.getBody();
    }

    public List<WorkspaceInfo> listWorkspacesPerRespository(String cheServerURL, String repository) {
        List<WorkspaceInfo> workspaces = listWorkspaces(cheServerURL);
        return workspaces.stream().filter(w -> w.getDescription().startsWith(repository)).collect(Collectors.toList());
    }

    public WorkspaceInfo createWorkspace(String cheServerURL, String name, String stack, String repo, String branch)
            throws IOException {
        // The first step is to create the workspace
        String url = generateURL(cheServerURL, CheRestEndpoints.CREATE_WORKSPACE);
        String jsonTemplate = workspaceTemplate.createRequest().
                                                setName(name).
                                                setStack(stack).
                                                setDescription(repo + "#" + branch).
                                                getJSON();

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(jsonTemplate, headers);

        ResponseEntity<Workspace> workspaceResponse = template.exchange(url, HttpMethod.POST, entity, Workspace.class);
        Workspace workspace = workspaceResponse.getBody();

        LOG.info("Workspace has been created: {}", workspace);

        WorkspaceInfo workspaceInfo = new WorkspaceInfo();
        workspaceInfo.setId(workspace.getId());
        workspaceInfo.setName(workspace.getConfig().getName());

        for (WorkspaceLink link : workspace.getLinks()) {
            if (WORKSPACE_LINK_IDE_URL.equals(link.getRel())) {
                workspaceInfo.setWorkspaceIdeUrl(link.getHref());
                break;
            }
        }

        return workspaceInfo;
    }

    @Async
    public void createProject(String cheServerURL, String workspaceId, String name, String repo, String branch)
            throws IOException {

        // Before we can create a project, we must start the new workspace
        startWorkspace(cheServerURL, workspaceId);

        // Poll until the workspace is started
        WorkspaceStatus status;

        status = checkWorkspace(cheServerURL, workspaceId);
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
        String url = generateURL(server.getUrl(), CheRestEndpoints.CREATE_PROJECT, workspaceId);
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

    public Workspace getWorkspaceByKey(String cheServerURL, String workspaceId) {
        String url = generateURL(cheServerURL, CheRestEndpoints.GET_WORKSPACE_BY_KEY, workspaceId);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        return template.exchange(url, HttpMethod.GET, entity, Workspace.class).getBody();
    }

    public void deleteWorkspace(String cheServerURL, String workspaceId) {
        String url = generateURL(cheServerURL, CheRestEndpoints.DELETE_WORKSPACE, workspaceId);
        RestTemplate template = new RestTemplate();
        template.delete(url);
    }

    public void startWorkspace(String cheServerURL, String workspaceId) {
        String url = generateURL(cheServerURL, CheRestEndpoints.START_WORKSPACE, workspaceId);
        RestTemplate template = new RestTemplate();
        template.postForLocation(url, null);
    }

    public WorkspaceStatus checkWorkspace(String cheServerURL, String workspaceId) {
        String url = generateURL(cheServerURL, CheRestEndpoints.CHECK_WORKSPACE, workspaceId);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<WorkspaceStatus> status = template.exchange(url, HttpMethod.GET, entity, WorkspaceStatus.class);
        return status.getBody();
    }

    public void stopWorkspace(String cheServerURL, String id) {
        String url = generateURL(cheServerURL, CheRestEndpoints.STOP_WORKSPACE, id);
        RestTemplate template = new RestTemplate();
        template.delete(url);
    }

    public void stopAllWorkspaces() {
        throw new UnsupportedOperationException("'stopAllWorkspaces' is currently not supported");
    }

    public List<Stack> listStacks(String cheServerURL) {
        String url = generateURL(cheServerURL, CheRestEndpoints.LIST_STACKS);
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<Stack>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Stack>>() {
                });
        return response.getBody();
    }

    private String generateURL(String cheServerURL, CheRestEndpoints endpoint) {
        return cheServerURL + endpoint.toString();
    }

    private String generateURL(String cheServerURL, CheRestEndpoints endpoint, String id) {
        return cheServerURL + endpoint.toString().replace("{id}", id);
    }

}
