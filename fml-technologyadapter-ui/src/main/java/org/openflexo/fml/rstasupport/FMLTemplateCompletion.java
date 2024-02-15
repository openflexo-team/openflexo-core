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
 * A template completion for FML.
 *
 * @author sylvain
 */
public class FMLTemplateCompletion extends TemplateCompletion implements FMLSourceCompletion {

	private String icon;

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 2;

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template) {
		this(provider, inputText, definitionString, template, null);
	}

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template,
			String shortDesc) {
		this(provider, inputText, definitionString, template, shortDesc, null);
	}

	public FMLTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template, String shortDesc,
			String summary) {
		super(provider, inputText, definitionString, template, shortDesc, summary);
		setRelevance(RELEVANCE);
		setIcon(IconFactory.TEMPLATE_ICON);
	}

	@Override
	public Icon getIcon() {
		return IconFactory.get().getIcon(icon);
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		JavaShorthandCompletion.renderText(g, getInputText(), getShortDescription(), x, y, selected);
	}

	public void setIcon(String iconId) {
		this.icon = iconId;
	}

}
