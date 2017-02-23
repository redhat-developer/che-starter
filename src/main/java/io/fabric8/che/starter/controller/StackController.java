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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.CheRestClient;
import io.fabric8.che.starter.model.Stack;
import io.fabric8.che.starter.openshift.Client;
import io.fabric8.che.starter.openshift.Router;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/stack")
public class StackController {

    private static final Logger LOG = LogManager.getLogger(StackController.class);

    @Autowired
    private CheRestClient cheRestClient;

    @Autowired
    private Router router;

    @Autowired
    private Client client;

    @ApiOperation(value = "List the available stacks")
    @GetMapping
    public List<Stack> list(@RequestParam String masterUrl, @RequestHeader("Authorization") String token) {
        LOG.info("Getting stacks from {}", masterUrl);
        String cheServerUrl = getCheServerUrl(masterUrl, token);
        return cheRestClient.listStacks(cheServerUrl);
    }

    private String getCheServerUrl(String masterUrl, String token) {
        OpenShiftClient openShiftClient = client.get(masterUrl, token);
        return router.getUrl(openShiftClient);
    }

}
