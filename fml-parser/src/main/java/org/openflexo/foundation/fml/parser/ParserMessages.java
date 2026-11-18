package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.parser.node.TDot;
import org.openflexo.foundation.fml.parser.parser.ParserException;

/**
 * Builds the message reported for a {@link ParserException}: the one of the parser, completed with a hint when the failure is a known
 * trap of the language (CORE-D-88).
 */
public class ParserMessages {

	private ParserMessages() {
	}

	/**
	 * A qualified argument (a criterion of a <code>match ... where (...)</code>, an argument of a <code>new X(...)</code>) is
	 * <code>name = expression</code>: <code>name</code> is a plain identifier. A dotted path on the left of the <code>=</code> (e.g.
	 * <code>selected.x = ...</code>) is refused at the <code>.</code>, where the parser only says it expects an <code>=</code>.
	 */
	public static String message(ParserException e) {
		String message = e.getMessage();
		if (e.getToken() instanceof TDot && message != null && message.endsWith("expecting: '='")) {
			return message + " (the left-hand side of a criterion or of a named argument is the name of a property or a parameter, "
					+ "not a path: write 'name = expression')";
		}
		return message;
	}
}
