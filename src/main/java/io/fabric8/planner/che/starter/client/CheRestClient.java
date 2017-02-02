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
package io.fabric8.planner.che.starter.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.fabric8.planner.che.starter.model.Workspace;

@Service
public class CheRestClient {

    public List<Workspace> listWorkspaces() {
        RestTemplate template = new RestTemplate();
        ResponseEntity<List<Workspace>> response =
                template.exchange(CheRestEndpoints.LIST_WORKSPACES.toString(),
                                  HttpMethod.GET,
                                  null,
                                  new ParameterizedTypeReference<List<Workspace>>() {});
        return response.getBody();
    }

    public void startWorkspace() {
        throw new UnsupportedOperationException("'startWorkspace' is currently not supported");
    }

    public void stopWorkspace() {
        throw new UnsupportedOperationException("'stopWorkspace' is currently not supported");
    }

    public void stopAllWorkspaces() {
        throw new UnsupportedOperationException("'stopAllWorkspaces' is currently not supported");
    }

}
