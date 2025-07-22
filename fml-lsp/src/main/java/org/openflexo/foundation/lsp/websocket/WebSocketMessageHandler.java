package org.openflexo.foundation.lsp.websocket;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.MessageIssueHandler;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;


/**
 * Handles incoming JSON-RPC messages received as JSON strings over WebSocket.
 *
 * Parses the JSON content into a {@link Message} and forwards it to the configured
 * {@link MessageConsumer}. Handles any parsing or validation issues using
 * a {@link MessageIssueHandler}.
 */
public class WebSocketMessageHandler {
    private MessageConsumer consumer;
    private MessageJsonHandler jsonHandler;
    private MessageIssueHandler issueHandler;

    public void setConfigs(MessageConsumer consumer, MessageJsonHandler jsonHandler, MessageIssueHandler issueHandler) {
        this.consumer = consumer;
        this.issueHandler = issueHandler;
        this.jsonHandler = jsonHandler;
    }

    public void onMessage(String content) {
        try {
            Message message = jsonHandler.parseMessage(content);
            consumer.consume(message);
        } catch (MessageIssueException exception) {
            issueHandler.handle(exception.getRpcMessage(), exception.getIssues());
        }
    }
}


