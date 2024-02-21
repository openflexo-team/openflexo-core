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

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.JavaIconFactory;

public class DefaultFunctionPathElementCompletion extends AbstractFunctionPathElementCompletion<FunctionPathElement<?>> {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 1;

	public DefaultFunctionPathElementCompletion(FMLSourceCompletionProvider provider, FunctionPathElement<?> pathElement) {
		super(provider, pathElement);
		setRelevance(RELEVANCE);

		List<Parameter> params = new ArrayList<>();
		for (FunctionArgument functionArgument : pathElement.getFunctionArguments()) {
			params.add(
					new Parameter(TypeUtils.simpleRepresentation(functionArgument.getArgumentType()), functionArgument.getArgumentName()));
		}
		setParams(params);

		/*String[] paramTypes = info.getParameterTypes();
		List<Parameter> params = new ArrayList<>(paramTypes.length);
		for (int i = 0; i < paramTypes.length; i++) {
			String name = ((MethodInfoData) data).getParameterName(i);
			String type = paramTypes[i].substring(paramTypes[i].lastIndexOf('.') + 1);
			params.add(new Parameter(type, name));
		}
		setParams(params);*/

	}

	@Override
	public Icon getIcon() {
		return JavaIconFactory.get().getIcon(JavaIconFactory.METHOD_PUBLIC_ICON);
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
