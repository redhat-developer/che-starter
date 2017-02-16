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
public class ProjectTemplate {
    private final static String PROJECT_NAME = "${project.name}";
    private final static String PROJECT_GIT_REPO = "${project.git.repo}";
    private final static String PROJECT_GIT_BRANCH = "${project.git.branch}";

    @Value(value = "classpath:templates/project_template.json")
    private Resource resource;

    @Autowired
    Generator generator;

    public class ProjectCreateRequest {
        private String name;
        private String repo;
        private String branch;
        private String stack;

        protected ProjectCreateRequest(ProjectTemplate template) {
            this.name = generator.generateName();
        }

        public ProjectCreateRequest setName(String name) {
            this.name = name;
            return this;
        }

        public ProjectCreateRequest setRepo(String repo) {
            this.repo = repo;
            return this;
        }

        public ProjectCreateRequest setBranch(String branch) {
            this.branch = branch;
            return this;
        }

        public ProjectCreateRequest setStack(String stack) {
            this.stack = stack;
            return this;
        }

        public String getJSON() throws IOException {
            String json = read(resource.getInputStream());
            json = StringUtils.replace(json, PROJECT_NAME, name);
            json = StringUtils.replace(json, PROJECT_GIT_REPO, repo);
            json = StringUtils.replace(json, PROJECT_GIT_BRANCH, branch);
            return json;
        }

        public String getBranch() {
            return branch;
        }

        public String getStack() {
            return stack;
        }
    }

    public ProjectCreateRequest createRequest() {
        return new ProjectCreateRequest(this);
    }

    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}
