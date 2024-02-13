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
package org.openflexo.fml.rstasupport.bpe;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.FunctionCompletion;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;

/**
 * Base class for completion based on a {@link FunctionPathElement}
 *
 * @author sylvain
 */
public abstract class AbstractFunctionPathElementCompletion<BPE extends FunctionPathElement<?>> extends FunctionCompletion {

	private BPE functionPathElement;

	AbstractFunctionPathElementCompletion(FMLSourceCompletionProvider provider, BPE functionPathElement) {
		super(provider, functionPathElement.getMethodName(), TypeUtils.simpleRepresentation(functionPathElement.getActualType()));
		this.functionPathElement = functionPathElement;
	}

	public BPE getFunctionPathElement() {
		return functionPathElement;
	}

	@Override
	public FMLSourceCompletionProvider getProvider() {
		return (FMLSourceCompletionProvider) super.getProvider();
	}

	@Override
	public String getAlreadyEntered(JTextComponent comp) {
		String temp = getProvider().getAlreadyEnteredText(comp);
		int lastDot = temp.lastIndexOf('.');
		if (lastDot > -1) {
			temp = temp.substring(lastDot + 1);
		}
		return temp;
	}

	@Override
	public String getSummary() {

		return functionPathElement.getTooltipText(functionPathElement.getType());

		/*String summary = data.getSummary(); // Could be just the method name
		
		// If it's the Javadoc for the method...
		if (summary != null && summary.startsWith("/**")) {
			summary = org.openflexo.fml.rstasupport.Util.docCommentToHtml(summary);
		}
		
		return summary;*/

	}

	/*@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		StringBuilder sb = new StringBuilder();
		sb.append(getFunctionPathElement().getLabel());
		sb.append(" : ");
		sb.append(TypeUtils.simpleRepresentation(getFunctionPathElement().getActualType()));
		g.drawString(sb.toString(), x, y);
	}*/

}
