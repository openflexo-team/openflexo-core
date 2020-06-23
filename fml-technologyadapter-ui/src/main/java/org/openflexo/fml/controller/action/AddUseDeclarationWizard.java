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

import java.awt.Dimension;
import java.awt.Image;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AddUseDeclaration;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class AddUseDeclarationWizard extends FlexoActionWizard<AddUseDeclaration> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddUseDeclarationWizard.class.getPackage().getName());

	private static final String NO_TECHNOLOGY_ADAPTER = "please_choose_a_technology_adapter";
	private static final String NO_MODEL_SLOT_TYPE = "please_choose_a_model_slot_type";

	private final DescribeUseDeclaration describeUseDeclaration;

	private static final Dimension DIMENSIONS = new Dimension(600, 500);

	public AddUseDeclarationWizard(AddUseDeclaration action, FlexoController controller) {
		super(action, controller);
		addStep(describeUseDeclaration = new DescribeUseDeclaration());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("declare_use_of_model_slot");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.MODEL_SLOT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeUseDeclaration getDescribeModelSlot() {
		return describeUseDeclaration;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeUseDeclaration.fib")
	public class DescribeUseDeclaration extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AddUseDeclaration getAction() {
			return AddUseDeclarationWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_model_slot");
		}

		@Override
		public boolean isValid() {

			if (getTechnologyAdapter() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_TECHNOLOGY_ADAPTER), IssueMessageType.ERROR);
				return false;
			}
			else if (getModelSlotClass() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_MODEL_SLOT_TYPE), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public TechnologyAdapter<?> getTechnologyAdapter() {
			return getAction().getModelSlotTechnologyAdapter();
		}

		public void setTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
			if (getTechnologyAdapter() != technologyAdapter) {
				TechnologyAdapter<?> oldValue = getTechnologyAdapter();
				getAction().setModelSlotTechnologyAdapter(technologyAdapter);
				getPropertyChangeSupport().firePropertyChange("technologyAdapter", oldValue, technologyAdapter);
				getPropertyChangeSupport().firePropertyChange("modelSlotClass", null, getModelSlotClass());
				getPropertyChangeSupport().firePropertyChange("availableModelSlotTypes", null, getAvailableModelSlotTypes());
				checkValidity();
			}
		}

		public List<Class<? extends ModelSlot<?>>> getAvailableModelSlotTypes() {
			if (getTechnologyAdapter() != null) {
				return getTechnologyAdapter().getAvailableModelSlotTypes();
			}
			return null;
		}

		public Class<? extends ModelSlot<?>> getModelSlotClass() {
			return getAction().getModelSlotClass();
		}

		public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
			if (getModelSlotClass() != modelSlotClass) {
				Class<? extends ModelSlot<?>> oldValue = getModelSlotClass();
				getAction().setModelSlotClass(modelSlotClass);
				getPropertyChangeSupport().firePropertyChange("modelSlotClass", oldValue, modelSlotClass);
				checkValidity();
			}
		}

	}

}
