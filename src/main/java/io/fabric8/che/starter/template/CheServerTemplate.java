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
package io.fabric8.che.starter.template;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.util.Reader;

@Component
public class CheServerTemplate {
    private static final Logger LOG = LogManager.getLogger(CheServerTemplate.class);

    private String template;

    @PostConstruct
    public void init() throws IOException {
       template = Reader.read(new URL(templateUrl));
       LOG.info("Che Server Template initialized");
    }

    @Value("${che.server.template.url}")
    private String templateUrl;

    public String get() throws MalformedURLException, IOException {
        return template;
    }
}
