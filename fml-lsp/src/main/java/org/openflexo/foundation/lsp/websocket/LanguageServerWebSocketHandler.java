package org.openflexo.foundation.lsp.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.lsp.server.FMLLanguageServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * A WebSocket handler that manages the lifecycle and communication of a language server
 * over a WebSocket connection.
 *
 * This class acts as a bridge between a WebSocket client (web editor) and 
 * a {@link FMLLanguageServer}. It handles connection establishment, message processing, 
 * error handling, and proper shutdown of the language server.
 *
 * Main responsibilities:
 *  - Initialize and connect a {@link FMLLanguageServer} when a new WebSocket session is established.
 *  - Forward incoming text messages from the WebSocket client to the language server via {@link WebSocketMessageHandler}.
 *  - Handle connection errors and ensure proper shutdown of the server when needed.
 *
 */
public class LanguageServerWebSocketHandler extends TextWebSocketHandler {


    private FMLLanguageServer languageServer;

    private WebSocketMessageHandler messageHandler;

    private final FlexoServiceManager serviceManager;

    private static final Logger logger = Logger.getLogger(LanguageServerWebSocketHandler.class.getPackage().getName());

    /**
     * Constructs a new WebSocket handler for the language server.
     *
     * @param serviceManager the {@link FlexoServiceManager} providing necessary services
     */
    @Autowired
    public LanguageServerWebSocketHandler(FlexoServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (messageHandler != null) {
            messageHandler.onMessage(message.getPayload());
        }
    }

    /**
     * Called when a WebSocket connection is successfully established.
     * Initializes the {@link FMLLanguageServer}, sets up the {@link WebSocketMessageHandler},
     * and uses a {@link WebSocketLauncherBuilder} to connect the server with the remote client.
     *
     * @param session the established WebSocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Connection established. sessionId: " + session.getId() + ", Client: " + session.getRemoteAddress());
        }

        try {
            languageServer = new FMLLanguageServer(serviceManager);
            messageHandler = new WebSocketMessageHandler();

            WebSocketLauncherBuilder<LanguageClient> builder = new WebSocketLauncherBuilder<>();
            builder
                    .setSession(session)
                    .setMessageHandler(messageHandler)
                    .setLocalService(languageServer)
                    .setRemoteInterface(LanguageClient.class);

            Launcher<LanguageClient> languageClientLauncher = builder.create();
            languageServer.connect(languageClientLauncher.getRemoteProxy());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Shutdown language server due to an error: " + exception.toString());
        }
        languageServer.shutdown();
    }

    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Shutting down language server.");
        }
        languageServer.shutdown();
    }
}



