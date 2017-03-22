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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.model.WorkspaceLink;
import io.fabric8.che.starter.model.WorkspaceStatus;
import io.fabric8.che.starter.template.WorkspaceTemplate;
import io.fabric8.che.starter.util.WorkspaceHelper;

@Component
public class WorkspaceClient {
    private static final Logger LOG = LogManager.getLogger(WorkspaceClient.class);

    public static final String WORKSPACE_LINK_IDE_URL = "ide url";
    public static final String WORKSPACE_LINK_START_WORKSPACE = "start workspace";
    public static final String WORKSPACE_STATUS_RUNNING = "RUNNING";
    public static final String WORKSPACE_STATUS_STARTING = "STARTING";
    public static final String WORKSPACE_STATUS_STOPPED = "STOPPED";

    @Autowired
    private WorkspaceTemplate workspaceTemplate;

    @Autowired
    private WorkspaceHelper workspaceHelper;

    @Autowired
    private StackClient stackClient;

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

    public Workspace createWorkspace(String cheServerUrl, String name, String stackId, String repo, String branch) throws StackNotFoundException, IOException {
        // The first step is to create the workspace
        String url = CheRestEndpoints.CREATE_WORKSPACE.generateUrl(cheServerUrl);

        name = StringUtils.isBlank(name) ? workspaceHelper.generateName() : name;

        String stackImage = stackClient.getStackImage(cheServerUrl, stackId, null);

        String jsonTemplate = workspaceTemplate.createRequest().
                                                setName(name).
                                                setStackImage(stackImage).
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

    public Workspace getWorkspace(String cheServerUrl, String workspaceId) {
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
        // Before starting a workspace, we must first stop all other running workspaces
        List<Workspace> workspaces = listWorkspaces(cheServerUrl);

        boolean alreadyStarted = false;

        for (Workspace workspace : workspaces) {
            if (workspace.getId().equals(workspaceId)) {
                if (WORKSPACE_STATUS_RUNNING.equals(workspace.getStatus()) ||
                    WORKSPACE_STATUS_STARTING.equals(workspace.getStatus())) {
                    alreadyStarted = true;
                }
            } else if (!WORKSPACE_STATUS_STOPPED.equals(workspace.getStatus())) {
                stopWorkspace(cheServerUrl, workspace.getId());
            }
        }

        if (!alreadyStarted) {
            String url = CheRestEndpoints.START_WORKSPACE.generateUrl(cheServerUrl, workspaceId);
            RestTemplate template = new RestTemplate();
            template.postForLocation(url, null);
        }
    }

    public WorkspaceStatus getWorkspaceStatus(String cheServerUrl, String workspaceId) {
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
