package org.openflexo.foundation.lsp.languageServer.providers;


import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.lsp.languageServer.utils.TextUtils;

public class HoverProvider {
	
	public HoverProvider() {
		
	}
    

	public CompletableFuture<Hover> provide(HoverParams params, Map<String,String> documents) {
	    String uri = params.getTextDocument().getUri();
	    String text = documents.get(uri);
	
	    if (text == null) {
	        return CompletableFuture.completedFuture(null);
	    }
	
	    Position pos = params.getPosition();
	    String word = TextUtils.extractWordAt(text, pos.getLine(), pos.getCharacter());
	
	    if (TextUtils.isBlank(word)) {
	        return CompletableFuture.completedFuture(null);
	    }
	
	    if (FMLKeywords.isKeyword(word)) {
	        MarkupContent content = new MarkupContent();
	        content.setKind(MarkupKind.MARKDOWN);
	        content.setValue("Mot-clé FML\n" +
	                         "`" + word + "`\n" +
	                         "_Ceci est un mot-clé du langage FML._");
	
	        Hover hover = new Hover(content);
	        return CompletableFuture.completedFuture(hover);
	    }
	
	    return CompletableFuture.completedFuture(null);
	}









}
