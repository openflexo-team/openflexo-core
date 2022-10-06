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
import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AManySuperTypeList;
import org.openflexo.foundation.fml.parser.node.AOneSuperTypeList;
import org.openflexo.foundation.fml.parser.node.ASuperClause;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PCompositeTident;
import org.openflexo.foundation.fml.parser.node.PSuperClause;
import org.openflexo.foundation.fml.parser.node.PSuperTypeList;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * Base class for FlexoConcept and VirtualModel, managing parent types definitions
 * 
 * @author sylvain
 */
public abstract class AbstractFlexoConceptNode<N extends Node, T extends FlexoConcept>
		extends FMLObjectNode<N, T, FMLCompilationUnitSemanticsAnalyzer> {

	public AbstractFlexoConceptNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public AbstractFlexoConceptNode(T concept, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(concept, analyzer);
	}

	protected void buildParentConcepts(FlexoConcept returned, PSuperClause superClause) {
		if (superClause instanceof ASuperClause) {
			buildParentConcepts(returned, ((ASuperClause) superClause).getSuperTypeList());
		}
	}

	private List<FlexoConceptInstanceType> parentTypes;

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
		if (parentTypes != null) {
			// System.out.println("finalizeDeserialization() for " + parentTypes + " in " + getASTNode());
			for (FlexoConceptInstanceType parentType : parentTypes) {
				// System.out.println(" > " + parentType);
				// System.out.println(" > " + parentType.getFlexoConcept());
				if (parentType != null && parentType.getFlexoConcept() != null) {
					try {
						// System.out.println(" >> " + getModelObject());
						getModelObject().addToParentFlexoConcepts(parentType.getFlexoConcept());
					} catch (InconsistentFlexoConceptHierarchyException e) {
						throwIssue("Inconsistent concept hierarchy", getSuperTypeListFragment());
					}
				}
				else if (parentType != null) {
					throwIssue("Parent concept " + parentType.getSerializationRepresentation() + " not found", getSuperTypeListFragment());
				}
			}
		}
	}

	private void buildParentConcepts(FlexoConcept returned, PSuperTypeList superTypeList) {
		parentTypes = new ArrayList<>();
		for (PCompositeTident pCompositeTident : extractIdentifiers(superTypeList)) {
			Type parentType = TypeFactory.makeType(pCompositeTident, getSemanticsAnalyzer().getTypingSpace());
			// FlexoConceptInstanceType parentType = getTypeFactory().lookupConceptNamed(getText(pCompositeTident),
			// getFragment(pCompositeTident));
			if (parentType instanceof FlexoConceptInstanceType) {
				parentTypes.add((FlexoConceptInstanceType) parentType);
			}
			else {
				throwIssue("Unexpected parent concept " + getText(pCompositeTident), getSuperTypeListFragment());
			}
		}
	}

	private List<PCompositeTident> extractIdentifiers(PSuperTypeList superTypeList) {
		List<PCompositeTident> returned = new ArrayList<>();
		appendIdentifiers(superTypeList, returned);
		return returned;
	}

	private void appendIdentifiers(PSuperTypeList superTypeList, List<PCompositeTident> l) {
		if (superTypeList instanceof AOneSuperTypeList) {
			l.add(((AOneSuperTypeList) superTypeList).getIdentifier());
		}
		else if (superTypeList instanceof AManySuperTypeList) {
			appendIdentifiers(((AManySuperTypeList) superTypeList).getSuperTypeList(), l);
			l.add(((AManySuperTypeList) superTypeList).getIdentifier());
		}
	}

	protected abstract RawSourceFragment getVisibilityFragment();

	protected abstract RawSourceFragment getNameFragment();

	protected abstract RawSourceFragment getLBrcFragment();

	protected abstract RawSourceFragment getRBrcFragment();

	protected abstract RawSourceFragment getSuperClauseFragment();

	protected abstract RawSourceFragment getExtendsFragment();

	protected abstract RawSourceFragment getSuperTypeListFragment();

	@Override
	public RawSourceFragment getFragment(FragmentContext context) {
		if (context != null) {
			switch (context) {
				case NAME:
					return getNameFragment();
				default:
					break;
			}
		}
		return super.getFragment(context);
	}
}
