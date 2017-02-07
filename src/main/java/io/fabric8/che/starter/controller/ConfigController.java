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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@RequestMapping("/config")
public class ConfigController {
    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @ApiOperation(value = "initializeService")
    @RequestMapping(method = RequestMethod.GET, path = "/initializeService", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "masterUrl", value = "Master URL", required = true, dataType = "string", paramType = "request") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success") })
    public void initializeService(@RequestParam String masterUrl) {
        LOG.info("Initializing service {}", masterUrl);
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).build();
        KubernetesClient client = new DefaultKubernetesClient(config);
    }

}
