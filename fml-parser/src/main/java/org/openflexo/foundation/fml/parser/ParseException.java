package org.openflexo.foundation.fml.parser;

public class ParseException extends Exception {

	private int line;
	private int position;

	/**
	 * Constructs a new parse exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ParseException(String message, int line, int position) {
		super(message);
	}

	public int getLine() {
		return line;
	}

	public int getPosition() {
		return position;
	}

}
