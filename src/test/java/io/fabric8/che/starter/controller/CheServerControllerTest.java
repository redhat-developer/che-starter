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

import io.fabric8.che.starter.TestConfig;

public class CheServerControllerTest extends TestConfig {
    private static final Logger LOG = LogManager.getLogger(CheServerControllerTest.class);

    @Autowired
    CheServerController controller;

    @Test
    public void readTemplate() throws IOException {
    	// TODO
    }

}
