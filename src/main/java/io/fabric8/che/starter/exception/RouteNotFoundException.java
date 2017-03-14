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

public class RouteNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public RouteNotFoundException() {
    }

    public RouteNotFoundException(String message) {
        super(message);
    }

    public RouteNotFoundException(Throwable cause) {
        super(cause);
    }

    public RouteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
