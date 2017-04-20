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
package io.fabric8.che.starter.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/status")
public class StatusController {
    private static final String STACK_CONTROLLER_BEAN_NAME = "stackController";
    private static final String WORKSPACE_CONTROLLER_BEAN_NAME = "workspaceController";

    @Autowired
    ApplicationContext applicationContext;

    @GetMapping
    public void getStatus(HttpServletResponse response) {
        if (applicationContext.containsBean(STACK_CONTROLLER_BEAN_NAME)
                && applicationContext.containsBean(WORKSPACE_CONTROLLER_BEAN_NAME)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
