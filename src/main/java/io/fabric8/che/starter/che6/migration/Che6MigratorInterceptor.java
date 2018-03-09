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
package io.fabric8.che.starter.che6.migration;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

public class Che6MigratorInterceptor  implements ClientHttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(Che6MigratorInterceptor.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String IDENTITY_ID_HEADER = "X-Identity-Id";
    private static final String USER_NAMESPACE_HEADER = "X-User-Namespace";

    private static final String REQUEST_ID_MDC_KEY = "req_id";
    private static final String TOKEN_PREFIX = "Bearer ";

    private String keycloakToken;
    private String namespace;

    public Che6MigratorInterceptor(final String keycloakToken, final String namespace) {
        this.keycloakToken = keycloakToken;
        this.namespace = namespace;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add(AUTHORIZATION_HEADER, keycloakToken);
        headers.add(REQUEST_ID_HEADER, getRequestId());
        headers.add(IDENTITY_ID_HEADER, getIdentityId(keycloakToken));
        headers.add(USER_NAMESPACE_HEADER, namespace);
        return execution.execute(request, body);
    }

    private String getRequestId() {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        LOG.debug("'X-Request-Id' sent {}", requestId);
        return StringUtils.isNoneBlank(requestId) ? requestId : "unknown-request-id";
    }

    private String getIdentityId(final String keycloakToken) throws JsonProcessingException, IOException {
        Jwt<Header, Claims> jwt = getJwt(keycloakToken);
        String identityId = jwt.getBody().getSubject();
        LOG.debug("'X-Identity-Id' sent {}", identityId);
        return identityId;
    }

    private Jwt<Header, Claims> getJwt(final String keycloakToken) {
        String jwt = keycloakToken.replaceFirst(TOKEN_PREFIX, "");
        String tokenWithoutSignature = getJWSWithoutSignature(jwt);
        return Jwts.parser().parseClaimsJwt(tokenWithoutSignature);
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
