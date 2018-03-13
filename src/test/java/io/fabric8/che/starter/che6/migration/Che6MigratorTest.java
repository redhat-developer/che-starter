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
package io.fabric8.che.starter.che6.migration;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.che.starter.TestConfig;

@Ignore
public class Che6MigratorTest extends TestConfig {
    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Value("${CHE_USER_NAMESPACE:ibuziuk-che}")
    private String namespace;

    @Autowired
    Che6Migrator che6Mirgator;

    @Test
    public void migrateWorkspacesFromChe5toChe6() throws JsonProcessingException, IOException {
        che6Mirgator.migrateWorkspaces(osioUserToken, namespace);
    }

}
