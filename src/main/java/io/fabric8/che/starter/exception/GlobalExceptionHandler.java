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
package io.fabric8.che.starter.exception;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Url is not valid")
    @ExceptionHandler({ URISyntaxException.class, MalformedURLException.class })
    public String handleURLException(Exception e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "MasterUrl is not valid")
    @ExceptionHandler(UnknownHostException.class)
    public String handleHostException(UnknownHostException e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Route not found")
    @ExceptionHandler(RouteNotFoundException.class)
    public String handleRouteNotFoundException(RouteNotFoundException e) {
        return e.getMessage();
    }

    @SuppressWarnings("restriction")
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Unable to find valid certification path to requested target")
    @ExceptionHandler(sun.security.provider.certpath.SunCertPathBuilderException.class)
    public String handleSunCertPathBuilderException(RouteNotFoundException e) {
        return e.getMessage();
    }

}
