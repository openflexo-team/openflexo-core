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

package org.openflexo.foundation.fml.parser.fmlnodes;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLPropertyValue;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFmlInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AJavaInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.PInnerConceptDecl;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;

/**
 * Represents a {@link ModelSlot} declaration in FML source code
 * 
 * <pre>
 *    | {fml} visibility? type cardinality? identifier kw_with [role]:identifier fml_parameters? semi
 *    | {fml_fully_qualified} visibility? type cardinality? identifier kw_with [ta_id]:identifier colon_colon [role]:identifier fml_parameters? semi
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class ModelSlotPropertyNode<N extends PInnerConceptDecl, MS extends ModelSlot<?>> extends AbstractRolePropertyNode<N, MS> {

	private static final Logger logger = Logger.getLogger(ModelSlotPropertyNode.class.getPackage().getName());

	public ModelSlotPropertyNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ModelSlotPropertyNode(MS modelSlot, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(modelSlot, analyzer);
	}

	@Override
	public MS buildModelObjectFromAST(PInnerConceptDecl astNode) {

		Type type = TypeFactory.makeType(getPType(), getSemanticsAnalyzer().getTypingSpace());

		Class<? extends FlexoRole<?>> roleClass = null;
		if (astNode instanceof AFmlInnerConceptDecl) {
			roleClass = getFMLFactory().getRoleClass(((AFmlInnerConceptDecl) astNode).getRole());
		}
		if (astNode instanceof AFmlFullyQualifiedInnerConceptDecl) {
			roleClass = getFMLFactory().getRoleClass(((AFmlFullyQualifiedInnerConceptDecl) astNode).getTaId(),
					((AFmlFullyQualifiedInnerConceptDecl) astNode).getRole());
		}
		if (astNode instanceof AJavaInnerConceptDecl && type instanceof VirtualModelInstanceType) {
			// In this case this is a FMLRTVirtualModelInstanceModelSlot
			roleClass = FMLRTVirtualModelInstanceModelSlot.class;
		}
		if (roleClass == null) {
			throwIssue("Cannot find roleClass", getFragment(astNode));
			return null;
		}

		MS returned = (MS) getFactory().newInstance(roleClass);

		returned.setVisibility(getVisibility(getPVisibility()));
		try {
			returned.setName(getLidentifierName().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + (getLidentifierName().getText()));
		}
		returned.setCardinality(getCardinality(getPCardinality()));

		// If the type is unresolved for a FMLRTModelSlot, manage an unresolved VirtualModelInstanceType to keep track of initial name
		if (FMLRTModelSlot.class.isAssignableFrom(roleClass) && type instanceof UnresolvedType) {
			// In this case this is a FMLRTVirtualModelInstanceModelSlot
			roleClass = FMLRTVirtualModelInstanceModelSlot.class;
			FMLTechnologyAdapter fmlTechnologyAdapter = getSemanticsAnalyzer().getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLTechnologyAdapter.class);
			type = new VirtualModelInstanceType(((UnresolvedType) type).getUnresolvedTypeName(),
					fmlTechnologyAdapter.getVirtualModelInstanceTypeFactory());
		}
		returned.setType(type);

		return returned;
	}

	/*
	 *   | {fml} visibility? type cardinality? identifier kw_with [role]:identifier fml_parameters? semi
	 *   | {fml_fully_qualified} visibility? type cardinality? identifier kw_with [ta_id]:identifier colon_colon [role]:identifier fml_parameters? semi
	 */
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType())), getTypeFragment());
		append(dynamicContents(() -> serializeCardinality(getModelObject().getCardinality())), getCardinalityFragment());
		append(dynamicContents(SPACE, () -> getModelObject().getName(), SPACE), getNameFragment());
		append(staticContents("", "with", SPACE), getWithFragment());
		when(() -> isFullQualified()).thenAppend(dynamicContents(() -> getFMLFactory().serializeTAId(getModelObject())), getTaIdFragment())
		.thenAppend(staticContents("::"), getColonColonFragment());
		append(dynamicContents(() -> serializeFlexoRoleName(getModelObject())), getRoleFragment());
		when(() -> hasFMLProperties()).thenAppend(staticContents("("), getFMLParametersLParFragment()).thenAppend(childrenContents("", "",
				() -> getModelObject().getFMLPropertyValues(getFactory()), ",", "", Indentation.DoNotIndent, FMLPropertyValue.class))
		.thenAppend(staticContents(")"), getFMLParametersRParFragment());
		append(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

}
