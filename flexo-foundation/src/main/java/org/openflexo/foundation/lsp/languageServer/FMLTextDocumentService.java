package org.openflexo.foundation.lsp.languageServer;



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
import org.openflexo.foundation.lsp.languageServer.providers.CompletionProvider;
import org.openflexo.foundation.lsp.languageServer.providers.HoverProvider;
import org.openflexo.foundation.lsp.languageServer.providers.RenameProvider;


public class FMLTextDocumentService implements TextDocumentService{

    private LanguageClient client;
    
    private Map<String, String> documents = new HashMap<>();
    														
    private CompletionProvider completionProvider = new CompletionProvider();
    private HoverProvider hoverProvider = new HoverProvider();
    private RenameProvider renameProvider = new RenameProvider();
    
    																		
    public FMLTextDocumentService(){
        
    }

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
    
    


 
			
    
	    
    
    //TODO edit parser import to use fmlParser and add it diagnostics
    /*
    private void publishDiagnostics(String uri, Parser parser){
        PublishDiagnosticsParams paramsDiag = new PublishDiagnosticsParams();
        paramsDiag.setUri(uri);
        paramsDiag.setDiagnostics(parser.diagnostics);
        client.publishDiagnostics(paramsDiag);
    }*/

    
    
    
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
    	String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        documents.put(uri, text);

    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
    	String uri = params.getTextDocument().getUri();
        String newText = params.getContentChanges().get(0).getText();
        documents.put(uri, newText);
        
        //TODO Besoin du parser à cet endroit là
        //c'est ici que l'on reçoit le document mis à jour

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
    	documents.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        throw new UnsupportedOperationException("Unimplemented method 'didSave'");
    }
    
}
