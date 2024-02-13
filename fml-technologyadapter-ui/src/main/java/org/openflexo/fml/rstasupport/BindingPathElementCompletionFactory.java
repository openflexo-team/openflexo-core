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

import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.binding.javareflect.JavaInstanceMethodPathElement;
import org.openflexo.fml.rstasupport.bpe.AbstractFunctionPathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.AbstractSimplePathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.ContainerPathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.DefaultFunctionPathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.DefaultSimplePathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.FlexoBehaviourPathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.FlexoPropertyPathElementCompletion;
import org.openflexo.fml.rstasupport.bpe.JavaInstanceMethodPathElementCompletion;
import org.openflexo.foundation.fml.binding.ContainerPathElement;
import org.openflexo.foundation.fml.binding.FlexoBehaviourPathElement;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;

/**
 * Factory for {@link BindingPathElement} completion
 *
 * @author sylvain
 */
public class BindingPathElementCompletionFactory {

	public static <BPE extends SimplePathElement<?>> AbstractSimplePathElementCompletion<BPE> makeSimplePathElementCompletion(
			FMLSourceCompletionProvider completionProvider, BPE bpe) {
		AbstractSimplePathElementCompletion returned;
		if (bpe instanceof FlexoPropertyPathElement) {
			returned = new FlexoPropertyPathElementCompletion(completionProvider, (FlexoPropertyPathElement<?>) bpe);
		}
		else if (bpe instanceof ContainerPathElement) {
			returned = new ContainerPathElementCompletion(completionProvider, (ContainerPathElement) bpe);
		}
		else {
			returned = new DefaultSimplePathElementCompletion(completionProvider, bpe);
		}
		return returned;
	}

	public static <BPE extends FunctionPathElement<?>> AbstractFunctionPathElementCompletion<BPE> makeFunctionPathElementCompletion(
			FMLSourceCompletionProvider completionProvider, BPE bpe) {
		AbstractFunctionPathElementCompletion returned;
		if (bpe instanceof FlexoBehaviourPathElement) {
			returned = new FlexoBehaviourPathElementCompletion(completionProvider, (FlexoBehaviourPathElement) bpe);
		}
		else if (bpe instanceof JavaInstanceMethodPathElement) {
			returned = new JavaInstanceMethodPathElementCompletion(completionProvider, (JavaInstanceMethodPathElement) bpe);
		}
		else {
			returned = new DefaultFunctionPathElementCompletion(completionProvider, bpe);
		}
		return returned;
	}

}
