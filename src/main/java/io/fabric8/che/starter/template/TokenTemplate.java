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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import io.fabric8.che.starter.util.Reader;

@Component
public class TokenTemplate {
    private final static String TOKEN_VALUE = "${token}";

    @Value(value = "classpath:templates/token_template.json")
    private Resource resource;
    
    String templateCache;
    
    @PostConstruct
    public void initCache() throws IOException {
        templateCache = Reader.read(resource.getInputStream());
    }    

    public class TokenCreateRequest {
        private String token;
        
        protected TokenCreateRequest() {}

        public TokenCreateRequest setToken(String token) {
            this.token = token;
            return this;
        }

        public String getJSON() {
            String json = templateCache;
            json = StringUtils.replace(json, TOKEN_VALUE, token);
            return json;
        }
        
    }

    public TokenCreateRequest createRequest() {
        return new TokenCreateRequest();
    }

}
