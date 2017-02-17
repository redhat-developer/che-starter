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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.model.request.WorkspaceCreateParams;
import io.fabric8.che.starter.model.response.CheServerInfo;
import io.fabric8.che.starter.template.CheServerTemplate;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/server")
public class CheServerController {
    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @Autowired
    CheServerTemplate template;

    @Value(value = "classpath:templates/che_server_template.json")
    private Resource cheServerTemplate;

    @ApiOperation(value = "Create a new Che server on OpenShift instance")
    @PostMapping
    public CheServerInfo startCheServer(@RequestParam String masterURL, @RequestBody WorkspaceCreateParams params,
            @RequestHeader("Authorization") String token) throws Exception {
        LOG.info("OpenShift master URL: {}", masterURL);

        Config openshiftConfig = getConfig(masterURL, token);
        OpenShiftClient client = new DefaultOpenShiftClient(openshiftConfig);

        Controller controller = new Controller(client);
        controller.applyJson(template.get());

        return new CheServerInfo();
    }

    private Config getConfig(String masterURL, String token) {
        return new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(token).build();
    }

}
