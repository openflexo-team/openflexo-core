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
package org.openflexo.foundation.fml.controlgraph;

import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * Encodes an empty control graph
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(EmptyControlGraph.EmptyControlGraphImpl.class)
@XMLElement
public interface EmptyControlGraph extends FMLControlGraph {

	public static abstract class EmptyControlGraphImpl extends FMLControlGraphImpl implements EmptyControlGraph {

		@Override
		public void sequentiallyAppend(FMLControlGraph controlGraph) {
			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();

			// controlGraph.setOwnerContext(getOwnerContext());
			// owner.setControlGraph(controlGraph, getOwnerContext());

			// Following statement is really important, we need first to "disconnect" actual control graph
			// Before to build the new sequence !!!
			owner.setControlGraph(null, ownerContext);

			replaceWith(controlGraph, owner, ownerContext);

		}

	}
}
