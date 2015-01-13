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

import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

/**
 * Root class for any object which is part of the model of an {@link FlexoConcept}<br>
 * A {@link FlexoConceptObject} "lives" in a {@link FlexoConcept} ecosystem<br>
 * Note that you can safely invoke {@link #getFlexoConcept()} which should return non-null value.
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoConceptObject.FlexoConceptObjectImpl.class)
public interface FlexoConceptObject extends FMLObject {

	@Override
	public FMLModelFactory getFMLModelFactory();

	/**
	 * Return the {@link FlexoConcept} in which this {@link FMLObject} is defined
	 * 
	 * @return
	 */
	public FlexoConcept getFlexoConcept();

	/**
	 * Return the {@link AbstractVirtualModel} in which {@link FlexoConcept} of this {@link FMLObject} is defined
	 * 
	 * @return
	 */
	public AbstractVirtualModel<?> getOwningVirtualModel();

	/**
	 * Build and return a String encoding this {@link FMLObject} in FML textual language
	 * 
	 * @param context
	 * @return
	 */
	@Override
	public String getFMLRepresentation(FMLRepresentationContext context);

	public abstract class FlexoConceptObjectImpl extends FMLObjectImpl implements FlexoConceptObject {

		@Override
		public FMLModelFactory getFMLModelFactory() {
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getFMLModelFactory();
			}
			return getDeserializationFactory();
		}

		/**
		 * Return
		 * 
		 * @return
		 */
		@Override
		public abstract FlexoConcept getFlexoConcept();

		@Override
		public AbstractVirtualModel<?> getResourceData() {
			if (this instanceof VirtualModelObject) {
				return ((VirtualModelObject) this).getVirtualModel();
			}
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getOwner();
			}
			return null;
		}

		@Override
		public AbstractVirtualModel<?> getOwningVirtualModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getOwner();
			}
			return null;
		}

		@Override
		public String getStringRepresentation() {
			return (getOwningVirtualModel() != null ? getOwningVirtualModel().getStringRepresentation() : "null") + "#"
					+ (getFlexoConcept() != null ? getFlexoConcept().getName() : "null") + "." + getClass().getSimpleName();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return "<not_implemented:" + getStringRepresentation() + ">";
		}

	}
}
