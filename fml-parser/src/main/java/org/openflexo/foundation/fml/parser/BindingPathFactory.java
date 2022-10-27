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

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimpleMethodPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.binding.StaticMethodPathElement;
import org.openflexo.connie.binding.javareflect.JavaNewInstanceMethodPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AbstractBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddClassInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddFlexoConceptInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AddVirtualModelInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingPathNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingVariableNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SimplePathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.StaticMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.node.ACidentifierUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.AClassMethodMethodInvocation;
import org.openflexo.foundation.fml.parser.node.AConstantCompositeIdent;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFieldPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AFmlActionExpressionStatementExpression;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.ALidentifierUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.AMethodPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewContainmentClause;
import org.openflexo.foundation.fml.parser.node.ANewInstancePrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewInstanceStatementExpression;
import org.openflexo.foundation.fml.parser.node.ANormalCompositeIdent;
import org.openflexo.foundation.fml.parser.node.APrimaryFieldAccess;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;
import org.openflexo.foundation.fml.parser.node.APrimaryNoIdPrimary;
import org.openflexo.foundation.fml.parser.node.AReferenceSuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.ASuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASuperMethodInvocation;
import org.openflexo.foundation.fml.parser.node.AUidentifierUriExpressionPrimary;
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
import org.openflexo.foundation.fml.parser.node.TCidentifier;
import org.openflexo.foundation.fml.parser.node.TKwSuper;
import org.openflexo.foundation.fml.parser.node.TLidentifier;
import org.openflexo.foundation.fml.parser.node.TUidentifier;
import org.openflexo.foundation.fml.parser.node.Token;

/**
 * This class implements the semantics analyzer for a parsed {@link BindingPath}<br>
 * Its main purpose is to structurally build a binding from a parsed AST<br>
 * 
 * The goal is here to linearize the AST to obtain a chain of {@link BindingPathElement}
 * 
 * @author sylvain
 * 
 */
public class BindingPathFactory {

	private final AbstractExpressionFactory expressionFactory;

	private BindingVariable bindingVariable;
	private List<BindingPathElement> bindingPathElements;

	private final List<AbstractBindingPathElementNode<?, ?>> nodesPath;
	private final Node rootNode;

	public static BindingPath makeBindingPath(Node node, AbstractExpressionFactory expressionFactory, BindingPathNode bindingPathNode) {

		BindingPathFactory bindingPathFactory = new BindingPathFactory(node, expressionFactory);

		// System.out.println("--------------> Make BindingPath for " + node);
		// ASTDebugger.debug(node);

		bindingPathFactory.explore();

		if (bindingPathNode != null) {
			for (AbstractBindingPathElementNode<?, ?> pathElementNode : bindingPathFactory.nodesPath) {
				bindingPathNode.addToChildren(pathElementNode);
			}
		}

		// System.out.println("<-------------- Done BindingPath for " + node);
		// System.out.println("bindingVariable=" + bindingPathFactory.bindingVariable);
		// System.out.println("bindingPathElements=" + bindingPathFactory.bindingPathElements);

		// return bindingPathFactory.path;
		return new BindingPath(bindingPathFactory.bindingVariable, bindingPathFactory.bindingPathElements,
				bindingPathFactory.getBindable(), FMLPrettyPrinter.getInstance());
	}

	private BindingPathFactory(Node node, AbstractExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
		this.rootNode = node;
		bindingPathElements = new ArrayList<>();
		nodesPath = new ArrayList<>();
	}

	public Bindable getBindable() {
		return expressionFactory.getBindable();
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
		if (rootNode instanceof AUidentifierUriExpressionPrimary) {
			appendBindingPath(((AUidentifierUriExpressionPrimary) rootNode).getUidentifier());
		}
		if (rootNode instanceof ALidentifierUriExpressionPrimary) {
			appendBindingPath(((ALidentifierUriExpressionPrimary) rootNode).getLidentifier());
		}
		if (rootNode instanceof ACidentifierUriExpressionPrimary) {
			appendBindingPath(((ACidentifierUriExpressionPrimary) rootNode).getCidentifier());
		}
	}

