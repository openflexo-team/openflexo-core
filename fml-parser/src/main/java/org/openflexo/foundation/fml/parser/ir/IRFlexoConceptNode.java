/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.ir;

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFlexoBehaviourDeclarationConceptBodyDeclaration;
import org.openflexo.foundation.fml.parser.node.AFlexoConceptConceptBodyDeclaration;
import org.openflexo.foundation.fml.parser.node.AFlexoConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.APropertyConceptBodyDeclaration;
import org.openflexo.foundation.fml.parser.node.PConceptBodyDeclaration;

public class IRFlexoConceptNode extends IRNode<FlexoConcept, AFlexoConceptDeclaration> {

	public IRFlexoConceptNode(AFlexoConceptDeclaration node, FMLSemanticsAnalyzer semanticsAnalyzer) {
		super(node, semanticsAnalyzer);

		for (PConceptBodyDeclaration pConceptBodyDeclaration : node.getConceptBodyDeclarations()) {
			if (pConceptBodyDeclaration instanceof APropertyConceptBodyDeclaration) {
				handleProperty((APropertyConceptBodyDeclaration) pConceptBodyDeclaration);
			}
			else if (pConceptBodyDeclaration instanceof AFlexoBehaviourDeclarationConceptBodyDeclaration) {
				handleBehaviour((AFlexoBehaviourDeclarationConceptBodyDeclaration) pConceptBodyDeclaration);
			}
			else if (pConceptBodyDeclaration instanceof AFlexoConceptConceptBodyDeclaration) {
				handleFlexoConcept((AFlexoConceptConceptBodyDeclaration) pConceptBodyDeclaration);
			}
		}
	}

	private void handleFlexoConcept(AFlexoConceptConceptBodyDeclaration flexoConceptDeclaration) {
		FlexoConcept concept = (FlexoConcept) getSemanticsAnalyzer().getRootNode().getParsedFMLObject(flexoConceptDeclaration);
		if (concept != null) {
			getFMLObject().addToEmbeddedFlexoConcepts(concept);
		}
		else {
			logger.warning("Could not find FMLObject for " + flexoConceptDeclaration);
		}
	}

	private void handleBehaviour(AFlexoBehaviourDeclarationConceptBodyDeclaration behaviourDeclaration) {
		FlexoBehaviour behaviour = (FlexoBehaviour) getSemanticsAnalyzer().getRootNode().getParsedFMLObject(behaviourDeclaration);
		if (behaviour != null) {
			getFMLObject().addToFlexoBehaviours(behaviour);
		}
		else {
			logger.warning("Could not find FMLObject for " + behaviourDeclaration);
		}
	}

	private void handleProperty(APropertyConceptBodyDeclaration propertyDeclaration) {
		FlexoProperty<?> property = (FlexoProperty) getSemanticsAnalyzer().getRootNode().getParsedFMLObject(propertyDeclaration);
		if (property != null) {
			getFMLObject().addToFlexoProperties(property);
		}
		else {
			logger.warning("Could not find FMLObject for " + propertyDeclaration);
		}
	}

	@Override
	FlexoConcept buildFMLObject() {
		FlexoConcept concept = getSemanticsAnalyzer().getFactory().newFlexoConcept();
		concept.setName(getNode().getIdentifier().getText());
		// System.out.println("******** Hop je cree un nouveau FlexoConcept " + getNode().getIdentifier().getText());
		// getFragment().printFragment();

		for (IRNode<?, ?> childNode : getChildren()) {
			if (childNode instanceof IRFlexoPropertyNode) {
				concept.addToFlexoProperties(((IRFlexoPropertyNode<?, ?>) childNode).getFMLObject());
			}
		}

		return concept;
	}

}
