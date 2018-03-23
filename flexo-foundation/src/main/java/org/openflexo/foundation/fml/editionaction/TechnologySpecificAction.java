/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.editionaction;

import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

/**
 * 
 * Represents an {@link EditionAction} which address a specific technology referenced by a {@link ModelSlot} class<br>
 * 
 * @author sylvain
 *
 * @param <MS>
 *            Type of model slot which contractualize access to a given technology resource on which this action applies
 * @param <T>
 *            Type of assigned value
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(TechnologySpecificAction.TechnologySpecificActionImpl.class)
public abstract interface TechnologySpecificAction<MS extends ModelSlot<?>, T> extends AssignableAction<T> {

	/**
	 * Return the {@link TechnologyAdapter} were this {@link EditionAction} was registered
	 * 
	 * @return
	 */
	public TechnologyAdapter getModelSlotTechnologyAdapter();

	/**
	 * Return {@link Class} of model slot were this {@link EditionAction} was registered
	 * 
	 * @return
	 */
	public Class<? extends MS> getModelSlotClass();

	/**
	 * Compute and return assigned model slot asserting this action is assigned to requested {@link ModelSlot}<br>
	 * 
	 * Please not there is absolutely no guarantee that this {@link EditionAction} is assigned to a {@link ModelSlot}<br>
	 * 
	 * @return null if this {@link EditionAction} is not assigned to a {@link ModelSlot}
	 */
	public MS getAssignedModelSlot();

	public static abstract class TechnologySpecificActionImpl<MS extends ModelSlot<?>, T> extends AssignableActionImpl<T>
			implements TechnologySpecificAction<MS, T> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TechnologySpecificAction.class.getPackage().getName());

		@Override
		public LocalizedDelegate getLocales() {
			if (getModelSlotTechnologyAdapter() != null) {
				return getModelSlotTechnologyAdapter().getLocales();
			}
			return super.getLocales();
		}

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getImplementedInterface().getSimpleName() + getParametersStringRepresentation();
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return getImplementedInterface().getSimpleName() + "()";
		}*/

		protected final String getTechnologyAdapterIdentifier() {
			if (getModelSlotTechnologyAdapter() != null) {
				return getModelSlotTechnologyAdapter().getIdentifier();
			}
			return "FML";
		}

		/**
		 * Return {@link Class} of model slot were this {@link EditionAction} was registered
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		@Override
		public final Class<? extends MS> getModelSlotClass() {
			return (Class<? extends MS>) TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getClass(), TechnologySpecificAction.class, 0));
		}

		/**
		 * Return the {@link TechnologyAdapter} were this {@link EditionAction} was registered
		 * 
		 * @return
		 */
		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(getModelSlotClass());
			}
			return null;
		}

		/**
		 * Compute and return assigned model slot asserting this action is assigned to requested {@link ModelSlot}<br>
		 * 
		 * Please not there is absolutely no guarantee that this {@link EditionAction} is assigned to a {@link ModelSlot}<br>
		 * 
		 * @return null if this {@link EditionAction} is not assigned to a {@link ModelSlot}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public MS getAssignedModelSlot() {
			if (getModelSlotClass().isAssignableFrom(getAssignedFlexoProperty().getClass())) {
				return (MS) getAssignedFlexoProperty();
			}
			return null;
		}

	}

}
