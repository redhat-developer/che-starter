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
package io.fabric8.che.starter.error;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {
    private static final String PATH = "/error";

    @Value("${include.error.stack.trace}")
    private boolean includeStackTrace;

    @Autowired
    private ErrorAttributes errorAttributes;

    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(PATH)
    public Error error(HttpServletRequest request, HttpServletResponse response) {
        // Appropriate HTTP response code (e.g. 404) is set automatically
        return new Error(response.getStatus(), getErrorAttributes(request, includeStackTrace));
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

}
