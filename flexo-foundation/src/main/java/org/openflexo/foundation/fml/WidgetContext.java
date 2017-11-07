/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
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
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
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

package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;

/**
 * Widget Context for an object allowing to construct a widget to edit its value.
 * 
 * This is the required API to be able to programmatically build a GINA fib widget
 *
 * Created by charlie on 13/03/2017.
 */
public interface WidgetContext extends FlexoConceptObject {

	default WidgetType getWidget() {
		return null;
	}

	default DataBinding<?> getContainer() {
		return null;
	}

	default Type getType() {
		return null;
	}

	/**
	 * Return a String encoding a {@link DataBinding} which should get access to represented data from the context beeing represented by
	 * this
	 * 
	 * @return
	 */
	String getWidgetAccess();

	/**
	 * Return a String encoding a {@link DataBinding} which should get access to represented data definition (which is this object)
	 * 
	 * @return
	 */
	String getWidgetDefinitionAccess();

	/**
	 * Depending of type of data to represent, return a list of objects which may be used to represented data
	 * 
	 * @return
	 */
	public List<?> getListOfObjects();
}
