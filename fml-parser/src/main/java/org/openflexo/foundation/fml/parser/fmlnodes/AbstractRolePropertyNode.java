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

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFmlInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedFmlParameters;
import org.openflexo.foundation.fml.parser.node.PFmlParameters;
import org.openflexo.foundation.fml.parser.node.PInnerConceptDecl;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractRolePropertyNode<N extends PInnerConceptDecl, R extends FlexoRole<?>> extends FlexoPropertyNode<N, R> {

	private static final Logger logger = Logger.getLogger(AbstractRolePropertyNode.class.getPackage().getName());

	public AbstractRolePropertyNode(N astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public AbstractRolePropertyNode(R role, MainSemanticsAnalyzer analyser) {
		super(role, analyser);
	}

	protected String serializeFlexoRoleName(FlexoRole<?> role) {
		return role.getFMLKeyword(getFactory());
		// return role.getImplementedInterface().getSimpleName();
	}

	protected boolean isFullQualified() {
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return true;
		}
		return false;
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

	protected RawSourceFragment getVisibilityFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getVisibility());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getVisibility());
		}
		return null;
	}

	protected RawSourceFragment getTypeFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getType());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getType());
		}
		return null;
	}

	protected RawSourceFragment getCardinalityFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getCardinality());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getCardinality());
		}
		return null;
	}

	protected RawSourceFragment getNameFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getIdentifier());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getIdentifier());
		}
		return null;
	}

	protected RawSourceFragment getWithFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getKwWith());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getKwWith());
		}
		return null;
	}

	protected RawSourceFragment getTaIdFragment() {
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getTaId());
		}
		return null;
	}

	protected RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getColonColon());
		}
		return null;
	}

	protected RawSourceFragment getRoleFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getRole());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getRole());
		}
		return null;
	}

	protected RawSourceFragment getSemiFragment() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return getFragment(((AFmlInnerConceptDecl) getASTNode()).getSemi());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return getFragment(((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getSemi());
		}
		return null;
	}

	protected PFmlParameters getFMLParameters() {
		if (getASTNode() instanceof AFmlInnerConceptDecl) {
			return ((AFmlInnerConceptDecl) getASTNode()).getFmlParameters();
		}
		if (getASTNode() instanceof AFmlFullyQualifiedInnerConceptDecl) {
			return ((AFmlFullyQualifiedInnerConceptDecl) getASTNode()).getFmlParameters();
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersFragment() {
		if (getFMLParameters() != null) {
			return getFragment(getFMLParameters());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getRPar());
		}
		return null;
	}

}
