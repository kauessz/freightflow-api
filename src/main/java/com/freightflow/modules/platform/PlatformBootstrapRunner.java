package com.freightflow.modules.platform;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PlatformBootstrapRunner implements ApplicationRunner {

    private final PlatformBootstrapService platformBootstrapService;

    public PlatformBootstrapRunner(PlatformBootstrapService platformBootstrapService) {
        this.platformBootstrapService = platformBootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        platformBootstrapService.bootstrapIfEnabled();
    }
}

