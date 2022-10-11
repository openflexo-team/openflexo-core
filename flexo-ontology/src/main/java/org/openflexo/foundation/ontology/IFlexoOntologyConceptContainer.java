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

/**
 * Concept Container.
 * 
 * @author gbesancon
 */
public interface IFlexoOntologyConceptContainer<TA extends TechnologyAdapter<TA>> {

	/**
	 * Sub container of container.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyContainer<TA>> getSubContainers();

	/**
	 * Concepts defined by Ontology.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyConcept<TA>> getConcepts();

	/**
	 * DataTypes defined by Ontology.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyDataType<TA>> getDataTypes();

	/**
	 * Retrieve an ontology object from its URI, in the context of this container (if this container is an ontology, will lookup in ontology
	 * and recursively on imported ontologies)<br>
	 * The current container defines the scope, in which to lookup returned object. This method does NOT try to lookup object from outer
	 * scope ontologies.
	 * 
	 * @param objectURI
	 * @return
	 */
	public IFlexoOntologyConcept<TA> getOntologyObject(String objectNameOrURI);

	/**
	 * Retrieve an class from its URI or name, in the context of this container (if this container is an ontology, will lookup in ontology
	 * and recursively on imported ontologies)<br>
	 * The current container defines the scope, in which to lookup returned object. This method does NOT try to lookup object from outer
	 * scope ontologies.
	 * 
	 * @param objectURI
	 * @return
	 */
	public IFlexoOntologyClass<TA> getClass(String classNameOrURI);

	/**
	 * Retrieve an individual from its URI, in the context of this container (if this container is an ontology, will lookup in ontology and
	 * recursively on imported ontologies)<br>
	 * The current container defines the scope, in which to lookup returned object. This method does NOT try to lookup object from outer
	 * scope ontologies.
	 * 
	 * 
	 * @param objectURI
	 * @return
	 */
	public IFlexoOntologyIndividual<TA> getIndividual(String individualURI);

	/**
	 * Retrieve an object property from its URI, in the context of this container (if this container is an ontology, will lookup in ontology
	 * and recursively on imported ontologies)<br>
	 * The current container defines the scope, in which to lookup returned object. This method does NOT try to lookup object from outer
	 * scope ontologies.
	 * 
	 * @param objectURI
	 * @return
	 */
	public IFlexoOntologyObjectProperty<TA> getObjectProperty(String propertyURI);

	/**
	 * Retrieve an datatype property from its URI, in the context of this container (if this container is an ontology, will lookup in
	 * ontology and recursively on imported ontologies)<br>
	 * The current container defines the scope, in which to lookup returned object. This method does NOT try to lookup object from outer
	 * scope ontologies.
	 * 
	 * 
	 * @param objectURI
	 * @return
	 */
	public IFlexoOntologyDataProperty<TA> getDataProperty(String propertyURI);

	/**
	 * Retrieve a property from its URI, in the context of this container (if this container is an ontology, will lookup in ontology and
	 * recursively on imported ontologies)<br>
	 * The current container defines the scope, in which to lookup returned object. This method does NOT try to lookup object from outer
	 * scope ontologies.
	 * 
	 * 
	 * @param objectURI
	 * @return
	 */
	public IFlexoOntologyStructuralProperty<TA> getProperty(String objectURI);

	/**
	 * Return all classes explicitely defined in this container (strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyClass<TA>> getClasses();

	/**
	 * Return all individuals explicitely defined in this container (strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyIndividual<TA>> getIndividuals();

	/**
	 * Return all datatype properties explicitely defined in this container (strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyDataProperty<TA>> getDataProperties();

	/**
	 * Return all object properties explicitely defined in this container (strict mode)
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyObjectProperty<TA>> getObjectProperties();

}
