/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.RawSource;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * The analyzer of a binding parsed alone, from its text (an expression typed in an inspector, {@code DataBinding#setUnparsedBinding}): it
 * gives its nodes the source they were parsed from, so that the binding can be written back as it was typed (see
 * {@link org.openflexo.connie.DataBindingPrettyPrintDelegate}). It belongs to no compilation unit; issues are not reported, as for a binding
 * parsed without analyzer.
 */
public class DetachedBindingAnalyzer extends FMLSemanticsAnalyzer {

	private final RawSource rawSource;
	private final FragmentManager fragmentManager;
	private final AbstractFMLTypingSpace typingSpace;
	private final FMLBindingFactory bindingFactory;
	private final Map<Node, ObjectNode<?, ?, ?>> nodesForAST = new HashMap<>();
	private final List<SemanticAnalysisIssue> issues = new ArrayList<>();

	public DetachedBindingAnalyzer(FMLModelFactory modelFactory, Node rootNode, RawSource rawSource, AbstractFMLTypingSpace typingSpace,
			FMLBindingFactory bindingFactory) {
		super(modelFactory, rootNode);
		this.rawSource = rawSource;
		this.fragmentManager = new FragmentManager(rawSource);
		this.typingSpace = typingSpace;
		this.bindingFactory = bindingFactory;
	}

	@Override
	public FMLCompilationUnit getCompilationUnit() {
		return null;
	}

	@Override
	public AbstractFMLTypingSpace getTypingSpace() {
		return typingSpace;
	}

	@Override
	public FMLBindingFactory getFMLBindingFactory() {
		return bindingFactory;
	}

	@Override
	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	@Override
	public RawSource getRawSource() {
		return rawSource;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends Node, FMLN extends ObjectNode<?, ?, ?>> FMLN retrieveFMLNode(N astNode, Function<N, FMLN> function) {
		FMLN returned = (FMLN) nodesForAST.get(astNode);
		if (returned == null) {
			returned = function.apply(astNode);
			nodesForAST.put(astNode, returned);
		}
		return returned;
	}

	@Override
	public void throwIssue(Object modelObject, String errorMessage, RawSourceFragment fragment, RawSourcePosition startPosition) {
		// Not reported, as for a binding parsed without analyzer: the binding reports itself as invalid
	}

	@Override
	public List<SemanticAnalysisIssue> getSemanticAnalysisIssues() {
		return issues;
	}

	@Override
	public FMLCompilationUnitSemanticsAnalyzer getCompilationUnitAnalyzer() {
		return null;
	}
}
