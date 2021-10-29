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

import java.util.logging.Logger;

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AGetDecl;
import org.openflexo.foundation.fml.parser.node.AGetSetPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.ASetDecl;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 * 		get_set_property_declaration =
 *              visibility? type identifier l_brc get_declaration set_declaration ? r_brc semi;
 *
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class GetSetPropertyNode extends FlexoPropertyNode<AGetSetPropertyInnerConceptDecl, GetProperty<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GetSetPropertyNode.class.getPackage().getName());

	public GetSetPropertyNode(AGetSetPropertyInnerConceptDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public GetSetPropertyNode(GetProperty<?> property, FMLCompilationUnitSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public GetProperty<?> buildModelObjectFromAST(AGetSetPropertyInnerConceptDecl astNode) {

		GetProperty<?> returned;

		if (astNode.getSetDecl() == null) {
			// This is a Get property
			returned = getFactory().newGetProperty();
		}
		else {
			returned = getFactory().newGetSetProperty();
		}

		returned.setVisibility(getVisibility(astNode.getVisibility()));
		try {
			returned.setName(astNode.getLidentifier().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + astNode.getLidentifier().getText());
		}
		returned.setDeclaredType(TypeFactory.makeType(astNode.getType(), getAnalyser().getTypingSpace()));

		AGetDecl getDeclaration = (AGetDecl) astNode.getGetDecl();
		ControlGraphNode<?, ?> getCGNode = makeControlGraphNode(getDeclaration.getFlexoBehaviourBody());
		returned.setGetControlGraph(getCGNode.getModelObject());

		if (astNode.getSetDecl() != null) {
			ASetDecl setDeclaration = (ASetDecl) astNode.getSetDecl();
			ControlGraphNode<?, ?> setCGNode = makeControlGraphNode(setDeclaration.getFlexoBehaviourBody());
			((GetSetProperty<?>) returned).setSetControlGraph(setCGNode.getModelObject());
		}

		return returned;
	}

	protected ControlGraphNode<?, ?> makeControlGraphNode(PFlexoBehaviourBody body) {

		return ControlGraphFactory.makeControlGraphNode(body, getAnalyser());
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType()), SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getName(), SPACE), getNameFragment());

		append(staticContents(SPACE, "{", LINE_SEPARATOR), getLBrcFragment());

		append(dynamicContents(DOUBLE_SPACE, () -> serializeType(getModelObject().getType()), SPACE), getGetTypeFragment());
		append(staticContents("get"), getGetFragment());
		append(staticContents("("), getGetLParFragment());
		append(staticContents(")"), getGetRParFragment());
		append(staticContents(SPACE, "{", ""), getGetLBrcFragment());
		append(childContents(LINE_SEPARATOR, () -> getGetControlGraph(), LINE_SEPARATOR, Indentation.Indent));
		append(staticContents(LINE_SEPARATOR + DOUBLE_SPACE, "}", ""), getGetRBrcFragment());

		when(() -> isSettable()).thenAppend(staticContents(LINE_SEPARATOR + DOUBLE_SPACE, "set", ""), getSetFragment())
				.thenAppend(staticContents("("), getSetLParFragment())
				.thenAppend(dynamicContents(() -> serializeType(getModelObject().getType()), SPACE), getSetTypeFragment())
				.thenAppend(dynamicContents(() -> ((GetSetProperty<?>) getModelObject()).getValueVariableName()),
						getSetVariableValueFragment())
				.thenAppend(staticContents(")"), getSetRParFragment()).thenAppend(staticContents(SPACE, "{", ""), getSetLBrcFragment())
				.thenAppend(childContents(LINE_SEPARATOR, () -> getSetControlGraph(), LINE_SEPARATOR, Indentation.Indent))
				.thenAppend(staticContents(LINE_SEPARATOR + DOUBLE_SPACE, "}", ""), getSetRBrcFragment());

		append(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		append(staticContents(";"), getSemiFragment());
		// @formatter:on
	}

	private FMLControlGraph getGetControlGraph() {
		if (getModelObject() != null) {
			return getModelObject().getGetControlGraph();
		}
		return null;
	}

	private FMLControlGraph getSetControlGraph() {
		if (getModelObject() instanceof GetSetProperty) {
			return ((GetSetProperty<?>) getModelObject()).getSetControlGraph();
		}
		return null;
	}

	protected boolean isSettable() {
		if (getASTNode() != null) {
			return (getASTNode().getSetDecl() != null);
		}
		else {
			return getModelObject() instanceof GetSetProperty;
		}
	}

	private RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null && getASTNode().getVisibility() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

	private RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLidentifier());
		}
		return null;
	}

	private RawSourceFragment getSemiFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getSemi());
		}
		return null;
	}

	private RawSourceFragment getLBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLBrc());
		}
		return null;
	}

	private RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRBrc());
		}
		return null;
	}

	private RawSourceFragment getGetFragment() {
		if (getASTNode() != null) {
			return getFragment(((AGetDecl) getASTNode().getGetDecl()).getKwGet());
		}
		return null;
	}

	private RawSourceFragment getGetTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(((AGetDecl) getASTNode().getGetDecl()).getType());
		}
		return null;
	}

	private RawSourceFragment getGetLParFragment() {
		if (getASTNode() != null) {
			return getFragment(((AGetDecl) getASTNode().getGetDecl()).getLPar());
		}
		return null;
	}

	private RawSourceFragment getGetRParFragment() {
		if (getASTNode() != null) {
			return getFragment(((AGetDecl) getASTNode().getGetDecl()).getRPar());
		}
		return null;
	}

	protected RawSourceFragment getGetLBrcFragment() {
		if (getASTNode() != null && getASTNode().getGetDecl() instanceof AGetDecl
				&& ((AGetDecl) getASTNode().getGetDecl()).getFlexoBehaviourBody() != null) {
			PFlexoBehaviourBody flexoBehaviourBody = ((AGetDecl) getASTNode().getGetDecl()).getFlexoBehaviourBody();
			if (flexoBehaviourBody instanceof ABlockFlexoBehaviourBody) {
				return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) flexoBehaviourBody).getBlock()).getLBrc());
			}
		}
		return null;
	}

	protected RawSourceFragment getGetRBrcFragment() {
		if (getASTNode() != null && getASTNode().getGetDecl() instanceof AGetDecl
				&& ((AGetDecl) getASTNode().getGetDecl()).getFlexoBehaviourBody() != null) {
			PFlexoBehaviourBody flexoBehaviourBody = ((AGetDecl) getASTNode().getGetDecl()).getFlexoBehaviourBody();
			if (flexoBehaviourBody instanceof ABlockFlexoBehaviourBody) {
				return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) flexoBehaviourBody).getBlock()).getRBrc());
			}
		}
		return null;
	}

	private RawSourceFragment getSetFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() != null) {
			return getFragment(((ASetDecl) getASTNode().getSetDecl()).getKwSet());
		}
		return null;
	}

	private RawSourceFragment getSetTypeFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() != null) {
			return getFragment(((ASetDecl) getASTNode().getSetDecl()).getType());
		}
		return null;
	}

	private RawSourceFragment getSetVariableValueFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() != null) {
			return getFragment(((ASetDecl) getASTNode().getSetDecl()).getLidentifier());
		}
		return null;
	}

	private RawSourceFragment getSetLParFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() != null) {
			return getFragment(((ASetDecl) getASTNode().getSetDecl()).getLPar());
		}
		return null;
	}

	private RawSourceFragment getSetRParFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() != null) {
			return getFragment(((ASetDecl) getASTNode().getSetDecl()).getRPar());
		}
		return null;
	}

	protected RawSourceFragment getSetLBrcFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() instanceof ASetDecl
				&& ((ASetDecl) getASTNode().getSetDecl()).getFlexoBehaviourBody() != null) {
			PFlexoBehaviourBody flexoBehaviourBody = ((ASetDecl) getASTNode().getSetDecl()).getFlexoBehaviourBody();
			if (flexoBehaviourBody instanceof ABlockFlexoBehaviourBody) {
				return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) flexoBehaviourBody).getBlock()).getLBrc());
			}
		}
		return null;
	}

	protected RawSourceFragment getSetRBrcFragment() {
		if (getASTNode() != null && getASTNode().getSetDecl() instanceof ASetDecl
				&& ((ASetDecl) getASTNode().getSetDecl()).getFlexoBehaviourBody() != null) {
			PFlexoBehaviourBody flexoBehaviourBody = ((ASetDecl) getASTNode().getSetDecl()).getFlexoBehaviourBody();
			if (flexoBehaviourBody instanceof ABlockFlexoBehaviourBody) {
				return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) flexoBehaviourBody).getBlock()).getRBrc());
			}
		}
		return null;
	}

}