	private AbstractBindingPathElementNode<?, ?> popBindingPath() {
		if (bindingPathElements.size() > 0) {
			bindingPathElements.remove(bindingPathElements.size() - 1);
		}
		else {
			bindingVariable = null;
		}
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
			AbstractBindingPathElementNode<?, ?> lastElementNode = (AbstractBindingPathElementNode<?, ?>) popBindingPath();
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
			appendFullQualifiedNewInstanceInvocation((AFullQualifiedNewInstance) node);
		}
	}

	private void appendBindingPath(PNewContainmentClause node) {
		if (node instanceof ANewContainmentClause) {
			appendBindingPath(((ANewContainmentClause) node).getCompositeIdent());
		}
	}

	private void appendBindingPath(PCompositeIdent node) {
		if (node instanceof ANormalCompositeIdent) {
			for (PIdentifierPrefix pIdentifierPrefix : ((ANormalCompositeIdent) node).getPrefixes()) {
				appendBindingPath(pIdentifierPrefix);
			}
			appendBindingPath(((ANormalCompositeIdent) node).getIdentifier());
		}
		if (node instanceof AConstantCompositeIdent) {
			appendBindingPath(((AConstantCompositeIdent) node).getIdentifier());
		}
	}

	private void appendBindingPath(PIdentifierPrefix node) {
		if (node instanceof AIdentifierPrefix) {
			appendBindingPath(((AIdentifierPrefix) node).getLidentifier());
		}
	}

	private void appendBindingPath(TLidentifier node) {
		makeNormalBindingPathElement(node);
	}

	private void appendBindingPath(TUidentifier node) {
		makeNormalBindingPathElement(node);
	}

	private void appendBindingPath(TCidentifier node) {
		makeNormalBindingPathElement(node);
	}

	private void appendBindingPath(TKwSuper node) {
		makeNormalBindingPathElement(node);
	}

	private IBindingPathElement retrieveActualParent() {
		if (bindingPathElements.size() == 0) {
			return bindingVariable;
		}
		else {
			return bindingPathElements.get(bindingPathElements.size() - 1);
		}
	}

	private IBindingPathElement makeNormalBindingPathElement(Token node) {
		if (bindingVariable == null && bindingPathElements.size() == 0) {
			BindingVariableNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new BindingVariableNode(n, expressionFactory, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			// bindingVariable = pathElementNode.buildModelObjectFromAST(node);
			bindingVariable = pathElementNode.getModelObject();
			// path.add(pathElement);

			/*if (getBindable().getBindingModel() != null) {
				bindingVariable = getBindable().getBindingModel().bindingVariableNamed(identifier);
			}
			if (bindingVariable == null) {
				// Unresolved
				bindingVariable = new UnresolvedBindingVariable(identifier);
			}*/
			// System.out.println(" > BV: " + bindingVariable);
			return bindingVariable;
		}
		else {
			final IBindingPathElement parent = retrieveActualParent();
			SimplePathElementNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new SimplePathElementNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			// SimplePathElement<?> pathElement = pathElementNode.buildModelObjectFromAST(node);
			SimplePathElement<?> pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
			return pathElement;
		}
	}

	private void appendMethodInvocation(APrimaryMethodInvocation node, AbstractBindingPathElementNode<?, ?> lastPathElementNode) {
		final IBindingPathElement parent = retrieveActualParent();
		MethodCallBindingPathElementNode pathElementNode = expressionFactory.retrieveFMLNode(node,
				n -> new MethodCallBindingPathElementNode(n, lastPathElementNode.getASTNode(), expressionFactory, parent,
						expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		SimpleMethodPathElement<?> methodCallElement = pathElementNode.getModelObject();
		bindingPathElements.add(methodCallElement);
	}

	private void appendSuperMethodInvocation(ASuperMethodInvocation node) {
		final IBindingPathElement parent = retrieveActualParent();
		SuperMethodCallBindingPathElementNode pathElementNode = expressionFactory.retrieveFMLNode(node,
				n -> new SuperMethodCallBindingPathElementNode(n, expressionFactory, parent, expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		SimpleMethodPathElement<?> methodCallElement = pathElementNode.getModelObject();
		bindingPathElements.add(methodCallElement);
	}

	private void appendSimpleNewInstanceInvocation(ASimpleNewInstance node) {

		appendBindingPath(node.getNewContainmentClause());

		Type type = TypeFactory.makeType(node.getType(), expressionFactory.getTypingSpace());
		// System.out.println("Found type " + type);

		final IBindingPathElement parent = retrieveActualParent();

		if (type instanceof VirtualModelInstanceType) {
			AddVirtualModelInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new AddVirtualModelInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		else if (type instanceof FlexoConceptInstanceType) {
			AddFlexoConceptInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new AddFlexoConceptInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		else {
			AddClassInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new AddClassInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			JavaNewInstanceMethodPathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);

		}

	}

	private void appendFullQualifiedNewInstanceInvocation(AFullQualifiedNewInstance node) {

		appendBindingPath(node.getNewContainmentClause());

		Type type = TypeFactory.makeType(node.getConceptName(), expressionFactory.getTypingSpace());
		// System.out.println("Found type " + type + " of " + type.getClass());

		final IBindingPathElement parent = retrieveActualParent();

		if (type instanceof VirtualModelInstanceType) {
			AddVirtualModelInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new AddVirtualModelInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		else if (type instanceof FlexoConceptInstanceType) {
			AddFlexoConceptInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new AddFlexoConceptInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		/*else {
			AddClassInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new AddClassInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			JavaNewInstanceMethodPathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		
		}*/

	}

	private void appendClassMethodInvocation(AClassMethodMethodInvocation node) {
		StaticMethodCallBindingPathElementNode pathElementNode = expressionFactory.retrieveFMLNode(node,
				n -> new StaticMethodCallBindingPathElementNode(n, expressionFactory, expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		StaticMethodPathElement<?> methodCallElement = pathElementNode.getModelObject();
		bindingPathElements.add(methodCallElement);
	}

}
