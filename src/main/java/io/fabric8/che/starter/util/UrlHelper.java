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

import java.util.HashMap;
import java.util.Map;

public final class UrlHelper {

    private UrlHelper() {
    }

    public static Map<String, String> splitQuery(String query) {
        Map<String, String> queryPairs = new HashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int delimiterIndex = pair.indexOf("=");
            queryPairs.put(pair.substring(0, delimiterIndex), pair.substring(delimiterIndex + 1));
        }
        return queryPairs;
    }

}
