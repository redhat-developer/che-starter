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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.exception.WorkspaceNotFound;
import io.fabric8.che.starter.model.project.Project;
import io.fabric8.che.starter.model.workspace.Workspace;
import io.fabric8.che.starter.model.workspace.WorkspaceConfig;
import io.fabric8.che.starter.model.workspace.WorkspaceState;
import io.fabric8.che.starter.model.workspace.WorkspaceStatus;
import io.fabric8.che.starter.model.workspace.WorkspaceV6;
import io.fabric8.che.starter.util.ProjectHelper;
import io.fabric8.che.starter.util.WorkspaceHelper;
import io.fabric8.che.starter.util.WorkspaceLegacyFormatAdapter;

@Component
public class WorkspaceClient {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceClient.class);

    @Value("${MULTI_TENANT_CHE_SERVER_URL:https://che.prod-preview.openshift.io}")
    private String multiTenantCheServerURL;

    @Value("${che.workspace.start.timeout}")
    private long workspaceStartTimeout;

    @Value("${che.workspace.stop.timeout}")
    private long workspaceStopTimeout;

    @Autowired
    private WorkspaceHelper workspaceHelper;

    @Autowired
    private StackClient stackClient;

    @Autowired
    private ProjectHelper projectHelper;

    public void waitUntilWorkspaceIsRunning(Workspace workspace, String keycloakToken) {
        WorkspaceStatus status = getWorkspaceStatus(workspace.getId(), keycloakToken);
        long currentTime = System.currentTimeMillis();
        while (!WorkspaceState.RUNNING.toString().equals(status.getWorkspaceStatus())
                && System.currentTimeMillis() < (currentTime + workspaceStartTimeout)) {
            try {
                Thread.sleep(1000);
                LOG.info("Polling workspace '{}' status...", workspace.getConfig().getName());
            } catch (InterruptedException e) {
                LOG.error("Error while polling for workspace status", e);
                break;
            }
            status = getWorkspaceStatus(workspace.getId(), keycloakToken);
        }
        LOG.info("Workspace '{}' is running", workspace.getConfig().getName());
    }

    /**
     * Blocks execution until the specified workspace has been stopped and its resources
     * made available again.
     * 
     * @param workspace The workspace to stop
     * @param keycloakToken The KeyCloak token
     */
    public void waitUntilWorkspaceIsStopped(Workspace workspace, String keycloakToken) {
        WorkspaceStatus status = getWorkspaceStatus(workspace.getId(), keycloakToken);
        long currentTime = System.currentTimeMillis();

        // Poll the Che server until it returns a status of 'STOPPED' for the workspace
        while (!WorkspaceState.STOPPED.toString().equals(status.getWorkspaceStatus())
                && System.currentTimeMillis() < (currentTime + workspaceStopTimeout)) {
            try {
                Thread.sleep(1000);
                LOG.info("Polling Che server for workspace '{}' status...", workspace.getConfig().getName());
            } catch (InterruptedException e) {
                LOG.error("Error while polling for workspace status", e);
                break;
            }
            status = getWorkspaceStatus(workspace.getId(), keycloakToken);
        }
    }

    public List<Workspace> listWorkspaces(String keycloakToken) {
        String url = CheRestEndpoints.LIST_WORKSPACES.generateUrl(multiTenantCheServerURL);
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        ResponseEntity<List<Workspace>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Workspace>>() {
                });

        return response.getBody();
    }

    public List<Workspace> listWorkspacesPerRepository(String repository, String keycloakToken) {
        List<Workspace> workspaces = listWorkspaces(keycloakToken);
        return workspaceHelper.filterByRepository(workspaces, repository);
    }

    /**
     * Create workspace on the Che server with given URL.
     * 
     * @param keycloakToken
     * @param stackId
     * @param repo
     * @param branch
     * @return
     * @throws StackNotFoundException
     * @throws IOException
     * @throws URISyntaxException 
     */
    public Workspace createWorkspace(String keycloakToken, String stackId, String repo,
            String branch, String description) throws StackNotFoundException, IOException, URISyntaxException {
        String url = CheRestEndpoints.CREATE_WORKSPACE.generateUrl(multiTenantCheServerURL);
        WorkspaceConfig wsConfig = stackClient.getStack(stackId, keycloakToken).getWorkspaceConfig();

        String projectName = projectHelper.getProjectNameFromGitRepository(repo);
        String projectType = stackClient.getProjectTypeByStackId(stackId);

        LOG.info("Stack: {}", stackId);
        LOG.info("Project type: {}", projectType);

        Project project = projectHelper.initProject(projectName, repo, branch, projectType);

        wsConfig.setProjects(Collections.singletonList(project));
        wsConfig.setName(workspaceHelper.generateName(projectName));
        wsConfig.setDescription(description);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Gson gson = new Gson();
        HttpEntity<WorkspaceConfig> entity = new HttpEntity<>(wsConfig, headers);
        String workspaceRequestRaw = gson.toJson(entity);
        ResponseEntity<String> workspaceResponseRaw = template.exchange(url, HttpMethod.POST, entity, String.class);
        Workspace workspace;
        try {
            workspace = gson.fromJson(workspaceResponseRaw.getBody(), Workspace.class);
        } catch (JsonSyntaxException e) {
            LOG.warn("Could not deserialize che server response, possibly v6");
            WorkspaceV6 workspaceV6 = gson.fromJson(workspaceResponseRaw.getBody(), WorkspaceV6.class);
            workspace = WorkspaceLegacyFormatAdapter.getWorkspaceLegacyFormat(workspaceV6);
        }
        LOG.info("Workspace has been created: {}", workspace.getId());

        return workspace;
    }

    public Workspace getWorkspaceById(String workspaceId, String keycloakToken) {
        String url = CheRestEndpoints.GET_WORKSPACE_BY_ID.generateUrl(multiTenantCheServerURL, workspaceId);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        return template.exchange(url, HttpMethod.GET, entity, Workspace.class).getBody();
    }

    public Workspace getWorkspaceByName(String workspaceName, String keycloakToken) throws WorkspaceNotFound {
        List<Workspace> workspaces = listWorkspaces( keycloakToken);
        for (Workspace workspace : workspaces) {
            if (workspace.getConfig().getName().equals(workspaceName)) {
                return getWorkspaceById(workspace.getId(), keycloakToken);
            }
        }
        throw new WorkspaceNotFound("Workspace '" + workspaceName + "' was not found");
    }

    /** Deletes a workspace. Workspace must be stopped before invoking its  deletion.
     * 
     * @param workspaceId workspace ID
     * @param keycloakToken keycloak token
     * @throws WorkspaceNotFound if workspace does not exists
     */
    public void deleteWorkspace(String workspaceId, String keycloakToken) throws WorkspaceNotFound {
        String url = CheRestEndpoints.DELETE_WORKSPACE.generateUrl(multiTenantCheServerURL, workspaceId);
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        template.delete(url);
    }

    public Workspace startWorkspace(String workspaceName, String keycloakToken) throws WorkspaceNotFound {
        List<Workspace> workspaces = listWorkspaces(keycloakToken);

        boolean alreadyStarted = false;
        Workspace workspaceToStart = null;

        for (Workspace workspace : workspaces) {
            if (workspace.getConfig().getName().equals(workspaceName)) {
                workspaceToStart = workspace;
                if (WorkspaceState.RUNNING.toString().equals(workspace.getStatus())
                        || WorkspaceState.STARTING.toString().equals(workspace.getStatus())) {
                    alreadyStarted = true;
                }
            } else if (!WorkspaceState.STOPPED.toString().equals(workspace.getStatus())) {
                stopWorkspace(workspace, keycloakToken);
                waitUntilWorkspaceIsStopped(workspace, keycloakToken);
            }
        }

        if (workspaceToStart == null) {
            throw new WorkspaceNotFound("Workspace '" + workspaceName + "' does not exist.");
        }

        if (!alreadyStarted) {
            String url = CheRestEndpoints.START_WORKSPACE.generateUrl(multiTenantCheServerURL, workspaceToStart.getId());
            RestTemplate template = new KeycloakRestTemplate(keycloakToken);
            template.postForLocation(url, null);
        }
        return workspaceToStart;
    }

    @Async
    public Workspace startWorkspaceAsync(String workspaceName, String keycloakToken) throws WorkspaceNotFound {
        return startWorkspace(workspaceName, keycloakToken);
    }

    public Workspace getStartedWorkspace(String keycloakToken) {
        List<Workspace> workspaces = listWorkspaces(keycloakToken);

        for (Workspace workspace : workspaces) {
            if (WorkspaceState.RUNNING.toString().equals(workspace.getStatus())
                    || WorkspaceState.STARTING.toString().equals(workspace.getStatus())) {
                return workspace;
            }
        }
        return null;
    }

    public WorkspaceStatus getWorkspaceStatus(String workspaceId, String keycloakToken) {
        String url = CheRestEndpoints.CHECK_WORKSPACE.generateUrl(multiTenantCheServerURL, workspaceId);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<WorkspaceStatus> status = template.exchange(url, HttpMethod.GET, entity, WorkspaceStatus.class);
        return status.getBody();
    }

    public void stopWorkspace(Workspace workspace, String keycloakToken) {
            LOG.info("Stopping workspace {}", workspace.getId());
            String url = CheRestEndpoints.STOP_WORKSPACE.generateUrl(multiTenantCheServerURL, workspace.getId());
            RestTemplate template = new KeycloakRestTemplate(keycloakToken);
            template.delete(url);
    }

}
