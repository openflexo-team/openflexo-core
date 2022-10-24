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

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLPropertyValue;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AFmlBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedFmlParameters;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.PFmlParameters;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *   | {fml} [annotations]:annotation* visibility? type? [name]:identifier l_par formal_arguments_list? r_par kw_with [behaviour]:identifier fml_parameters? flexo_behaviour_body
 *   | {fml_fully_qualified} [annotations]:annotation* visibility? type? [name]:identifier l_par formal_arguments_list? r_par kw_with [ta_id]:identifier colon_colon [behaviour]:identifier fml_parameters? flexo_behaviour_body
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class FMLBehaviourNode<N extends Node, B extends FlexoBehaviour> extends FlexoBehaviourNode<N, B> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLBehaviourNode.class.getPackage().getName());

	public FMLBehaviourNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FMLBehaviourNode(B behaviour, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(behaviour, analyzer);
	}

	@Override
	public B buildModelObjectFromAST(N astNode) {

		Class<B> behaviourClass = null;
		if (astNode instanceof AFmlBehaviourDecl) {
			behaviourClass = (Class<B>) getFMLFactory().getBehaviourClass(((AFmlBehaviourDecl) astNode).getBehaviour());
		}
		if (astNode instanceof AFmlFullyQualifiedBehaviourDecl) {
			behaviourClass = (Class<B>) getFMLFactory().getBehaviourClass(((AFmlFullyQualifiedBehaviourDecl) astNode).getTaId(),
					((AFmlFullyQualifiedBehaviourDecl) astNode).getBehaviour());
		}

		// System.out.println("behaviourClass=" + behaviourClass);

		B returned = getFactory().newInstance(behaviourClass);
		if (astNode instanceof AFmlBehaviourDecl) {
			returned.setVisibility(getVisibility(((AFmlBehaviourDecl) astNode).getVisibility()));
			try {
				returned.setName(((AFmlBehaviourDecl) astNode).getName().getText());
			} catch (InvalidNameException e) {
				throwIssue("Invalid name: " + ((AFmlBehaviourDecl) astNode).getName().getText());
			}
		}
		if (astNode instanceof AFmlFullyQualifiedBehaviourDecl) {
			returned.setVisibility(getVisibility(((AFmlFullyQualifiedBehaviourDecl) astNode).getVisibility()));
			try {
				returned.setName(((AFmlFullyQualifiedBehaviourDecl) astNode).getName().getText());
			} catch (InvalidNameException e) {
				throwIssue("Invalid name: " + ((AFmlFullyQualifiedBehaviourDecl) astNode).getName().getText());
			}
		}

		PFlexoBehaviourBody flexoBehaviourBody = getFlexoBehaviourBody(astNode);
		if (flexoBehaviourBody instanceof ABlockFlexoBehaviourBody) {
			ControlGraphNode<?, ?> cgNode = ControlGraphFactory.makeControlGraphNode(getFlexoBehaviourBody(astNode),
					getSemanticsAnalyzer());
			if (cgNode != null) {
				returned.setControlGraph(cgNode.getModelObject());
				addToChildren(cgNode);
			}
		}
		else {
			// AEmptyFlexoBehaviourBody : keep the ControlGraph null
		}

		return returned;
	}

	/**
	 * <pre>
	 * [annotations]:annotation* visibility? type? [name]:identifier l_par formal_arguments_list? r_par kw_with [behaviour]:identifier fml_parameters? flexo_behaviour_body
	 * [annotations]:annotation* visibility? type? [name]:identifier l_par formal_arguments_list? r_par kw_with [ta_id]:identifier colon_colon [behaviour]:identifier fml_parameters? flexo_behaviour_body
	 * </pre>
	 */
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		// @formatter:off	
		//append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent,
		//		FMLMetaData.class));
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		when(() -> hasReturnType())
			.thenAppend(dynamicContents(() -> serializeType(getModelObject().getReturnType()),SPACE), getTypeFragment());
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment());
		append(staticContents("("), getLParFragment());
		append(childrenContents("", "", () -> getModelObject().getParameters(), ","+SPACE, "", Indentation.DoNotIndent,
				FlexoBehaviourParameter.class));
		append(staticContents(")"), getRParFragment());
	
		append(staticContents(SPACE, "with", SPACE), getWithFragment());
		when(() -> isFullQualified())
			.thenAppend(dynamicContents(() -> getFMLFactory().serializeTAId(getModelObject())), getTaIdFragment())
			.thenAppend(staticContents("::"), getColonColonFragment());
		append(dynamicContents(() -> serializeFlexoBehaviourName(getModelObject())), getBehaviourFragment());
		when(() -> hasFMLProperties())
		.thenAppend(staticContents("("), getFMLParametersLParFragment())
		.thenAppend(childrenContents("","", () -> getModelObject().getFMLPropertyValues(getFactory()), ",","", Indentation.DoNotIndent,
				FMLPropertyValue.class))
		.thenAppend(staticContents(")"), getFMLParametersRParFragment());

		when(() -> hasNoImplementation())
			.thenAppend(staticContents(";"), getSemiFragment())
			.elseAppend(staticContents(SPACE,"{", ""), getLBrcFragment())
			.elseAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, Indentation.Indent))
			.elseAppend(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());
		// @formatter:on

	}

	protected boolean isFullQualified() {
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return true;
		}
		return false;
	}

	protected boolean hasReturnType() {
		if (getASTNode() != null) {
			return getType() != null;
		}
		if (getModelObject() != null) {
			Type returnedType = getModelObject().getReturnType();
			return !(((Void.class.equals(returnedType)) || (Void.TYPE.equals(returnedType))));
		}
		return false;
	}

	protected String serializeFlexoBehaviourName(FlexoBehaviour behaviour) {
		return behaviour.getFMLKeyword(getFactory());
	}

	protected boolean hasFMLProperties() {
		if (getFMLParameters() != null) {
			return true;
		}
		if (getModelObject() != null) {
			return getModelObject().hasFMLProperties(getFactory());
		}
		return false;
	}

	protected RawSourceFragment getWithFragment() {
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return getFragment(((AFmlBehaviourDecl) getASTNode()).getKwWith());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return getFragment(((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getKwWith());
		}
		return null;
	}

	protected RawSourceFragment getTaIdFragment() {
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return getFragment(((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getTaId());
		}
		return null;
	}

	protected RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return getFragment(((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getColonColon());
		}
		return null;
	}

	protected RawSourceFragment getBehaviourFragment() {
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return getFragment(((AFmlBehaviourDecl) getASTNode()).getBehaviour());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return getFragment(((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getBehaviour());
		}
		return null;
	}

	protected PFmlParameters getFMLParameters() {
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return ((AFmlBehaviourDecl) getASTNode()).getFmlParameters();
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return ((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getFmlParameters();
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersFragment() {
		if (getFMLParameters() != null) {
			return getFragment(getFMLParameters());
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersLParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersRParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getRPar());
		}
		return null;
	}

}
