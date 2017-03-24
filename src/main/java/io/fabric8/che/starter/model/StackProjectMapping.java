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
package io.fabric8.che.starter.model;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Provides mapping between Stack ID and Project Type
 * 
 * @see https://github.com/eclipse/che/blob/master/ide/che-core-ide-stacks/src/main/resources/stacks.json
 * @see https://github.com/eclipse/che/blob/master/ide/che-core-ide-templates/src/main/resources/samples.json
 */
public final class StackProjectMapping {
    public static final String BLANK_PROJECT_TYPE = "blank";
    private static final Map<String, String> STACK_PROJECT_MAPPING = ImmutableMap.<String, String> builder().
            put("vert.x", "maven").
            put("java-default", "maven").
            put("node-default", "node-js").
            put("dotnet-default", "blank").
            build();

    private StackProjectMapping() {
    }

    public static Map<String, String> get() {
        return STACK_PROJECT_MAPPING;
    }
}
