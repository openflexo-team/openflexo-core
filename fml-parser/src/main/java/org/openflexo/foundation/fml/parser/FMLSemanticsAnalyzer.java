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
import org.openflexo.foundation.fml.parser.ir.IRAbstractPropertyNode;
import org.openflexo.foundation.fml.parser.ir.IRCompilationUnitNode;
import org.openflexo.foundation.fml.parser.ir.IRExpressionPropertyNode;
import org.openflexo.foundation.fml.parser.ir.IRFlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.ir.IRFlexoConceptNode;
import org.openflexo.foundation.fml.parser.ir.IRJavaImportNode;
import org.openflexo.foundation.fml.parser.ir.IRJavaRoleNode;
import org.openflexo.foundation.fml.parser.ir.IRNode;
import org.openflexo.foundation.fml.parser.ir.IRPrimitiveRoleNode;
import org.openflexo.foundation.fml.parser.ir.IRVirtualModelNode;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AExpressionPropertyDeclaration;
import org.openflexo.foundation.fml.parser.node.AFlexoBehaviourDeclarationConceptBodyDeclaration;
import org.openflexo.foundation.fml.parser.node.AFlexoConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.AJavaRoleDeclarationPrimitiveRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.APrimitivePrimitiveRoleDeclaration;
import org.openflexo.foundation.fml.parser.node.AResourceImportDeclaration;
import org.openflexo.foundation.fml.parser.node.ASimpleJavaImportDeclaration;
import org.openflexo.foundation.fml.parser.node.AUseDeclaration;
import org.openflexo.foundation.fml.parser.node.AVirtualModelDeclaration;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class FMLSemanticsAnalyzer extends DepthFirstAdapter {

	private final FMLModelFactory factory;
	private final FMLCompilationUnit fmlCompilationUnit;
	// private VirtualModel virtualModel;
	private final FlexoServiceManager serviceManager;
	private final FMLSyntaxAnalyzer syntaxAnalyzer;
	private IRCompilationUnitNode rootNode;

	public FMLSemanticsAnalyzer(FMLCompilationUnit fmlCompilationUnit, FMLSyntaxAnalyzer syntaxAnalyzer, FlexoServiceManager serviceManager)
			throws ModelDefinitionException {
		this.fmlCompilationUnit = fmlCompilationUnit;
		this.syntaxAnalyzer = syntaxAnalyzer;
		this.serviceManager = serviceManager;
		factory = new FMLModelFactory(null, serviceManager);
		// System.out.println("----------> New FMLSemanticsAnalyzer " + getClass().getSimpleName());
		// Thread.dumpStack();
	}

	public FMLModelFactory getFactory() {
		return factory;
	}

	public FMLSyntaxAnalyzer getSyntaxAnalyzer() {
		return syntaxAnalyzer;
	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	public FMLCompilationUnit getFMLCompilationUnit() {
		return fmlCompilationUnit;
	}

	@Override
	public void inStart(Start node) {
		super.inStart(node);
		rootNode = new IRCompilationUnitNode(node, this);
		irNodeStack = new Stack<>();
		irNodeStack.push(rootNode);
	}

	public IRCompilationUnitNode getRootNode() {
		return rootNode;
	}

	Stack<IRNode<?, ?>> irNodeStack;

	private IRNode<?, ?> pushIRNode(IRNode<?, ?> node) {
		IRNode<?, ?> current = irNodeStack.peek();
		current.addToChilren(node);
		irNodeStack.push(node);
		return node;
	}

	private IRNode<?, ?> popIRNode() {
		IRNode<?, ?> current = irNodeStack.pop();
		current.makeFMLObject();
		return current;
	}

	@Override
	public void outAUseDeclaration(AUseDeclaration node) {
		super.outAUseDeclaration(node);
		// System.out.println("-------------> Tiens, un use: " + node + " ta=" + node.getTechnologyAdapter() + " id=" + node.getTaId());
	}

	@Override
	public void inASimpleJavaImportDeclaration(ASimpleJavaImportDeclaration node) {
		super.inASimpleJavaImportDeclaration(node);
		pushIRNode(new IRJavaImportNode(node, this));
	}

	@Override
	public void outASimpleJavaImportDeclaration(ASimpleJavaImportDeclaration node) {
		super.outASimpleJavaImportDeclaration(node);
		popIRNode();
	}

	@Override
	public void outAResourceImportDeclaration(AResourceImportDeclaration node) {
		super.outAResourceImportDeclaration(node);
		// System.out.println("-------------> Tiens, un resource import: " + node);
	}

	@Override
	public void inAVirtualModelDeclaration(AVirtualModelDeclaration node) {
		super.inAVirtualModelDeclaration(node);
		pushIRNode(new IRVirtualModelNode(node, this));
	}

	@Override
	public void outAVirtualModelDeclaration(AVirtualModelDeclaration node) {
		super.outAVirtualModelDeclaration(node);
		IRVirtualModelNode virtualModelNode = (IRVirtualModelNode) popIRNode();
		rootNode.setVirtualModelNode(virtualModelNode);
	}

	@Override
	public void inAFlexoConceptDeclaration(AFlexoConceptDeclaration node) {
		super.inAFlexoConceptDeclaration(node);
		pushIRNode(new IRFlexoConceptNode(node, this));
	}

	@Override
	public void outAFlexoConceptDeclaration(AFlexoConceptDeclaration node) {
		super.outAFlexoConceptDeclaration(node);
		popIRNode();
	}

	@Override
	public void inAJavaRoleDeclarationPrimitiveRoleDeclaration(AJavaRoleDeclarationPrimitiveRoleDeclaration node) {
		super.inAJavaRoleDeclarationPrimitiveRoleDeclaration(node);
		pushIRNode(new IRJavaRoleNode(node, this));
	}

	@Override
	public void outAJavaRoleDeclarationPrimitiveRoleDeclaration(AJavaRoleDeclarationPrimitiveRoleDeclaration node) {
		super.outAJavaRoleDeclarationPrimitiveRoleDeclaration(node);
		popIRNode();
	}

	@Override
	public void inAPrimitivePrimitiveRoleDeclaration(APrimitivePrimitiveRoleDeclaration node) {
		super.inAPrimitivePrimitiveRoleDeclaration(node);
		pushIRNode(new IRPrimitiveRoleNode(node, this));
	}

	@Override
	public void outAPrimitivePrimitiveRoleDeclaration(APrimitivePrimitiveRoleDeclaration node) {
		super.outAPrimitivePrimitiveRoleDeclaration(node);
		popIRNode();
	}

	@Override
	public void inAAbstractPropertyDeclaration(AAbstractPropertyDeclaration node) {
		super.inAAbstractPropertyDeclaration(node);
		pushIRNode(new IRAbstractPropertyNode(node, this));
	}

	@Override
	public void outAAbstractPropertyDeclaration(AAbstractPropertyDeclaration node) {
		super.outAAbstractPropertyDeclaration(node);
		popIRNode();
	}

	@Override
	public void inAExpressionPropertyDeclaration(AExpressionPropertyDeclaration node) {
		super.inAExpressionPropertyDeclaration(node);
		pushIRNode(new IRExpressionPropertyNode(node, this));
	}

	@Override
	public void outAExpressionPropertyDeclaration(AExpressionPropertyDeclaration node) {
		super.outAExpressionPropertyDeclaration(node);
		popIRNode();
	}

	@Override
	public void inAFlexoBehaviourDeclarationConceptBodyDeclaration(AFlexoBehaviourDeclarationConceptBodyDeclaration node) {
		super.inAFlexoBehaviourDeclarationConceptBodyDeclaration(node);
		pushIRNode(new IRFlexoBehaviourNode(node, this));
	}

	@Override
	public void outAFlexoBehaviourDeclarationConceptBodyDeclaration(AFlexoBehaviourDeclarationConceptBodyDeclaration node) {
		super.outAFlexoBehaviourDeclarationConceptBodyDeclaration(node);
		popIRNode();
	}

}
