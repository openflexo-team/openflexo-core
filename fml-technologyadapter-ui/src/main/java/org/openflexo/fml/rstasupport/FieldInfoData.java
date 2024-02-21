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
package org.openflexo.fml.rstasupport;

import java.util.Iterator;

import org.openflexo.fml.rstasupport.MemberCompletion.Data;
import org.openflexo.fml.rstasupport.buildpath.SourceLocation;
import org.openflexo.fml.rstasupport.classreader.ClassFile;
import org.openflexo.fml.rstasupport.classreader.FieldInfo;
import org.openflexo.fml.rstasupport.classreader.Util;
import org.openflexo.fml.rstasupport.rjc.ast.CompilationUnit;
import org.openflexo.fml.rstasupport.rjc.ast.Field;
import org.openflexo.fml.rstasupport.rjc.ast.Member;
import org.openflexo.fml.rstasupport.rjc.ast.TypeDeclaration;


/**
 * Metadata about a field as read from a class file.  This class is used by
 * instances of {@link FieldCompletion}.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FieldInfoData implements Data {

	private FieldInfo info;
	private FMLSourceCompletionProvider provider;


	FieldInfoData(FieldInfo info, FMLSourceCompletionProvider provider) {
		this.info = info;
		this.provider = provider;
	}


	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		return info.getClassFile().getClassName(fullyQualified);
	}


	@Override
	public String getIcon() {

		String key;
		int flags = info.getAccessFlags();

		if (Util.isDefault(flags)) {
			key = JavaIconFactory.FIELD_DEFAULT_ICON;
		}
		else if (Util.isPrivate(flags)) {
			key = JavaIconFactory.FIELD_PRIVATE_ICON;
		}
		else if (Util.isProtected(flags)) {
			key = JavaIconFactory.FIELD_PROTECTED_ICON;
		}
		else if (Util.isPublic(flags)) {
			key = JavaIconFactory.FIELD_PUBLIC_ICON;
		}
		else {
			key = JavaIconFactory.FIELD_DEFAULT_ICON;
		}

		return key;

	}


	@Override
	public String getSignature() {
		return info.getName();
	}


	@Override
	public String getSummary() {

		ClassFile cf = info.getClassFile();
		SourceLocation loc = provider.getSourceLocForClass(cf.getClassName(true));
		String summary = null;

		// First, try to parse the Javadoc for this method from the attached
		// source.
		if (loc!=null) {
			summary = getSummaryFromSourceLoc(loc, cf);
		}

		// Default to the field name.
		if (summary==null) {
			summary = info.getName();
		}
		return summary;

	}


	/**
	 * Scours the source in a location (zip file, directory), looking for a
	 * particular class's source.  If it is found, it is parsed, and the
	 * Javadoc for this field (if any) is returned.
	 *
	 * @param loc The zip file, jar file, or directory to look in.
	 * @param cf The {@link ClassFile} representing the class of this field.
	 * @return The summary, or <code>null</code> if the field has no javadoc,
	 *         the class's source was not found, or an IO error occurred.
	 */
	private String getSummaryFromSourceLoc(SourceLocation loc, ClassFile cf) {

		String summary = null;
		CompilationUnit cu = org.openflexo.fml.rstasupport.Util.
									getCompilationUnitFromDisk(loc, cf);

		// If the class's source was found and successfully parsed, look for
		// this method.
		if (cu!=null) {

			Iterator<TypeDeclaration> i = cu.getTypeDeclarationIterator();
			while (i.hasNext()) {

				TypeDeclaration td = i.next();
				String typeName = td.getName();

				// Avoid inner classes, etc.
				if (typeName.equals(cf.getClassName(false))) {

					// Locate our field!
					Iterator<Member> j = td.getMemberIterator();
					while (j.hasNext()) {
						Member member = j.next();
						if (member instanceof Field &&
								member.getName().equals(info.getName())) {
							Field f2 = (Field)member;
							summary = f2.getDocComment();
							break;
						}
					}

				} // if (typeName.equals(cf.getClassName(false)))

			} // for (Iterator i=cu.getTypeDeclarationIterator(); i.hasNext(); )

		} // if (cu!=null)

		return summary;

	}


	@Override
	public String getType() {
		return info.getTypeString(false);
	}


	/**
	 * Always returns <code>false</code> since fields cannot be abstract.
	 *
	 * @return <code>false</code> always.
	 */
	@Override
	public boolean isAbstract() {
		return false; // Fields cannot be abstract
	}


	/**
	 * Always returns <code>false</code>, fields cannot be constructors.
	 *
	 * @return <code>false</code> always.
	 */
	@Override
	public boolean isConstructor() {
		return false;
	}


	@Override
	public boolean isDeprecated() {
		return info.isDeprecated();
	}


	@Override
	public boolean isFinal() {
		return info.isFinal();
	}


	@Override
	public boolean isStatic() {
		return info.isStatic();
	}


}
