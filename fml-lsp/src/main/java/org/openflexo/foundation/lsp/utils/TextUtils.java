package org.openflexo.foundation.lsp.utils;

/**
 * Utility class for common text processing operations used by the FML language server.
 * 
 * This class is stateless and cannot be instantiated.
 */
public final class TextUtils {

    private TextUtils() {
    }
	
    
    /**
     * Extracts the word at a specific position (line and character) in the given text.
     * 
     * A word is defined as a sequence of characters considered valid Java identifier parts
     * (letters, digits, underscore).
     *
     * @param text the full text to search in
     * @param line the zero-based line number
     * @param character the zero-based character index within the line
     * @return the word at the specified position, or {@code null} if out of bounds
     */
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
    
	/**
     * Checks if a string is null, empty, or contains only whitespace characters.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }  
}
