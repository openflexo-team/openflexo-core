/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012, THALES SYSTEMES AEROPORTES - All Rights Reserved
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

package org.openflexo.foundation.ontology;

import java.util.List;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * Concept of Individual.
 * 
 * @author gbesancon
 * 
 */
@FIBPanel("Fib/FIBOntologyIndividualEditor.fib")
public interface IFlexoOntologyIndividual<TA extends TechnologyAdapter> extends IFlexoOntologyConcept<TA> {
	/**
	 * Return types of Individual.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyClass<TA>> getTypes();

	/**
	 * Add supplied type to the list of types implemented by this individual
	 * 
	 * @param aType
	 */
	// @Deprecated
	// public void addToTypes(IFlexoOntologyClass aType);
	/**
	 * Remove supplied type from the list of types implemented by this individual
	 * 
	 * @param aType
	 */
	// @Deprecated
	// public void removeFromTypes(IFlexoOntologyClass aType);
	/**
	 * Is this an Individual of aClass.
	 * 
	 * @param aClass
	 * @return
	 */
	public boolean isIndividualOf(IFlexoOntologyClass<TA> aClass);

	/**
	 * Property Values.
	 * 
	 * @return
	 */
	public List<? extends IFlexoOntologyPropertyValue<TA>> getPropertyValues();

	/**
	 * Return the {@link IFlexoOntologyPropertyValue} matching supplied property and defined for this individual<br>
	 * If no values were defined for supplied property, return null
	 * 
	 * @param property
	 * @return
	 */
	public IFlexoOntologyPropertyValue<TA> getPropertyValue(IFlexoOntologyStructuralProperty<TA> property);

	/**
	 * Add newValue as a value for supplied property<br>
	 * Return the {@link IFlexoOntologyPropertyValue} matching supplied property and defined for this individual<br>
	 * 
	 * @param property
	 * @param newValue
	 * @return
	 */
	public IFlexoOntologyPropertyValue<TA> addToPropertyValue(IFlexoOntologyStructuralProperty<TA> property, Object newValue);

	/**
	 * Remove valueToRemove from list of values for supplied property<br>
	 * Return the {@link IFlexoOntologyPropertyValue} matching supplied property and defined for this individual<br>
	 * If the supplied valueToRemove parameter was the only value defined for supplied property for this individual, return null
	 * 
	 * @param property
	 * @param valueToRemove
	 * @return
	 */
	public IFlexoOntologyPropertyValue<TA> removeFromPropertyValue(IFlexoOntologyStructuralProperty<TA> property, Object valueToRemove);
}
