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
import io.fabric8.che.starter.model.WorkspaceTemplate;

@Service
public class CheRestClient {
    private static final Logger LOG = LogManager.getLogger(CheRestClient.class);

    @Autowired
    WorkspaceTemplate workspcaceTemplate;

    public List<Workspace> listWorkspaces(String cheServerURL) {
        String url = generateURL(cheServerURL, CheRestEndpoints.LIST_WORKSPACES);
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<Workspace>> response =
                template.exchange(url,
                                  HttpMethod.GET,
                                  null,
                                  new ParameterizedTypeReference<List<Workspace>>() {});
        return response.getBody();
    }

    public String createWorkspace(String cheServerURL) throws IOException {
        String url = generateURL(cheServerURL, CheRestEndpoints.CREATE_WORKSPACE);
        String jsonTemplate = workspcaceTemplate.getJSON();
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(jsonTemplate, headers);
        ResponseEntity<String> response = template
                .exchange(url, HttpMethod.POST, entity, String.class);
        LOG.info("Workspace has been created: {}", response);
        return response.getBody();
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
