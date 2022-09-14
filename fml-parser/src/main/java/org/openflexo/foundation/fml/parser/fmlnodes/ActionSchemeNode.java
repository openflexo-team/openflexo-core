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
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 * 		{anonymous_constructor} [annotations]:annotation* visibility? create l_par formal_arguments_list? r_par flexo_behaviour_body |
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class ActionSchemeNode extends FlexoBehaviourNode<AMethodBehaviourDecl, ActionScheme> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ActionSchemeNode.class.getPackage().getName());

	public ActionSchemeNode(AMethodBehaviourDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public ActionSchemeNode(ActionScheme creationScheme, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(creationScheme, analyzer);
	}

	@Override
	public ActionScheme buildModelObjectFromAST(AMethodBehaviourDecl astNode) {
		ActionScheme returned = getFactory().newActionScheme();
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		returned.setAbstract(astNode.getKwAbstract() != null);

		// handleParameters(astNode);

		try {
			returned.setName(astNode.getName().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + astNode.getName().getText());
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

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		// @formatter:off	
		//append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent,
		//		FMLMetaData.class));
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		when(() -> isAbstract()).thenAppend(staticContents("","abstract", SPACE), getAbstractFragment());
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment());
		append(staticContents("("), getLParFragment());
		append(childrenContents("", "", () -> getModelObject().getParameters(), ","+SPACE, "", Indentation.DoNotIndent,
				FlexoBehaviourParameter.class));
		append(staticContents(")"), getRParFragment());
		when(() -> hasNoImplementation())
		.thenAppend(staticContents(";"), getSemiFragment())
		.elseAppend(staticContents(SPACE,"{", ""), getLBrcFragment())
		.elseAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, Indentation.Indent))
		.elseAppend(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());
		// @formatter:on

		/*appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		
		appendDynamicContents(() -> getModelObject().getName(), SPACE, getNameFragment());
		appendStaticContents("(", getLParFragment());
		appendStaticContents(")", getRParFragment());
		if (getFlexoBehaviourBody() instanceof AEmptyFlexoBehaviourBody) {
			appendStaticContents(";", getSemiFragment());
		}
		else {
			appendStaticContents(SPACE, "{", getLBrcFragment());
			appendToChildPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR,
					Indentation.DoNotIndent);
			appendStaticContents(LINE_SEPARATOR, "}", getRBrcFragment());
		}*/
	}

	// TODO: maybe abstract should be implemented at FlexoBehaviour level ???
	public boolean isAbstract() {
		if (getASTNode() != null) {
			return getASTNode().getKwAbstract() != null;
		}
		return getModelObject().isAbstract();
	}

	protected RawSourceFragment getAbstractFragment() {
		if (getASTNode() != null && getASTNode().getKwAbstract() != null) {
			return getFragment(getASTNode().getKwAbstract());
		}
		return null;
	}

}
