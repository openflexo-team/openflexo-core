/*
 * 06/25/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.openflexo.fml.rstasupport;

import java.awt.Graphics;

import org.fife.ui.autocomplete.TemplateCompletion;
import org.openflexo.foundation.FlexoServiceManager;

public abstract class AbstractTemplateCompletion extends TemplateCompletion implements FMLSourceCompletion {

	public AbstractTemplateCompletion(FMLSourceCompletionProvider provider, String inputText, String definitionString, String template) {
		this(provider, inputText, definitionString, template, null, null);
	}

	public AbstractTemplateCompletion(FMLSourceCompletionProvider provider, String inputText, String definitionString, String template,
			String shortDescription, String summary) {
		super(provider, inputText, definitionString, template, shortDescription, summary);
	}

	@Override
	public FMLSourceCompletionProvider getProvider() {
		return (FMLSourceCompletionProvider) super.getProvider();
	}

	public FlexoServiceManager getServiceManager() {
		if (getProvider() != null) {
			return getProvider().getServiceManager();
		}
		return null;
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		JavaShorthandCompletion.renderText(g, getInputText(), getShortDescription(), x, y, selected);
	}

}
