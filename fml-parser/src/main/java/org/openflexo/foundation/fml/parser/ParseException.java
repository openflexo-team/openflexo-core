package org.openflexo.foundation.fml.parser;

public class ParseException extends Exception {

	private int line;
	private int position;
	private int length;

	/**
	 * Constructs a new parse exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ParseException(String message, int line, int position, int length) {
		super(message);
		this.line = line;
		this.position = position;
		this.length = length;
	}

	public int getLine() {
		return line;
	}

	public int getPosition() {
		return position;
	}

	public int getLength() {
		return length;
	}
}
