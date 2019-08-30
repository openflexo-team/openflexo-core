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

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AAnonymousConstructorBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.AAnonymousDestructorBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AEmptyFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamedConstructorBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamedDestructorBehaviourDeclaration;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.PVisibility;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
public abstract class FlexoBehaviourNode<N extends Node, T extends FlexoBehaviour> extends FMLObjectNode<N, T, MainSemanticsAnalyzer> {

	private static final Logger logger = Logger.getLogger(FlexoBehaviourNode.class.getPackage().getName());

	public FlexoBehaviourNode(N astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public FlexoBehaviourNode(T property, MainSemanticsAnalyzer analyser) {
		super(property, analyser);
	}

	@Override
	public FlexoBehaviourNode<N, T> deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getModelObject().addToFlexoBehaviours(getModelObject());
		}
		if (getParent() instanceof FlexoConceptNode) {
			((FlexoConceptNode) getParent()).getModelObject().addToFlexoBehaviours(getModelObject());
		}
		return this;
	}

	public PFlexoBehaviourBody getFlexoBehaviourBody() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDeclaration) {
			return ((AAnonymousConstructorBehaviourDeclaration) getASTNode()).getFlexoBehaviourBody();
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDeclaration) {
			return ((ANamedConstructorBehaviourDeclaration) getASTNode()).getFlexoBehaviourBody();
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDeclaration) {
			return ((AAnonymousDestructorBehaviourDeclaration) getASTNode()).getFlexoBehaviourBody();
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return ((ANamedDestructorBehaviourDeclaration) getASTNode()).getFlexoBehaviourBody();
		}
		if (getASTNode() instanceof AMethodBehaviourDeclaration) {
			return ((AMethodBehaviourDeclaration) getASTNode()).getFlexoBehaviourBody();
		}
		return null;
	}

	protected boolean isAbstract() {
		if (getASTNode() != null) {
			return (getFlexoBehaviourBody() instanceof AEmptyFlexoBehaviourBody);
		}
		else {
			return getModelObject().isAbstract();
		}
	}

	/**
	 * Return Semi fragment (non-null only if behaviour body is not defined)
	 * 
	 * @return
	 */
	protected RawSourceFragment getSemiFragment() {
		if (getFlexoBehaviourBody() instanceof AEmptyFlexoBehaviourBody) {
			return getFragment(((AEmptyFlexoBehaviourBody) getFlexoBehaviourBody()).getSemi());
		}
		return null;
	}

	/**
	 * Return LBrc fragment (non-null only if not simple semi)
	 * 
	 * @return
	 */
	protected RawSourceFragment getLBrcFragment() {
		if (getFlexoBehaviourBody() instanceof ABlockFlexoBehaviourBody) {
			return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) getFlexoBehaviourBody()).getBlock()).getLBrc());
		}
		return null;
	}

	/**
	 * Return LBrc fragment (non-null only if not simple semi)
	 * 
	 * @return
	 */
	protected RawSourceFragment getRBrcFragment() {
		if (getFlexoBehaviourBody() instanceof ABlockFlexoBehaviourBody) {
			return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) getFlexoBehaviourBody()).getBlock()).getRBrc());
		}
		return null;
	}

	protected PVisibility getVisibility() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDeclaration) {
			return ((AAnonymousConstructorBehaviourDeclaration) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDeclaration) {
			return ((ANamedConstructorBehaviourDeclaration) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDeclaration) {
			return ((AAnonymousDestructorBehaviourDeclaration) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return ((ANamedDestructorBehaviourDeclaration) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof AMethodBehaviourDeclaration) {
			return ((AMethodBehaviourDeclaration) getASTNode()).getVisibility();
		}
		return null;
	}

	protected RawSourceFragment getVisibilityFragment() {
		if (getVisibility() != null) {
			return getFragment(getVisibility());
		}
		return null;
	}

	protected TIdentifier getName() {
		if (getASTNode() instanceof ANamedConstructorBehaviourDeclaration) {
			return ((ANamedConstructorBehaviourDeclaration) getASTNode()).getName();
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return ((ANamedDestructorBehaviourDeclaration) getASTNode()).getName();
		}
		if (getASTNode() instanceof AMethodBehaviourDeclaration) {
			return ((AMethodBehaviourDeclaration) getASTNode()).getName();
		}
		return null;
	}

	protected RawSourceFragment getNameFragment() {
		if (getName() != null) {
			return getFragment(getName());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDeclaration) {
			return getFragment(((AAnonymousConstructorBehaviourDeclaration) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDeclaration) {
			return getFragment(((ANamedConstructorBehaviourDeclaration) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDeclaration) {
			return getFragment(((AAnonymousDestructorBehaviourDeclaration) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return getFragment(((ANamedDestructorBehaviourDeclaration) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AMethodBehaviourDeclaration) {
			return getFragment(((AMethodBehaviourDeclaration) getASTNode()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDeclaration) {
			return getFragment(((AAnonymousConstructorBehaviourDeclaration) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDeclaration) {
			return getFragment(((ANamedConstructorBehaviourDeclaration) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDeclaration) {
			return getFragment(((AAnonymousDestructorBehaviourDeclaration) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDeclaration) {
			return getFragment(((ANamedDestructorBehaviourDeclaration) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AMethodBehaviourDeclaration) {
			return getFragment(((AMethodBehaviourDeclaration) getASTNode()).getRPar());
		}
		return null;
	}

}
