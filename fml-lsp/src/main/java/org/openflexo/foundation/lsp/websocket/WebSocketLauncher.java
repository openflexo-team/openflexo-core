package org.openflexo.foundation.lsp.websocket;

import org.openflexo.foundation.FlexoServiceManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * WebSocket Launcher for the fml language server.
 */
@SpringBootApplication
public class WebSocketLauncher {

    private static FlexoServiceManager serviceManager;

    public void start(FlexoServiceManager sm) {
        serviceManager = sm;
        SpringApplication.run(WebSocketLauncher.class);
    }

    @Bean
    public FlexoServiceManager flexoServiceManager() {
        return serviceManager;
    }
}

