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
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.che.starter.util.ProjectHelper;
import io.fabric8.che.starter.util.WorkspaceHelper;

@Component
public class WorkspaceClient {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceClient.class);

    @Value("${che.workspace.start.timeout}")
    private long workspaceStartTimeout;

    @Value("${che.workspace.stop.timeout}")
    private long workspaceStopTimeout;

    @Autowired
    private WorkspaceHelper workspaceHelper;

    @Autowired
    private StackClient stackClient;

    @Autowired
    ProjectHelper projectHelper;

    @Autowired
    OpenShiftClientWrapper openshiftClientWrapper;

    @Value("${che.openshift.start.timeout}")
    private String startTimeout;

    @Value("${che.openshift.deploymentconfig}")
    private String deploymentConfigName;

    public void waitUntilWorkspaceIsRunning(String cheServerURL, Workspace workspace, String keycloakToken) {
        WorkspaceStatus status = getWorkspaceStatus(cheServerURL, workspace.getId(), keycloakToken);
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
            status = getWorkspaceStatus(cheServerURL, workspace.getId(), keycloakToken);
        }
        LOG.info("Workspace '{}' is running", workspace.getConfig().getName());
    }

    /**
     * This method blocks execution until the specified workspace has been stopped and its resources
     * made available again.
     * 
     * @param masterUrl The master URL for the OpenShift API
     * @param namespace The OpenShift namespace
     * @param openShiftToken The OpenShift token
     * @param cheServerURL Che server URL
     * @param workspace The workspace to stop
     * @param keycloakToken The KeyCloak token
     */
    public void waitUntilWorkspaceIsStopped(String masterUrl, String namespace, String openShiftToken,
            String cheServerURL, Workspace workspace, String keycloakToken) {
        WorkspaceStatus status = getWorkspaceStatus(cheServerURL, workspace.getId(), keycloakToken);
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
            status = getWorkspaceStatus(cheServerURL, workspace.getId(), keycloakToken);
        }
    }

    public List<Workspace> listWorkspaces(String cheServerUrl, String keycloakToken) {
        String url = CheRestEndpoints.LIST_WORKSPACES.generateUrl(cheServerUrl);
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        ResponseEntity<List<Workspace>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Workspace>>() {
                });

        return response.getBody();
    }

    public List<Workspace> listWorkspacesPerRepository(String cheServerUrl, String repository, String keycloakToken) {
        List<Workspace> workspaces = listWorkspaces(cheServerUrl, keycloakToken);
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
     * @throws URISyntaxException 
     */
    public Workspace createWorkspace(String cheServerURL, String keycloakToken, String stackId, String repo,
            String branch, String description) throws StackNotFoundException, IOException, URISyntaxException {
        String url = CheRestEndpoints.CREATE_WORKSPACE.generateUrl(cheServerURL);
        WorkspaceConfig wsConfig = stackClient.getStack(cheServerURL, stackId, keycloakToken).getWorkspaceConfig();

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

        HttpEntity<WorkspaceConfig> entity = new HttpEntity<WorkspaceConfig>(wsConfig, headers);

        ResponseEntity<Workspace> workspaceResponse = template.exchange(url, HttpMethod.POST, entity, Workspace.class);
        Workspace workspace = workspaceResponse.getBody();

        LOG.info("Workspace has been created: {}", workspace);

        return workspace;
    }

    public Workspace getWorkspaceById(String cheServerURL, String workspaceId, String keycloakToken) {
        String url = CheRestEndpoints.GET_WORKSPACE_BY_ID.generateUrl(cheServerURL, workspaceId);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        return template.exchange(url, HttpMethod.GET, entity, Workspace.class).getBody();
    }

    public Workspace getWorkspaceByName(String cheServerURL, String workspaceName, String keycloakToken) throws WorkspaceNotFound {
        List<Workspace> workspaces = listWorkspaces(cheServerURL, keycloakToken);
        for (Workspace workspace : workspaces) {
            if (workspace.getConfig().getName().equals(workspaceName)) {
                return getWorkspaceById(cheServerURL, workspace.getId(), keycloakToken);
            }
        }
        throw new WorkspaceNotFound("Workspace '" + workspaceName + "' was not found");
    }

    /** Deletes a workspace. Workspace must be stopped before invoking its  deletion.
     * 
     * @param cheServerURL Che server URL
     * @param workspaceId workspace ID
     * @throws WorkspaceNotFound if workspace does not exists
     */
    public void deleteWorkspace(String cheServerURL, String workspaceId, String keycloakToken) throws WorkspaceNotFound {
        String url = CheRestEndpoints.DELETE_WORKSPACE.generateUrl(cheServerURL, workspaceId);
        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        template.delete(url);
    }

    /**
     * Starts and gets a workspace by its name.
     * 
     * @param cheServerURL
     *            Che server URL
     * @param workspaceName
     *            name of workspace to start
     * @return started workspace
     * @throws WorkspaceNotFound
     */
    public Workspace startWorkspace(String cheServerURL, String workspaceName, String masterUrl, String namespace,
            String openShiftToken, String keycloakToken) throws WorkspaceNotFound {
        List<Workspace> workspaces = listWorkspaces(cheServerURL, keycloakToken);

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
                stopWorkspace(cheServerURL, workspace, keycloakToken);
                waitUntilWorkspaceIsStopped(masterUrl, namespace, openShiftToken, cheServerURL, workspace, keycloakToken);
            }
        }

        if (workspaceToStart == null) {
            throw new WorkspaceNotFound("Workspace '" + workspaceName + "' does not exist.");
        }

        if (!alreadyStarted) {
            String url = CheRestEndpoints.START_WORKSPACE.generateUrl(cheServerURL, workspaceToStart.getId());
            RestTemplate template = new KeycloakRestTemplate(keycloakToken);
            template.postForLocation(url, null);
        }
        return workspaceToStart;
    }

    @Async
    public Workspace startWorkspaceAsync(String cheServerURL, String workspaceName, String masterUrl, String namespace,
            String openShiftToken, String keycloakToken) throws WorkspaceNotFound {
        return startWorkspace(cheServerURL, workspaceName, masterUrl, namespace, openShiftToken, keycloakToken);
    }

    /**
     * Gets started/starting workspace.
     * 
     * @param cheServerUrl
     *            url of che server
     * @return started workspace or null if there is no running/starting
     *         workspace
     */
    public Workspace getStartedWorkspace(String cheServerURL, String keycloakToken) {
        List<Workspace> workspaces = listWorkspaces(cheServerURL, keycloakToken);

        for (Workspace workspace : workspaces) {
            if (WorkspaceState.RUNNING.toString().equals(workspace.getStatus())
                    || WorkspaceState.STARTING.toString().equals(workspace.getStatus())) {
                return workspace;
            }
        }
        return null;
    }

    /**
     * Gets workspace status
     * 
     * @param cheServerURL
     *            Che server URL
     * @param workspaceId
     *            workspace ID
     * @return workspace status
     */
    public WorkspaceStatus getWorkspaceStatus(String cheServerURL, String workspaceId, String keycloakToken) {
        String url = CheRestEndpoints.CHECK_WORKSPACE.generateUrl(cheServerURL, workspaceId);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<WorkspaceStatus> status = template.exchange(url, HttpMethod.GET, entity, WorkspaceStatus.class);
        return status.getBody();
    }

    /**
     * Stops a running workspace.
     */
    public void stopWorkspace(String cheServerURL, Workspace workspace, String keycloakToken) {
            LOG.info("Stopping workspace {}", workspace.getId());
            String url = CheRestEndpoints.STOP_WORKSPACE.generateUrl(cheServerURL, workspace.getId());
            RestTemplate template = new KeycloakRestTemplate(keycloakToken);
            template.delete(url);
    }

}
