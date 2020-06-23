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
import org.openflexo.p2pp.P2PPNode;

/**
 * Base class implementing semantics analyzer, based on sablecc grammar visitor<br>
 * 
 * @author sylvain
 * 
 */
public abstract class FMLSemanticsAnalyzer extends DepthFirstAdapter {

	private final FMLModelFactory factory;

	// Stack of FMLObjectNode beeing build during semantics analyzing
	protected Stack<FMLObjectNode<?, ?, ?>> fmlNodes = new Stack<>();

	private Node rootNode;

	public FMLSemanticsAnalyzer(FMLModelFactory factory, Node rootNode) {
		this.factory = factory;
		this.rootNode = rootNode;
	}

	public final FMLModelFactory getFactory() {
		return factory;
	}

	public abstract MainSemanticsAnalyzer getMainAnalyzer();

	public FragmentManager getFragmentManager() {
		return getMainAnalyzer().getFragmentManager();
	}

	public Node getRootNode() {
		return rootNode;
	}

	public final FlexoServiceManager getServiceManager() {
		return getFactory().getServiceManager();
	}

	protected final void finalizeDeserialization(FMLObjectNode<?, ?, ?> node) {
		node.finalizeDeserialization();
		for (P2PPNode<?, ?> child : node.getChildren()) {
			finalizeDeserialization((FMLObjectNode<?, ?, ?>) child);
		}
	}

	protected void push(FMLObjectNode<?, ?, ?> fmlNode) {
		if (!fmlNodes.isEmpty()) {
			FMLObjectNode<?, ?, ?> current = fmlNodes.peek();
			current.addToChildren(fmlNode);
		}
		fmlNodes.push(fmlNode);
	}

	protected <N extends FMLObjectNode<?, ?, ?>> N pop() {
		N builtFMLNode = (N) fmlNodes.pop();
		builtFMLNode.deserialize();
		// builtFMLNode.initializePrettyPrint();
		return builtFMLNode;
	}

	public FMLObjectNode<?, ?, ?> peek() {
		if (!fmlNodes.isEmpty()) {
			return fmlNodes.peek();
		}
		return null;
	}

	public FMLObjectNode<?, ?, ?> getCurrentNode() {
		return peek();
	}

}
