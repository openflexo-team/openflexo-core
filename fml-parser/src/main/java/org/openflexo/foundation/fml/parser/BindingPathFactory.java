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

import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.BindingValue.AbstractBindingPathElement;
import org.openflexo.connie.expr.BindingValue.MethodCallBindingPathElement;
import org.openflexo.connie.expr.BindingValue.NewInstanceBindingPathElement;
import org.openflexo.connie.expr.BindingValue.NormalBindingPathElement;
import org.openflexo.connie.expr.BindingValue.StaticMethodCallBindingPathElement;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.expr.NewFlexoConceptInstanceBindingPathElement;
import org.openflexo.foundation.fml.expr.NewVirtualModelInstanceBindingPathElement;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AbstractBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddClassInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddFlexoConceptInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddVirtualModelInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingPathNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NormalBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.StaticMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.node.AClassMethodMethodInvocation;
import org.openflexo.foundation.fml.parser.node.ACompositeIdent;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFieldPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AFmlActionExpressionStatementExpression;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.AMethodPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewContainmentClause;
import org.openflexo.foundation.fml.parser.node.ANewInstancePrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewInstanceStatementExpression;
import org.openflexo.foundation.fml.parser.node.APrimaryFieldAccess;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;
import org.openflexo.foundation.fml.parser.node.APrimaryNoIdPrimary;
import org.openflexo.foundation.fml.parser.node.AReferenceSuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.ASuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASuperMethodInvocation;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PFieldAccess;
import org.openflexo.foundation.fml.parser.node.PIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.PLeftHandSide;
import org.openflexo.foundation.fml.parser.node.PMethodInvocation;
import org.openflexo.foundation.fml.parser.node.PNewContainmentClause;
import org.openflexo.foundation.fml.parser.node.PNewInstance;
import org.openflexo.foundation.fml.parser.node.PPrimary;
import org.openflexo.foundation.fml.parser.node.PPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.PStatementExpression;
import org.openflexo.foundation.fml.parser.node.TKwSuper;
import org.openflexo.foundation.fml.parser.node.TLidentifier;

/**
 * This class implements the semantics analyzer for a parsed {@link BindingValue}<br>
 * Its main purpose is to structurally build a binding from a parsed AST<br>
 * 
 * The goal is here to linearize the AST to obtain a chain of {@link BindingPathElement}
 * 
 * @author sylvain
 * 
 */
public class BindingPathFactory {

	private final ExpressionFactory expressionFactory;
	private final List<AbstractBindingPathElement> path;
	private final List<AbstractBindingPathElementNode<?, ?>> nodesPath;
	private final Node rootNode;

	public static List<AbstractBindingPathElement> makeBindingPath(Node node, ExpressionFactory expressionAnalyzer,
			BindingPathNode bindingPathNode) {

		BindingPathFactory bindingPathFactory = new BindingPathFactory(node, expressionAnalyzer);
		bindingPathFactory.explore();

		if (bindingPathNode != null) {
			for (AbstractBindingPathElementNode<?, ?> pathElementNode : bindingPathFactory.nodesPath) {
				bindingPathNode.addToChildren(pathElementNode);
			}
		}

		return bindingPathFactory.path;
	}

	private BindingPathFactory(Node node, ExpressionFactory expressionAnalyzer) {
		this.expressionFactory = expressionAnalyzer;
		this.rootNode = node;
		path = new ArrayList<>();
		nodesPath = new ArrayList<>();
	}

	private void explore() {

		// System.out.println("Analyzing path " + rootNode);
		// ASTDebugger.debug(rootNode);

		if (rootNode instanceof PPrimaryNoId) {
			appendBindingPath((PPrimaryNoId) rootNode);
		}
		if (rootNode instanceof PPrimary) {
			appendBindingPath((PPrimary) rootNode);
		}
		if (rootNode instanceof PLeftHandSide) {
			appendBindingPath((PLeftHandSide) rootNode);
		}
		if (rootNode instanceof PStatementExpression) {
			appendBindingPath((PStatementExpression) rootNode);
		}
	}

