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
package io.fabric8.che.starter.openshift;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.template.CheServerTemplate;
import io.fabric8.kubernetes.api.Controller;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.api.model.DoneableTemplate;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.ParameterValue;
import io.fabric8.openshift.client.dsl.ClientTemplateResource;

public class OpenShiftTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(OpenShiftTest.class);
    private static final String CHE_OPENSHIFT_ENDPOINT = "CHE_OPENSHIFT_ENDPOINT";

    @Autowired
    private CheServerTemplate template;
    
    @Autowired
    private OpenShiftConfig openShiftConfig;

    @Value(value = "classpath:templates/che_server_template.json")
    private Resource cheServerTemplate;

    @Value("${che.server.template.url}")
    private String templateUrl;

    @Value("${che.openshift.endpoint}")
    private String endpoint;

    @Value("${che.openshift.project}")
    private String project;

    @Value("${che.openshift.username}")
    private String username;

    @Value("${che.openshift.password}")
    private String password;

    @Ignore("Test is run against local minishift cluster and requires additional setup")
    @Test
    public void createCheServer() throws Exception {
        Config config = openShiftConfig.get(endpoint, username, password);
        OpenShiftClient client = new DefaultOpenShiftClient(config);

        ProjectRequest projectRequest = createTestProject(client);
        LOG.info("Number of projects: {}", getNumberOfProjects(client));

        LOG.info("Test project has been deleted: {}", deleteTestProject(client, projectRequest));
        LOG.info("Number of projects: {}", getNumberOfProjects(client));

        Controller controller = new Controller(client);
        controller.applyJson(template.get());

//        Template template = loadTemplate(client);
//        LOG.info("Number of templates {}", getNumberOfTemplates(client));
//
//        List<Parameter> parameters = template.getParameters();
//        LOG.info("Number of template parameters: {}", parameters.size());
//
//        List<ParameterValue> pvs = new ArrayList<>();
//        for (Parameter parameter : parameters) {
//            String name = parameter.getName();
//            String value = parameter.getValue();
//            LOG.info("Template Parameter Name: {}", name);
//            LOG.info("Template Parameter Value: {}", value);
//            if (CHE_OPENSHIFT_ENDPOINT.equals(name) && value.isEmpty()) {
//                value = endpoint;
//            }
//            pvs.add(new ParameterValue(name, value));
//        }

//        LOG.info("Number of templates {}", getNumberOfTemplates(client));
//        KubernetesList list = processTemplate(client, pvs);
//        createResources(client, list);

        LOG.info("Pods: {}", getNumberOfPods(client));
    }

    private int getNumberOfProjects(OpenShiftClient client) {
        return client.projects().list().getItems().size();
    }

    private int getNumberOfTemplates(OpenShiftClient client) {
        return client.templates().inNamespace(project).list().getItems().size();
    }

    private int getNumberOfPods(OpenShiftClient client) {
        return client.pods().inNamespace(project).list().getItems().size();
    }

    private ProjectRequest createTestProject(OpenShiftClient client) {
        return client.projectrequests().
               createNew().
               withNewMetadata().
               withName("test-project").
               endMetadata().
               withDescription("Test Project").
               withDisplayName("Test Project").done();
    }
    
    private KubernetesList processTemplate(OpenShiftClient client, List<ParameterValue> parameterValues) throws IOException {
          ClientTemplateResource<Template, KubernetesList, DoneableTemplate> templateHandle = client.templates().load(cheServerTemplate.getInputStream());
          return templateHandle.process(parameterValues.toArray(new ParameterValue[parameterValues.size()]));
    }

    private boolean deleteTestProject(OpenShiftClient client, ProjectRequest projectRequest) {
        return client.projects().withName(projectRequest.getMetadata().getName()).delete();
    }

    private Template loadTemplate(OpenShiftClient client) throws IOException {
        return client.templates().load(cheServerTemplate.getInputStream()).get();
    }
    
    private KubernetesList createResources(OpenShiftClient client, KubernetesList list) {
        return client.lists().inNamespace(project).create(list);
    }

}
