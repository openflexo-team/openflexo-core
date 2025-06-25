package org.openflexo.foundation.lsp.languageServer.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.openflexo.foundation.lsp.languageServer.utils.TextUtils;

/**
 * Provides rename functionality for symbols in the FML language.
 * 
 * Handles requests to rename identifiers by locating all occurrences
 * of the target word within a document and preparing the corresponding edits.
 * 
 * Note: Currently, renaming is based on simple word matching and does not
 * distinguish symbol types or scopes.
 */
public class RenameProvider {
	
	public RenameProvider() {
		
	}
	
	//TODO make the symbol detection uses the symbol and not juste the word
    public CompletableFuture<WorkspaceEdit> provide(RenameParams params, Map<String,String> documents) {
        String uri = params.getTextDocument().getUri();
        Position pos = params.getPosition();
        String newName = params.getNewName();

        String text = documents.get(uri);
        if (text == null) {
            return CompletableFuture.completedFuture(new WorkspaceEdit());
        }

        // get the word under the carret
        String oldName = TextUtils.extractWordAt(text, pos.getLine(), pos.getCharacter());
        if (oldName == null || TextUtils.isBlank(oldName)) {
            return CompletableFuture.completedFuture(new WorkspaceEdit());
        }

        // check all the occurences of the word (without check if it's a symbol or not)
        List<TextEdit> edits = new ArrayList<>();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int index = 0;
            while ((index = line.indexOf(oldName, index)) != -1) {
                if ((index == 0 || !Character.isJavaIdentifierPart(line.charAt(index - 1))) &&
                    (index + oldName.length() == line.length() || !Character.isJavaIdentifierPart(line.charAt(index + oldName.length())))) {

                    edits.add(new TextEdit(
                        new Range(new Position(i, index), new Position(i, index + oldName.length())),
                        newName
                    ));
                }
                index += oldName.length();
            }
        }

        // create the file edit
        WorkspaceEdit edit = new WorkspaceEdit();
        Map<String, List<TextEdit>> changes = new HashMap<>();
        changes.put(uri, edits);
        edit.setChanges(changes);

        return CompletableFuture.completedFuture(edit);
    }

}
