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
package io.fabric8.che.starter.multi.tenant;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MultiTenantNamespaces {
    private static final Logger LOG = LoggerFactory.getLogger(MultiTenantNamespaces.class);

    private Set<String> namespaces;

    @Value("${che.multi.tenant.namespaces:#{null}}")
    private String multiTenantNamespaces;

    @PostConstruct
    public void init() throws IOException {
        if (StringUtils.isBlank(multiTenantNamespaces)) {
            this.namespaces = Collections.emptySet();
        } else {
            String[] namespacesArray = toArray(multiTenantNamespaces);
            this.namespaces = new HashSet<>(Arrays.asList(namespacesArray));
        }
        LOG.info("Multi-tenant namespaces: {}", namespaces.toString());
    }

    public boolean contains(final String namespace) {
        return namespaces.contains(namespace);
    }

    private String[] toArray(final String namespaces) {
        String sanitizedNamespaces = removeWhiteSpaces(namespaces);
        return sanitizedNamespaces.split(",");
    }

    private String removeWhiteSpaces(final String string) {
        return string.replaceAll("\\s+","");
    }

}
