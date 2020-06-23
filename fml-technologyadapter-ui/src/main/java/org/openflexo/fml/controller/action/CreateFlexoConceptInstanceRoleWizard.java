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

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoConceptInstanceRoleWizard extends AbstractCreateFlexoRoleWizard<CreateFlexoConceptInstanceRole, FMLRTModelSlot> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoConceptInstanceRoleWizard.class.getPackage().getName());

	private static final String NO_FLEXO_CONCEPT_TYPE = "please_choose_type_of_flexo_concept";

	public CreateFlexoConceptInstanceRoleWizard(CreateFlexoConceptInstanceRole action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected DescribeFlexoConceptInstanceRole makeDescriptionStep() {
		return new DescribeFlexoConceptInstanceRole();
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_flexo_concept_instance_role");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	@Override
	public DescribeFlexoConceptInstanceRole getDescribeProperty() {
		return (DescribeFlexoConceptInstanceRole) super.getDescribeProperty();
	}

	/**
	 * This step is used to set general data on role
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeFlexoConceptInstanceRole.fib")
	public class DescribeFlexoConceptInstanceRole extends AbstractDescribeFlexoRole {

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getFlexoConceptInstanceType() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_FLEXO_CONCEPT_TYPE), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		public VirtualModel getModelSlotVirtualModel() {
			return getAction().getModelSlotVirtualModel();
		}

		/*@Override
		public void setModelSlot(FMLRTModelSlot modelSlot) {
			super.setModelSlot(modelSlot);
		}*/

		@Override
		protected void fireModelSlotChanged(ModelSlot<?> oldValue, ModelSlot<?> newValue) {
			super.fireModelSlotChanged(oldValue, newValue);
			getPropertyChangeSupport().firePropertyChange("modelSlotVirtualModel", null, getModelSlotVirtualModel());
		}

		public FlexoConcept getFlexoConceptInstanceType() {
			return getAction().getFlexoConceptInstanceType();
		}

		public void setFlexoConceptInstanceType(FlexoConcept flexoConceptInstanceType) {
			if (getFlexoConceptInstanceType() != flexoConceptInstanceType) {
				FlexoConcept oldValue = getFlexoConceptInstanceType();
				getAction().setFlexoConceptInstanceType(flexoConceptInstanceType);
				getPropertyChangeSupport().firePropertyChange("flexoConceptInstanceType", oldValue, flexoConceptInstanceType);
				checkValidity();
			}
		}

		public boolean isUseModelSlot() {
			return getAction().isUseModelSlot();
		}

		public void setUseModelSlot(boolean useModelSlot) {
			if (isUseModelSlot() != useModelSlot) {
				boolean oldValue = isUseModelSlot();
				getAction().setUseModelSlot(useModelSlot);
				getPropertyChangeSupport().firePropertyChange("useModelSlot", oldValue, useModelSlot);
				getPropertyChangeSupport().firePropertyChange("modelSlot", null, getModelSlot());
				getPropertyChangeSupport().firePropertyChange("flexoRoleClass", null, getFlexoRoleClass());
				getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
				getPropertyChangeSupport().firePropertyChange("modelSlotVirtualModel", null, getModelSlotVirtualModel());
			}
		}

		public DataBinding<VirtualModelInstance<?, ?>> getVirtualModelInstance() {
			return getAction().getVirtualModelInstance();
		}

		public void setVirtualModelInstance(DataBinding<VirtualModelInstance<?, ?>> virtualModelInstance) {
			getAction().setVirtualModelInstance(virtualModelInstance);
			getPropertyChangeSupport().firePropertyChange("virtualModelInstance", null, virtualModelInstance);
			getPropertyChangeSupport().firePropertyChange("virtualModelType", null, getVirtualModelType());
		}

		@Override
		public CreateFlexoConceptInstanceRole getAction() {
			return super.getAction();
		}

		public VirtualModel getVirtualModelType() {
			return getAction().getVirtualModelType();
		}
	}

}
