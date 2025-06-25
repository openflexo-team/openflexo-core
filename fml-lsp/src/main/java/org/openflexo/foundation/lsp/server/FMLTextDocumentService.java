package org.openflexo.foundation.lsp.server;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.openflexo.foundation.lsp.server.providers.CompletionProvider;
import org.openflexo.foundation.lsp.server.providers.HoverProvider;
import org.openflexo.foundation.lsp.server.providers.RenameProvider;

/**
 * Text document service implementation for the FML Language Server.
 * 
 * This class handles document-related LSP features such as:
 * - Receiving and storing document content on open/change/close events.
 * - Providing language features like completion, hover information, and rename.
 *
 * It delegates feature logic to specialized providers: {@link CompletionProvider},
 * {@link HoverProvider}, and {@link RenameProvider}.
 *
 * A connection to the LSP client can be established via the {@code connect} method.
 */
public class FMLTextDocumentService implements TextDocumentService{

    private LanguageClient client;
    
    // In-memory storage of currently open documents, keyed by their URI
    private Map<String, String> documents = new HashMap<>();
    														
    private CompletionProvider completionProvider = new CompletionProvider();
    private HoverProvider hoverProvider = new HoverProvider();
    private RenameProvider renameProvider = new RenameProvider();
    
    																		
    public FMLTextDocumentService(){
        
    }
    
    /**
     * Connects the text document service to the LSP client.
     */
    public void connect(LanguageClient client){
        this.client = client;
    }
    
    
    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
    	return completionProvider.provide(params); 
    }
    

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
    	return hoverProvider.provide(params, documents);
    }
    
    
    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return renameProvider.provide(params, documents);
    } 
    
    /**
     * Handles opening a document. Stores its initial content.
     */
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
    	String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        documents.put(uri, text);

    }
    
    /**
     * Handles content changes in a document. Updates the in-memory content.
     */
    @Override
    public void didChange(DidChangeTextDocumentParams params) {
    	String uri = params.getTextDocument().getUri();
        String newText = params.getContentChanges().get(0).getText();
        documents.put(uri, newText);
        
        //TODO Besoin du parser à cet endroit là
        //c'est ici que l'on reçoit le document mis à jour

    }
    
    /**
     * Handles closing a document. Removes it from in-memory storage.
     */
    @Override
    public void didClose(DidCloseTextDocumentParams params) {
    	documents.remove(params.getTextDocument().getUri());
    }
    
    /**
     * Not implemented yet
     */
    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        throw new UnsupportedOperationException("Unimplemented method 'didSave'");
    }
    
}
