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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

@Component
public class OpenShiftClientWrapper {
    private static final Logger LOG = LogManager.getLogger(OpenShiftClientWrapper.class);

    @Autowired
    private CheServerRoute route;

    @Autowired
    private CheDeploymentConfig dc;

    @Value("${KUBERNETES_CERTS_CA_FILE:#{null}}")
    private String caCertFile;

    /**
     * Gets OpenShift client. When using, you are responsible for closing it.
     * 
     * @param masterUrl URL of OpenShift master
     * @param username user name of OpenShift user
     * @param password user's password
     * @return OpenShift client
     */
    public OpenShiftClient get(String masterUrl, String username, String password) {
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).withUsername(username).withPassword(password).build();
        return new DefaultOpenShiftClient(config);
    }

    /**
     * Gets OpenShift client. When using, you are responsible for closing it.
     * @param masterUrl URL of OpenShift master
     * @param token authorization token
     * @return OpenShift client
     */
    public OpenShiftClient get(String masterUrl, String token) {
        LOG.info("Certificate file: {}", caCertFile);
        Config config = (StringUtils.isBlank(caCertFile))
                ? new ConfigBuilder().withMasterUrl(masterUrl).withOauthToken(token).build()
                : new ConfigBuilder().withMasterUrl(masterUrl).withOauthToken(token).withCaCertFile(caCertFile).build();
        return new DefaultOpenShiftClient(config);
    }

    /**
     * Gets URL of Che server running in user's namespace on OpenShift.
     * 
     * @param masterUrl URL of OpenShift master
     * @param namespace user's namespace
     * @param token authorization token
     * @return URL of Che server 
     * @throws RouteNotFoundException 
     */
    public String getCheServerUrl(String masterUrl, String namespace, String token) throws RouteNotFoundException {
        String routeURL;
        OpenShiftClient openShiftClient = null;
        try {
            openShiftClient = this.get(masterUrl, token);
            dc.deployCheIfSuspended(openShiftClient, namespace);
            routeURL = route.getUrl(openShiftClient, namespace);
        } finally {
            if (openShiftClient != null) {
                openShiftClient.close();
            }
        }
        return routeURL;
    }

}
