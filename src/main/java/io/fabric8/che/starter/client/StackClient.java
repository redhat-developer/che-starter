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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.stack.Stack;
import io.fabric8.che.starter.model.stack.StackProjectMapping;
import io.fabric8.che.starter.util.CheServerUrlProvider;

@Component
public class StackClient {

    @Autowired
    CheServerUrlProvider cheServerUrlProvider;

    public List<Stack> listStacks(String keycloakToken) {
        String url = CheRestEndpoints.LIST_STACKS.generateUrl(cheServerUrlProvider.getUrl(keycloakToken));

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);

        ResponseEntity<List<Stack>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Stack>>() {
                });
        return response.getBody();
    }

    /**
     * Gets stack for specified stack ID. Throws StackNotFoundException if there is
     * no such stack on the Che server.
     * 
     * @param stackId       stack ID
     * @param keycloakToken Keycloak token
     * @return image name for stack
     * @throws StackNotFoundException if no image name exists for such stack ID or
     *                                call to Che server was not successful
     */
    public Stack getStack(String stackId, String keycloakToken) throws StackNotFoundException {
        List<Stack> stacks = listStacks(keycloakToken);
        if (stacks != null && !stacks.isEmpty()) {
            return stacks.stream().filter(s -> stackId.equals(s.getId())).findFirst()
                    .orElseThrow(() -> new StackNotFoundException("Stack with id '" + stackId + "' was not found"));
        }
        throw new StackNotFoundException("No stacks were returned from Che server");
    }

    public String getProjectTypeByStackId(final String stackId) {
        return StackProjectMapping.get().getOrDefault(stackId, StackProjectMapping.BLANK_PROJECT_TYPE);
    }

}
