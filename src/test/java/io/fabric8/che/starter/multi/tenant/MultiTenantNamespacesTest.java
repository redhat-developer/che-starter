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
package io.fabric8.che.starter.multi.tenant;

import org.springframework.beans.factory.annotation.Autowired;

import io.fabric8.che.starter.TestConfig;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MultiTenantNamespacesTest extends TestConfig {

    @Autowired
    MultiTenantNamespaces multiTenantNamespaces;

    @Test
    public void isMultiTenantNamespace() {
        assertTrue(multiTenantNamespaces.contains("ibuziuk-che"));
        assertTrue(multiTenantNamespaces.contains("dfestal-che"));
        assertTrue(multiTenantNamespaces.contains("mloriedo-che"));
    }

}
