package org.openflexo.foundation.lsp.languageServer;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class FMLLanguageServer implements LanguageServer, LanguageClientAware{
    private TextDocumentService textDocumentService;
    private WorkspaceService workspaceService;
    private LanguageClient client;
    private int errorCode = 1;

    public FMLLanguageServer(){
        this.textDocumentService = new FMLTextDocumentService();
        this.workspaceService = new FMLWorkspaceService();
    }


    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        ((FMLTextDocumentService) textDocumentService).connect(this.client);

    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
    
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    
        capabilities.setCompletionProvider(new CompletionOptions());
        
        capabilities.setHoverProvider(true);
        
        capabilities.setRenameProvider(true);
        

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }
    


    @Override
    public CompletableFuture<Object> shutdown() {
        errorCode=0;
        return null;
    }

    @Override
    public void exit() {
        System.exit(errorCode);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return ((FMLTextDocumentService) textDocumentService);
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

}
