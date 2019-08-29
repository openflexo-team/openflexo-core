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
import org.openflexo.foundation.fml.parser.AssignableActionFactory;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.node.AAssignmentStatementExpression;
import org.openflexo.foundation.fml.parser.node.AExpressionAssignment;
import org.openflexo.foundation.fml.parser.node.AFieldLeftHandSide;
import org.openflexo.foundation.fml.parser.node.AIdentifierAssignment;
import org.openflexo.foundation.fml.parser.node.AIdentifierLeftHandSide;
import org.openflexo.foundation.fml.parser.node.PAssignment;
import org.openflexo.foundation.fml.parser.node.PAssignmentOperator;
import org.openflexo.foundation.fml.parser.node.PLeftHandSide;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class AssignationActionNode extends AssignableActionNode<AAssignmentStatementExpression, AssignationAction<?>> {

	private static final Logger logger = Logger.getLogger(AssignationActionNode.class.getPackage().getName());

	public AssignationActionNode(AAssignmentStatementExpression astNode, ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);

		if (getSemiFragment() != null) {
			System.out.println("Juste avant: endPosition=" + getEndPosition());
			setEndPosition(getSemiFragment().getEndPosition());
			System.out.println("Et maintenant endPosition=" + getEndPosition());
			System.out.println("lastParsed: " + getLastParsedFragment() + " " + getLastParsedFragment().getRawText());
		}

	}

	public AssignationActionNode(AssignationAction<?> action, ControlGraphFactory cgFactory) {
		super(action, cgFactory);
	}

	/*protected FMLControlGraph getSimpleControlGraph(PAssignmentExpression expression) {
		if (expression ins)
	}*/

	@Override
	public AssignationAction<?> buildModelObjectFromAST(AAssignmentStatementExpression astNode) {
		AssignationAction<?> returned = getFactory().newAssignationAction();
		System.out.println(">>>>>> Assignation " + astNode);

		// Left
		returned.setAssignation((DataBinding) extractLeft(returned));

		PAssignment assignment = getASTNode().getAssignment();
		if (assignment instanceof AExpressionAssignment) {
			System.out.println("J'analyse ce que je trouve a droite: " + ((AExpressionAssignment) assignment).getRight());
			// System.out.println("A priori c'est de type: " + ((AExpressionAssignment) assignment).getRight().getClass().getSimpleName());
			AssignableActionFactory aaFactory = new AssignableActionFactory(((AExpressionAssignment) assignment).getRight(),
					getAbstractAnalyser());
			if (aaFactory.getAssignableAction() != null) {
				returned.setAssignableAction((AssignableAction) aaFactory.getAssignableAction());
				addToChildren(aaFactory.getRootControlGraphNode());
			}
			System.out.println("Et c'est: " + aaFactory.getAssignableAction());
			// System.exit(-1);
		}
		else if (assignment instanceof AIdentifierAssignment) {
			InnerExpressionActionNode expressionNode = new InnerExpressionActionNode(assignment, getAbstractAnalyser());
			System.out.println("La c'est special, faut que je fasses un InnerExpressionAssignment");
			expressionNode.deserialize();
			returned.setAssignableAction((AssignableAction) expressionNode.getModelObject());
			addToChildren(expressionNode);
		}

		// Right
		// returned.setAssignableAction((ExpressionAction) getFactory().newExpressionAction(extractRight(returned)));

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		if (hasParsedVersion) {
			appendDynamicContents(() -> getModelObject().getAssignation().toString(), getLeftHandSideFragment());
			appendDynamicContents(SPACE, () -> "=", getOperatorFragment());
		}
		else {
			appendDynamicContents(() -> getModelObject().getAssignation().toString());
			appendDynamicContents(() -> "=");
		}

		appendToChildPrettyPrintContents(SPACE, () -> getModelObject().getAssignableAction(), "", -1);

		if (hasParsedVersion) {
			appendStaticContents(";", getSemiFragment());
		}
		else {
			appendStaticContents(";");
		}

	}

	private PLeftHandSide getLefthandSide() {
		if (getASTNode() != null) {
			PAssignment assignment = getASTNode().getAssignment();
			if (assignment instanceof AExpressionAssignment) {
				return ((AExpressionAssignment) assignment).getLeft();
			}
			else if (assignment instanceof AIdentifierAssignment) {
				return ((AIdentifierAssignment) assignment).getLeft();
			}
		}
		return null;
	}

	private PAssignmentOperator getOperator() {
		if (getASTNode() != null) {
			PAssignment assignment = getASTNode().getAssignment();
			if (assignment instanceof AExpressionAssignment) {
				return ((AExpressionAssignment) assignment).getAssignmentOperator();
			}
			else if (assignment instanceof AIdentifierAssignment) {
				return ((AIdentifierAssignment) assignment).getAssignmentOperator();
			}
		}
		return null;
	}

	private DataBinding<?> extractLeft(AssignationAction<?> bindable) {
		if (getLefthandSide() instanceof AFieldLeftHandSide) {
			return makeBinding((AFieldLeftHandSide) getLefthandSide(), bindable);
		}
		else if (getLefthandSide() instanceof AIdentifierLeftHandSide) {
			return makeBinding(((AIdentifierLeftHandSide) getLefthandSide()).getIdentifier(),
					((AIdentifierLeftHandSide) getLefthandSide()).getAdditionalIdentifiers(), bindable);
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
