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
package io.fabric8.planner.che.starter.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.planner.che.starter.model.CheServer;

@CrossOrigin
@RestController
@RequestMapping("/server")
public class CheServerController {

    private static final Logger LOG = LogManager.getLogger(CheServerController.class);

    @GetMapping("/{id}")
    public CheServer startCheServer(@PathVariable String id) {
        LOG.info("Strarting new Che Server {}", id);
        return new CheServer(id);
    }

    @DeleteMapping("/{id}")
    public CheServer stopCheServer(@PathVariable String id) {
        LOG.info("Stopping Che Server {}", id);
        return new CheServer("id");
    }

}
