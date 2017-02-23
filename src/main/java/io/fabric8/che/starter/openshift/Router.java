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
package io.fabric8.che.starter.openshift;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class Router {
    private static final Logger LOG = LogManager.getLogger(Router.class);

    @Value("${che.openshift.project}")
    private String project;
    
    @Value("${che.openshift.router}")
    private String router;

    public String getUrl(OpenShiftClient client) {
        Route route = client.routes().inNamespace(project).withName(router).get();
        String host = route.getSpec().getHost();
        LOG.info("Router host {}: ", host);
        return  "http://" + host;
    }

}
