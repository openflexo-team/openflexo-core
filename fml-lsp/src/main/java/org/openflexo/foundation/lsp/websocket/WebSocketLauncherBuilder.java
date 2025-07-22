package org.openflexo.foundation.lsp.websocket;

import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.springframework.web.socket.WebSocketSession;


/**
 * A custom {@link Launcher.Builder} implementation for creating a language server launcher
 * over a WebSocket connection.
 *
 * This builder integrates a {@link WebSocketSession} and a {@link WebSocketMessageHandler}
 * into the standard LSP4J launcher creation process, enabling message exchange between
 * a language server and client using WebSockets.
 *
 */
public class WebSocketLauncherBuilder<T> extends Launcher.Builder<T> {
    protected WebSocketMessageHandler messageHandler;
    protected WebSocketSession session;
    
    /**
     * Creates a {@link Launcher} configured to communicate via WebSocket.
     * Sets up the JSON handler, message consumer, remote endpoint, and remote proxy,
     * then wires everything into the {@link WebSocketMessageHandler}.
     *
     * @return a configured {@link Launcher} instance
     */
    @Override
    public Launcher<T> create() {
        MessageJsonHandler jsonHandler = createJsonHandler();
        RemoteEndpoint remoteEndpoint = createRemoteEndpoint(jsonHandler);
        MessageConsumer messageConsumer = wrapMessageConsumer(remoteEndpoint);
        messageHandler.setConfigs(messageConsumer, jsonHandler, remoteEndpoint);

        T remoteProxy = createProxy(remoteEndpoint);
        return createLauncher(null, remoteProxy, remoteEndpoint, null);
    }
    

    @Override
    protected RemoteEndpoint createRemoteEndpoint(MessageJsonHandler jsonHandler) {
        MessageConsumer outgoingMessageStream = new WebSocketMessageConsumer(jsonHandler, session);
        outgoingMessageStream = wrapMessageConsumer(outgoingMessageStream);
        Endpoint localEndpoint = ServiceEndpoints.toEndpoint(localServices);
        RemoteEndpoint remoteEndpoint;
        if (exceptionHandler == null)
            remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint);
        else
            remoteEndpoint = new RemoteEndpoint(outgoingMessageStream, localEndpoint, exceptionHandler);
        jsonHandler.setMethodProvider(remoteEndpoint);
        return remoteEndpoint;
    }

    public WebSocketLauncherBuilder<T> setSession(WebSocketSession session) {
        this.session = session;
        return this;
    }

    public WebSocketLauncherBuilder<T> setMessageHandler(WebSocketMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }
}
