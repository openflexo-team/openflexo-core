/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.ontology.technologyadapter;

import java.util.Hashtable;
import java.util.logging.Logger;

import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.ontology.SubClassOfClass;
import org.openflexo.foundation.ontology.SubPropertyOfProperty;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyContextManager;

/**
 * A {@link TechnologyContextManager} dedicated to a technology conform to FlexoOntogy API
 * 
 * A {@link FlexoOntologyTechnologyContextManager} manages for a technology, and shared by all {@link FlexoResourceCenter} declared in the
 * scope of {@link FlexoResourceCenterService}, all references and links between all resources managed by a given technology, it their
 * original technical space.
 * 
 * This class is responsible for maintaining consistency of related technology, in its original technological space (maintaining technical
 * links between all technology-specific stuff)
 * 
 * @author sylvain
 * 
 */
public abstract class FlexoOntologyTechnologyContextManager<TA extends TechnologyAdapter<TA>> extends TechnologyContextManager<TA> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoOntologyTechnologyContextManager.class.getPackage().getName());

	protected Hashtable<IFlexoOntologyClass<TA>, IndividualOfClass<TA, ?, ?>> individualsOfClass;
	protected Hashtable<IFlexoOntologyClass<TA>, SubClassOfClass<TA>> subclassesOfClass;
	protected Hashtable<IFlexoOntologyStructuralProperty<TA>, SubPropertyOfProperty<TA>> subpropertiesOfProperty;

	public IndividualOfClass<TA, ?, ?> getIndividualOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		if (individualsOfClass.get(anOntologyClass) != null) {
			return individualsOfClass.get(anOntologyClass);
		}
		else {
			try {
				IndividualOfClass<TA, ?, ?> returned = makeIndividualOfClass(anOntologyClass); // new IndividualOfClass<>(anOntologyClass);
				individualsOfClass.put(anOntologyClass, returned);
				return returned;
			} catch (ClassCastException e) {
				logger.warning(e.getMessage());
				return null;
			}
		}
	}

	public abstract IndividualOfClass<TA, ?, ?> makeIndividualOfClass(IFlexoOntologyClass<TA> anOntologyClass);

	public SubClassOfClass<TA> getSubClassOfClass(IFlexoOntologyClass<TA> anOntologyClass) {
		if (subclassesOfClass.get(anOntologyClass) != null) {
			return subclassesOfClass.get(anOntologyClass);
		}
		else {
			SubClassOfClass<TA> returned = new SubClassOfClass<>(anOntologyClass);
			subclassesOfClass.put(anOntologyClass, returned);
			return returned;
		}
	}

	public SubPropertyOfProperty<TA> getSubPropertyOfProperty(IFlexoOntologyStructuralProperty<TA> anOntologyProperty) {
		if (subpropertiesOfProperty.get(anOntologyProperty) != null) {
			return subpropertiesOfProperty.get(anOntologyProperty);
		}
		else {
			SubPropertyOfProperty<TA> returned = new SubPropertyOfProperty<>(anOntologyProperty);
			subpropertiesOfProperty.put(anOntologyProperty, returned);
			return returned;
		}
	}

	public FlexoOntologyTechnologyContextManager(TA adapter, FlexoResourceCenterService resourceCenterService) {
		super(adapter, resourceCenterService);
		individualsOfClass = new Hashtable<>();
		subclassesOfClass = new Hashtable<>();
		subpropertiesOfProperty = new Hashtable<>();
	}

}
