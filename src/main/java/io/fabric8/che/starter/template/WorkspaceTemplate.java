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
package io.fabric8.che.starter.template;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.util.Generator;
import io.fabric8.che.starter.util.Reader;

@Component
public class WorkspaceTemplate {
    private final static String WORKSPACE_NAME = "${workspace.name}";
    private final static String WORKSPACE_STACK = "${workspace.stack}";
    private final static String WORKSPACE_DESCRIPTION = "${workspace.description}";

    @Value(value = "classpath:templates/workspace_template.json")
    private Resource resource;

    @Autowired
    private Generator generator;
    
    @Autowired
    private Reader reader;

    public class WorkspaceCreateRequest {
        private String name;
        private String stack;
        private String description;

        protected WorkspaceCreateRequest(WorkspaceTemplate template) {
            this.name = generator.generateName();
        }

        public WorkspaceCreateRequest setName(String name) {
            this.name = name;
            return this;
        }

        public WorkspaceCreateRequest setStack(String stack) {
            this.stack = stack;
            return this;
        }

        public WorkspaceCreateRequest setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getName() {
            return name;
        }

        public String getStack() {
            return stack;
        }

        public String getDescription() {
            return description;
        }

        public String getJSON() throws IOException {
            String json = reader.read(resource.getInputStream());
            json = StringUtils.replace(json, WORKSPACE_NAME, name);
            json = StringUtils.replace(json, WORKSPACE_STACK, stack);
            json = StringUtils.replace(json, WORKSPACE_DESCRIPTION, description);
            return json;
        }
        
    }

    public WorkspaceCreateRequest createRequest() {
        return new WorkspaceCreateRequest(this);
    }

}
