package org.openflexo.foundation.lsp.languageServer.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openflexo.foundation.fml.FMLKeywords;


/**
 * Central provider for code completion suggestions in the FML language.
 * 
 * Responsible for generating and supplying completion items such as keywords,
 * snippets, and potentially other code constructs to assist users while editing FML files.
 * 
 * This class serves as the main source of completion data for the language server.
 */

public class CompletionProvider {
	public CompletionProvider(){

    }

    public CompletionItem createKeywordsCompletion(String keyword){
        CompletionItem item = new CompletionItem();
        item.setLabel(keyword);
        item.setKind(CompletionItemKind.Keyword);
        item.setInsertText(keyword);
        return item;
    }
    
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> provide(CompletionParams params) {
	    return CompletableFuture.supplyAsync(() -> {
	        try{
	           
	            List<CompletionItem> items = new ArrayList<>();
	                    
	            //create the completion items for all FML Keywords 
	            for (FMLKeywords keyword : FMLKeywords.values()) {
	                items.add(createKeywordsCompletion(keyword.getKeyword()));
	            }
	            
	            CompletionItem item = new CompletionItem("concept");
	            item.setInsertText("concept ${1:MyConcept} {\n\t${2:// content}\n}");
	            item.setInsertTextFormat(InsertTextFormat.Snippet);
	            item.setDetail("Déclaration de concept FML");
	            item.setDocumentation(new MarkupContent(
	                MarkupKind.MARKDOWN,
	                "**concept** permet de définir une entité dans FML.\n\n" +
	                "Utilisez `concept` pour déclarer un concept personnalisé avec ses propriétés.\n\n" +
	                "```fml\n" +
	                "concept MyConcept {\n" +
	                "    // propriétés ici\n" +
	                "}\n" +
	                "```"
	            ));   
	            items.add(item);
	            
	            
	            return Either.forLeft(items);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            return Either.forLeft(Collections.emptyList());
	        }
	    });
    }

}
