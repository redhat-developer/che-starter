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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakInterceptor;
import io.fabric8.che.starter.exception.StackNotFoundException;
import io.fabric8.che.starter.model.stack.Stack;
import io.fabric8.che.starter.model.stack.StackProjectMapping;

@Component
public class StackClient {

    public List<Stack> listStacks(String cheServerUrl, String keycloakToken) {
        String url = CheRestEndpoints.LIST_STACKS.generateUrl(cheServerUrl);
        RestTemplate template = new RestTemplate();

        if (keycloakToken != null) {
            template.setInterceptors(Collections.singletonList(new KeycloakInterceptor(keycloakToken)));
        }

        ResponseEntity<List<Stack>> response = template.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Stack>>() {
                });
        return response.getBody();
    }

    /**
     * Gets image for specified stack ID. Throws StackNotFoundException if there is no such stack
     * on the Che server.
     * 
     * @param cheServerUrl URL of Che server
     * @param stackId stack ID
     * @param keycloakToken Keycloak token
     * @return image name for stack
     * @throws StackNotFoundException if no image name exists for such stack ID or call to Che server was not successful
     */
    public Stack getStack(String cheServerUrl, String stackId, String keycloakToken) throws StackNotFoundException {
        List<Stack> stacks = listStacks(cheServerUrl, keycloakToken);
        if (stacks != null && !stacks.isEmpty()) {
            try {
                Stack stack = stacks.stream().filter(s -> stackId.equals(s.getId())).findFirst().get();
                return stack;
            } catch (NoSuchElementException e) {
                throw new StackNotFoundException("Stack with id '" + stackId + "' was not found");
            }
        } else {
            throw new StackNotFoundException("No stacks were returned from Che server");
        }
    }

    public String getProjectTypeByStackId(final String stackId) {
        return StackProjectMapping.get().getOrDefault(stackId, StackProjectMapping.BLANK_PROJECT_TYPE);
    }

}
