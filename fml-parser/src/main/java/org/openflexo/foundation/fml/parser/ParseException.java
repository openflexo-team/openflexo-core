package org.openflexo.foundation.fml.parser;

import org.openflexo.p2pp.RawSource;

public class ParseException extends org.openflexo.connie.ParseException {

	private int line;
	private int position;
	private int length;
	private RawSource rawSource;

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

	/**
	 * Constructs a new parse exception with the specified detail message and parsed rawSource
	 * 
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 */
	public ParseException(String message, int line, int position, int length, RawSource rawSource) {
		this(message, line, position, length);
		this.rawSource = rawSource;
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

	public RawSource getRawSource() {
		return rawSource;
	}
}
