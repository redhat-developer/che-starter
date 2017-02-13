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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import io.fabric8.che.starter.TestConfig;

public class WorkspaceControllerTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(WorkspaceControllerTest.class);

    @Value(value = "classpath:templates/workspace_template.json")
    private Resource workspaceTemplate;

    @Autowired
    WorkspaceController controller;

    @Test(expected = UnsupportedOperationException.class)
    public void stopAllWorskpaces() {
        controller.stopAll();
    }

    @Test
    public void readTemplate() throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(workspaceTemplate.getInputStream()))) {
            String template = buffer.lines().collect(Collectors.joining("\n"));
            LOG.info("Workspace template: {}", template);
        }
    }

}
