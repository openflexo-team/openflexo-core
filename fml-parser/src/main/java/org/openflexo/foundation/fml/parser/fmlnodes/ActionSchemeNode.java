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
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDecl;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public class ActionSchemeNode extends FlexoBehaviourNode<AMethodBehaviourDecl, ActionScheme> {

	private static final Logger logger = Logger.getLogger(ActionSchemeNode.class.getPackage().getName());

	public ActionSchemeNode(AMethodBehaviourDecl astNode, FMLSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public ActionSchemeNode(ActionScheme behaviour, FMLSemanticsAnalyzer analyser) {
		super(behaviour, analyser);
	}

	@Override
	public ActionScheme buildModelObjectFromAST(AMethodBehaviourDecl astNode) {
		ActionScheme returned = getFactory().newActionScheme();
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		returned.setName(astNode.getName().getText());
		System.out.println("TODO: set return type to " + getTypeFactory().makeType(astNode.getType()));
		// returned.setType(getTypeFactory().makeType(astNode.getType()));
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
		appendStaticContents("coucou la methode " + SPACE);
		/**
		 * if (hasParsedVersion) { appendDynamicContents(() -> serializeType(getModelObject().getType()), SPACE, getTypeFragment());
		 * appendDynamicContents(() -> getModelObject().getName(), getNameFragment()); appendStaticContents(";", getSemiFragment()); } else
		 * { appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE); appendDynamicContents(() ->
		 * serializeType(getModelObject().getType()), SPACE); appendDynamicContents(() -> getModelObject().getName());
		 * appendStaticContents(";"); }
		 */
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
			return getFragment(getASTNode().getName());
		}
		return null;
	}

}
