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
package org.openflexo.fml.rstasupport.tree;

import org.fife.ui.autocomplete.Util;
import org.openflexo.fml.rstasupport.IconFactory;
import org.openflexo.fml.rstasupport.rjc.ast.LocalVariable;


/**
 * Tree node for a local variable.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class LocalVarTreeNode extends JavaTreeNode {

	private String text;


	LocalVarTreeNode(LocalVariable var) {

		super(var);
		setIcon(IconFactory.get().getIcon(IconFactory.LOCAL_VARIABLE_ICON));
		setSortPriority(PRIORITY_LOCAL_VAR);

		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append(var.getName());
		sb.append(" : ");
		sb.append("<font color='#888888'>");
		MemberTreeNode.appendType(var.getType(), sb);
		text = sb.toString();
	}


	@Override
	public String getText(boolean selected) {
		// Strip out HTML tags
		return selected ? Util.stripHtml(text).
				replaceAll("&lt;", "<").replaceAll("&gt;", ">") : text;
	}


}
