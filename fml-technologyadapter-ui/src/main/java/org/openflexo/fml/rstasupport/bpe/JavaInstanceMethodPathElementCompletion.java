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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.openflexo.connie.binding.Function.FunctionArgument;
import org.openflexo.connie.binding.javareflect.JavaInstanceMethodDefinition;
import org.openflexo.connie.binding.javareflect.JavaInstanceMethodPathElement;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.rstasupport.FMLSourceCompletionProvider;
import org.openflexo.fml.rstasupport.JavaIconFactory;
import org.openflexo.fml.rstasupport.MemberCompletion;

public class JavaInstanceMethodPathElementCompletion extends AbstractFunctionPathElementCompletion<JavaInstanceMethodPathElement>
		implements MemberCompletion {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	private static final int RELEVANCE = 3;

	public JavaInstanceMethodPathElementCompletion(FMLSourceCompletionProvider provider, JavaInstanceMethodPathElement pathElement) {
		super(provider, pathElement);
		setRelevance(RELEVANCE);

		List<Parameter> params = new ArrayList<>();
		for (FunctionArgument functionArgument : pathElement.getFunctionArguments()) {
			params.add(
					new Parameter(TypeUtils.simpleRepresentation(functionArgument.getArgumentType()), functionArgument.getArgumentName()));
		}
		setParams(params);

	}

	public JavaInstanceMethodDefinition getMethodDefinition() {
		return getFunctionPathElement().getMethodDefinition();
	}

	@Override
	public Icon getIcon() {
		return JavaIconFactory.get().getIcon(JavaIconFactory.METHOD_PUBLIC_ICON);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof JavaInstanceMethodPathElementCompletion)
				&& ((JavaInstanceMethodPathElementCompletion) obj).getReplacementText().equals(getReplacementText());
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
		return fullyQualified ? TypeUtils.fullQualifiedRepresentation(getMethodDefinition().getDeclaringType())
				: TypeUtils.simpleRepresentation(getMethodDefinition().getDeclaringType());
	}

	@Override
	public String getSignature() {
		return getMethodDefinition().getSimplifiedSignature();
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

}
