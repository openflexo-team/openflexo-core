package org.openflexo.foundation.lsp.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;


/**
 * WebSocket configuration enabling a handler for the /ls endpoint.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	
	/**
     * Registers the WebSocket handler for the /ls endpoint, allowing all origins.
     *
     * @param registry the {@link WebSocketHandlerRegistry} to configure
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/ls")
                .setAllowedOrigins("*");
    }
    
    /**
     * Provides a per-connection WebSocket handler for {@link LanguageServerWebSocketHandler}.
     *
     * @return a new {@link WebSocketHandler} instance
     */
    @Bean
    public WebSocketHandler webSocketHandler() {
        return new PerConnectionWebSocketHandler(LanguageServerWebSocketHandler.class);
    }
}
