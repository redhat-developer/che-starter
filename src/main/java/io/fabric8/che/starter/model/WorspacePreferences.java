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

    @JsonProperty("git.committer.name")
    private String commiterName;

    @JsonProperty("git.committer.email")
    private String commiterEmail;

    public String getCommiterName() {
        return commiterName;
    }

    public void setCommitterName(String commiterName) {
        this.commiterName = commiterName;
    }

    public String getCommiterEmail() {
        return commiterEmail;
    }

    public void setCommitterEmail(String commiterEmail) {
        this.commiterEmail = commiterEmail;
    }

}
