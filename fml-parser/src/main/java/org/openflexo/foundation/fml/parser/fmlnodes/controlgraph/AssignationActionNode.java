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

package org.openflexo.foundation.fml.parser.fmlnodes.controlgraph;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AExpressionAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AFmlActionAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.PAssignmentOperator;
import org.openflexo.foundation.fml.parser.node.PAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.PLeftHandSide;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class AssignationActionNode extends AssignableActionNode<PAssignmentStatementExpression, AssignationAction<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AssignationActionNode.class.getPackage().getName());

	public AssignationActionNode(PAssignmentStatementExpression astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public AssignationActionNode(AssignationAction<?> action, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(action, analyzer);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AssignationAction<?> buildModelObjectFromAST(PAssignmentStatementExpression astNode) {
		AssignationAction<?> returned = getFactory().newAssignationAction();
		// System.out.println(">>>>>> Assignation " + astNode);

		// Left
		returned.setAssignation((DataBinding) extractLeft(returned));

		// Right
		ControlGraphNode<?, ?> assignableActionNode = null;
		if (astNode instanceof AExpressionAssignmentStatementExpression) {
			assignableActionNode = ControlGraphFactory.makeControlGraphNode(((AExpressionAssignmentStatementExpression) astNode).getRight(),
					getSemanticsAnalyzer());
		}
		if (astNode instanceof AFmlActionAssignmentStatementExpression) {
			assignableActionNode = ControlGraphFactory.makeControlGraphNode(((AFmlActionAssignmentStatementExpression) astNode).getRight(),
					getSemanticsAnalyzer());
		}
		if (assignableActionNode != null) {
			if (assignableActionNode.getModelObject() instanceof AssignableAction) {
				returned.setAssignableAction((AssignableAction) assignableActionNode.getModelObject());
				addToChildren(assignableActionNode);
			}
			else {
				System.err.println("Unexpected " + assignableActionNode.getModelObject());
				Thread.dumpStack();
			}
		}
		else {
			System.err.println("Unexpected " + astNode);
			Thread.dumpStack();
		}
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getAssignation().toString()), getLeftHandSideFragment());
		append(dynamicContents(SPACE, () -> "="), getOperatorFragment());
		append(childContents(SPACE, () -> getModelObject().getAssignableAction(), "", Indentation.DoNotIndent));
		when(() -> requiresSemi(), true).thenAppend(staticContents(";"), getSemiFragment());
		// append(staticContents(";"), getSemiFragment());

	}

	private PLeftHandSide getLefthandSide() {
		if (getASTNode() instanceof AExpressionAssignmentStatementExpression) {
			return ((AExpressionAssignmentStatementExpression) getASTNode()).getLeft();
		}
		if (getASTNode() instanceof AFmlActionAssignmentStatementExpression) {
			return ((AFmlActionAssignmentStatementExpression) getASTNode()).getLeft();
		}
		return null;
	}

	private PAssignmentOperator getOperator() {
		if (getASTNode() instanceof AExpressionAssignmentStatementExpression) {
			return ((AExpressionAssignmentStatementExpression) getASTNode()).getAssignmentOperator();
		}
		if (getASTNode() instanceof AFmlActionAssignmentStatementExpression) {
			return ((AFmlActionAssignmentStatementExpression) getASTNode()).getAssignmentOperator();
		}
		return null;
	}

	private DataBinding<?> extractLeft(AssignationAction<?> bindable) {

		if (getLefthandSide() instanceof AFieldLeftHandSide) {
			// return makeBinding((AFieldLeftHandSide) getLefthandSide(), bindable);
			return ExpressionFactory.makeDataBinding((AFieldLeftHandSide) getLefthandSide(), bindable, BindingDefinitionType.SET,
					Object.class, getSemanticsAnalyzer(), this);
		}
		else if (getLefthandSide() instanceof AIdentifierLeftHandSide) {
			// return makeBinding(((AIdentifierLeftHandSide) getLefthandSide()).getCompositeIdent(), bindable);
			return ExpressionFactory.makeDataBinding(((AIdentifierLeftHandSide) getLefthandSide()), bindable, BindingDefinitionType.SET,
					Object.class, getSemanticsAnalyzer(), this);
		}
		return null;
	}

	// TODO: on doit plutot trouver une autre action, c'est plus complique que ca
	/*private DataBinding<?> extractRight(AssignationAction<?> bindable) {
		if (getASTNode() != null) {
			PAssignment assignment = getASTNode().getAssignment();
			if (assignment instanceof AExpressionAssignment) {
				return makeBinding(((AExpressionAssignment) assignment).getRight(), bindable);
			}
			else if (assignment instanceof AIdentifierAssignment) {
				return makeBinding(((AIdentifierAssignment) assignment).getIdentifier(),
						((AIdentifierAssignment) assignment).getAdditionalIdentifiers(), bindable);
			}
		}
		return null;
	}*/

	private RawSourceFragment getLeftHandSideFragment() {
		if (getLefthandSide() != null) {
			return getFragment(getLefthandSide());
		}
		return null;
	}

	private RawSourceFragment getOperatorFragment() {
		if (getLefthandSide() != null) {
			return getFragment(getOperator());
		}
		return null;
	}

	/*private RawSourceFragment getRightHandSideFragment() {
		if (getASTNode() != null) {
			PAssignment assignment = getASTNode().getAssignment();
			if (assignment instanceof AExpressionAssignment) {
				return getFragment(((AExpressionAssignment) assignment).getRight());
			}
			else if (assignment instanceof AIdentifierAssignment) {
				return getFragment(((AIdentifierAssignment) assignment).getIdentifier(),
						((AIdentifierAssignment) assignment).getAdditionalIdentifiers());
			}
		}
		return null;
	}*/

}
