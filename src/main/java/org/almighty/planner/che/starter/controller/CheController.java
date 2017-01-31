package org.almighty.planner.che.starter.controller;

import org.almighty.planner.che.starter.model.CheServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheController {
    
    @RequestMapping("/start/server")
    public CheServer startCheServer() {
        return new CheServer("id");
    }

    @RequestMapping("/stop/server")
    public CheServer stopCheServer() {
        return new CheServer("id");
    }

}
