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
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.DataBindingPrettyPrintDelegate;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.ObjectNode;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.ChildContents;
import org.openflexo.p2pp.DerivedRawSource;
import org.openflexo.p2pp.PrettyPrintContext;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;

/**
 * @author sylvain
 * 
 */
public class DataBindingNode extends ObjectNode<Node, DataBinding<?>, FMLCompilationUnitSemanticsAnalyzer>
		implements DataBindingPrettyPrintDelegate {

	private Bindable bindable;
	private BindingDefinitionType bindingDefinitionType;
	private Type expectedType;
	private boolean detached = false;

	public DataBindingNode(Node astNode, Bindable bindable, BindingDefinitionType bindingDefinitionType, Type expectedType,
			FMLSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
		this.bindable = bindable;
		this.bindingDefinitionType = bindingDefinitionType;
		this.expectedType = expectedType;
		// buildModelObjectFromAST() was already called, but too early (bindable not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public DataBindingNode(DataBinding<?> dataBinding, FMLSemanticsAnalyzer analyzer) {
		super(dataBinding, analyzer);
	}

	/**
	 * A detached node only carries the parsed expression back to its caller: the expression is set on its {@link DataBinding} without
	 * notifying the bindable, as nothing was edited (CORE-D-45)
	 */
	public boolean isDetached() {
		return detached;
	}

	public void setDetached(boolean detached) {
		this.detached = detached;
	}

	@Override
	public DataBinding<?> buildModelObjectFromAST(Node astNode) {
		if (bindable != null && expectedType != null && bindingDefinitionType != null) {
			return new DataBinding(bindable, expectedType, bindingDefinitionType);
		}
		return null;
	}

	@Override
	public DataBindingNode deserialize() {
		return this;
	}

	@Override
	public void finalizeDeserialization() {
		if (getModelObject() != null) {
			if (!getModelObject().isValid()) {
				// No reason to trigger a final error now, since some structural modifications may not have been performed yet.
				// We put it to the list of DataBinding to be validated at the end of semantics analyzing
				getSemanticsAnalyzer().addToInvalidBindings(getModelObject());
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------
	// Preserving pretty-print (CORE-D-33)
	// The text of a DataBinding is the text of its source in which the text of its expression is replaced by the pretty-print of the
	// expression node: what surrounds the expression (parentheses around the whole expression) is kept. A DataBinding whose expression
	// was replaced (another Expression object, after an edit of its text) is written again from the model.
	// ------------------------------------------------------------------------------------------------------------------------------

	private ChildContents<Node, DataBinding<?>, Object, Expression> expressionContents;
	private boolean prettyPrintPrepared = false;

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		if (prettyPrintPrepared) {
			return;
		}
		prettyPrintPrepared = true;
		if (hasParsedVersion && getModelObject() != null && getModelObject().getExpression() != null
				&& getObjectNode(getModelObject().getExpression()) instanceof ExpressionNode) {
			expressionContents = append(childContents("", () -> getModelObject().getExpression(), "", Indentation.DoNotIndent));
			if (!isDetached()) {
				// The binding of the model is written through this node; a detached one is a vehicle, whose parser gives this node to the
				// binding that takes the expression
				getModelObject().setPrettyPrintDelegate(this);
			}
		}
	}

	/** Tells whether the pretty-print of this node was prepared (it is not reached through the contents of its parent node) */
	public boolean isPrettyPrintPrepared() {
		return prettyPrintPrepared;
	}

	private boolean canPreserveParsedText() {
		return getLastParsedFragment() != null && expressionContents != null && expressionContents.getParsedChildNode() != null
				&& getModelObject() != null && getModelObject().getExpression() == expressionContents.getParsedChildNode().getModelObject();
	}

	private String getNormalizedText() {
		return getModelObject() != null ? getModelObject().toString() : "";
	}

	@Override
	protected DerivedRawSource computeTextualRepresentation(PrettyPrintContext context) {
		if (getLastParsedFragment() != null && !canPreserveParsedText()) {
			DerivedRawSource returned = new DerivedRawSource(getLastParsedFragment());
			returned.replace(getLastParsedFragment(), getNormalizedText());
			return returned;
		}
		return super.computeTextualRepresentation(context);
	}

	/**
	 * The text of the supplied binding (the one this node was built for, or the one that took its expression after a parse of a typed text):
	 * the parsed text, where it did not change, when the binding still holds the expression parsed by this node; the normalized text
	 * otherwise
	 */
	@Override
	public String getPrettyPrint(DataBinding<?> binding) {
		if (binding == null) {
			return null;
		}
		if (getASTNode() == null || expressionContents == null || expressionContents.getParsedChildNode() == null
				|| binding.getExpression() != expressionContents.getParsedChildNode().getModelObject()) {
			return binding.toString();
		}
		return getTextualRepresentation(makePrettyPrintContext());
	}

}
