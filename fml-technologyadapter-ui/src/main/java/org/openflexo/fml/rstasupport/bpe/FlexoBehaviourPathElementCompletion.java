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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.MemberCompletion;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.binding.FlexoBehaviourPathElement;
import org.openflexo.view.controller.FlexoController;

public class FlexoBehaviourPathElementCompletion extends AbstractFunctionPathElementCompletion<FlexoBehaviourPathElement>
		implements MemberCompletion {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 3;

	public FlexoBehaviourPathElementCompletion(FMLSourceCompletionProvider provider, FlexoBehaviourPathElement pathElement) {
		super(provider, pathElement);
		setRelevance(RELEVANCE);

		List<Parameter> params = new ArrayList<>();
		for (FunctionArgument functionArgument : pathElement.getFunctionArguments()) {
			params.add(
					new Parameter(TypeUtils.simpleRepresentation(functionArgument.getArgumentType()), functionArgument.getArgumentName()));
		}
		setParams(params);

	}

	public FlexoBehaviour getFlexoBehaviour() {
		return getFunctionPathElement().getFlexoBehaviour();
	}

	@Override
	public Icon getIcon() {
		return FlexoController.statelessIconForObject(getFlexoBehaviour());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FlexoBehaviourPathElementCompletion)
				&& ((FlexoBehaviourPathElementCompletion) obj).getReplacementText().equals(getReplacementText());
	}

	@Override
	public int hashCode() {
		return getReplacementText().hashCode(); // Match equals()
	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		rendererText(this, g, x, y, selected);
	}

	@Override
	public String getEnclosingClassName(boolean fullyQualified) {
		return getFlexoBehaviour().getFlexoConcept().getName();
	}

	@Override
	public String getSignature() {
		return getFlexoBehaviour().getSignature();
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

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
