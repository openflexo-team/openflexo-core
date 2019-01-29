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

import java.util.Stack;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.AFmlCompilationUnit;
import org.openflexo.foundation.fml.parser.node.AModelDeclaration;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.toolbox.StringUtils;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
class FMLSemanticsAnalyzer extends DepthFirstAdapter {

	private final FMLModelFactory factory;

	private FMLCompilationUnitNode compilationUnitNode;

	// Stack of FMLObjectNode beeing build during semantics analyzing
	protected Stack<FMLObjectNode<?, ?>> fmlNodes = new Stack<>();

	public FMLSemanticsAnalyzer(FMLModelFactory factory, Start tree) {
		this.factory = factory;
		tree.apply(this);
		finalizeDeserialization();
	}

	public FMLCompilationUnit getCompilationUnit() {
		return compilationUnitNode.getFMLObject();
	}

	public FMLModelFactory getFactory() {
		return factory;
	}

	public FlexoServiceManager getServiceManager() {
		return getFactory().getServiceManager();
	}

	public FMLCompilationUnitNode getCompilationUnitNode() {
		return compilationUnitNode;
	}

	private void finalizeDeserialization() {
		finalizeDeserialization(compilationUnitNode);
		debug(compilationUnitNode, 0);
	}

	private void finalizeDeserialization(FMLObjectNode<?, ?> node) {
		node.finalizeDeserialization();
		for (FMLObjectNode<?, ?> child : node.getChildren()) {
			finalizeDeserialization(child);
		}
	}

	private void debug(FMLObjectNode<?, ?> node, int indent) {
		System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + " > " + node.getClass().getSimpleName() + " from "
				+ node.getStartLine() + ":" + node.getStartChar() + " to " + node.getEndLine() + ":" + node.getEndChar());
		indent++;
		for (FMLObjectNode<?, ?> child : node.getChildren()) {
			debug(child, indent);
		}
	}

	/*@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		// System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + " > " + node.getClass().getSimpleName());
		// indent++;
	}
	
	@Override
	public void defaultOut(Node node) {
		super.defaultOut(node);
		// indent--;
		// System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + " < " + node.getClass().getSimpleName());
	}*/

	@Override
	public void inAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.inAFmlCompilationUnit(node);
		FMLCompilationUnitNode newNode = new FMLCompilationUnitNode(node, this);
		fmlNodes.push(newNode);
	}

	@Override
	public void outAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.outAFmlCompilationUnit(node);
		compilationUnitNode = (FMLCompilationUnitNode) fmlNodes.pop().deserialize();
	}

	@Override
	public void inAModelDeclaration(AModelDeclaration node) {
		super.inAModelDeclaration(node);
		VirtualModelNode newNode = new VirtualModelNode(node, this);
		fmlNodes.push(newNode);
	}

	@Override
	public void outAModelDeclaration(AModelDeclaration node) {
		super.outAModelDeclaration(node);
		fmlNodes.pop().deserialize();
	}

	@Override
	public void inAConceptDeclaration(AConceptDeclaration node) {
		super.inAConceptDeclaration(node);
		// System.out.println("DEBUT Nouveau concept " + node.getIdentifier().getText());
		FlexoConceptNode newNode = new FlexoConceptNode(node, this);
		fmlNodes.push(newNode);
	}

	@Override
	public void outAConceptDeclaration(AConceptDeclaration node) {
		super.outAConceptDeclaration(node);
		fmlNodes.pop().deserialize();
		// System.out.println("FIN Nouveau concept " + node.getIdentifier().getText());
		// System.out.println("fmlNodes=" + fmlNodes);
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
