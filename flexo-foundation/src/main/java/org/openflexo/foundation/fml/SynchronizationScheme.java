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
package org.openflexo.foundation.fml;

import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link SynchronizationScheme} is applied to a {@link VirtualModelInstance} to automatically manage contained
 * {@link FlexoConceptInstance}
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/FML/SynchronizationSchemePanel.fib")
@ModelEntity
@ImplementationClass(SynchronizationScheme.SynchronizationSchemeImpl.class)
@XMLElement
public interface SynchronizationScheme extends AbstractActionScheme {

	public VirtualModel getSynchronizedVirtualModel();

	public void setSynchronizedVirtualModel(AbstractVirtualModel<?> virtualModel);

	public static abstract class SynchronizationSchemeImpl extends AbstractActionSchemeImpl implements SynchronizationScheme {

		@Override
		public VirtualModel getSynchronizedVirtualModel() {
			return (VirtualModel) getFlexoConcept();
		}

		@Override
		public void setSynchronizedVirtualModel(AbstractVirtualModel<?> virtualModel) {
			setFlexoConcept(virtualModel);
		}

	}
}
