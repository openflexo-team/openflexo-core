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
package org.openflexo.fml.rstasupport.bv;

import org.openflexo.connie.BindingVariable;
import org.openflexo.fml.rstasupport.AbstractFMLSourceCompletion;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;

/**
 * Base class for completion based on a {@link BindingVariable}
 *
 * @author sylvain
 */
public abstract class AbstractBindingVariableCompletion<BV extends BindingVariable> extends AbstractFMLSourceCompletion {

	private BV bindingVariable;

	AbstractBindingVariableCompletion(FMLSourceCompletionProvider provider, BV bindingVariable) {
		super(provider, bindingVariable.getVariableName());
		this.bindingVariable = bindingVariable;
	}

	public BV getBindingVariable() {
		return bindingVariable;
	}

	@Override
	public FMLSourceCompletionProvider getProvider() {
		return (FMLSourceCompletionProvider) super.getProvider();
	}

	@Override
	public String getSummary() {

		return bindingVariable.getTooltipText(bindingVariable.getType());

		/*String summary = data.getSummary(); // Could be just the method name
		
		// If it's the Javadoc for the method...
		if (summary != null && summary.startsWith("/**")) {
			summary = org.openflexo.fml.rstasupport.Util.docCommentToHtml(summary);
		}
		
		return summary;*/

	}

}
