/*-
 * #%L
 * che-starter
 * %%
 * Copyright (C) 2018 Red Hat, Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package io.fabric8.che.starter.oso;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.NamespaceNotFoundException;
import io.fabric8.che.starter.oso.user.services.model.Namespace;
import io.fabric8.che.starter.oso.user.services.model.UserServices;

@Component
public class ClusterCapacityTracker {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterCapacityTracker.class);

    @Value("${OSIO_API_BASE_URL:https://api.prod-preview.openshift.io/api/}")
    private String apiBaseUrl;
    private String userServicesEndpoint;

    @PostConstruct
    public void init() {
        this.userServicesEndpoint = apiBaseUrl + "user/services";
    }

    public boolean isClusterFull(final String osioUserToken) throws NamespaceNotFoundException {
        RestTemplate template = new KeycloakRestTemplate(osioUserToken);
        ResponseEntity<UserServices> response = template.exchange(userServicesEndpoint, HttpMethod.GET, null, UserServices.class);
        UserServices userServices = response.getBody();
        return isClusterCapacityExhaustedInCheNamespace(userServices);
    }

    private boolean isClusterCapacityExhaustedInCheNamespace(UserServices userServices)
            throws NamespaceNotFoundException {
        List<Namespace> namespaces = userServices.getData().getAttributes().getNamespaces();
        Namespace cheNamespace = namespaces.stream().filter(n -> n.getName().endsWith("-che")).findFirst()
                .orElseThrow(() -> new NamespaceNotFoundException());

        String name = cheNamespace.getName();
        String clusterAppDomain = cheNamespace.getClusterAppDomain();
        boolean clusterCapacityExhausted = cheNamespace.isClusterCapacityExhausted();

        LOG.info("'{}' namespace info: domain - {}, isClusterFull - {}", name, clusterAppDomain, clusterCapacityExhausted);
        return !clusterCapacityExhausted;
    }

}
