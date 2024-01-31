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

/**
 * Interface for tree nodes that can hold type declarations (e.g.
 * {@link CompilationUnit}s and {@link TypeDeclaration}s).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface TypeDeclarationContainer {


	/**
	 * Adds a type declaration to this container.
	 *
	 * @param typeDec The new type declaration.
	 */
	void addTypeDeclaration(TypeDeclaration typeDec);


}
