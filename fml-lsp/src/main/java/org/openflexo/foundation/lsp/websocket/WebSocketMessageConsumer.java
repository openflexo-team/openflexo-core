package org.openflexo.foundation.lsp.websocket;

import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


/**
 * A message consumer that converts JSON-RPC messages into JSON text and sends them
 * to the client through a WebSocket connection.
 *
 * This class is part of the mechanism that links a Language Server to a client
 * (web editor) using the WebSocket protocol.
 */

public class WebSocketMessageConsumer implements MessageConsumer {
    private MessageJsonHandler jsonHandler;
    private WebSocketSession session;

    public WebSocketMessageConsumer(MessageJsonHandler jsonHandler, WebSocketSession session) {
        this.session = session;
        this.jsonHandler = jsonHandler;
    }

    @Override
    public void consume(Message message) throws MessageIssueException, JsonRpcException {
        try {
            String content = jsonHandler.serialize(message);
            if (session.isOpen()) {
                TextMessage textMessage = new TextMessage(content);
                session.sendMessage(textMessage);
            }
        } catch (IOException exception) {
            throw new JsonRpcException(exception);
        }
    }
}

