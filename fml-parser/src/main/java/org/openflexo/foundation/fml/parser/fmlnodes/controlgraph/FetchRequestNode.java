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

import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ASelectActionFmlActionExp;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class FetchRequestNode<FR extends AbstractFetchRequest<?, ?, ?, ?>> extends AssignableActionNode<ASelectActionFmlActionExp, FR> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FetchRequestNode.class.getPackage().getName());

	public FetchRequestNode(ASelectActionFmlActionExp astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public FetchRequestNode(FR action, MainSemanticsAnalyzer analyser) {
		super(action, analyser);
	}

	/*protected FMLControlGraph getSimpleControlGraph(PAssignmentExpression expression) {
		if (expression ins)
	}*/

	// private FlexoConceptInstanceType conceptType;
	// private String creationSchemeName;

	/*@Override
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
				else  {
					// TODO
					getModelObject().setCreationScheme((CreationScheme) flexoConceptType.getFlexoBehaviour(creationSchemeName));
				}
	
			}
	
		}
	}*/

	@Override
	public FR buildModelObjectFromAST(ASelectActionFmlActionExp astNode) {
		FR returned = null;

		FlexoConceptInstanceType type = getTypeFactory().lookupConceptNamed(astNode.getSelectedTypeName());

		if (type instanceof VirtualModelInstanceType) {
			if (astNode.getKwUnique() != null) {
				returned = (FR) getFactory().newSelectUniqueVirtualModelInstance();
			}
			else {
				returned = (FR) getFactory().newSelectVirtualModelInstance();
			}
		}
		else if (type instanceof FlexoConceptInstanceType) {
			if (astNode.getKwUnique() != null) {
				returned = (FR) getFactory().newSelectUniqueFlexoConceptInstance();
			}
			else {
				returned = (FR) getFactory().newSelectFlexoConceptInstance();
			}
		}
		else {
			returned = (FR) getFactory().newSelectFlexoConceptInstance();
		}
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

		// @formatter:off	
		append(staticContents("", "select Toto from (this)", SPACE), getSelectFragment());
		/*append(dynamicContents(() -> serializeType(getModelObject().getFlexoConceptType())), getConceptNameFragment());
		when(() -> isFullQualified())
			.thenAppend(staticContents("::"), getColonColonFragment())
			.thenAppend(dynamicContents(() -> serializeFlexoBehaviour(getModelObject().getCreationScheme())), getConstructorNameFragment());
		append(staticContents("("), getLParFragment());
		append(childrenContents("", "", () -> getModelObject().getParameters(), ",", "", Indentation.DoNotIndent,
				AddFlexoConceptInstanceParameter.class));
		append(staticContents(")"), getRParFragment());*/
		// @formatter:on	
	}

	private RawSourceFragment getSelectFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwSelect());
		}
		return null;
	}

}
