package org.openflexo.foundation.lsp.languageServer.utils;


/**
 * Utility class for common text processing operations used by the FML language server.
 * 
 * This class is stateless and cannot be instantiated.
 */
public final class TextUtils {

    private TextUtils() {
    }
	
	public static String extractWordAt(String text, int line, int character) {
        String[] lines = text.split("\n");
        if (line >= lines.length) {
            return null;
        }
        String lineText = lines[line];
        if (character >= lineText.length()) {
            return null;
        }

        int start = character;
        while (start > 0 && Character.isJavaIdentifierPart(lineText.charAt(start - 1))) {
            start--;
        }

        int end = character;
        while (end < lineText.length() && Character.isJavaIdentifierPart(lineText.charAt(end))) {
            end++;
        }

        return lineText.substring(start, end);
    }
    
    
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }  
}
