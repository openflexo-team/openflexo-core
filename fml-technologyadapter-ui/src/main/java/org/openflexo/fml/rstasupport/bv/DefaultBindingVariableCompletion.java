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

import java.awt.Graphics;

import javax.swing.Icon;

import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.JavaIconFactory;

/**
 * Default class for completion based on a {@link BindingVariable}
 *
 * @author sylvain
 */
public class DefaultBindingVariableCompletion extends AbstractBindingVariableCompletion<BindingVariable> {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 3;

	public DefaultBindingVariableCompletion(FMLSourceCompletionProvider provider, BindingVariable bindingVariable) {
		super(provider, bindingVariable);
		setRelevance(RELEVANCE);
	}

	@Override
	public Icon getIcon() {
		return JavaIconFactory.get().getIcon(JavaIconFactory.LOCAL_VARIABLE_ICON);
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		StringBuilder sb = new StringBuilder();
		sb.append(getBindingVariable().getVariableName());
		sb.append(" : ");
		sb.append(TypeUtils.simpleRepresentation(getBindingVariable().getType()));
		g.drawString(sb.toString(), x, y);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DefaultBindingVariableCompletion)
				&& ((DefaultBindingVariableCompletion) obj).getReplacementText().equals(getReplacementText());
	}

	@Override
	public int hashCode() {
		return getReplacementText().hashCode(); // Match equals()
	}

}
