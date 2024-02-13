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

import java.awt.Graphics;

import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.AbstractFMLSourceCompletion;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;

/**
 * Base class for completion based on a {@link SimplePathElement}
 *
 * @author sylvain
 */
public abstract class AbstractSimplePathElementCompletion<BPE extends SimplePathElement<?>> extends AbstractFMLSourceCompletion {

	private BPE simplePathElement;

	AbstractSimplePathElementCompletion(FMLSourceCompletionProvider provider, BPE simplePathElement) {
		super(provider, simplePathElement.getPropertyName());
		this.simplePathElement = simplePathElement;
	}

	public BPE getSimplePathElement() {
		return simplePathElement;
	}

	@Override
	public FMLSourceCompletionProvider getProvider() {
		return (FMLSourceCompletionProvider) super.getProvider();
	}

	@Override
	public String getSummary() {

		return simplePathElement.getTooltipText(simplePathElement.getType());

		/*String summary = data.getSummary(); // Could be just the method name
		
		// If it's the Javadoc for the method...
		if (summary != null && summary.startsWith("/**")) {
			summary = org.openflexo.fml.rstasupport.Util.docCommentToHtml(summary);
		}
		
		return summary;*/

	}

	@Override
	public void rendererText(Graphics g, int x, int y, boolean selected) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSimplePathElement().getPropertyName());
		sb.append(" : ");
		sb.append(TypeUtils.simpleRepresentation(getSimplePathElement().getActualType()));
		g.drawString(sb.toString(), x, y);
	}

}
