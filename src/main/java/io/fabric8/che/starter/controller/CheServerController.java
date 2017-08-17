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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.CheServerClient;
import io.fabric8.che.starter.client.keycloak.KeycloakClient;
import io.fabric8.che.starter.client.keycloak.KeycloakTokenValidator;
import io.fabric8.che.starter.exception.RouteNotFoundException;
import io.fabric8.che.starter.model.server.CheServerInfo;
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
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken, HttpServletRequest request) throws Exception {

        KeycloakTokenValidator.validate(keycloakToken);
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        OpenShiftClient openShiftClient = openShiftClientWrapper.get(masterUrl, openShiftToken);
        String requestURL = request.getRequestURL().toString();
        return cheServerClient.getCheServerInfo(openShiftClient, namespace, requestURL);
    }

    @ApiOperation(value = "Get Che server info")
    @GetMapping("/server/oso")
    public CheServerInfo getCheServerInfoOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken, HttpServletRequest request) throws Exception {

        OpenShiftClient openShiftClient = openShiftClientWrapper.get(masterUrl, openShiftToken);
        String requestURL = request.getRequestURL().toString();
        return cheServerClient.getCheServerInfo(openShiftClient, namespace, requestURL);
    }

    @ApiOperation(value = "Start Che Server")
    @PatchMapping("/server")
    public CheServerInfo startCheServer(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken, HttpServletResponse response, HttpServletRequest request) throws Exception {

        KeycloakTokenValidator.validate(keycloakToken);
        String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
        CheServerInfo info = startServer(masterUrl, openShiftToken, namespace, response, request);
        return info;
    }

    @ApiOperation(value = "Start Che Server")
    @PatchMapping("/server/oso")
    public CheServerInfo startCheServerOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "OpenShift token", required = true) @RequestHeader("Authorization") String openShiftToken, HttpServletResponse response, HttpServletRequest request) throws Exception {

        CheServerInfo info = startServer(masterUrl, openShiftToken, namespace, response, request);
        return info;
    }

    private CheServerInfo startServer(String masterUrl, String openShiftToken, String namespace, HttpServletResponse response,
            HttpServletRequest request) throws RouteNotFoundException {
        OpenShiftClient openShiftClient = openShiftClientWrapper.get(masterUrl, openShiftToken);
        String requestURL = request.getRequestURL().toString();

        CheServerInfo cheServerInfo = cheServerClient.getCheServerInfo(openShiftClient, namespace, requestURL);
        if (!cheServerInfo.isRunning()) {
            cheServerClient.startCheServer(openShiftClient, namespace);
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        return cheServerInfo;
    }

}
