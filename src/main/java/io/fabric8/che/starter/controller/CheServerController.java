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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.CheServerClient;
import io.fabric8.che.starter.client.keycloak.KeycloakClient;
import io.fabric8.che.starter.model.response.CheServerInfo;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class CheServerController {

    @Autowired
    OpenShiftClientWrapper openShiftClientWrapper;

    @Autowired
    KeycloakClient keycloakClient;

    @Autowired
    CheServerClient cheServerClient;

    @ApiOperation(value = "Get Che server info")
    @GetMapping("/server")
    public CheServerInfo getCheServerInfo(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken) throws Exception {
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        OpenShiftClient openShiftClient = openShiftClientWrapper.get(masterUrl, openShiftToken);
        return cheServerClient.getCheServerInfo(openShiftClient, namespace);
    }

    @ApiOperation(value = "Get Che server info")
    @GetMapping("/server/oso")
    public CheServerInfo getCheServerInfoOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken) throws Exception {
        OpenShiftClient openShiftClient = openShiftClientWrapper.get(masterUrl, openShiftToken);
        return cheServerClient.getCheServerInfo(openShiftClient, namespace);
    }

}
