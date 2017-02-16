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
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.che.starter.model.OpenShiftConfig;
import io.fabric8.che.starter.model.request.CheServerCreateParams;
import io.fabric8.che.starter.model.response.CheServerInfo;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.DoneableTemplate;
import io.fabric8.openshift.api.model.Parameter;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.ParameterValue;
import io.fabric8.openshift.client.dsl.ClientTemplateResource;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/server")
public class CheServerController {
    private static final String CHE_PROJECT_NAME = "che-server-project";
    private static final String CHE_PROJECT_DISPLAY_NAME = "Che Server";
    private static final String CHE_PROJECT_DESCRIPTION = "Che Server";
    private static final String CHE_OPENSHIFT_ENDPOINT = "CHE_OPENSHIFT_ENDPOINT";

    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @Value(value = "classpath:che_server_template.json")
    private Resource cheServerTemplate;

    @ApiOperation(value = "Create a new Che server on OpenShift instance")
    @PostMapping
    public CheServerInfo startCheServer(@RequestBody CheServerCreateParams params) throws IOException {
        OpenShiftConfig config = params.getOpenShiftConfig();
        String userToken = config.getUserToken();
        String masterUrl = config.getMasterURL();
        LOG.info("OpenShift master URL: {}", masterUrl);

        Config openshiftConfig = getConfig(masterUrl, userToken);
        OpenShiftClient client = new DefaultOpenShiftClient(openshiftConfig);

        createProject(client);
        List<ParameterValue> parameterValues = getTemplateParameterValues(client, masterUrl);
        KubernetesList kubernetesList = processTemplate(client, parameterValues);
        createResources(client, kubernetesList);

        return new CheServerInfo();
    }

    private ProjectRequest createProject(OpenShiftClient client) {
        return client.projectrequests().
                      createNew().
                      withNewMetadata().
                      withName(CHE_PROJECT_NAME).
                      endMetadata().
                      withDescription(CHE_PROJECT_DESCRIPTION).
                      withDisplayName(CHE_PROJECT_DISPLAY_NAME).
                      done();
    }

    private Config getConfig(String masterURL, String token) {
        return new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(token).build();
    }

    private List<ParameterValue> getTemplateParameterValues(OpenShiftClient client, String masterURL)
            throws IOException {
        Template template = loadTemplate(client);
        List<ParameterValue> pvs = new ArrayList<>();
        for (Parameter parameter : template.getParameters()) {
            String name = parameter.getName();
            String value = parameter.getValue();
            LOG.info("Template Parameter Name: {}", name);
            LOG.info("Template Parameter Value: {}", value);
            if (CHE_OPENSHIFT_ENDPOINT.equals(name) && value.isEmpty()) {
                value = masterURL;
            }
        }
        return pvs;
    }

    private Template loadTemplate(OpenShiftClient client) throws IOException {
        return client.templates().load(cheServerTemplate.getInputStream()).get();
    }

    private KubernetesList processTemplate(OpenShiftClient client, List<ParameterValue> parameterValues)
            throws IOException {
        ClientTemplateResource<Template, KubernetesList, DoneableTemplate> templateHandle = client.templates()
                .load(cheServerTemplate.getInputStream());
        return templateHandle.process(parameterValues.toArray(new ParameterValue[parameterValues.size()]));
    }

    private KubernetesList createResources(OpenShiftClient client, KubernetesList list) {
        return client.lists().inNamespace(CHE_PROJECT_NAME).create(list);
    }

}
