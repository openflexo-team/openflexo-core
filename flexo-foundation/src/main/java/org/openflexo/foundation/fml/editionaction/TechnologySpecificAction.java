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
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * An edition action contributed by a technology adapter, written {@code TA::Action(name=value, ...)} in FML: {@code TA} is the
 * identifier under which the model slot class of the technology adapter is used in the compilation unit
 * ({@code use ... as TA;}), and {@code Action} the name of the action declared by that model slot class.
 * <p>
 * The type argument {@code MS} designates that model slot class ({@link #getModelSlotClass()}), from which the technology adapter is
 * retrieved ({@link #getModelSlotTechnologyAdapter()}). An action which cannot be resolved while parsing is kept as an
 * {@link UnresolvedTechnologySpecificAction}. The actions applying on a given object, written with a clause {@code in (receiver)}, are
 * {@link TechnologySpecificActionDefiningReceiver}s; fetch requests ({@link AbstractFetchRequest}) are among them.
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
public abstract interface TechnologySpecificAction<MS extends ModelSlot<?,?>, T> extends AssignableAction<T> {

	/**
	 * Return the {@link TechnologyAdapter} of the model slot class declaring this action
	 *
	 * @return the technology adapter, null when no service manager is available
	 */
	public TechnologyAdapter getModelSlotTechnologyAdapter();

	/**
	 * Return the model slot class declaring this action, given by the type argument {@code MS}
	 *
	 * @return the model slot class
	 */
	public Class<? extends MS> getModelSlotClass();

	/**
	 * Return the model slot assigned by this action, when this action is the right-hand side of an assignation whose target is a model
	 * slot of type {@code MS}.
	 * <p>
	 * Beware: this method must only be called when this action is the right-hand side of the assignation of a property (see
	 * {@link #getAssignedFlexoProperty()}); otherwise it throws a {@link NullPointerException}.
	 *
	 * @return the assigned model slot, null when the assigned property is not a model slot of type {@code MS}
	 */
	public MS getAssignedModelSlot();

	public static abstract class TechnologySpecificActionImpl<MS extends ModelSlot<?,?>, T> extends AssignableActionImpl<T>
			implements TechnologySpecificAction<MS, T> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TechnologySpecificAction.class.getPackage().getName());

		/**
		 * Return a string representation suitable for a common user<br>
		 * This representation will be used in all GUIs
		 */
		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getImplementedInterface().getSimpleName() + getParametersStringRepresentation();
		}

		protected final String getTechnologyAdapterIdentifier() {
			if (getModelSlotTechnologyAdapter() != null) {
				return getModelSlotTechnologyAdapter().getIdentifier();
			}
			return "FML";
		}

		@SuppressWarnings("unchecked")
		@Override
		public final Class<? extends MS> getModelSlotClass() {
			return (Class<? extends MS>) TypeUtils.getBaseClass(TypeUtils.getTypeArgument(getClass(), TechnologySpecificAction.class, 0));
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(getModelSlotClass());
			}
			return null;
		}

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
