/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
	 * Return the {@link VirtualModel} in which {@link FlexoConcept} of this {@link FMLObject} is defined
	 * 
	 * @return
	 */
	// TODO harmonize with Get Owner from FlexoConcept
	public VirtualModel getOwningVirtualModel();

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
			if (getOwningVirtualModel() != null && getOwningVirtualModel().getFMLModelFactory() != null) {
				return getOwningVirtualModel().getFMLModelFactory();
			}
			if (getFlexoConcept() instanceof VirtualModel && ((VirtualModel) getFlexoConcept()).getFMLModelFactory() != null) {
				return getFlexoConcept().getFMLModelFactory();
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
		public VirtualModel getResourceData() {
			if (this instanceof VirtualModelObject) {
				return ((VirtualModelObject) this).getVirtualModel();
			}
			if (getFlexoConcept() != null && getFlexoConcept().getOwner() != null) {
				return getFlexoConcept().getOwner();
			}
			return null;
		}

		@Override
		public VirtualModel getOwningVirtualModel() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getOwner();
			}
			return null;
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
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
