package org.openflexo.foundation.fml.parser;

public class ParseException extends Exception {

	/**
	 * Constructs a new parse exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ParseException(String message) {
		super(message);
	}

}