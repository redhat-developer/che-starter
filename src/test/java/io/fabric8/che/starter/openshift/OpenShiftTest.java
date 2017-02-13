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
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.Parameter;
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
        Config config = new ConfigBuilder().
                withMasterUrl(endpoint).
                withUsername(username).
                withPassword(password).build();

        OpenShiftClient client = new DefaultOpenShiftClient(config);
        
        LOG.info("Number of projects: {}", client.projects().list().getItems().size());
        LOG.info("Projects: {}", client.projects().list());

        Template template = client.templates().load(cheServerTemplate.getInputStream()).get();

        List<Parameter> parameters = template.getParameters();
        LOG.info("Number of template parameters: {}", parameters.size());
        for (Parameter parameter : parameters) {
            LOG.info("Template Parameter: {}", parameter);
        }

        template.setParameters(parameters);

        LOG.info("Number of templates", client.templates().inNamespace(project).list().getItems().size());

        client.templates().inNamespace(project).createOrReplace(template);

        LOG.info("Number of templates", client.templates().inNamespace(project).list().getItems().size());

        Boolean isDeleted = client.resource(template).inNamespace(project).delete();
        LOG.info("Templates has been deleted: {}", isDeleted);
        
        LOG.info("Number of templates", client.templates().inNamespace(project).list().getItems().size());

        LOG.info("Pods: {}", client.pods().inNamespace(project).list());
    }

}