	private AbstractBindingPathElementNode<?, ?> popBindingPath() {
		path.remove(path.size() - 1);
		if (nodesPath.size() > 0) {
			return nodesPath.remove(nodesPath.size() - 1);
		}
		return null;
	}

	private void appendBindingPath(PPrimaryNoId node) {
		if (node instanceof AFieldPrimaryNoId) {
			appendBindingPath(((AFieldPrimaryNoId) node).getFieldAccess());
		}
		else if (node instanceof AMethodPrimaryNoId) {
			appendBindingPath(((AMethodPrimaryNoId) node).getMethodInvocation());
		}
		else if (node instanceof ANewInstancePrimaryNoId) {
			appendBindingPath(((ANewInstancePrimaryNoId) node).getNewInstance());
		}
	}

	private void appendBindingPath(PFieldAccess node) {
		if (node instanceof APrimaryFieldAccess) {
			appendBindingPath(((APrimaryFieldAccess) node).getPrimaryNoId());
			appendBindingPath(((APrimaryFieldAccess) node).getLidentifier());
		}
		else if (node instanceof AReferenceSuperFieldAccess) {
			appendBindingPath(((AReferenceSuperFieldAccess) node).getIdentifier1());
			appendBindingPath(((AReferenceSuperFieldAccess) node).getKwSuper());
			appendBindingPath(((AReferenceSuperFieldAccess) node).getIdentifier2());
		}
		else if (node instanceof ASuperFieldAccess) {
			appendBindingPath(((ASuperFieldAccess) node).getKwSuper());
			appendBindingPath(((ASuperFieldAccess) node).getLidentifier());
		}
	}

	private void appendBindingPath(PPrimary node) {
		if (node instanceof AIdentifierPrimary) {
			appendBindingPath(((AIdentifierPrimary) node).getCompositeIdent());
		}
		else if (node instanceof APrimaryNoIdPrimary) {
			appendBindingPath(((APrimaryNoIdPrimary) node).getPrimaryNoId());
		}
	}

	private void appendBindingPath(PLeftHandSide node) {
		if (node instanceof AFieldLeftHandSide) {
			appendBindingPath(((AFieldLeftHandSide) node).getFieldAccess());
		}
		else if (node instanceof AIdentifierLeftHandSide) {
			appendBindingPath(((AIdentifierLeftHandSide) node).getCompositeIdent());
		}
	}

	private void appendBindingPath(PStatementExpression node) {
		if (node instanceof AMethodInvocationStatementExpression) {
			appendBindingPath(((AMethodInvocationStatementExpression) node).getMethodInvocation());
		}
		else if (node instanceof ANewInstanceStatementExpression) {
			appendBindingPath(((ANewInstanceStatementExpression) node).getNewInstance());
		}
		else if (node instanceof AFmlActionExpressionStatementExpression) {
			// TODO
		}
	}

	private void appendBindingPath(PMethodInvocation node) {
		if (node instanceof APrimaryMethodInvocation) {
			appendBindingPath(((APrimaryMethodInvocation) node).getPrimary());
			NormalBindingPathElementNode lastElementNode = (NormalBindingPathElementNode) popBindingPath();
			appendMethodInvocation((APrimaryMethodInvocation) node, lastElementNode);
		}
		else if (node instanceof ASuperMethodInvocation) {
			appendSuperMethodInvocation((ASuperMethodInvocation) node);
		}
		else if (node instanceof AClassMethodMethodInvocation) {
			appendClassMethodInvocation((AClassMethodMethodInvocation) node);
		}
	}

	private void appendBindingPath(PNewInstance node) {
		if (node instanceof ASimpleNewInstance) {
			appendSimpleNewInstanceInvocation((ASimpleNewInstance) node);
		}
		else if (node instanceof AFullQualifiedNewInstance) {
			// TODO: Do the stuff for full qualified new instance
		}
	}

