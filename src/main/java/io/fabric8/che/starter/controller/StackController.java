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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.client.StackClient;
import io.fabric8.che.starter.client.keycloak.KeycloakClient;
import io.fabric8.che.starter.exception.KeycloakException;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.model.stack.Stack;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class StackController {
    private static final Logger LOG = LogManager.getLogger(StackController.class);

    @Autowired
    private StackClient stackClient;

    @Autowired
    private OpenShiftClientWrapper openShiftClientWrapper;

    @Autowired
    KeycloakClient keycloakClient;

    @ApiOperation(value = "List the available stacks")
    @GetMapping("/stack")
    public List<Stack> list(@RequestParam String masterUrl, @RequestParam String namespace, @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken)
            throws RouteNotFoundException, JsonProcessingException, IOException, KeycloakException {
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        return getStacks(masterUrl, namespace, openShiftToken, keycloakToken);
    }

    @ApiOperation(value = "List the available stacks")
    @GetMapping("/stack/oso")
    public List<Stack> listOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace, @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken)
            throws RouteNotFoundException, JsonProcessingException, IOException {
        return getStacks(masterUrl, namespace, openShiftToken, null);
    }

    private List<Stack> getStacks(String masterUrl, String namespace, String openShiftToken, String keycloakToken) throws RouteNotFoundException {
        LOG.info("Getting stacks from masterUrl {}", masterUrl);
        LOG.info("Getting stacks from namespace {}", namespace);

        String cheServerUrl = openShiftClientWrapper.getCheServerUrl(masterUrl, namespace, openShiftToken);
        return stackClient.listStacks(cheServerUrl, keycloakToken);
    }

}
