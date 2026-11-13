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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.ConditionalExpression;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.UnaryOperatorExpression;
import org.openflexo.foundation.fml.parser.AbstractExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.ObjectNode;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.p2pp.DerivedRawSource;
import org.openflexo.p2pp.PrettyPrintContext;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public abstract class ExpressionNode<N extends Node, T extends Expression> extends ObjectNode<N, T, FMLCompilationUnitSemanticsAnalyzer> {

	private AbstractExpressionFactory expressionFactory;

	public ExpressionNode(N astNode, AbstractExpressionFactory expressionFactory) {
		super(astNode, expressionFactory);
		this.expressionFactory = expressionFactory;
	}

	public ExpressionNode(T constant, AbstractExpressionFactory expressionFactory) {
		super(constant, expressionFactory);
		this.expressionFactory = expressionFactory;
	}

	public AbstractExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	@Override
	public ExpressionNode<N, T> deserialize() {
		if (getParent() instanceof DataBindingNode) {
			if (((DataBindingNode) getParent()).getModelObject() == null) {
				System.out.println("While processing " + getModelObject() + " for " + getASTNode());
				System.out.println("Found DataBinding parent with null model object " + ((DataBindingNode) getParent()).getModelObject());
				// System.exit(-1);
			}
			else {
				DataBindingNode dataBindingNode = (DataBindingNode) getParent();
				dataBindingNode.getModelObject().setExpression(getModelObject(), !dataBindingNode.isDetached());
			}
		}
		return this;
	}

	// ------------------------------------------------------------------------------------------------------------------------------
	// Preserving pretty-print (CORE-D-33)
	// An expression that was parsed is printed as the text of its source in which the text of its operands is replaced by their own
	// pretty-print: the operators, the parentheses and the spaces written by the user (what lies between the operands) are kept. The
	// leaves (constants, names, types) keep their parsed text while their value is the one they had once the unit was built
	// (DynamicContents#keepingParsedTextWhileUnchanged). An expression whose structure changed in place (another operand object, another
	// operator) is written again from the model.
	// ------------------------------------------------------------------------------------------------------------------------------

	/** The operands, as they were when the pretty-print was prepared, in source order */
	private final List<Supplier<? extends Expression>> operands = new ArrayList<>();
	private Object parsedOperator;

	/** The {@link Bindable} this expression is written in, used to write it from the model */
	public Bindable getBindable() {
		return expressionFactory != null ? expressionFactory.getBindable() : null;
	}

	/** This expression written from the model, as {@link org.openflexo.connie.DataBinding#toString()} writes it */
	public String getNormalizedText() {
		if (getModelObject() == null) {
			return "";
		}
		return getModelObject().toString(getBindable());
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		if (!hasParsedVersion || getModelObject() == null) {
			append(dynamicContents(() -> getNormalizedText()), null);
			return;
		}
		prepareParsedPrettyPrint();
	}

	/**
	 * Declare the contents of an expression that was parsed. By default: the operands of an operator, or the whole text as a leaf that is
	 * kept while its value is unchanged
	 */
	protected void prepareParsedPrettyPrint() {
		T expression = getModelObject();
		if (expression instanceof BinaryOperatorExpression) {
			BinaryOperatorExpression e = (BinaryOperatorExpression) expression;
			parsedOperator = e.getOperator();
			appendOperand(() -> e.getLeftArgument());
			appendOperand(() -> e.getRightArgument());
		}
		else if (expression instanceof UnaryOperatorExpression) {
			UnaryOperatorExpression e = (UnaryOperatorExpression) expression;
			parsedOperator = e.getOperator();
			appendOperand(() -> e.getArgument());
		}
		else if (expression instanceof ConditionalExpression) {
			ConditionalExpression e = (ConditionalExpression) expression;
			appendOperand(() -> e.getCondition());
			appendOperand(() -> e.getThenExpression());
			appendOperand(() -> e.getElseExpression());
		}
		else {
			appendLeaf(() -> getNormalizedText(), getLastParsedFragment());
		}
	}

	/** An operand, printed by its own node: its text is replaced in the text of this expression */
	protected void appendOperand(Supplier<? extends Expression> operand) {
		operands.add(operand);
		append(childContents("", operand, "", Indentation.DoNotIndent));
	}

	/** A leaf: its parsed text is kept while the supplied value is the one captured once the unit was built */
	protected void appendLeaf(Supplier<String> value, RawSourceFragment fragment) {
		append(dynamicContents(value).keepingParsedTextWhileUnchanged(), fragment);
	}

	/** The operator, when this expression applies one, to tell that it was not changed in place */
	protected Object getCurrentOperator() {
		if (getModelObject() instanceof BinaryOperatorExpression) {
			return ((BinaryOperatorExpression) getModelObject()).getOperator();
		}
		if (getModelObject() instanceof UnaryOperatorExpression) {
			return ((UnaryOperatorExpression) getModelObject()).getOperator();
		}
		return null;
	}

	/**
	 * Tells whether the parsed text of this expression can still be used: same operator, and every operand is still an object printed by
	 * a node of this expression
	 */
	protected boolean canPreserveParsedText() {
		if (getModelObject() == null) {
			return false;
		}
		if (parsedOperator != null && parsedOperator != getCurrentOperator()) {
			return false;
		}
		for (Supplier<? extends Expression> operand : operands) {
			Expression current = operand.get();
			if (current == null || getObjectNode(current) == null) {
				return false;
			}
		}
		return true;
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

}
