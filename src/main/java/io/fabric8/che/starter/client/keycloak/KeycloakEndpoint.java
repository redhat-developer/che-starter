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


/**
 * In order to obtain an OSO token please follow these steps:
 *
 * 1. Manually link your Keycloak account to our "OSO" cluster. Go to
 * http://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity and
 * press Add button for "Openshift v3". You will be redirected to
 * developers.redhat.com to login if you are not logged in yet. Then OS will ask
 * you to authorize our app. If success you will see the linked account in
 * http://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity
 *
 * 2. Now you can obtain the OSO token using GET
 * http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/openshift-v3/token
 * authorization: Bearer <keycloakAccessToken>
 *
 * It's all the same as for github but for github the URL is
 * http://sso.prod-preview.openshift.io/auth/realms/fabric8/broker/github/token
 *
 *@see https://jwt.io - decode token / json token
 *@see http://prod-preview.openshift.io/home
 *@see http://sso.prod-preview.openshift.io/auth/realms/fabric8/account
 *@see http://sso.prod-preview.openshift.io/auth/realms/fabric8/account/identity
 */
public final class KeycloakEndpoint {

    public static final String OPENSHIFT_TOKEN_URL = System.getenv("OPENSHIFT_TOKEN_URL");
    public static final String GITHUB_TOKEN_URL = System.getenv("GITHUB_TOKEN_URL");

    private KeycloakEndpoint(){}
}
