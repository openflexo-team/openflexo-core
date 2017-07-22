/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoRole;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractCreateFlexoRoleWizard<A extends AbstractCreateFlexoRole<A, MS>, MS extends ModelSlot>
		extends AbstractCreateFlexoPropertyWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoRoleWizard.class.getPackage().getName());

	protected static final String NO_ROLE_TYPE = "please_choose_a_role_type";

	public AbstractCreateFlexoRoleWizard(A action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected abstract AbstractDescribeFlexoRole makeDescriptionStep();

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_flexo_role");
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractDescribeFlexoRole getDescribeProperty() {
		return (AbstractDescribeFlexoRole) super.getDescribeProperty();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	public abstract class AbstractDescribeFlexoRole extends DescribeProperty {

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_flexo_role");
		}

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getFlexoRoleClass() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_ROLE_TYPE), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public String getRoleName() {
			return getPropertyName();
		}

		public void setRoleName(String roleName) {
			setPropertyName(roleName);
			getPropertyChangeSupport().firePropertyChange("roleName", null, roleName);
			checkValidity();
		}

		public PropertyCardinality getCardinality() {
			return getAction().getCardinality();
		}

		public void setCardinality(PropertyCardinality roleCardinality) {
			if (roleCardinality != getCardinality()) {
				PropertyCardinality oldValue = getCardinality();
				getAction().setCardinality(roleCardinality);
				getPropertyChangeSupport().firePropertyChange("cardinality", oldValue, roleCardinality);
			}
		}

		public MS getModelSlot() {
			return getAction().getModelSlot();
		}

		public void setModelSlot(MS modelSlot) {
			if (getModelSlot() != modelSlot) {
				ModelSlot<?> oldValue = getModelSlot();
				getAction().setModelSlot(modelSlot);
				fireModelSlotChanged(oldValue, modelSlot);
			}
		}

		protected void fireModelSlotChanged(ModelSlot<?> oldValue, ModelSlot<?> newValue) {
			getPropertyChangeSupport().firePropertyChange("modelSlot", oldValue, newValue);
			getPropertyChangeSupport().firePropertyChange("adressedFlexoMetaModel", null, getAdressedFlexoMetaModel());
			getPropertyChangeSupport().firePropertyChange("flexoRoleClass", null, getModelSlot());
			getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
			checkValidity();
		}

		public Class<? extends FlexoRole<?>> getFlexoRoleClass() {
			return getAction().getFlexoRoleClass();
		}

		public FlexoMetaModel<?> getAdressedFlexoMetaModel() {
			return getAction().getAdressedFlexoMetaModel();
		}

		public List<MS> getAvailableModelSlots() {
			return getAction().getAvailableModelSlots();
		}

		public TechnologyAdapter getTechnologyAdapterForModelSlot() {
			return getAction().getTechnologyAdapterForModelSlot();
		}

	}

}
