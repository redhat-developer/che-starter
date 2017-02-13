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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.Parameter;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

public class OpenShiftTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(OpenShiftTest.class);

    @Value(value = "classpath:templates/che_server_template.json")
    private Resource cheServerTemplate;

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
    public void createCheServer() throws IOException {
        Config config = new ConfigBuilder()
                            .withMasterUrl(endpoint)
                            .withUsername(username)
                            .withPassword(password)
                            .build();

        OpenShiftClient client = new DefaultOpenShiftClient(config);

        LOG.info("Number of projects: {}", getNumberOfProjects(client));
        ProjectRequest projectRequest = createTestProject(client);
        LOG.info("Number of projects: {}", getNumberOfProjects(client));
        
        LOG.info("Test project has been deleted: {}", deleteTestProject(client, projectRequest));
        LOG.info("Number of projects: {}", getNumberOfProjects(client));

        Template template = loadTemplate(client);
        LOG.info("Number of templates", getNumberOfTemplates(client));

        List<Parameter> parameters = template.getParameters();
        LOG.info("Number of template parameters: {}", parameters.size());
        for (Parameter parameter : parameters) {
            LOG.info("Template Parameter: {}", parameter.getName());
        }

        LOG.info("Number of templates", getNumberOfTemplates(client));
        installTemplate(client, template);
        LOG.info("Number of templates", getNumberOfTemplates(client));

        LOG.info("Template has been deleted: {}", deleteTemplate(client, template));
        LOG.info("Number of templates", getNumberOfTemplates(client));

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

    private boolean deleteTestProject(OpenShiftClient client, ProjectRequest projectRequest) {
        return client.projects().withName(projectRequest.getMetadata().getName()).delete();
    }

    private Template loadTemplate(OpenShiftClient client) throws IOException {
        return client.templates().load(cheServerTemplate.getInputStream()).get();
    }

    private Template installTemplate(OpenShiftClient client, Template template) throws IOException {
        return client.templates().inNamespace(project).createOrReplace(template);
    }

    private boolean deleteTemplate(OpenShiftClient client, Template template) {
        return client.resource(template).inNamespace(project).delete();
    }

}
