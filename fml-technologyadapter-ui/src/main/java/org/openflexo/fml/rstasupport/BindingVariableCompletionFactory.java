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
package org.openflexo.fml.rstasupport;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingVariable;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.SuperBindingVariable;

/**
 * Factory for {@link AbstractBindingVariableCompletion}
 *
 * @author sylvain
 */
public class BindingVariableCompletionFactory {

	public static <BV extends BindingVariable> AbstractBindingVariableCompletion<BV> makeBindingVariableCompletion(
			FMLSourceCompletionProvider completionProvider, BV bv) {
		if (bv instanceof FlexoPropertyBindingVariable) {
			return (AbstractBindingVariableCompletion<BV>) new FlexoPropertyBindingVariableCompletion(completionProvider,
					(FlexoPropertyBindingVariable) bv);
		}
		if (bv instanceof FlexoConceptBindingVariable) {
			return (AbstractBindingVariableCompletion<BV>) new FlexoConceptBindingVariableCompletion(completionProvider,
					(FlexoConceptBindingVariable) bv);
		}
		if (bv instanceof SuperBindingVariable) {
			return (AbstractBindingVariableCompletion<BV>) new SuperBindingVariableCompletion(completionProvider,
					(SuperBindingVariable) bv);
		}
		return (AbstractBindingVariableCompletion<BV>) new DefaultBindingVariableCompletion(completionProvider, bv);
	}

}
