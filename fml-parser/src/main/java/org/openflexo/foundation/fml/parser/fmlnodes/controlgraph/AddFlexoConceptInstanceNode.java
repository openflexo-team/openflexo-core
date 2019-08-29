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

import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedConceptInstanceCreationExpression;
import org.openflexo.foundation.fml.parser.node.ASimplifiedConceptInstanceCreationExpression;
import org.openflexo.foundation.fml.parser.node.PConceptInstanceCreationExpression;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class AddFlexoConceptInstanceNode extends AssignableActionNode<PConceptInstanceCreationExpression, AddFlexoConceptInstance<?>> {

	private static final Logger logger = Logger.getLogger(AddFlexoConceptInstanceNode.class.getPackage().getName());

	public AddFlexoConceptInstanceNode(PConceptInstanceCreationExpression astNode, ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);
	}

	public AddFlexoConceptInstanceNode(AddFlexoConceptInstance<?> action, ControlGraphFactory cgFactory) {
		super(action, cgFactory);
	}

	/*protected FMLControlGraph getSimpleControlGraph(PAssignmentExpression expression) {
		if (expression ins)
	}*/

	private FlexoConceptInstanceType conceptType;
	private String creationSchemeName;

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		if (conceptType != null && conceptType.isResolved()) {
			FlexoConcept flexoConceptType = conceptType.getFlexoConcept();
			if (flexoConceptType != null) {
				getModelObject().setFlexoConceptType(flexoConceptType);
				if (flexoConceptType.getCreationSchemes().size() == 0) {
					// No constructor: !! problem
					// TODO
				}
				else if (flexoConceptType.getCreationSchemes().size() == 1) {
					getModelObject().setCreationScheme(flexoConceptType.getCreationSchemes().get(0));
				}
				else /* flexoConceptType.getCreationSchemes().size() > 1 */ {
					// TODO
					getModelObject().setCreationScheme((CreationScheme) flexoConceptType.getFlexoBehaviour(creationSchemeName));
				}

			}

		}
	}

	@Override
	public AddFlexoConceptInstance<?> buildModelObjectFromAST(PConceptInstanceCreationExpression astNode) {
		AddFlexoConceptInstance<?> returned = getFactory().newAddFlexoConceptInstance();
		System.out.println(">>>>>> New FCI " + astNode);

		conceptType = getTypeFactory().lookupConceptNamed(getConceptName().getText());
		if (isFullQualified()) {
			creationSchemeName = getConstructorName().getText();
		}

		System.out.println("conceptType:" + conceptType);

		// Left
		/*returned.setAssignation((DataBinding) extractLeft(returned));
		
		PAssignment assignment = getASTNode().getAssignment();
		if (assignment instanceof AExpressionAssignment) {
			System.out.println("J'analyse ce que je trouve a droite: " + ((AExpressionAssignment) assignment).getRight());
			// System.out.println("A priori c'est de type: " + ((AExpressionAssignment) assignment).getRight().getClass().getSimpleName());
			AssignableActionFactory aaFactory = new AssignableActionFactory(((AExpressionAssignment) assignment).getRight(), getAnalyser());
			if (aaFactory.getAssignableAction() != null) {
				returned.setAssignableAction((AssignableAction) aaFactory.getAssignableAction());
				addToChildren(aaFactory.getRootControlGraphNode());
			}
			System.out.println("Et c'est: " + aaFactory.getAssignableAction());
			// System.exit(-1);
		}
		else if (assignment instanceof AIdentifierAssignment) {
			InnerExpressionActionNode expressionNode = new InnerExpressionActionNode(assignment, getAnalyser());
			System.out.println("La c'est special, faut que je fasses un InnerExpressionAssignment");
			expressionNode.deserialize();
			returned.setAssignableAction((AssignableAction) expressionNode.getModelObject());
			addToChildren(expressionNode);
		}
		
		// Right
		// returned.setAssignableAction((ExpressionAction) getFactory().newExpressionAction(extractRight(returned)));
		 */

		return returned;

	}

	/*
	 * <pre>
	 * concept_instance_creation_expression =
	 *   {simplified} new [concept_name]:identifier l_par argument_list? r_par |
	 *   {full_qualified} new [concept_name]:identifier colon_colon [constructor_name]:identifier l_par argument_list? r_par;
	 * </pre>
	 */

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// aConcept = new MyConcept::init(name);
		// aConcept = new MyConcept(name);

		// if (hasParsedVersion) {
		appendStaticContents("new", SPACE, getNewFragment());
		appendDynamicContents(() -> serializeType(getModelObject().getFlexoConceptType()), getConceptNameFragment());
		if (isFullQualified()) {
			appendStaticContents("::", getColonColonFragment());
			appendDynamicContents(() -> serializeFlexoBehaviour(getModelObject().getCreationScheme()), getConstructorNameFragment());
		}
		appendStaticContents("(", getLParFragment());
		appendToChildrenPrettyPrintContents("", "", () -> getModelObject().getParameters(), ",", "", -1,
				AddFlexoConceptInstanceParameter.class);
		appendStaticContents(")", getRParFragment());

		/*}
		else {
			appendStaticContents("new", SPACE);
			appendDynamicContents(() -> serializeType(getModelObject().getFlexoConceptType()));
			if (isFullQualified()) {
				appendStaticContents("::");
				appendDynamicContents(() -> serializeFlexoBehaviour(getModelObject().getCreationScheme()));
			}
			appendStaticContents("(");
			appendToChildrenPrettyPrintContents("", "", () -> getModelObject().getParameters(), ",", "", -1,
					AddFlexoConceptInstanceParameter.class);
			appendStaticContents(")");
		}*/

	}

	private boolean isFullQualified() {
		if (getASTNode() != null) {
			return getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression;
		}
		else {
			return getModelObject().getFlexoConceptType().getCreationSchemes().size() > 1;
		}
	}

	private RawSourceFragment getNewFragment() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return getFragment(((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getNew());
		}
		if (getASTNode() instanceof ASimplifiedConceptInstanceCreationExpression) {
			return getFragment(((ASimplifiedConceptInstanceCreationExpression) getASTNode()).getNew());
		}
		return null;
	}

	private TIdentifier getConceptName() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return ((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getConceptName();
		}
		if (getASTNode() instanceof ASimplifiedConceptInstanceCreationExpression) {
			return ((ASimplifiedConceptInstanceCreationExpression) getASTNode()).getConceptName();
		}
		return null;
	}

	private RawSourceFragment getConceptNameFragment() {
		if (getConceptName() != null) {
			return getFragment(getConceptName());
		}
		return null;
	}

	private RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return getFragment(((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getColonColon());
		}
		return null;
	}

	private TIdentifier getConstructorName() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return ((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getConstructorName();
		}
		return null;
	}

	private RawSourceFragment getConstructorNameFragment() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return getFragment(((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getConstructorName());
		}
		return null;
	}

	private RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return getFragment(((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof ASimplifiedConceptInstanceCreationExpression) {
			return getFragment(((ASimplifiedConceptInstanceCreationExpression) getASTNode()).getLPar());
		}
		return null;
	}

	private RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AFullQualifiedConceptInstanceCreationExpression) {
			return getFragment(((AFullQualifiedConceptInstanceCreationExpression) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof ASimplifiedConceptInstanceCreationExpression) {
			return getFragment(((ASimplifiedConceptInstanceCreationExpression) getASTNode()).getRPar());
		}
		return null;
	}

}
