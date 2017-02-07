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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.model.Workspace;

@Service
public class CheRestClient {
    private static final Logger LOG = LogManager.getLogger(CheRestClient.class);

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

    public void createAndStartWorkspace(String cheServerURL) {
        String url = generateURL(cheServerURL, CheRestEndpoints.CREATE_WORKSPACE);
        RestTemplate template = new RestTemplate();
        Workspace workspace = template.postForObject(url, new Workspace(), Workspace.class);
        LOG.info("New Workspace has been created (id: {}),", workspace.getId());
    }

    public void stopWorkspace(String cheServerURL, String id) {
        String url = generateURL(cheServerURL, CheRestEndpoints.STOP_WORKSPACE, id);
        RestTemplate template = new RestTemplate();
        template.delete(url);
    }

    public void stopAllWorkspaces() {
        throw new UnsupportedOperationException("'stopAllWorkspaces' is currently not supported");
    }

    private String generateURL(String cheServerURL, CheRestEndpoints endpoint) {
        return cheServerURL + endpoint.toString();
    }

    private String generateURL(String cheServerURL, CheRestEndpoints endpoint, String id) {
        return cheServerURL + endpoint.toString().replace("{id}", id);
    }

}
