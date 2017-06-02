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

import java.util.Collections;

import io.fabric8.che.starter.model.server.CheServerInfo;
import io.fabric8.che.starter.model.server.CheServerLink;

public final class CheServerHelper {
    public static final String CHE_SERVER_STATUS_URL = "server status url";
    private static final String HTTP_METHOD_GET = "GET";

    private CheServerHelper() {
    }

    public static CheServerInfo generateCheServerInfo(boolean isRunning, String requestURL) {
        CheServerInfo info = new CheServerInfo();
        info.setRunning(isRunning);
        CheServerLink statusLink = CheServerHelper.generateStatusLink(requestURL);
        info.setLinks(Collections.singletonList(statusLink));
        return info;
    }

    private static CheServerLink generateStatusLink(final String href) {
        CheServerLink statusLink = new CheServerLink();
        statusLink.setHref(href);
        statusLink.setMethod(HTTP_METHOD_GET);
        statusLink.setRel(CHE_SERVER_STATUS_URL);
        return statusLink;
    }

}
