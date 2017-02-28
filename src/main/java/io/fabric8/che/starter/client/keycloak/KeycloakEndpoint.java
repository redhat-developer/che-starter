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
package io.fabric8.che.starter.client.keycloak;

enum KeycloakEndpoint {
    // jwt.io - decode token / json token 
    // http://prod-preview.openshift.io/home
    // http://sso.prod-preview.openshift.io/auth/realms/fabric8/account
    // authorization: Bearer <ACCESS_TOKEN> 
    // http://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity
    GET_OPENSHIFT_TOKEN ("http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/openshift/token"),
    GET_GITHUB_TOKEN    ("http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token");

    private final String endpoint;

    private KeycloakEndpoint (final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return endpoint;
    }

}
