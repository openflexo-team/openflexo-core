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

import org.openflexo.fml.rstasupport.rjc.lexer.Scanner;


/**
 * Represents an enum declaration.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class EnumDeclaration extends AbstractTypeDeclarationNode {

	//private EnumBody enumBody;


	public EnumDeclaration(Scanner s, int offs, String name) {
		super(name, s.createOffset(offs), s.createOffset(offs+name.length()));
	}


	@Override
	public String getTypeString() {
		return "enum";
	}


	//public void setEnumBody(EnumBody enumBody) {
	//	this.enumBody = enumBody;
	//}


}
