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
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.AAnonymousDestructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.ANamedDestructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.PBehaviourDecl;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
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
public class DeletionSchemeNode extends FlexoBehaviourNode<PBehaviourDecl, DeletionScheme> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeletionSchemeNode.class.getPackage().getName());

	public DeletionSchemeNode(PBehaviourDecl astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public DeletionSchemeNode(DeletionScheme DeletionScheme, MainSemanticsAnalyzer analyser) {
		super(DeletionScheme, analyser);
	}

	private boolean isAnonymous() {
		if (getASTNode() != null) {
			return getASTNode() instanceof AAnonymousDestructorBehaviourDecl;
		}
		else {
			return getModelObject().isAnonymous();
		}
	}

	@Override
	public DeletionScheme buildModelObjectFromAST(PBehaviourDecl astNode) {
		DeletionScheme returned = getFactory().newDeletionScheme();
		if (isAnonymous()) {
			returned.setAnonymous(true);
		}
		else {
			returned.setAnonymous(false);
			returned.setName(getName().getText());
		}
		returned.setVisibility(getVisibility(getVisibility()));

		ControlGraphNode<?, ?> cgNode = getControlGraphFactory().makeControlGraphNode();
		if (cgNode != null) {
			returned.setControlGraph(cgNode.getModelObject());
			addToChildren(cgNode);
		}

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment());
		append(staticContents("delete"), getDeleteFragment());
		when(() -> !isAnonymous())
				.thenAppend(staticContents("::"), getColonColonFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getName()), getNameFragment());
		append(staticContents(SPACE, "(", ""), getLParFragment());
		append(staticContents(")"), getRParFragment());
		when(() -> isAbstract())
				.thenAppend(staticContents(";"), getSemiFragment())
				.elseAppend(staticContents(SPACE,"{", ""), getLBrcFragment())
				.elseAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, Indentation.Indent))
				.elseAppend(staticContents(LINE_SEPARATOR, "}", ""), getLBrcFragment());
		// @formatter:on

		/*appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
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
			appendToChildPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR,
					Indentation.DoNotIndent);
			appendStaticContents(LINE_SEPARATOR, "}", getRBrcFragment());
		}*/
	}

	private RawSourceFragment getDeleteFragment() {
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDecl) {
			return getFragment(((AAnonymousDestructorBehaviourDecl) getASTNode()).getKwDelete());
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDecl) {
			return getFragment(((ANamedDestructorBehaviourDecl) getASTNode()).getKwDelete());
		}
		return null;
	}

	private RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof ANamedDestructorBehaviourDecl) {
			return getFragment(((ANamedDestructorBehaviourDecl) getASTNode()).getColonColon());
		}
		return null;
	}

}
