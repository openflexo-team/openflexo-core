/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.openflexo.fml.rstasupport.rjc.lang;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.fml.rstasupport.rjc.lang.Type;
import org.openflexo.fml.rstasupport.rjc.lexer.Token;


/**
 * A TypeParameter.
 *
 * <pre>
 * TypeParameter:
 *    Identifier ['extends' Bound]
 *
 * Bound:
 *    Type { '&amp;' Type }
 * </pre>
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TypeParameter {

	private Token name;
	private List<Type> bounds;


	public TypeParameter(Token name) {
		this.name = name;
	}


	/**
	 * Adds a bound to this type parameter.
	 *
	 * @param bound The bound to add.
	 */
	public void addBound(Type bound) {
		if (bounds==null) {
			bounds = new ArrayList<>(1); // Usually just 1
		}
		bounds.add(bound);
	}


	public String getName() {
		return name.getLexeme();
	}


}
