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

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

@Component
public class KeycloakTokenParser {
    private static final String TOKEN_PREFIX = "Bearer ";

    @SuppressWarnings("rawtypes")
    public String getIdentityId(final String keycloakToken) throws JsonProcessingException, IOException {
        String jwt = keycloakToken.replaceFirst(TOKEN_PREFIX, "");
        String tokenWithoutSignature = getJWSWithoutSignature(jwt);
        Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
        return untrusted.getBody().getSubject();
    }

    /**
     * @see <a href=
     *      "https://github.com/jwtk/jjwt/issues/67#issuecomment-182527735">Ability
     *      to inspect body of signed JWT</a>
     */
    private String getJWSWithoutSignature(final String token) {
        int i = token.lastIndexOf('.');
        String withoutSignature = token.substring(0, i + 1);
        return withoutSignature;
    }

}
