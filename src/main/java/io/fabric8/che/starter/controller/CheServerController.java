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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.model.CheServer;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@RequestMapping("/server")
public class CheServerController {

    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @ApiOperation(value = "initializeUser")
    @RequestMapping(method = RequestMethod.GET, path="/initializeUser/{userToken}", produces="application/json")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userToken", value = "User Token", required = true, dataType = "string", paramType="path")
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success")
    })
    public void initializeUser(@PathVariable String userToken) {
    	LOG.info("Initializing user {}",  userToken);
    }

    @GetMapping("/{masterURL}/{userToken}")
    public void startCheServer(@PathVariable String masterUrl, @PathVariable String userToken) {
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).
                withOauthToken(userToken).build();
        OpenShiftClient client = new DefaultOpenShiftClient(config);
    }

    @DeleteMapping("/{id}")
    public CheServer stopCheServer(@PathVariable String id) {
        LOG.info("Stopping Che Server {}", id);
        return new CheServer("id");
    }

}
