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
package org.openflexo.fml.rstasupport.classreader.constantpool;

import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantTypes;

/**
 * A <code>ConstantPool</code> table entry.<p>
 *
 * See <a href="http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#20080">
 * http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#20080</a>
 * for more information.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class ConstantPoolInfo implements ConstantTypes {

	private int tag;


	/**
	 * Constructor.
	 *
	 * @param tag The tag.
	 */
	public ConstantPoolInfo(int tag) {
		this.tag = tag;
	}


	/**
	 * Returns the tag item for this structure.
	 *
	 * @return The tag item.
	 */
	public int getTag() {
		return tag;
	}


}
