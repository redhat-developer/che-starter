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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorspacePreferences {

    private String commiterName;

    private String commiterEmail;

    public String getCommiterName() {
        return commiterName;
    }

    @JsonProperty("git.committer.name")
    public void setCommiterName(String commiterName) {
        this.commiterName = commiterName;
    }

    public String getCommiterEmail() {
        return commiterEmail;
    }

    @JsonProperty("git.committer.email")
    public void setCommiterEmail(String commiterEmail) {
        this.commiterEmail = commiterEmail;
    }

}
