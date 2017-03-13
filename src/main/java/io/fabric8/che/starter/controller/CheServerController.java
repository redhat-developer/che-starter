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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.model.response.CheServerInfo;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.che.starter.template.CheServerTemplate;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/server")
public class CheServerController {
    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @Autowired
    CheServerTemplate template;

    @Autowired
    OpenShiftClientWrapper clientWrapper;

    @ApiOperation(value = "Create Che server on OpenShift instance")
    @PostMapping
    public CheServerInfo startCheServer(@RequestParam String masterUrl, @RequestParam String namespace, @RequestHeader("Authorization") String token) throws Exception {
        LOG.info("OpenShift master URL: {}", masterUrl);
        LOG.info("OpenShift namespace {}", namespace);
        OpenShiftClient openShiftClient = clientWrapper.get(masterUrl, token);
        Controller controller = new Controller(openShiftClient);
        controller.applyJson(template.get());
        return new CheServerInfo();
    }

}
