/** Copyright (c) 2012, THALES SYSTEMES AEROPORTES - All Rights Reserved
 * Author : Gilles Besançon
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or 
 * combining it with eclipse EMF (or a modified version of that library), 
 * containing parts covered by the terms of EPL 1.0, the licensors of this 
 * Program grant you additional permission to convey the resulting work.
 *
 * Contributors :
 *
 */
package org.openflexo.foundation.ontology.util;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyConcept;
import org.openflexo.foundation.ontology.IFlexoOntologyConceptContainer;
import org.openflexo.foundation.ontology.IFlexoOntologyContainer;
import org.openflexo.foundation.ontology.IFlexoOntologyDataType;
import org.openflexo.foundation.ontology.IFlexoOntologyFeature;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.util.visitor.ToStringVisitor;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * Utilities for FlexoOntology.
 * 
 * @author gbesancon
 */
public class FlexoOntologyUtility {

	/**
	 * To String.
	 * 
	 * @param flexoOntology
	 * @return
	 */
	public static <TA extends TechnologyAdapter> String toString(IFlexoOntology<TA> flexoOntology) {
		StringBuilder builder = new StringBuilder();
		try {
			builder.append("Ontology : ");
			builder.append(flexoOntology.getName());
			builder.append(" (");
			builder.append(flexoOntology.getURI());
			builder.append(")\n");
			for (IFlexoOntologyConcept<TA> concept : flexoOntology.getConcepts()) {
				builder.append(concept.accept(new ToStringVisitor()));
				builder.append("\n");
			}
			for (IFlexoOntologyDataType<TA> dataType : flexoOntology.getDataTypes()) {
				builder.append(dataType.getName());
				builder.append(" (");
				builder.append(dataType.getURI());
				builder.append(")\n");
			}
			for (IFlexoOntologyContainer<TA> subContainer : flexoOntology.getSubContainers()) {
				builder.append(toString(subContainer));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder.toString();
	}

	/**
	 * To String.
	 * 
	 * @param container
	 * @return
	 */
	protected static <TA extends TechnologyAdapter> String toString(IFlexoOntologyContainer<TA> container) {
		StringBuilder builder = new StringBuilder();
		builder.append("Container : ");
		builder.append(container.getName());
		builder.append("\n");
		for (IFlexoOntologyConcept<TA> concept : container.getConcepts()) {
			builder.append(concept.accept(new ToStringVisitor()));
			builder.append("\n");
		}
		for (IFlexoOntologyDataType<TA> dataType : container.getDataTypes()) {
			builder.append(dataType.getName());
			builder.append(" (");
			builder.append(dataType.getURI());
			builder.append(")\n");
		}
		for (IFlexoOntologyContainer<TA> subContainer : container.getSubContainers()) {
			builder.append(toString(subContainer));
		}
		return builder.toString();
	}

	/**
	 * Get All Concepts.
	 * 
	 * @param container
	 * @return
	 */
	public static <TA extends TechnologyAdapter> List<IFlexoOntologyConcept<TA>> getAllConcepts(IFlexoOntologyConceptContainer<TA> container) {
		List<IFlexoOntologyConcept<TA>> result = new ArrayList<IFlexoOntologyConcept<TA>>();
		result.addAll(container.getConcepts());
		for (IFlexoOntologyConceptContainer<TA> subContainer : container.getSubContainers()) {
			result.addAll(getAllConcepts(subContainer));
		}
		return result;
	}

	/**
	 * Get Class from uri.
	 * 
	 * @param flexoOntology
	 * @param uri
	 * @return
	 */
	public static <TA extends TechnologyAdapter> IFlexoOntologyClass<TA> getClass(IFlexoOntology<TA> flexoOntology, String uri) {
		IFlexoOntologyClass<TA> result = null;
		for (IFlexoOntologyConcept<TA> concept : getAllConcepts(flexoOntology)) {
			if (concept instanceof IFlexoOntologyClass) {
				if (concept.getURI().equalsIgnoreCase(uri)) {
					result = (IFlexoOntologyClass<TA>) concept;
				}
			}
		}
		return result;
	}

	/**
	 * Get Individual from uri.
	 * 
	 * @param flexoOntology
	 * @param uri
	 * @return
	 */
	public static <TA extends TechnologyAdapter> IFlexoOntologyIndividual<TA> getIndividual(IFlexoOntology<TA> flexoOntology, String uri) {
		IFlexoOntologyIndividual<TA> result = null;
		for (IFlexoOntologyConcept<TA> concept : getAllConcepts(flexoOntology)) {
			if (concept instanceof IFlexoOntologyIndividual) {
				if (concept.getURI().equalsIgnoreCase(uri)) {
					result = (IFlexoOntologyIndividual<TA>) concept;
				}
			}
		}
		return result;
	}

	/**
	 * Get Individual of type.
	 * 
	 * @param flexoOntology
	 * @param uri
	 * @return
	 */
	public static <TA extends TechnologyAdapter> List<IFlexoOntologyIndividual<TA>> getIndividualOfType(IFlexoOntology<TA> flexoOntology,
			IFlexoOntologyClass<TA> emfClass) {
		List<IFlexoOntologyIndividual<TA>> result = new ArrayList<IFlexoOntologyIndividual<TA>>();
		for (IFlexoOntologyConcept<TA> concept : getAllConcepts(flexoOntology)) {
			if (concept instanceof IFlexoOntologyIndividual) {
				if (((IFlexoOntologyIndividual<TA>) concept).isIndividualOf(emfClass)) {
					result.add((IFlexoOntologyIndividual<TA>) concept);
				}
			}
		}
		return result;
	}

	/**
	 * Get Feature from uri.
	 * 
	 * @param flexoOntology
	 * @param uri
	 * @return
	 */
	public static <TA extends TechnologyAdapter> IFlexoOntologyFeature<TA> getFeature(IFlexoOntology<TA> flexoOntology, String uri) {
		IFlexoOntologyFeature<TA> result = null;
		for (IFlexoOntologyConcept<TA> concept : getAllConcepts(flexoOntology)) {
			if (concept instanceof IFlexoOntologyFeature) {
				if (concept.getURI().equalsIgnoreCase(uri)) {
					result = (IFlexoOntologyFeature<TA>) concept;
				}
			}
		}
		return result;
	}
}
