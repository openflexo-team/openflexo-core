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

import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantPoolInfo;

/**
 * Class corresponding to the <code>CONSTANT_Integer_info</code> structure.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ConstantIntegerInfo extends ConstantPoolInfo {

	private long bytes; // u4


	/**
	 * Constructor.
	 *
	 * @param bytes The bytes that make up this object.
	 */
	public ConstantIntegerInfo(long bytes) {
		super(CONSTANT_String);
		this.bytes = bytes;
	}


	public long getBytes() {
		return bytes;
	}


	/**
	 * Returns the <code>int</code>represented by this info.
	 *
	 * @return The <code>int</code> represented.
	 */
	public int getIntValue() {
		return (int)bytes;
	}


	/**
	 * Returns a string representation of this object.  Useful for debugging.
	 *
	 * @return A string representation of this object.
	 */
	@Override
	public String toString() {
		return "[ConstantIntegerInfo: " +
				"bytes=" + getBytes() +
				"]";
	}


}
