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
package io.fabric8.che.starter.client.github;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.fabric8.che.starter.client.CheRestEndpoints;
import io.fabric8.che.starter.client.keycloak.KeycloakRestTemplate;
import io.fabric8.che.starter.exception.GitHubOAthTokenException;
import io.fabric8.che.starter.model.Token;
import io.fabric8.che.starter.model.github.GitHubEmail;
import io.fabric8.che.starter.model.github.GitHubUserInfo;

@Component
public class GitHubClient {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubClient.class);

    @Value("${MULTI_TENANT_CHE_SERVER_URL:https://che.prod-preview.openshift.io}")
    private String multiTenantCheServerURL;

    @Value("${GITHUB_USER_URL:https://api.github.com/user}")
    private String GITHUB_USER_URL;

    @Value("${GITHUB_EMAIL_URL:https://api.github.com/user/emails}")
    private String GITHUB_EMAIL_URL;
    /**
     * Uploads the Github oAuth token to the workspace so that the developer can send push requests
     *  
     * @param cheServerURL
     * @param gitHubToken
     * @throws IOException
     * @throws GitHubOAthTokenException 
     */
    public void setGitHubOAuthToken(final String gitHubToken, final String keycloakToken)
            throws IOException, GitHubOAthTokenException {
        String url = CheRestEndpoints.SET_OAUTH_TOKEN.generateUrl(multiTenantCheServerURL);

        Token token = new Token();
        token.setToken(gitHubToken);

        RestTemplate template = new KeycloakRestTemplate(keycloakToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Token> entity = new HttpEntity<Token>(token, headers);

        try {
            template.postForLocation(url, entity);
            LOG.debug("GitHub OAuth token has been successfully set via '{}'", url);
        } catch (Exception e) {
            throw new GitHubOAthTokenException("Error setting GitHub OAuth token", e);
        }
    }

    /**
     * Get {@link GitHubUserInfo} using GitHub public API. If there is no public
     * email specified - fetch "primary" email using `/user/emails` endpoint
     * 
     * @param gitHubToken
     * @return GitHub committer info - 'name' and 'email'
     */
    public GitHubUserInfo getUserInfo(final String gitHubToken) {
        RestTemplate template = new GitHubRestTemplate(gitHubToken);
        GitHubUserInfo info = template.getForObject(GITHUB_USER_URL, GitHubUserInfo.class);
        String publicEmail = info.getEmail();
        if (StringUtils.isBlank(publicEmail)) {
            GitHubEmail primaryEmail = getPrimaryEmail(gitHubToken);
            info.setEmail(primaryEmail.getEmail());
        }
        return info;
    }

    public GitHubEmail getPrimaryEmail(final String gitHubToken) {
        RestTemplate template = new GitHubRestTemplate(gitHubToken);
        ResponseEntity<List<GitHubEmail>> response = template.exchange(GITHUB_EMAIL_URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<GitHubEmail>>(){});
        List<GitHubEmail> emails = response.getBody();
        GitHubEmail primary = emails.stream().filter(e -> e.isPrimary()).findFirst().get();
        return primary;
    }

}
