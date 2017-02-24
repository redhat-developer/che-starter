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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.util.Reader;

public class CheServerControllerTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(CheServerControllerTest.class);

    @Value(value = "classpath:templates/che_server_template.json")
    private Resource cheServerTemplate;

    @Autowired
    CheServerController controller;

    @Autowired
    Reader reader;

    @Test
    public void readTemplate() throws IOException {
        String template = reader.read(cheServerTemplate.getInputStream());
        LOG.info("Che server template: {}", template);
    }

}
