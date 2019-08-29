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

import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AAnonymousDestructorBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.AEmptyFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.ANamedDestructorBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.PBehaviourDeclaration;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *	   {anonymous_destructor} [annotations]:annotation* visibility? delete l_par formal_arguments_list? r_par flexo_behaviour_body |
 *	   {named_destructor} [annotations]:annotation* visibility? delete colon_colon [name]:identifier l_par formal_arguments_list? r_par flexo_behaviour_body |
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class DeletionSchemeNode extends FlexoBehaviourNode<PBehaviourDeclaration, DeletionScheme> {

	private static final Logger logger = Logger.getLogger(DeletionSchemeNode.class.getPackage().getName());

	public DeletionSchemeNode(PBehaviourDeclaration astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public DeletionSchemeNode(DeletionScheme DeletionScheme, MainSemanticsAnalyzer analyser) {
		super(DeletionScheme, analyser);
	}

	private boolean isAnonymous() {
		if (getASTNode() != null) {
			return getASTNode() instanceof AAnonymousDestructorBehaviourDeclaration;
		}
		else {
			return getModelObject().isAnonymous();
		}
	}

	@Override
	public DeletionScheme buildModelObjectFromAST(PBehaviourDeclaration astNode) {
		DeletionScheme returned = getFactory().newDeletionScheme();
		if (isAnonymous()) {
			returned.setAnonymous(true);
		}
		else {
			returned.setAnonymous(false);
			returned.setName(getName().getText());
		}
		returned.setVisibility(getVisibility(getVisibility()));

		ControlGraphFactory cgFactory = new ControlGraphFactory(getFlexoBehaviourBody(), getAnalyser());
		returned.setControlGraph(cgFactory.getControlGraph());
		addToChildren(cgFactory.getRootControlGraphNode());

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
			appendStaticContents("", "delete", SPACE, getDeleteFragment());
			if (!isAnonymous()) {
				appendStaticContents("::", getColonColonFragment());
				appendDynamicContents(() -> getModelObject().getName(), getNameFragment());
			}
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
			appendStaticContents("", "delete", SPACE);
			if (!isAnonymous()) {
				appendStaticContents("::");
				appendDynamicContents(() -> getModelObject().getName());
			}
			appendStaticContents("(");
			appendStaticContents(")");
			appendStaticContents(SPACE, "{");
			appendToChildPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, 0);
			appendStaticContents(LINE_SEPARATOR, "}");
		}
	}

	private RawSourceFragment getDeleteFragment() {
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDeclaration) {
			return getFragment(((AAnonymousDestructorBehaviourDeclaration) getASTNode()).getDelete());
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return getFragment(((ANamedDestructorBehaviourDeclaration) getASTNode()).getDelete());
		}
		return null;
	}

	private RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return getFragment(((ANamedDestructorBehaviourDeclaration) getASTNode()).getColonColon());
		}
		return null;
	}

}
