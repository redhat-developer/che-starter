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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.CheRestClient;
import io.fabric8.che.starter.client.Generator;
import io.fabric8.che.starter.model.che.Stack;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/stack")
public class StackController {

    private static final Logger LOG = LogManager.getLogger(StackController.class);

    @Value("${che.server.url}")
    String cheServerURL;

    @Autowired
    CheRestClient cheRestClient;

    @Autowired
    Generator generator;

    @ApiOperation(value = "List the available stacks")
    @GetMapping
    public List<Stack> list() {
        return cheRestClient.listStacks(cheServerURL);
    }

}
