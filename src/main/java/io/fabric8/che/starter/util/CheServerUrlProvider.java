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
package io.fabric8.che.starter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.che6.toggle.Che6Toggle;

@Component
public class CheServerUrlProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CheServerUrlProvider.class);

    @Value("${MULTI_TENANT_CHE_SERVER_URL:https://che.prod-preview.openshift.io}")
    private String che5Url;

    @Value("${CHE_SERVER_URL:https://rhche.prod-preview.openshift.io}")
    private String che6Url;

    @Autowired
    Che6Toggle che6toggle;

    public String getUrl(final String keycloakToken) {
        LOG.info("Che 6 url: {}", che6Url);
        LOG.info("Che 5 url: {}", che5Url);
        String url = che6toggle.isChe6(keycloakToken) ? che6Url : che5Url;
        LOG.info("Che host url: {}", url);
        return url;
    }

    public String getChe5Url() {
        return che5Url;
    }

    public String getChe6Url() {
        return che6Url;
    }

}
