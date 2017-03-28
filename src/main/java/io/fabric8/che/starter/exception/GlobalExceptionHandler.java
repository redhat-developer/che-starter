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
import org.springframework.web.client.HttpClientErrorException;

import io.fabric8.kubernetes.client.KubernetesClientException;

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

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Stack not found")
    @ExceptionHandler(StackNotFoundException.class)
    public String handleStackNotFoundException(StackNotFoundException e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad Request (most likely the token is invalid)")
    @ExceptionHandler(HttpClientErrorException.class)
    public String handleHttpClientErrorException(HttpClientErrorException e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Credentials are invalid")
    @ExceptionHandler(KubernetesClientException.class)
    public String handleKubernetesClientException(KubernetesClientException e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error setting GitHub oAuth token on Che Server")
    @ExceptionHandler(GitHubOAthTokenException.class)
    public String handleGitHubOAthTokenException(GitHubOAthTokenException e) {
        return e.getMessage();
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error occurred while creating project")
    @ExceptionHandler(ProjectCreationException.class)
    public String handleProjectCreationException(ProjectCreationException e) {
        return e.getMessage();
    }

}
