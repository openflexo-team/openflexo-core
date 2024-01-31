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
package org.openflexo.fml.rstasupport.rjc.ast;

import org.openflexo.fml.rstasupport.rjc.lexer.Offset;


/**
 * An EnumBody.
 *
 * <pre>
 * EnumBody:
 *    '{' [EnumConstants] [,] [EnumBodyDeclarations] '}'
 *
 *
 * EnumConstants:
 *    EnumConstant
 *    EnumConstants , EnumConstant
 *
 * EnumConstant:
 *    Annotations Identifier [Arguments] [ClassBody]
 *
 * EnumBodyDeclarations:
 *    ; {ClassBodyDeclaration}
 * </pre>
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class EnumBody extends AbstractASTNode {


	public EnumBody(String name, Offset start) {
		super(name, start);
	}


}