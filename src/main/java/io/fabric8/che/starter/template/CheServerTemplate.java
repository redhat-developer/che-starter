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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.util.Reader;

@Component
public class CheServerTemplate {
    private static final Logger LOG = LogManager.getLogger(CheServerTemplate.class);

    private String template;

    @Value("${che.server.template.url}")
    private String templateUrl;

    @Autowired
    private Reader reader;

    public String get() throws MalformedURLException, IOException {
        if (template == null) {
            template = reader.read(new URL(templateUrl));
            LOG.info("Che Server Template: {}", template);
        }
        return template;
    }
}
