/*
 * (c) Copyright 2010-2011 AgileBirds
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
 */
package org.openflexo.foundation.nature;

import org.openflexo.foundation.FlexoObject;

/**
 * Generic super interface defining the nature of any concept<br>
 * 
 * A nature might be seen as an interpretation of a given concept.
 * 
 * @author sylvain
 * 
 * @param <E>
 *            type of introspected concept
 */
public interface FlexoNature<E extends FlexoObject> {

	/**
	 * Return boolean indicating if supplied concept might be interpreted according to this nature
	 * 
	 * @param concept
	 * @return
	 */
	public boolean hasNature(E concept);

}
