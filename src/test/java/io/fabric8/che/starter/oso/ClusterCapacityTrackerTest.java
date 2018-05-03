/*-
 * #%L
 * che-starter
 * %%
 * Copyright (C) 2018 Red Hat, Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package io.fabric8.che.starter.oso;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.fabric8.che.starter.TestConfig;
import io.fabric8.che.starter.exception.NamespaceNotFoundException;


public class ClusterCapacityTrackerTest extends TestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterCapacityTrackerTest.class);

    @Value("${OSIO_USER_TOKEN:#{null}}")
    private String osioUserToken;

    @Autowired
    ClusterCapacityTracker clusterCapacityTracker;
    
    @Test
    public void getClusterCapacity() throws NamespaceNotFoundException {
        boolean clusterFull = clusterCapacityTracker.isClusterFull(osioUserToken);
        LOG.info("Is cluster full: {}", clusterFull);
        assertFalse(clusterFull);
    }
}
