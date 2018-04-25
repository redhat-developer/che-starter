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

import io.fabric8.che.starter.client.keycloak.KeycloakTokenParser;
import io.fabric8.che.starter.client.keycloak.KeycloakTokenValidator;
import io.fabric8.che.starter.model.server.CheServerInfo;
import io.fabric8.che.starter.util.CheServerHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class CheServerController {
    @Autowired
    KeycloakTokenParser keycoakTokenParser;


    @ApiOperation(value = "Get Che server info")
    @GetMapping("/server")
    public CheServerInfo getCheServerInfo(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken, HttpServletRequest request) throws Exception {
        KeycloakTokenValidator.validate(keycloakToken);
        return getCheServerInfo(request, true);
    }

    /*
     * Deprecated since che-starter is not supposed to start multi-tenant che server which never idles
     */
    @Deprecated
    @ApiOperation(value = "Start Che Server")
    @PatchMapping("/server")
    public CheServerInfo startCheServer(@RequestParam String masterUrl, @RequestParam String namespace,
            @ApiParam(value = "Keycloak token", required = true) @RequestHeader("Authorization") String keycloakToken, HttpServletResponse response, HttpServletRequest request) throws Exception {

        KeycloakTokenValidator.validate(keycloakToken);
        return getCheServerInfo(request, true);
    }

    private CheServerInfo getCheServerInfo(HttpServletRequest request, boolean isReady) {
        String requestURL = request.getRequestURL().toString();
        return CheServerHelper.generateCheServerInfo(isReady, requestURL, true);
    }

}
