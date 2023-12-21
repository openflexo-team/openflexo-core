/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012, THALES SYSTEMES AEROPORTES - All Rights Reserved
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.ontology;

import java.util.List;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * Flexo Ontology.
 * 
 * Reified interface for handling multi-technological implementation of ontology-like modelling technologies
 * 
 * @author gbesancon
 */
@FIBPanel("Fib/FIBPlainOntologyEditor.fib")
public interface IFlexoOntology<TA extends TechnologyAdapter<TA>> extends IFlexoOntologyObject<TA>, IFlexoOntologyConceptContainer<TA> {

	/**
	 * Version of Ontology.
	 * 
	 * @return
	 */
	public String getVersion();

	/**
	 * Ontologies imported by Ontology.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntology<TA>> getImportedOntologies();

	/**
	 * Annotations upon Ontology.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyAnnotation> getAnnotations();

	/**
	 * Return all classes accessible in the context of this ontology.<br>
	 * This means that classes are also retrieved from imported ontologies (non-strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyClass<TA>> getAccessibleClasses();

	/**
	 * Return all individuals accessible in the context of this ontology.<br>
	 * This means that individuals are also retrieved from imported ontologies (non-strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyIndividual<TA>> getAccessibleIndividuals();

	/**
	 * Return all object properties accessible in the context of this ontology.<br>
	 * This means that properties are also retrieved from imported ontologies (non-strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyObjectProperty<TA>> getAccessibleObjectProperties();

	/**
	 * Return all data properties accessible in the context of this ontology.<br>
	 * This means that properties are also retrieved from imported ontologies (non-strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyDataProperty<TA>> getAccessibleDataProperties();

	/**
	 * Retrieve an ontology object from its URI, in the strict context of this ontology. That means that only objects declared in this
	 * ontology are subject to look up. If searched object is declared in an imported ontology for example, this method will not find it and
	 * will return null. Use {@link #getOntologyObject(String)} instead.
	 * 
	 * @param objectURI
	 * @return
	 */
	public abstract IFlexoOntologyConcept<TA> getDeclaredOntologyObject(String objectURI);

	/**
	 * Retrieve an class from its URI, in the strict context of this ontology. That means that only objects declared in this ontology are
	 * subject to look up. If searched object is declared in an imported ontology for example, this method will not find it and will return
	 * null. Use {@link #getClass()} instead.
	 * 
	 * 
	 * @param objectURI
	 * @return
	 */
	public abstract IFlexoOntologyClass<TA> getDeclaredClass(String classURI);

	/**
	 * Retrieve an individual from its URI, in the strict context of this ontology. That means that only objects declared in this ontology
	 * are subject to look up. If searched object is declared in an imported ontology for example, this method will not find it and will
	 * return null. Use {@link #getIndividual()} instead.
	 * 
	 * 
	 * 
	 * @param objectURI
	 * @return
	 */
	public abstract IFlexoOntologyIndividual<TA> getDeclaredIndividual(String individualURI);

	/**
	 * Retrieve an object property from its URI, in the strict context of this ontology. That means that only objects declared in this
	 * ontology are subject to look up. If searched object is declared in an imported ontology for example, this method will not find it and
	 * will return null. Use {@link #getObjectProperty()} instead.
	 * 
	 * 
	 * @param objectURI
	 * @return
	 */
	public abstract IFlexoOntologyObjectProperty<TA> getDeclaredObjectProperty(String propertyURI);

	/**
	 * Retrieve an datatype property from its URI, in the strict context of this ontology. That means that only objects declared in this
	 * ontology are subject to look up. If searched object is declared in an imported ontology for example, this method will not find it and
	 * will return null. Use {@link #getDataProperty()} instead.
	 * 
	 * @param objectURI
	 * @return
	 */
	public abstract IFlexoOntologyDataProperty<TA> getDeclaredDataProperty(String propertyURI);

	/**
	 * Retrieve a property from its URI, in the strict context of this ontology. That means that only objects declared in this ontology are
	 * subject to look up. If searched object is declared in an imported ontology for example, this method will not find it and will return
	 * null. Use {@link #getProperty()} instead.
	 * 
	 * @param objectURI
	 * @return
	 */
	public abstract IFlexoOntologyStructuralProperty<TA> getDeclaredProperty(String objectURI);

	/**
	 * Return the root concept accessible from the scope defined by this ontology (for example in OWL technology this is the owl:Thing
	 * concept, in Java this is java.lang.object, etc...)
	 * 
	 * @return
	 */
	public IFlexoOntologyClass<TA> getRootConcept();
}
