/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.technologyadapter;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.TechnologySpecificCustomType;

/**
 * This class represent the {@link BindingFactory} dedicated to handle technology-specific binding elements<br>
 * This is the place where we implements binding path element strategy for types handled by this technology.<br>
 * For example, any {@link FlexoRole} defined in this technology-specific adapter should by handled. <br>
 * Following methods should be implemented: getAccessibleSimplePathElements(BindingPathElement),
 * getAccessibleFunctionPathElements(BindingPathElement), makeSimplePathElement(BindingPathElement,String),
 * makeFunctionPathElement(BindingPathElement,Function,List<DataBinding<?>>)
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyAdapterBindingFactory extends JavaBindingFactory {
	static final Logger logger = Logger.getLogger(TechnologyAdapterBindingFactory.class.getPackage().getName());

	private final HashMap<BindingPathElement, HashMap<Object, SimplePathElement>> storedBindingPathElements;

	public TechnologyAdapterBindingFactory() {
		storedBindingPathElements = new HashMap<BindingPathElement, HashMap<Object, SimplePathElement>>();
	}

	protected final SimplePathElement getSimplePathElement(Object object, BindingPathElement parent) {
		HashMap<Object, SimplePathElement> storedValues = storedBindingPathElements.get(parent);
		if (storedValues == null) {
			storedValues = new HashMap<Object, SimplePathElement>();
			storedBindingPathElements.put(parent, storedValues);
		}
		SimplePathElement returned = storedValues.get(object);
		if (returned == null) {
			returned = makeSimplePathElement(object, parent);
			storedValues.put(object, returned);
		}
		return returned;
	}

	protected abstract SimplePathElement makeSimplePathElement(Object object, BindingPathElement parent);

	/**
	 * Return boolean indicating if this binding path element strategy should apply to supplied type
	 * 
	 * @param technologySpecificType
	 * @return
	 */
	public abstract boolean handleType(TechnologySpecificCustomType<?> technologySpecificType);

}
