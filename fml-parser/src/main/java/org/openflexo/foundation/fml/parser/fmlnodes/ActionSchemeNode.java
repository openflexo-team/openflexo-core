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

import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AEmptyFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDeclaration;

/**
 * <pre>
 * 		{anonymous_constructor} [annotations]:annotation* visibility? create l_par formal_arguments_list? r_par flexo_behaviour_body |
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class ActionSchemeNode extends FlexoBehaviourNode<AMethodBehaviourDeclaration, ActionScheme> {

	private static final Logger logger = Logger.getLogger(ActionSchemeNode.class.getPackage().getName());

	public ActionSchemeNode(AMethodBehaviourDeclaration astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public ActionSchemeNode(ActionScheme creationScheme, MainSemanticsAnalyzer analyser) {
		super(creationScheme, analyser);
	}

	@Override
	public ActionScheme buildModelObjectFromAST(AMethodBehaviourDeclaration astNode) {
		ActionScheme returned = getFactory().newActionScheme();
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		returned.setName(astNode.getName().getText());

		ControlGraphFactory cgFactory = new ControlGraphFactory(getFlexoBehaviourBody(), getAnalyser());
		if (cgFactory.getControlGraph() != null) {
			returned.setControlGraph(cgFactory.getControlGraph());
			addToChildren(cgFactory.getRootControlGraphNode());
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		if (hasParsedVersion && getVisibilityFragment() != null) {
			appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		}
		else {
			appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE);
		}
		if (hasParsedVersion) {
			appendDynamicContents(() -> getModelObject().getName(), SPACE, getNameFragment());
			appendStaticContents("(", getLParFragment());
			appendStaticContents(")", getRParFragment());
			if (getFlexoBehaviourBody() instanceof AEmptyFlexoBehaviourBody) {
				appendStaticContents(";", getSemiFragment());
			}
			else {
				appendStaticContents(SPACE, "{", getLBrcFragment());
				appendToChildPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, 0);
				appendStaticContents(LINE_SEPARATOR, "}", getRBrcFragment());
			}
		}
		else {
			appendDynamicContents(() -> getModelObject().getName(), SPACE);
			appendStaticContents("(");
			appendStaticContents(")");
			appendStaticContents(SPACE, "{");
			appendToChildPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, 0);
			appendStaticContents(LINE_SEPARATOR, "}");
		}
	}

}
