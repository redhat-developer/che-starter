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
package io.fabric8.che.starter.model.workspace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceFileToOpen {

    private String filePath;
    private int    line;

    public String getFilePath() {
        return filePath;
    }

    public int getLine() {
        return line;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public WorkspaceFileToOpen withFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public WorkspaceFileToOpen withLine(int line) {
        this.line = line;
        return this;
    }

}
