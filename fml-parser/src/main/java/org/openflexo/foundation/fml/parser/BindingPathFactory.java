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
import java.util.logging.Logger;

import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimpleMethodPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.binding.StaticMethodPathElement;
import org.openflexo.connie.binding.javareflect.JavaNewInstanceMethodPathElement;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.expr.FMLBindingPath;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.AbstractBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NewClassInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NewFlexoConceptInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.NewVirtualModelInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingPathNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.BindingVariableNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.MethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SimplePathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.StaticMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.SuperMethodCallBindingPathElementNode;
import org.openflexo.foundation.fml.parser.node.AConceptAuthorizedKwInCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ACreateAuthorizedKwInCompositeIdent;
import org.openflexo.foundation.fml.parser.node.AEscapedCompositeIdent;
import org.openflexo.foundation.fml.parser.node.AEscapedIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFieldPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AFmlActionExpressionStatementExpression;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.AGetAuthorizedKwInCompositeIdent;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFieldCalleePrimary;
import org.openflexo.foundation.fml.parser.node.AIdentifierCalleePrimary;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.AMethodCalleePrimary;
import org.openflexo.foundation.fml.parser.node.ANewInstanceCalleePrimary;
import org.openflexo.foundation.fml.parser.node.AKwCompositeIdent;
import org.openflexo.foundation.fml.parser.node.AKwFieldAccess;
import org.openflexo.foundation.fml.parser.node.AIdentUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.ALitteralUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.AMethodInvocationStatementExpression;
import org.openflexo.foundation.fml.parser.node.AMethodPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AModelAuthorizedKwInCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ANewContainmentClause;
import org.openflexo.foundation.fml.parser.node.ANewInstancePrimaryNoId;
import org.openflexo.foundation.fml.parser.node.ANewInstanceStatementExpression;
import org.openflexo.foundation.fml.parser.node.ANormalCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ANormalIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.APrimaryFieldAccess;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;
import org.openflexo.foundation.fml.parser.node.APrimaryNoIdPrimary;
import org.openflexo.foundation.fml.parser.node.AReferenceSuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASetAuthorizedKwInCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.ASuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASuperMethodInvocation;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAuthorizedKwInCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PCalleePrimary;
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
import org.openflexo.foundation.fml.parser.node.PUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.TEscapedIdentifier;
import org.openflexo.foundation.fml.parser.node.TIdent;
import org.openflexo.foundation.fml.parser.node.TKwSuper;
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

	private static final Logger logger = Logger.getLogger(BindingPathFactory.class.getPackage().getName());

	private final AbstractExpressionFactory expressionFactory;

	private BindingVariable bindingVariable;
	private List<BindingPathElement> bindingPathElements;

	private final List<AbstractBindingPathElementNode<?, ?>> nodesPath;

	/** The receiver of the first element of the path, when it is a call taken for a static call (see {@link FMLBindingPath}) */
	private List<String> staticCallReceiver;
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
		FMLBindingPath returned = new FMLBindingPath(bindingPathFactory.bindingVariable, bindingPathFactory.bindingPathElements,
				bindingPathFactory.getBindable(), FMLPrettyPrinter.getInstance());
		returned.setStaticCallReceiver(bindingPathFactory.staticCallReceiver);
		return returned;
	}

	public static Token getKwToken(PAuthorizedKwInCompositeIdent node) {
		if (node instanceof AGetAuthorizedKwInCompositeIdent) {
			return ((AGetAuthorizedKwInCompositeIdent) node).getKwGet();
		}
		else if (node instanceof ASetAuthorizedKwInCompositeIdent) {
			return ((ASetAuthorizedKwInCompositeIdent) node).getKwSet();
		}
		else if (node instanceof AModelAuthorizedKwInCompositeIdent) {
			return ((AModelAuthorizedKwInCompositeIdent) node).getKwModel();
		}
		else if (node instanceof AConceptAuthorizedKwInCompositeIdent) {
			return ((AConceptAuthorizedKwInCompositeIdent) node).getKwConcept();
		}
		else if (node instanceof ACreateAuthorizedKwInCompositeIdent) {
			return ((ACreateAuthorizedKwInCompositeIdent) node).getKwCreate();
		}
		return null;
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

	// IMPORTANT : Take care that the rootNode MUST match one of those types
	private void explore() {

		// System.out.println("Analyzing path " + rootNode);
		// ASTDebugger.debug(rootNode);

		if (rootNode instanceof PPrimaryNoId) {
			appendBindingPath((PPrimaryNoId) rootNode);
		}
		else if (rootNode instanceof PPrimary) {
			appendBindingPath((PPrimary) rootNode);
		}
		else if (rootNode instanceof PLeftHandSide) {
			appendBindingPath((PLeftHandSide) rootNode);
		}
		else if (rootNode instanceof PStatementExpression) {
			appendBindingPath((PStatementExpression) rootNode);
		}
		else if (rootNode instanceof PNewInstance) {
			appendBindingPath((PNewInstance) rootNode);
		}
		else if (rootNode instanceof PUriExpressionPrimary) {
			appendBindingPath((PUriExpressionPrimary) rootNode);
		}
		else {
			if (rootNode != null) {
				logger.warning("Unhandled rootNode: " + rootNode + " of " + rootNode.getClass());
			}
			else {
				logger.warning("Unexpected null rootNode");
			}
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
			appendBindingPath(((APrimaryFieldAccess) node).getIdent());
		}
		else if (node instanceof AKwFieldAccess) {
			appendBindingPath(((AKwFieldAccess) node).getPrimaryNoId());
			appendBindingPath(((AKwFieldAccess) node).getAuthorizedKwInCompositeIdent());
		}
		else if (node instanceof AReferenceSuperFieldAccess) {
			appendBindingPath(((AReferenceSuperFieldAccess) node).getIdentifier1());
			appendBindingPath(((AReferenceSuperFieldAccess) node).getKwSuper());
			appendBindingPath(((AReferenceSuperFieldAccess) node).getIdentifier2());
		}
		else if (node instanceof ASuperFieldAccess) {
			appendBindingPath(((ASuperFieldAccess) node).getKwSuper());
			appendBindingPath(((ASuperFieldAccess) node).getIdent());
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
			// 'Foo.bar(x)' is a call on a path: a static call when the receiver names a type (CORE-F-5)
			if (getStaticCallReceiverType((APrimaryMethodInvocation) node) != null) {
				appendStaticMethodInvocation((APrimaryMethodInvocation) node);
				return;
			}
			appendBindingPath(((APrimaryMethodInvocation) node).getCalleePrimary());
			AbstractBindingPathElementNode<?, ?> lastElementNode = popBindingPath();
			boolean escapedSerialization = false;
			if (lastElementNode instanceof SimplePathElementNode) {
				escapedSerialization = ((SimplePathElementNode) lastElementNode).getEscapedSerialization();
			}
			appendMethodInvocation((APrimaryMethodInvocation) node, lastElementNode, escapedSerialization);
		}
		else if (node instanceof ASuperMethodInvocation) {
			appendSuperMethodInvocation((ASuperMethodInvocation) node);
		}
	}

	private void appendBindingPath(PUriExpressionPrimary node) {
		if (node instanceof AIdentUriExpressionPrimary) {
			appendBindingPath(((AIdentUriExpressionPrimary) rootNode).getIdent());
		}
		if (node instanceof ALitteralUriExpressionPrimary) {
			makeNormalBindingPathElement(((ALitteralUriExpressionPrimary) rootNode).getLitString());
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
		if (node instanceof AEscapedCompositeIdent) {
			for (PIdentifierPrefix pIdentifierPrefix : ((AEscapedCompositeIdent) node).getPrefixes()) {
				appendBindingPath(pIdentifierPrefix);
			}
			appendBindingPath(((AEscapedCompositeIdent) node).getIdentifier());
		}
		if (node instanceof AKwCompositeIdent) {
			// A keyword used as a path element (e.g. 'get', 'set', ...) reaches this {kw} alternative of
			// composite_ident; the prefixes (the receiver, e.g. 'l' in 'l.get(1)') must be walked here too,
			// exactly as for the {normal} and {escaped} alternatives, otherwise the receiver is dropped and
			// the resulting path element has no parent.
			for (PIdentifierPrefix pIdentifierPrefix : ((AKwCompositeIdent) node).getPrefixes()) {
				appendBindingPath(pIdentifierPrefix);
			}
			appendBindingPath(((AKwCompositeIdent) node).getIdentifier());
		}
	}

	private void appendBindingPath(PIdentifierPrefix node) {
		if (node instanceof ANormalIdentifierPrefix) {
			appendBindingPath(((ANormalIdentifierPrefix) node).getIdent());
		}
		if (node instanceof AEscapedIdentifierPrefix) {
			appendBindingPath(((AEscapedIdentifierPrefix) node).getEscapedIdentifier());
		}
	}

	private void appendBindingPath(TIdent node) {
		makeNormalBindingPathElement(node);
	}

	private void appendBindingPath(TEscapedIdentifier node) {
		makeNormalBindingPathElement(node, true);
	}

	private void appendBindingPath(PCalleePrimary node) {
		if (node instanceof AIdentifierCalleePrimary) {
			appendBindingPath(((AIdentifierCalleePrimary) node).getCompositeIdent());
		}
		else if (node instanceof AFieldCalleePrimary) {
			appendBindingPath(((AFieldCalleePrimary) node).getFieldAccess());
		}
		else if (node instanceof AMethodCalleePrimary) {
			appendBindingPath(((AMethodCalleePrimary) node).getMethodInvocation());
		}
		else if (node instanceof ANewInstanceCalleePrimary) {
			appendBindingPath(((ANewInstanceCalleePrimary) node).getNewInstance());
		}
	}

	private void appendBindingPath(TKwSuper node) {
		makeNormalBindingPathElement(node);
	}

	private void appendBindingPath(PAuthorizedKwInCompositeIdent node) {
		makeNormalBindingPathElement(getKwToken(node));
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
		return makeNormalBindingPathElement(node, false);
	}

	private IBindingPathElement makeNormalBindingPathElement(Token node, boolean escapedSerialization) {
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
					n -> new SimplePathElementNode(n, expressionFactory, parent, expressionFactory.getBindable(), escapedSerialization));
			nodesPath.add(pathElementNode);
			// SimplePathElement<?> pathElement = pathElementNode.buildModelObjectFromAST(node);
			SimplePathElement<?> pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
			return pathElement;
		}
	}

	private void appendMethodInvocation(APrimaryMethodInvocation node, AbstractBindingPathElementNode<?, ?> lastPathElementNode,
			boolean escapedSerialization) {
		final IBindingPathElement parent = retrieveActualParent();
		// System.out.println(">>> DBG appendMethodInvocation node=" + node + " parent=" + parent + " bindingVariable="
		// + bindingVariable + " bindingPathElements=" + bindingPathElements + " lastPathElementNode=" + lastPathElementNode);
		MethodCallBindingPathElementNode pathElementNode = expressionFactory.retrieveFMLNode(node,
				n -> new MethodCallBindingPathElementNode(n, lastPathElementNode.getASTNode(), expressionFactory, parent,
						expressionFactory.getBindable(), escapedSerialization));
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

		// 'new X(...) with (...)' whose X does not resolve (a missing import) names a concept: only a concept has a 'with' clause. It is
		// taken for one, as 'new X::ctor(...)' is (CORE-D-58); instantiated as a Java class, it lost its clause and got arguments of a Java
		// constructor (CORE-D-64, CORE-D-66). Without the clause, an unresolved name may be a Java class ('new a.MyA()'): left as it was
		if (type instanceof UnresolvedType && node.getPreciseFmlParametersClause() != null) {
			type = TypeFactory.asFlexoConceptInstanceType(type, expressionFactory.getTypingSpace());
		}

		final IBindingPathElement parent = retrieveActualParent();

		if (type instanceof VirtualModelInstanceType) {
			NewVirtualModelInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new NewVirtualModelInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		else if (type instanceof FlexoConceptInstanceType) {
			NewFlexoConceptInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new NewFlexoConceptInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		else {
			NewClassInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new NewClassInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
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

		// 'new X::ctor(...)' names a concept: one that does not resolve is kept as an unresolved concept (CORE-D-58), the node reports it
		if (type instanceof UnresolvedType) {
			type = TypeFactory.asFlexoConceptInstanceType(type, expressionFactory.getTypingSpace());
		}

		if (type instanceof VirtualModelInstanceType) {
			NewVirtualModelInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new NewVirtualModelInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		else if (type instanceof FlexoConceptInstanceType) {
			NewFlexoConceptInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new NewFlexoConceptInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			CreationSchemePathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		}
		/*else {
			NewClassInstanceNode pathElementNode = expressionFactory.retrieveFMLNode(node,
					n -> new NewClassInstanceNode(n, expressionFactory, parent, expressionFactory.getBindable()));
			nodesPath.add(pathElementNode);
			JavaNewInstanceMethodPathElement pathElement = pathElementNode.getModelObject();
			bindingPathElements.add(pathElement);
		
		}*/

	}

	/**
	 * The type a call {@code a.b.C.m(...)} is a static call on, null when it is a call on a value. The grammar has one form for both
	 * (CORE-F-5): the call is static when its receiver {@code a.b.C} is a name that resolves to a type, and its first segment is not a
	 * variable of the binding model (a variable hides a type of the same name, as in Java)
	 */
	private Type getStaticCallReceiverType(APrimaryMethodInvocation node) {
		return getStaticCallReceiverType(node, expressionFactory.getBindable(), expressionFactory.getTypingSpace());
	}

	/** The segments of the receiver of a call on a dotted name ('a', 'b', 'C' in 'a.b.C.m()'), null for another call */
	private static List<String> getReceiverSegments(APrimaryMethodInvocation node) {
		if (!(node.getCalleePrimary() instanceof AIdentifierCalleePrimary)) {
			return null;
		}
		PCompositeIdent compositeIdent = ((AIdentifierCalleePrimary) node.getCalleePrimary()).getCompositeIdent();
		List<PIdentifierPrefix> prefixes;
		if (compositeIdent instanceof ANormalCompositeIdent) {
			prefixes = ((ANormalCompositeIdent) compositeIdent).getPrefixes();
		}
		else if (compositeIdent instanceof AKwCompositeIdent) {
			prefixes = ((AKwCompositeIdent) compositeIdent).getPrefixes();
		}
		else {
			return null;
		}
		List<String> returned = new ArrayList<>();
		for (PIdentifierPrefix prefix : prefixes) {
			if (!(prefix instanceof ANormalIdentifierPrefix)) {
				return null;
			}
			returned.add(((ANormalIdentifierPrefix) prefix).getIdent().getText());
		}
		return returned.isEmpty() ? null : returned;
	}

	public static Type getStaticCallReceiverType(APrimaryMethodInvocation node, Bindable bindable, AbstractFMLTypingSpace typingSpace) {
		if (!(node.getCalleePrimary() instanceof AIdentifierCalleePrimary)) {
			return null;
		}
		PCompositeIdent compositeIdent = ((AIdentifierCalleePrimary) node.getCalleePrimary()).getCompositeIdent();
		List<PIdentifierPrefix> prefixes;
		if (compositeIdent instanceof ANormalCompositeIdent) {
			prefixes = ((ANormalCompositeIdent) compositeIdent).getPrefixes();
		}
		else if (compositeIdent instanceof AKwCompositeIdent) {
			prefixes = ((AKwCompositeIdent) compositeIdent).getPrefixes();
		}
		else {
			return null;
		}
		if (prefixes.isEmpty()) {
			return null;
		}
		StringBuffer receiver = new StringBuffer();
		for (PIdentifierPrefix prefix : prefixes) {
			if (!(prefix instanceof ANormalIdentifierPrefix)) {
				return null;
			}
			String segment = ((ANormalIdentifierPrefix) prefix).getIdent().getText();
			if (receiver.length() == 0 && bindable != null && bindable.getBindingModel() != null
					&& bindable.getBindingModel().bindingVariableNamed(segment) != null) {
				return null;
			}
			receiver.append(receiver.length() > 0 ? "." : "").append(segment);
		}
		return TypeFactory.lookupType(receiver.toString(), typingSpace);
	}

	private void appendStaticMethodInvocation(APrimaryMethodInvocation node) {
		if (bindingVariable == null && bindingPathElements.isEmpty()) {
			staticCallReceiver = getReceiverSegments(node);
		}
		StaticMethodCallBindingPathElementNode pathElementNode = expressionFactory.retrieveFMLNode(node,
				n -> new StaticMethodCallBindingPathElementNode(n, expressionFactory, expressionFactory.getBindable()));
		nodesPath.add(pathElementNode);
		StaticMethodPathElement<?> methodCallElement = pathElementNode.getModelObject();
		bindingPathElements.add(methodCallElement);
	}

}
