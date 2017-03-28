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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.util.Reader;

@Component
public class ProjectTemplate {
    private final static String PROJECT_NAME = "${project.name}";
    private final static String PROJECT_TYPE = "${project.type}";
    private final static String PROJECT_GIT_REPO = "${project.git.repo}";
    private final static String PROJECT_GIT_BRANCH = "${project.git.branch}";

    @Value(value = "classpath:templates/project_template.json")
    private Resource resource;

    private String template;

    @PostConstruct
    public void init() throws IOException {
        template = Reader.read(resource.getInputStream());
    }

    public ProjectCreateRequest create() {
        return new ProjectCreateRequest();
    }

    public class ProjectCreateRequest {
        private String name;
        private String projectType;
        private String repo;
        private String branch;

        public ProjectCreateRequest setName(String name) {
            this.name = name;
            return this;
        }

        public ProjectCreateRequest setProjectType(String projectType) {
            this.projectType = projectType;
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

        public String getJSON() {
            String json = template;
            json = StringUtils.replace(json, PROJECT_NAME, name);
            json = StringUtils.replace(json, PROJECT_TYPE, projectType);
            json = StringUtils.replace(json, PROJECT_GIT_REPO, repo);
            json = StringUtils.replace(json, PROJECT_GIT_BRANCH, branch);
            return json;
        }
    }

}
