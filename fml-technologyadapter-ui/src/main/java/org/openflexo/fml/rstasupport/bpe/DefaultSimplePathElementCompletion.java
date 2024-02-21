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

import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.JavaIconFactory;

public class DefaultSimplePathElementCompletion extends AbstractSimplePathElementCompletion<SimplePathElement<?>> {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 1;

	public DefaultSimplePathElementCompletion(FMLSourceCompletionProvider provider, SimplePathElement<?> pathElement) {
		super(provider, pathElement);
		setRelevance(RELEVANCE);
	}

	@Override
	public Icon getIcon() {
		return JavaIconFactory.get().getIcon(JavaIconFactory.LOCAL_VARIABLE_ICON);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof DefaultSimplePathElementCompletion)
				&& ((DefaultSimplePathElementCompletion) obj).getReplacementText().equals(getReplacementText());
	}

	@Override
	public int hashCode() {
		return getReplacementText().hashCode(); // Match equals()
	}

}
