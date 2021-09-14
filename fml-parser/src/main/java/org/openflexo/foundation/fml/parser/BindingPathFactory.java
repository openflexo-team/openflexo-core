/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.BindingValue.AbstractBindingPathElement;
import org.openflexo.connie.expr.BindingValue.MethodCallBindingPathElement;
import org.openflexo.connie.expr.BindingValue.NewInstanceBindingPathElement;
import org.openflexo.connie.expr.BindingValue.NormalBindingPathElement;
import org.openflexo.connie.expr.BindingValue.StaticMethodCallBindingPathElement;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.java.expr.JavaPrettyPrinter;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.expr.NewVirtualModelInstanceBindingPathElement;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddClassInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingPathNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NormalBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.node.AClassMethodMethodInvocation;
import org.openflexo.foundation.fml.parser.node.AComplexType;
import org.openflexo.foundation.fml.parser.node.ACompositeIdent;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFieldPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.AManyArgumentList;
import org.openflexo.foundation.fml.parser.node.AMethodPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewContainmentClause;
import org.openflexo.foundation.fml.parser.node.ANewInstancePrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AOneArgumentList;
import org.openflexo.foundation.fml.parser.node.APrimaryFieldAccess;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;
import org.openflexo.foundation.fml.parser.node.AReferenceSuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.ASuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASuperMethodInvocation;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PArgumentList;
import org.openflexo.foundation.fml.parser.node.PExpression;

