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
import java.util.Collections;
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

import io.fabric8.che.starter.client.keycloak.KeycloakInterceptor;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceConfig;
import io.fabric8.che.starter.model.workspace.WorkspaceStatus;
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
    private WorkspaceHelper workspaceHelper;

    @Autowired
    private StackClient stackClient;

    public List<Workspace> listWorkspaces(String cheServerUrl) {
        String url = CheRestEndpoints.LIST_WORKSPACES.generateUrl(cheServerUrl);
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<Workspace>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Workspace>>() {
                });

        return response.getBody();
    }

    public List<Workspace> listWorkspacesPerRepository(String cheServerUrl, String repository) {
        List<Workspace> workspaces = listWorkspaces(cheServerUrl);
        return workspaceHelper.filterByRepository(workspaces, repository);
    }

    /**
     * Create workspace on the Che server with given URL.
     * 
     * @param cheServerUrl
     * @param keycloakToken
     * @param name
     * @param stackId
     * @param repo
     * @param branch
     * @return
     * @throws StackNotFoundException
     * @throws IOException
     */
    public Workspace createWorkspace(String cheServerUrl, String keycloakToken, String name, String stackId, String repo, String branch) throws StackNotFoundException, IOException {
        // The first step is to create the workspace
        String url = CheRestEndpoints.CREATE_WORKSPACE.generateUrl(cheServerUrl);

        name = StringUtils.isBlank(name) ? workspaceHelper.generateName() : name;

        WorkspaceConfig wsConfig = stackClient.getStack(cheServerUrl, stackId, null).getWorkspaceConfig();
        wsConfig.setName(name);
        wsConfig.setDescription(repo + "#" + branch);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (keycloakToken != null) {
            template.setInterceptors(Collections.singletonList(new KeycloakInterceptor(keycloakToken)));
        }

        HttpEntity<WorkspaceConfig> entity = new HttpEntity<WorkspaceConfig>(wsConfig, headers);

        ResponseEntity<Workspace> workspaceResponse = template.exchange(url, HttpMethod.POST, entity, Workspace.class);
        Workspace workspace = workspaceResponse.getBody();

        LOG.info("Workspace has been created: {}", workspace);

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
