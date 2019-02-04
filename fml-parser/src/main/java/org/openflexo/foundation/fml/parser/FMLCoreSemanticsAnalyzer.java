/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

import java.util.Stack;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.node.Token;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public abstract class FMLCoreSemanticsAnalyzer extends DepthFirstAdapter {

	private final FMLModelFactory factory;

	private final TypeFactory typeFactory;

	// Stack of FMLObjectNode beeing build during semantics analyzing
	protected Stack<FMLObjectNode<?, ?>> fmlNodes = new Stack<>();

	// Raw source as when this analyzer was last parsed
	private RawSource rawSource;

	private FragmentManager fragmentManager;

	public FMLCoreSemanticsAnalyzer(FMLModelFactory factory, Start tree, RawSource rawSource) {
		this.factory = factory;
		this.rawSource = rawSource;
		fragmentManager = new FragmentManager(rawSource);
		typeFactory = new TypeFactory((FMLSemanticsAnalyzer) this);
		tree.apply(this);
		finalizeDeserialization();
	}

	public FMLModelFactory getFactory() {
		return factory;
	}

	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
	}

	public FlexoServiceManager getServiceManager() {
		return getFactory().getServiceManager();
	}

	protected abstract void finalizeDeserialization();

	protected final void finalizeDeserialization(FMLObjectNode<?, ?> node) {
		node.finalizeDeserialization();
		for (FMLObjectNode<?, ?> child : node.getChildren()) {
			finalizeDeserialization(child);
		}
	}

	public RawSource getRawSource() {
		return rawSource;
	}

	protected void push(FMLObjectNode<?, ?> fmlNode) {
		if (!fmlNodes.isEmpty()) {
			FMLObjectNode<?, ?> current = fmlNodes.peek();
			current.addToChildren(fmlNode);
		}
		fmlNodes.push(fmlNode);
	}

	protected <N extends FMLObjectNode<?, ?>> N pop() {
		N builtFMLNode = (N) fmlNodes.pop();
		builtFMLNode.deserialize();
		builtFMLNode.preparePrettyPrint();
		return builtFMLNode;
	}

	@Override
	public void defaultCase(Node node) {
		super.defaultCase(node);
		if (node instanceof Token && !fmlNodes.isEmpty()) {
			FMLObjectNode<?, ?> currentNode = fmlNodes.peek();
			if (currentNode != null) {
				// System.out.println("Token: " + ((Token) node).getText() + " de " + ((Token) node).getLine() + ":" + ((Token)
				// node).getPos()
				// + ":" + ((Token) node).getOffset());
				currentNode.handleToken((Token) node);
			}
		}
	}

}
