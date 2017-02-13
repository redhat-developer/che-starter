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
package io.fabric8.che.starter.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.model.CheServer;
import io.fabric8.che.starter.model.OpenShiftConfig;
import io.fabric8.che.starter.model.response.CheServerInfo;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("/server")
public class CheServerController {

    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @Value(value = "classpath:che_server_template.json")
    private Resource cheServerTemplate;

    @ApiOperation(value = "Create a new Che server on OpenShift instance")
    @PostMapping
    public CheServerInfo startCheServer(@RequestParam OpenShiftConfig config, @RequestParam String masterUrl, @RequestParam String userToken) throws IOException {
        LOG.info("OpenShift config: {}", config);
        LOG.info("OpenShift master URL: {}", userToken);
        LOG.info("OAth token: {}", userToken);

        Config openshiftConfig = new ConfigBuilder().withMasterUrl(masterUrl).withOauthToken(userToken).build();
        OpenShiftClient client = new DefaultOpenShiftClient(openshiftConfig);
        Template template = client.templates().load(cheServerTemplate.getInputStream()).get();
        return new CheServerInfo();
    }

    @ApiIgnore
    @DeleteMapping("/{id}")
    public CheServer stopCheServer(@PathVariable String id) {
        LOG.info("Stopping Che Server {}", id);
        throw new UnsupportedOperationException("'stopCheServer' functionality is currently not supported");
    }

}
