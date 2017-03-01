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

import io.fabric8.che.starter.client.StackClient;
import io.fabric8.che.starter.model.Stack;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/stack")
public class StackController {
    private static final Logger LOG = LogManager.getLogger(StackController.class);

    @Autowired
    private StackClient stackClient;
    
    @Autowired
    private OpenShiftClientWrapper clientWrapper;

    @ApiOperation(value = "List the available stacks")
    @GetMapping
    public List<Stack> list(@RequestParam String masterUrl, @RequestHeader("Authorization") String token) {
        LOG.info("Getting stacks from {}", masterUrl);
        String cheServerUrl = clientWrapper.getCheServerUrl(masterUrl, token);
        
        return stackClient.listStacks(cheServerUrl);
    }

}
