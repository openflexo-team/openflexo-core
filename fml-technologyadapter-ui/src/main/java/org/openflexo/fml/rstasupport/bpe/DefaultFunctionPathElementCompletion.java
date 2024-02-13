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

import javax.swing.Icon;

import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.IconFactory;

public class DefaultFunctionPathElementCompletion extends AbstractFunctionPathElementCompletion<FunctionPathElement<?>> {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 1;

	public DefaultFunctionPathElementCompletion(FMLSourceCompletionProvider provider, FunctionPathElement<?> pathElement) {
		super(provider, pathElement);
		setRelevance(RELEVANCE);
	}

	@Override
	public Icon getIcon() {
		return IconFactory.get().getIcon(IconFactory.METHOD_PUBLIC_ICON);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DefaultFunctionPathElementCompletion)
				&& ((DefaultFunctionPathElementCompletion) obj).getReplacementText().equals(getReplacementText());
	}

	@Override
	public int hashCode() {
		return getReplacementText().hashCode(); // Match equals()
	}

}
