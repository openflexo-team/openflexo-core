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

import javax.swing.Icon;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

/**
 * A template completion for FML
 *
 * @author sylvain
 */
public class FMLTemplateCompletion extends TemplateCompletion implements FMLSourceCompletion {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 2;

	private Icon icon;

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template) {
		this(provider, inputText, definitionString, template, (String) null);
	}

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template, Icon icon) {
		this(provider, inputText, definitionString, template);
		this.icon = icon;
	}

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template,
			String shortDesc) {
		this(provider, inputText, definitionString, template, shortDesc, null);
	}

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template, String shortDesc,
			String summary) {
		super(provider, inputText, definitionString, template, shortDesc, summary);
		setRelevance(RELEVANCE);
	}

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template, String shortDesc,
			String summary, Icon icon) {
		this(provider, inputText, definitionString, template, shortDesc, summary);
		this.icon = icon;
	}

	@Override
	public Icon getIcon() {
		if (icon != null) {
			return icon;
		}
		return JavaIconFactory.get().getIcon(JavaIconFactory.TEMPLATE_ICON);
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		JavaShorthandCompletion.renderText(g, getInputText(), getShortDescription(), x, y, selected);
	}

}
