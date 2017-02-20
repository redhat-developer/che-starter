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

public class WorkspaceControllerTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(WorkspaceControllerTest.class);

    @Value(value = "classpath:templates/workspace_template.json")
    private Resource workspaceTemplate;

    @Autowired
    WorkspaceController controller;
    
    @Autowired
    Reader reader;

    @Test(expected = UnsupportedOperationException.class)
    public void stopAllWorskpaces() {
        controller.stopAll();
    }

    @Test
    public void readTemplate() throws IOException {
        String template = reader.read(workspaceTemplate.getInputStream());
        LOG.info("Workspace template: {}", template);
    }

}
