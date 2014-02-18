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
package org.openflexo.foundation.view;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

/**
 * A {@link ViewObject} is an abstract run-time concept (instance) for an object "living" in a {@link View} (instanceof a {@link ViewPoint})
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ViewObject.ViewObjectImpl.class)
public interface ViewObject extends FlexoProjectObject {

	/**
	 * Return the {@link View} where this object is declared and living
	 * 
	 * @return
	 */
	public abstract View getView();

	public abstract class ViewObjectImpl extends FlexoProjectObjectImpl implements FlexoProjectObject {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ViewObject.class.getPackage().getName());

	}

}