	private void appendBindingPath(PNewContainmentClause node) {
		if (node instanceof ANewContainmentClause) {
			appendBindingPath(((ANewContainmentClause) node).getCompositeIdent());
		}
	}

	private void appendBindingPath(PCompositeIdent node) {
		if (node instanceof ACompositeIdent) {
			for (PIdentifierPrefix pIdentifierPrefix : ((ACompositeIdent) node).getPrefixes()) {
				appendBindingPath(pIdentifierPrefix);
			}
			appendBindingPath(((ACompositeIdent) node).getIdentifier());
		}
	}

	private void appendBindingPath(PIdentifierPrefix node) {
		if (node instanceof AIdentifierPrefix) {
			appendBindingPath(((AIdentifierPrefix) node).getLidentifier());
		}
	}

	private void appendBindingPath(TLidentifier node) {
		NormalBindingPathElementNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
				n -> new NormalBindingPathElementNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		NormalBindingPathElement pathElement = pathElementNode.buildModelObjectFromAST(node);
		path.add(pathElement);
	}

	private void appendBindingPath(TKwSuper node) {
		SuperBindingPathElementNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
				n -> new SuperBindingPathElementNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		NormalBindingPathElement superElement = pathElementNode.buildModelObjectFromAST(node);
		path.add(superElement);
	}

	private void appendMethodInvocation(APrimaryMethodInvocation node, NormalBindingPathElementNode lastPathElementNode) {
		MethodCallBindingPathElementNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
				n -> new MethodCallBindingPathElementNode(n, lastPathElementNode.getASTNode(), expressionFactory.getMainAnalyzer(),
						expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		MethodCallBindingPathElement methodCallElement = pathElementNode.getModelObject();
		path.add(methodCallElement);
	}

	private void appendSuperMethodInvocation(ASuperMethodInvocation node) {
		SuperMethodCallBindingPathElementNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
				n -> new SuperMethodCallBindingPathElementNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		MethodCallBindingPathElement methodCallElement = pathElementNode.buildModelObjectFromAST(node);
		path.add(methodCallElement);
	}

	private void appendSimpleNewInstanceInvocation(ASimpleNewInstance node) {

		appendBindingPath(node.getNewContainmentClause());

		Type type = TypeFactory.makeType(node.getType(), expressionFactory.getTypingSpace());
		// System.out.println("Found type " + type);

		if (type instanceof VirtualModelInstanceType
		// this is too early, FlexoConcept is not yet set
		/*&& ((VirtualModelInstanceType) type).getVirtualModel().getCreationSchemes().size() == 1*/) {
			// Simple new instance with non ambigous creation scheme
			AddVirtualModelInstanceNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
					n -> new AddVirtualModelInstanceNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			NewVirtualModelInstanceBindingPathElement pathElement = pathElementNode.getModelObject();
			path.add(pathElement);
		}
		else if (type instanceof FlexoConceptInstanceType
		// this is too early, FlexoConcept is not yet set
		/*&& ((FlexoConceptInstanceType) type).getFlexoConcept().getCreationSchemes().size() == 1*/) {
			// Simple new instance with non ambigous creation scheme
			AddFlexoConceptInstanceNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
					n -> new AddFlexoConceptInstanceNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			NewFlexoConceptInstanceBindingPathElement pathElement = pathElementNode.getModelObject();
			path.add(pathElement);
		}
		else {
			AddClassInstanceNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
					n -> new AddClassInstanceNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			NewInstanceBindingPathElement pathElement = pathElementNode.getModelObject();
			path.add(pathElement);

		}

	}

	private void appendClassMethodInvocation(AClassMethodMethodInvocation node) {
		StaticMethodCallBindingPathElementNode pathElementNode = expressionFactory.getMainAnalyzer().retrieveFMLNode(node,
				n -> new StaticMethodCallBindingPathElementNode(n, expressionFactory.getMainAnalyzer(), expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		StaticMethodCallBindingPathElement methodCallElement = pathElementNode.getModelObject();
		path.add(methodCallElement);
	}

}
