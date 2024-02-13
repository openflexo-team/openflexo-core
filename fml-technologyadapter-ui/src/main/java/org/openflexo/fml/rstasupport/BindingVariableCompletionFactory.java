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
import org.openflexo.fml.rstasupport.bv.AbstractBindingVariableCompletion;
import org.openflexo.fml.rstasupport.bv.DefaultBindingVariableCompletion;
import org.openflexo.fml.rstasupport.bv.FlexoConceptBindingVariableCompletion;
import org.openflexo.fml.rstasupport.bv.FlexoPropertyBindingVariableCompletion;
import org.openflexo.fml.rstasupport.bv.SuperBindingVariableCompletion;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingVariable;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.binding.SuperBindingVariable;

/**
 * Factory for {@link BindingVariable} completion
 *
 * @author sylvain
 */
public class BindingVariableCompletionFactory {

	public static <BV extends BindingVariable> AbstractBindingVariableCompletion<BV> makeBindingVariableCompletion(
			FMLSourceCompletionProvider completionProvider, BV bv) {
		AbstractBindingVariableCompletion returned;
		if (bv instanceof FlexoPropertyBindingVariable) {
			returned = new FlexoPropertyBindingVariableCompletion(completionProvider, (FlexoPropertyBindingVariable) bv);
		}
		else if (bv instanceof FlexoConceptBindingVariable) {
			returned = new FlexoConceptBindingVariableCompletion(completionProvider, (FlexoConceptBindingVariable) bv);
		}
		else if (bv instanceof SuperBindingVariable) {
			returned = new SuperBindingVariableCompletion(completionProvider, (SuperBindingVariable) bv);
		}
		else {
			returned = new DefaultBindingVariableCompletion(completionProvider, bv);
		}
		return returned;
	}

}
