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
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.PAssignmentOperator;
import org.openflexo.foundation.fml.parser.node.PLeftHandSide;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class AssignationActionNode extends AssignableActionNode<AAssignmentStatementExpression, AssignationAction<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AssignationActionNode.class.getPackage().getName());

	public AssignationActionNode(AAssignmentStatementExpression astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);

		if (getSemiFragment() != null) {
			setEndPosition(getSemiFragment().getEndPosition());
		}

	}

	public AssignationActionNode(AssignationAction<?> action, MainSemanticsAnalyzer analyser) {
		super(action, analyser);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AssignationAction<?> buildModelObjectFromAST(AAssignmentStatementExpression astNode) {
		AssignationAction<?> returned = getFactory().newAssignationAction();
		// System.out.println(">>>>>> Assignation " + astNode);

		// Left
		returned.setAssignation((DataBinding) extractLeft(returned));

		// Right
		ControlGraphNode<?, ?> assignableActionNode = ControlGraphFactory.makeControlGraphNode(astNode.getRight(), getAnalyser());
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
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getAssignation().toString()), getLeftHandSideFragment());
		append(dynamicContents(SPACE, () -> "="), getOperatorFragment());
		append(childContents(SPACE, () -> getModelObject().getAssignableAction(), "", Indentation.DoNotIndent));
		append(staticContents(";"), getSemiFragment());

	}

	private PLeftHandSide getLefthandSide() {
		if (getASTNode() != null) {
			return getASTNode().getLeft();
		}
		return null;
	}

	private PAssignmentOperator getOperator() {
		if (getASTNode() != null) {
			return getASTNode().getAssignmentOperator();
		}
		return null;
	}

	private DataBinding<?> extractLeft(AssignationAction<?> bindable) {
		if (getLefthandSide() instanceof AFieldLeftHandSide) {
			return makeBinding((AFieldLeftHandSide) getLefthandSide(), bindable);
		}
		else if (getLefthandSide() instanceof AIdentifierLeftHandSide) {
			return makeBinding(((AIdentifierLeftHandSide) getLefthandSide()).getCompositeIdent(), bindable);
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
