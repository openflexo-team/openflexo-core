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

package org.openflexo.foundation.fml.parser.fmlnodes.expr;

import java.lang.reflect.Type;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.StaticMethodPathElement;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import java.util.List;

import org.openflexo.foundation.fml.parser.BindingPathFactory;
import org.openflexo.foundation.fml.parser.node.AIdentifierCalleePrimary;
import org.openflexo.foundation.fml.parser.node.AKwCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ANormalCompositeIdent;
import org.openflexo.foundation.fml.parser.node.ANormalIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.PCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.APrimaryMethodInvocation;

/**
 * @author sylvain
 * 
 */
public class StaticMethodCallBindingPathElementNode
		extends AbstractCallBindingPathElementNode<APrimaryMethodInvocation, StaticMethodPathElement<?>> {

	public StaticMethodCallBindingPathElementNode(APrimaryMethodInvocation astNode, FMLSemanticsAnalyzer analyzer, Bindable bindable) {
		super(astNode, analyzer, bindable);
		setReadyToBuildModelObject(true);
		// buildModelObjectFromAST() was already called, but too early (parent not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public StaticMethodCallBindingPathElementNode(StaticMethodPathElement<?> bindingPathElement, FMLSemanticsAnalyzer analyzer,
			Bindable bindable) {
		super(bindingPathElement, analyzer, bindable);
	}

	/**
	 * The first segment of the receiver ('Foo' in 'Foo.bar()', 'java' in 'java.lang.Math.max(1, 2)'), null when it cannot be read
	 */
	public String getReceiverFirstSegment() {
		if (getASTNode() != null && getASTNode().getCalleePrimary() instanceof AIdentifierCalleePrimary) {
			PCompositeIdent compositeIdent = ((AIdentifierCalleePrimary) getASTNode().getCalleePrimary()).getCompositeIdent();
			List<PIdentifierPrefix> prefixes = null;
			if (compositeIdent instanceof ANormalCompositeIdent) {
				prefixes = ((ANormalCompositeIdent) compositeIdent).getPrefixes();
			}
			else if (compositeIdent instanceof AKwCompositeIdent) {
				prefixes = ((AKwCompositeIdent) compositeIdent).getPrefixes();
			}
			if (prefixes != null && !prefixes.isEmpty() && prefixes.get(0) instanceof ANormalIdentifierPrefix) {
				return ((ANormalIdentifierPrefix) prefixes.get(0)).getIdent().getText();
			}
		}
		return null;
	}

	/**
	 * Tells if the receiver of this call, taken for a type when it was parsed, names a variable of the binding model of the bindable as it
	 * is now. A variable hides a type of the same name (CORE-F-5): the call is then a call on that variable
	 */
	public boolean receiverIsAVariable() {
		String firstSegment = getReceiverFirstSegment();
		return firstSegment != null && getBindable() != null && getBindable().getBindingModel() != null
				&& getBindable().getBindingModel().bindingVariableNamed(firstSegment) != null;
	}

	/**
	 * Release what this node built, when the path holding it is built again (see {@link BindingPathNode#reconsiderStaticCalls()}): the
	 * element stops listening, its arguments are deleted
	 */
	public void dispose() {
		if (getModelObject() != null && getModelObject().isActivated()) {
			getModelObject().desactivate();
		}
		if (getArguments() != null) {
			for (DataBinding<?> argument : getArguments()) {
				argument.delete();
			}
		}
	}

	@Override
	public StaticMethodPathElement<?> buildModelObjectFromAST(APrimaryMethodInvocation astNode) {

		if (readyToBuildModelObject()) {
			handleArguments(astNode.getArgumentList());
			// 'Foo.bar(x)': a call whose receiver names a type (CORE-F-5, see BindingPathFactory)
			Type type = BindingPathFactory.getStaticCallReceiverType(astNode, getBindable(), getSemanticsAnalyzer().getTypingSpace());
			if (type != null && !getSemanticsAnalyzer().getTypingSpace().isTypeImported(type)) {
				getSemanticsAnalyzer().getTypingSpace().importType(type);
			}
			String methodName = getLastPathIdentifier(astNode.getCalleePrimary());
			StaticMethodPathElement<?> pathElement = getBindingFactory().makeStaticMethodPathElement(type, methodName, getArguments(),
					getBindable());
			pathElement.setBindingPathElementOwner(this);
			return pathElement;
		}
		return null;

		/*if (getBindable() != null) {
			Type type = TypeFactory.makeType(getASTNode().getType(), getanalyzer().getTypingSpace());
			handleArguments(astNode.getArgumentList());
			String method = astNode.getIdent().getText();
			StaticMethodCallBindingPathElement returned = new StaticMethodCallBindingPathElement(type, method, getArguments());
			return returned;
		}
		return null;*/

	}

}
