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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.client.Generator;

@Component
public class WorkspaceTemplate {
    private final static String WORKSPACE_NAME = "${workspace.name}";
    private final static String WORKSPACE_GIT_REPO = "${workspace.git.repo}";
    private final static String WORKSPACE_GIT_BRANCH = "${workspace.git.branch}";

    @Value(value = "classpath:workspace_template.json")
    private Resource resource;

    @Autowired
    Generator generator;

    public class WorkspaceCreateRequest {
        private String name;
        private String repo;
        private String branch;
        private String stack;

        protected WorkspaceCreateRequest(WorkspaceTemplate template) {
            this.name = generator.generateName();
        }

        public WorkspaceCreateRequest setName(String name) {
            this.name = name;
            return this;
        }

        public WorkspaceCreateRequest setRepo(String repo) {
            this.repo = repo;
            return this;
        }

        public WorkspaceCreateRequest setBranch(String branch) {
            this.branch = branch;
            return this;
        }

        public WorkspaceCreateRequest setStack(String stack) {
            this.stack = stack;
            return this;
        }

        public String getJSON() throws IOException {
            String json = read(resource.getInputStream());
            json = StringUtils.replace(json, WORKSPACE_NAME, name);
            json = StringUtils.replace(json, WORKSPACE_GIT_REPO, repo);
            return json;
        }

        public String getBranch() {
            return branch;
        }

        public String getStack() {
            return stack;
        }
    }

    /*
     * public String getJSON() throws IOException { String template =
     * read(resource.getInputStream()); return StringUtils.replace(template,
     * WORKSPACE_NAME, generator.generateName()); }
     */

    public WorkspaceCreateRequest createRequest() {
        return new WorkspaceCreateRequest(this);
    }

    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}
