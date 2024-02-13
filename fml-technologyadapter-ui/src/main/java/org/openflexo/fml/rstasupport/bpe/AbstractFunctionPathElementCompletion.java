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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.FunctionCompletion;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.MemberCompletion;

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

	/**
	 * Renders a member completion.
	 *
	 * @param mc
	 *            The completion to render.
	 * @param g
	 *            The graphics context.
	 * @param x
	 *            The x-offset at which to render.
	 * @param y
	 *            The y-offset at which to render.
	 * @param selected
	 *            Whether the completion is selected/active.
	 */
	public static void rendererText(MemberCompletion mc, Graphics g, int x, int y, boolean selected) {

		String shortType = mc.getType();
		int dot = shortType.lastIndexOf('.');
		if (dot > -1) {
			shortType = shortType.substring(dot + 1);
		}

		// Draw the method signature
		String sig = mc.getSignature();
		FontMetrics fm = g.getFontMetrics();
		g.drawString(sig, x, y);
		int newX = x + fm.stringWidth(sig);
		if (mc.isDeprecated()) {
			int midY = y + fm.getDescent() - fm.getHeight() / 2;
			g.drawLine(x, midY, newX, midY);
		}
		x = newX;

		// Append the return type
		StringBuilder sb = new StringBuilder(" : ").append(shortType);
		sb.append(" - ");
		String s = sb.toString();
		g.drawString(s, x, y);
		x += fm.stringWidth(s);

		// Append the type of the containing class of this member.
		Color origColor = g.getColor();
		if (!selected) {
			g.setColor(Color.GRAY);
		}
		g.drawString(mc.getEnclosingClassName(false), x, y);
		if (!selected) {
			g.setColor(origColor);
		}

	}

}