/**
 * This class implements the semantics analyzer for a parsed {@link BindingValue}<br>
 * Its main purpose is to structurally build a binding from a parsed AST<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
class BindingPathFactory extends DepthFirstAdapter {

	private final ExpressionFactory expressionAnalyzer;
	private final List<AbstractBindingPathElement> path;
	private final Node rootNode;

	private int depth = -1;

	int ident = 0;

	private boolean weAreDealingWithTheRightBindingPath() {
		return depth == 0;
	}

	private Stack<Boolean> isInsideTypeStack;

	private BindingPathNode bindingPathNode = null;

	public static BindingValue makeBindingPath(Node node, ExpressionFactory expressionAnalyzer, DataBindingNode parentNode,
			boolean appendBindingPathNode) {

		BindingPathNode bindingPathNode = null;

		if (expressionAnalyzer.getAnalyzer() != null && parentNode != null && appendBindingPathNode) {
			bindingPathNode = expressionAnalyzer.getAnalyzer().registerFMLNode(node,
					new BindingPathNode(node, expressionAnalyzer.getAnalyzer()));
			// Otherwise, we throw a ClassCastException if same node support also DataBindingNode
			// bindingPathNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node,
			// n -> new BindingPathNode(n, expressionAnalyzer.getAnalyzer()));
			parentNode.addToChildren(bindingPathNode);
		}

		List<AbstractBindingPathElement> bindingPath = makeBindingPath(node, expressionAnalyzer, bindingPathNode);

		BindingValue returned = new BindingValue(bindingPath, expressionAnalyzer.getBindable(), JavaPrettyPrinter.getInstance());
		if (bindingPathNode != null) {
			bindingPathNode.setModelObject(returned);
		}
		return returned;
	}

	private static List<AbstractBindingPathElement> makeBindingPath(Node node, ExpressionFactory expressionAnalyzer,
			BindingPathNode bindingPathNode) {

		BindingPathFactory bsa = new BindingPathFactory(node, expressionAnalyzer, bindingPathNode);
		node.apply(bsa);

		return bsa.getPath();
	}

	private BindingPathFactory(Node node, ExpressionFactory expressionAnalyzer, BindingPathNode bindingPathNode) {
		this.expressionAnalyzer = expressionAnalyzer;
		this.rootNode = node;
		this.bindingPathNode = bindingPathNode;
		path = new ArrayList<>();
		isInsideTypeStack = new Stack<>();
		isInsideTypeStack.push(new Boolean(false));
	}

	// boolean DEBUG = false;

	public Node getRootNode() {
		return rootNode;
	}

	private List<BindingValue.AbstractBindingPathElement> getPath() {
		return path;
	};

	@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		ident++;
		// System.out.println(StringUtils.buildWhiteSpaceIndentation(ident) + " > " + node.getClass().getSimpleName() + " " + node);
	}

	@Override
	public void defaultOut(Node node) {
		// TODO Auto-generated method stub
		super.defaultOut(node);
		ident--;
	}

	@Override
	public void inAIdentifierLeftHandSide(AIdentifierLeftHandSide node) {
		super.inAIdentifierLeftHandSide(node);
		depth++;
	}

	@Override
	public void outAIdentifierLeftHandSide(AIdentifierLeftHandSide node) {
		super.outAIdentifierLeftHandSide(node);
		depth--;
	}

	@Override
	public void inAFieldLeftHandSide(AFieldLeftHandSide node) {
		super.inAFieldLeftHandSide(node);
		depth++;
	}

	@Override
	public void outAFieldLeftHandSide(AFieldLeftHandSide node) {
		super.outAFieldLeftHandSide(node);
		depth--;
	}

	@Override
	public void inAIdentifierPrimary(AIdentifierPrimary node) {
		super.inAIdentifierPrimary(node);
		depth++;
	}

	@Override
	public void outAIdentifierPrimary(AIdentifierPrimary node) {
		super.outAIdentifierPrimary(node);
		depth--;
	}

	@Override
	public void inAMethodPrimaryNoId(AMethodPrimaryNoId node) {
		super.inAMethodPrimaryNoId(node);
		depth++;
	}

	@Override
	public void outAMethodPrimaryNoId(AMethodPrimaryNoId node) {
		super.outAMethodPrimaryNoId(node);
		depth--;
	}

	@Override
	public void inAFieldPrimaryNoId(AFieldPrimaryNoId node) {
		super.inAFieldPrimaryNoId(node);
		depth++;
	}

	@Override
	public void outAFieldPrimaryNoId(AFieldPrimaryNoId node) {
		super.outAFieldPrimaryNoId(node);
		depth--;
	}

	// When we enter in a type, increase level
	@Override
	public void inAReferenceType(AReferenceType node) {
		super.inAReferenceType(node);
		depth++;
	}

	// When we exit out a type, decrease level
	@Override
	public void outAReferenceType(AReferenceType node) {
		super.outAReferenceType(node);
		depth--;
	}

	@Override
	public void inANewInstancePrimaryNoId(ANewInstancePrimaryNoId node) {
		super.inANewInstancePrimaryNoId(node);
		depth++;
	}

	@Override
	public void outANewInstancePrimaryNoId(ANewInstancePrimaryNoId node) {
		super.outANewInstancePrimaryNoId(node);
		depth--;
	}

	@Override
	public void inAComplexType(AComplexType node) {
		super.inAComplexType(node);
		isInsideTypeStack.push(new Boolean(true));
	}

	@Override
	public void outAComplexType(AComplexType node) {
		super.outAComplexType(node);
		isInsideTypeStack.pop();
	}

	@Override
	public void outAIdentifierPrefix(AIdentifierPrefix node) {
		super.outAIdentifierPrefix(node);
		// if (DEBUG)
		// System.out.println("outAIdentifierPrefix " + node.getLidentifier().getText());
		if (weAreDealingWithTheRightBindingPath() && !isInsideTypeStack.peek()) {
			NormalBindingPathElement pathElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				NormalBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getLidentifier(),
						n -> new NormalBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				pathElement = pathElementNode.buildModelObjectFromAST(node.getLidentifier());
			}
			else {
				pathElement = new NormalBindingPathElement(node.getLidentifier().getText());
			}
			path.add(pathElement);
		}
	}

	@Override
	public void outACompositeIdent(ACompositeIdent node) {
		super.outACompositeIdent(node);
		// if (DEBUG)
		// System.out.println("outACompositeIdent " + node.getIdentifier().getText());
		if (weAreDealingWithTheRightBindingPath() && !isInsideTypeStack.peek()) {
			NormalBindingPathElement pathElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				NormalBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getIdentifier(),
						n -> new NormalBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				pathElement = pathElementNode.buildModelObjectFromAST(node.getIdentifier());
			}
			else {
				pathElement = new NormalBindingPathElement(node.getIdentifier().getText());
			}
			path.add(pathElement);
		}
	}

	@Override
	public void outAPrimaryFieldAccess(APrimaryFieldAccess node) {
		super.outAPrimaryFieldAccess(node);
		// if (DEBUG)
		// System.out.println("outAPrimaryFieldAccess " + node + " rightbinding=" + weAreDealingWithTheRightBinding());
		if (weAreDealingWithTheRightBindingPath()) {
			List<AbstractBindingPathElement> bindingPath = makeBindingPath(node.getPrimaryNoId(), expressionAnalyzer, null);
			for (int i = 0; i < bindingPath.size(); i++) {
				path.add(bindingPath.get(i));
			}
			NormalBindingPathElement pathElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				NormalBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getLidentifier(),
						n -> new NormalBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				pathElement = pathElementNode.buildModelObjectFromAST(node.getLidentifier());
			}
			else {
				pathElement = new NormalBindingPathElement(node.getLidentifier().getText());
			}
			path.add(pathElement);
		}
	}

	@Override
	public void outASuperFieldAccess(ASuperFieldAccess node) {
		super.outASuperFieldAccess(node);
		if (weAreDealingWithTheRightBindingPath()) {
			NormalBindingPathElement superElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				SuperBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getKwSuper(),
						n -> new SuperBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				superElement = pathElementNode.buildModelObjectFromAST(node.getKwSuper());
			}
			else {
				superElement = new NormalBindingPathElement("super");
			}
			path.add(superElement);
			NormalBindingPathElement pathElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				NormalBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getLidentifier(),
						n -> new NormalBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				pathElement = pathElementNode.buildModelObjectFromAST(node.getLidentifier());
			}
			else {
				pathElement = new NormalBindingPathElement(node.getLidentifier().getText());
			}
			path.add(pathElement);
		}
	}

	@Override
	public void outAReferenceSuperFieldAccess(AReferenceSuperFieldAccess node) {
		// TODO Auto-generated method stub
		super.outAReferenceSuperFieldAccess(node);

		if (weAreDealingWithTheRightBindingPath()) {
			List<AbstractBindingPathElement> bindingPath = makeBindingPath(node.getIdentifier1(), expressionAnalyzer, null);
			for (int i = 0; i < bindingPath.size(); i++) {
				path.add(bindingPath.get(i));
			}
			NormalBindingPathElement superElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				SuperBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getKwSuper(),
						n -> new SuperBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				superElement = pathElementNode.buildModelObjectFromAST(node.getKwSuper());
			}
			else {
				superElement = new NormalBindingPathElement("super");
			}
			path.add(superElement);
			NormalBindingPathElement pathElement;
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				NormalBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node.getIdentifier2(),
						n -> new NormalBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
				pathElement = pathElementNode.buildModelObjectFromAST(node.getIdentifier2());
			}
			else {
				pathElement = new NormalBindingPathElement(node.getIdentifier2().getText());
			}
			path.add(pathElement);
		}
	}

	@Override
	public void outAPrimaryMethodInvocation(APrimaryMethodInvocation node) {
		super.outAPrimaryMethodInvocation(node);
		if (weAreDealingWithTheRightBindingPath()) {

			List<AbstractBindingPathElement> bindingPath = makeBindingPath(node.getPrimary(), expressionAnalyzer, null);
			for (int i = 0; i < bindingPath.size() - 1; i++) {
				path.add(bindingPath.get(i));
			}

			NormalBindingPathElement pathElement = (NormalBindingPathElement) bindingPath.get(bindingPath.size() - 1);
			String identifier = pathElement.property;
			MethodCallBindingPathElement returned = new MethodCallBindingPathElement(identifier, makeArgs(node.getArgumentList()));
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				MethodCallBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node,
						n -> new MethodCallBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
			}
			path.add(returned);

		}
	}

	@Override
	public void outASuperMethodInvocation(ASuperMethodInvocation node) {
		super.outASuperMethodInvocation(node);
		if (weAreDealingWithTheRightBindingPath()) {
			MethodCallBindingPathElement returned = new MethodCallBindingPathElement("super", makeArgs(node.getArgumentList()));
			if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
				SuperMethodCallBindingPathElementNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node,
						n -> new SuperMethodCallBindingPathElementNode(n, expressionAnalyzer.getAnalyzer()));
				bindingPathNode.addToChildren(pathElementNode);
			}
			path.add(returned);
		}
	}

	@Override
	public void outASimpleNewInstance(ASimpleNewInstance node) {
		super.outASimpleNewInstance(node);
		if (weAreDealingWithTheRightBindingPath()) {
			if (node.getNewContainmentClause() instanceof ANewContainmentClause) {
				List<AbstractBindingPathElement> bindingPath = makeBindingPath(
						((ANewContainmentClause) node.getNewContainmentClause()).getCompositeIdent(), expressionAnalyzer, null);
				for (int i = 0; i < bindingPath.size(); i++) {
					path.add(bindingPath.get(i));
				}
				// System.out.println("le containment: " + path);
			}
			Type type = TypeFactory.makeType(node.getType(), expressionAnalyzer.getTypingSpace());

			if (type instanceof VirtualModelInstanceType
					&& ((VirtualModelInstanceType) type).getVirtualModel().getCreationSchemes().size() == 1) {
				// Simple new instance with non ambigous creation scheme
				NewVirtualModelInstanceBindingPathElement returned = new NewVirtualModelInstanceBindingPathElement(
						(VirtualModelInstanceType) type, null, // default constructor,
						makeArgs(node.getArgumentList()));
				path.add(returned);
			}
			else if (type instanceof FlexoConceptInstanceType
					&& ((FlexoConceptInstanceType) type).getFlexoConcept().getCreationSchemes().size() == 1) {
				// Simple new instance with non ambigous creation scheme
				NewVirtualModelInstanceBindingPathElement returned = new NewVirtualModelInstanceBindingPathElement(
						(VirtualModelInstanceType) type, null, // default constructor,
						makeArgs(node.getArgumentList()));
				path.add(returned);
			}
			else {
				NewInstanceBindingPathElement pathElement;
				if (expressionAnalyzer.getAnalyzer() != null && bindingPathNode != null) {
					AddClassInstanceNode pathElementNode = expressionAnalyzer.getAnalyzer().retrieveFMLNode(node,
							n -> new AddClassInstanceNode(n, expressionAnalyzer.getAnalyzer(), expressionAnalyzer.getBindable()));
					bindingPathNode.addToChildren(pathElementNode);
					pathElement = pathElementNode.buildModelObjectFromAST(node);
				}
				else {
					pathElement = new NewInstanceBindingPathElement(type, null, // a java constructor has no name,
							makeArgs(node.getArgumentList()));
				}
				path.add(pathElement);

			}

			// System.out.println("Type: " + type);
			NewInstanceBindingPathElement returned = new NewInstanceBindingPathElement(type, null, // a java constructor has no name,
					makeArgs(node.getArgumentList()));
			path.add(returned);
			// System.out.println("le path: " + path);
		}
	}

	@Override
	public void outAFullQualifiedNewInstance(AFullQualifiedNewInstance node) {
		super.outAFullQualifiedNewInstance(node);
		if (weAreDealingWithTheRightBindingPath()) {
			if (node.getNewContainmentClause() instanceof ANewContainmentClause) {
				List<AbstractBindingPathElement> bindingPath = makeBindingPath(
						((ANewContainmentClause) node.getNewContainmentClause()).getCompositeIdent(), expressionAnalyzer, null);
				for (int i = 0; i < bindingPath.size(); i++) {
					path.add(bindingPath.get(i));
				}
			}
			Type type = TypeFactory.makeType(node.getConceptName(), expressionAnalyzer.getTypingSpace());
			NewInstanceBindingPathElement returned = new NewInstanceBindingPathElement(type, null, // a java constructor has no name,
					makeArgs(node.getArgumentList()));
			path.add(returned);
		}
	}

	/*	@Override
		public void outABasicJavaInstanceCreationInvokation(ABasicJavaInstanceCreationInvokation node) {
			super.outABasicJavaInstanceCreationInvokation(node);
			if (weAreDealingWithTheRightBinding()) {
				Type type = TypeAnalyzer.makeType(node.getType(), expressionAnalyzer);
				NewInstanceBindingPathElement returned = new NewInstanceBindingPathElement(type, null, // a java constructor has no name,
						makeArgs(node.getArgumentList()));
				path.add(returned);
			}
		}
	
		@Override
		public void outAInnerJavaInstanceCreationInvokation(AInnerJavaInstanceCreationInvokation node) {
			super.outAInnerJavaInstanceCreationInvokation(node);
			if (weAreDealingWithTheRightBinding()) {
				List<AbstractBindingPathElement> bindingPath = makeBindingPath(node.getPrimary(), expressionAnalyzer);
				for (int i = 0; i < bindingPath.size(); i++) {
					path.add(bindingPath.get(i));
				}
				Type type = TypeAnalyzer.makeType(node.getType(), expressionAnalyzer);
				NewInstanceBindingPathElement returned = new NewInstanceBindingPathElement(type, null, // a java constructor has no name ,
						makeArgs(node.getArgumentList()));
				path.add(returned);
			}
		}*/

	@Override
	public void outAClassMethodMethodInvocation(AClassMethodMethodInvocation node) {
		super.outAClassMethodMethodInvocation(node);
		if (weAreDealingWithTheRightBindingPath()) {
			Type type = TypeFactory.makeType(node.getType(), expressionAnalyzer.getTypingSpace());
			StaticMethodCallBindingPathElement returned = new StaticMethodCallBindingPathElement(type, node.getLidentifier().getText(),
					makeArgs(node.getArgumentList()));
			path.add(returned);
		}
	}

	private List<Expression> makeArgs(PArgumentList argumentList) {
		List<Expression> args = new ArrayList<>();
		if (argumentList == null) {
			// No argument
		}
		else if (argumentList instanceof AOneArgumentList) {
			// One argument
			PExpression pExpression = ((AOneArgumentList) argumentList).getExpression();
			// System.out.println("Tiens je cherche pour " + pExpression + " of " + pExpression.getClass().getSimpleName());
			// System.out.println("Et je tombe sur: " + expressionAnalyzer.getExpression(pExpression) + " of "
			// + expressionAnalyzer.getExpression(pExpression).getClass().getSimpleName());
			args.add(expressionAnalyzer.getExpression(pExpression));
		}
		else if (argumentList instanceof AManyArgumentList) {
			List<PExpression> arguments = makeArguments((AManyArgumentList) argumentList);
			for (PExpression pExpression : arguments) {
				// System.out.println("Tiens je cherche pour " + pExpression + " of " + pExpression.getClass().getSimpleName());
				// System.out.println("Et je tombe sur: " + expressionAnalyzer.getExpression(pExpression) + " of "
				// + expressionAnalyzer.getExpression(pExpression).getClass().getSimpleName());
				args.add(expressionAnalyzer.getExpression(pExpression));
			}
		}
		return args;
	}

	private List<PExpression> makeArguments(AManyArgumentList args) {
		List<PExpression> returned = new ArrayList<>();
		buildArguments(args, returned);
		return returned;
	}

	private void buildArguments(PArgumentList argList, List<PExpression> expressions) {
		if (argList instanceof AOneArgumentList) {
			expressions.add(0, ((AOneArgumentList) argList).getExpression());
		}
		else if (argList instanceof AManyArgumentList) {
			expressions.add(0, ((AManyArgumentList) argList).getExpression());
			buildArguments(((AManyArgumentList) argList).getArgumentList(), expressions);
		}
	}

}
