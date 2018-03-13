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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Che6MigrationMap {
    private final Map<String, Boolean> INSTANCE = Collections.synchronizedMap(new HashMap<>());

    private Che6MigrationMap() {
    }

    public Map<String, Boolean> get() {
        return INSTANCE;
    }
}
