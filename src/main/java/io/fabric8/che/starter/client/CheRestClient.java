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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.model.Stack;
import io.fabric8.che.starter.model.Workspace;
import io.fabric8.che.starter.model.WorkspaceLink;
import io.fabric8.che.starter.model.WorkspaceTemplate;
import io.fabric8.che.starter.model.response.WorkspaceInfo;

@Service
public class CheRestClient {
    private static final Logger LOG = LogManager.getLogger(CheRestClient.class);

    public static final String WORKSPACE_LINK_IDE_URL = "ide url";
    public static final String WORKSPACE_LINK_START_WORKSPACE = "start workspace";
    
    @Autowired
    WorkspaceTemplate workspaceTemplate;

    public List<WorkspaceInfo> listWorkspaces(String cheServerURL) {
        String url = generateURL(cheServerURL, CheRestEndpoints.LIST_WORKSPACES);
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<WorkspaceInfo>> response =
                template.exchange(url,
                                  HttpMethod.GET,
                                  null,
                                  new ParameterizedTypeReference<List<WorkspaceInfo>>() {});
        return response.getBody();
    }

    public WorkspaceInfo createWorkspace(String cheServerURL, String name, String repo, String branch)
            throws IOException {
        String url = generateURL(cheServerURL, CheRestEndpoints.CREATE_WORKSPACE);
        String jsonTemplate = workspaceTemplate.createRequest()
                                                .setName(name)
                                                .setRepo(repo)
                                                .setBranch(branch)
                                                .getJSON();

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(jsonTemplate, headers);

        ResponseEntity<Workspace> workspace = template.exchange(url, HttpMethod.POST, entity, Workspace.class);
        LOG.info("Workspace has been created: {}", workspace);

        WorkspaceInfo response = new WorkspaceInfo();
        response.setId(workspace.getBody().getId());
        response.setName(workspace.getBody().getConfig().getName());

        for (WorkspaceLink link : workspace.getBody().getLinks()) {
            if (WORKSPACE_LINK_IDE_URL.equals(link.getRel())) {
                response.setWorkspaceIdeUrl(link.getHref());
                break;
            }
        }

        return response;
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
        ResponseEntity<List<Stack>> response =
                template.exchange(url,
                                  HttpMethod.GET,
                                  null,
                                  new ParameterizedTypeReference<List<Stack>>() {});
        return response.getBody();
    }

    private String generateURL(String cheServerURL, CheRestEndpoints endpoint) {
        return cheServerURL + endpoint.toString();
    }

    private String generateURL(String cheServerURL, CheRestEndpoints endpoint, String id) {
        return cheServerURL + endpoint.toString().replace("{id}", id);
    }

}
