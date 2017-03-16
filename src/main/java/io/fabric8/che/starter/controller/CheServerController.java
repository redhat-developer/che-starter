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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.client.keycloak.KeycloakClient;
import io.fabric8.che.starter.model.response.CheServerInfo;
import io.fabric8.che.starter.openshift.OpenShiftClientWrapper;
import io.fabric8.che.starter.template.CheServerTemplate;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.openshift.client.OpenShiftClient;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
public class CheServerController {
	private static final Logger LOG = LogManager.getLogger(CheServerController.class);

	@Autowired
	CheServerTemplate template;

	@Autowired
	OpenShiftClientWrapper openShiftClientWrapper;

	@Autowired
	KeycloakClient keycloakClient;

	@ApiOperation(value = "Create Che server on OpenShift instance")
	@PostMapping("/server")
	public CheServerInfo startCheServer(@RequestParam String masterUrl, @RequestParam String namespace,
			@ApiParam("keycloak token") @RequestHeader("Authorization") String keycloakToken) throws Exception {
		LOG.info("OpenShift master Url: {}", masterUrl);
		LOG.info("OpenShift namespace {}", namespace);

		String openShiftToken = keycloakClient.getOpenShiftToken(keycloakToken);
		return getCheServerInfo(masterUrl, namespace, openShiftToken);
	}

	@ApiOperation(value = "Create Che server on OpenShift instance ")
	@PostMapping(path="/server/oso")
	public CheServerInfo startCheServerOnOpenShift(@RequestParam String masterUrl, @RequestParam String namespace,
			@ApiParam("OpenShift token") @RequestHeader("Authorization") String openShiftToken) throws Exception {
		LOG.info("OpenShift master Url: {}", masterUrl);
		LOG.info("OpenShift namespace {}", namespace);

		return getCheServerInfo(masterUrl, namespace, openShiftToken);
	}

	private CheServerInfo getCheServerInfo(String masterUrl, String namespace, String openShiftToken) throws Exception {

		OpenShiftClient openShiftClient = null;
		try {
			openShiftClient = openShiftClientWrapper.get(masterUrl, openShiftToken);
			Controller controller = new Controller(openShiftClient);
			controller.applyJson(template.get());
		} finally {
			if (openShiftClient != null) {
				openShiftClient.close();
			}
		}

		return new CheServerInfo();
	}

}
