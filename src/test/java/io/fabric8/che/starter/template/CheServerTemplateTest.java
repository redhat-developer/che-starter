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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import io.fabric8.che.starter.TestConfig;

public class CheServerTemplateTest extends TestConfig {

    @Autowired
    CheServerTemplate template;

    @Test
    public void processTemplate() throws MalformedURLException, IOException {
        String json = template.get();
        assertTrue(!StringUtils.isEmpty(json));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        assertTrue(node.getNodeType() == JsonNodeType.OBJECT);
    }

}
