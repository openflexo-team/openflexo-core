/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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
import java.util.List;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Token;

/**
 * @author sylvain
 * 
 */
public abstract class FMLObjectNode<N extends Node, T extends FMLObject> {

	private final N astNode;
	private final FMLSemanticsAnalyzer analyser;
	private T fmlObject;

	private FMLObjectNode<?, ?> parent;
	private List<FMLObjectNode<?, ?>> children = new ArrayList<>();

	private int startLine = -1, startChar = -1;
	private int endLine = -1, endChar = -1;

	public FMLObjectNode(N astNode, FMLSemanticsAnalyzer analyser) {
		this.astNode = astNode;
		this.analyser = analyser;
		fmlObject = makeFMLObject();
		fmlObject.initializeDeserialization(getFactory());
		if (!analyser.fmlNodes.isEmpty()) {
			parent = analyser.fmlNodes.peek();
			System.out.println("Parent " + parent.getClass().getSimpleName() + Integer.toHexString(parent.hashCode()) + " > "
					+ getClass().getSimpleName() + Integer.toHexString(hashCode()));
			parent.children.add(this);
		}
	}

	public FMLModelFactory getFactory() {
		return analyser.getFactory();
	}

	public N getASTNode() {
		return astNode;
	}

	public FMLObjectNode<?, ?> getParent() {
		return parent;
	}

	public List<FMLObjectNode<?, ?>> getChildren() {
		return children;
	}

	public abstract T makeFMLObject();

	public abstract FMLObjectNode<N, T> deserialize();

	public void finalizeDeserialization() {
		// Override when required
	}

	public T getFMLObject() {
		return fmlObject;
	}

	protected void handleToken(Token token) {
		if (startLine == -1 || startLine > token.getLine()) {
			startLine = token.getLine();
			if (startChar == -1 || startChar > token.getPos()) {
				startChar = token.getPos();
			}
		}
		if (endLine == -1 || endLine < token.getLine()) {
			endLine = token.getLine();
			if (endChar == -1 || endChar < token.getPos()) {
				endChar = token.getPos();
			}
		}
		if (getParent() != null) {
			getParent().handleToken(token);
		}
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartChar() {
		return startChar;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndChar() {
		return endChar;
	}
}
