/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.openflexo.fml.rstasupport;

import java.awt.Graphics;

import javax.swing.Icon;

import org.fife.ui.autocomplete.CompletionProvider;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;

/**
 * A completion for a {@link FMLProperty}
 *
 * @author sylvain
 * @version 1.0
 */
class BindingVariableCompletion extends AbstractFMLSourceCompletion implements MemberCompletion {

	private BindingVariable bindingVariable;

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 3;

	BindingVariableCompletion(CompletionProvider provider, BindingVariable bindingVariable) {
		super(provider, bindingVariable.getVariableName());
		this.bindingVariable = bindingVariable;
		setRelevance(RELEVANCE);
	}

	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		// return data.getEnclosingClassName(fullyQualified);
		return "Prout";
	}

	@Override
	public Icon getIcon() {
		return IconFactory.get().getIcon(IconFactory.LOCAL_VARIABLE_ICON);
	}

	@Override
	public String getSignature() {
		return bindingVariable.getVariableName();
	}

	@Override
	public String getSummary() {

		return "La documentation pour la bv " + bindingVariable.getTooltipText(bindingVariable.getType());

		/*String summary = data.getSummary(); // Could be just the method name
		
		// If it's the Javadoc for the method...
		if (summary != null && summary.startsWith("/**")) {
			summary = org.openflexo.fml.rstasupport.Util.docCommentToHtml(summary);
		}
		
		return summary;*/

	}

	@Override
	public String getType() {
		// return data.getType();
		return TypeUtils.simpleRepresentation(bindingVariable.getType());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BindingVariableCompletion) && ((BindingVariableCompletion) obj).getSignature().equals(getSignature());
	}

	@Override
	public int hashCode() {
		return getSignature().hashCode();
	}

	@Override
	public boolean isDeprecated() {
		// return data.isDeprecated();
		return false;
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		MethodCompletion.rendererText(this, g, x, y, selected);
	}

}
