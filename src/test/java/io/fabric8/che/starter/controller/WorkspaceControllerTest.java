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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.che.starter.TestConfig;

public class WorkspaceControllerTest extends TestConfig {

    @Autowired
    WorkspaceController controller;

    @Test(expected = UnsupportedOperationException.class)
    public void stopAllWorskpaces() {
        controller.stopAll();
    }

}
