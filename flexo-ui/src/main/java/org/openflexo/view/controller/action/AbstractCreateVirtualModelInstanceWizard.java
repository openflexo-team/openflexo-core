/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTModelSlotInstanceConfiguration;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.AbstractCreateVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.FreeModelSlotInstanceConfiguration;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlotInstanceConfiguration;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractCreateVirtualModelInstanceWizard<A extends AbstractCreateVirtualModelInstance<?, ?, ?, ?>> extends
		FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateVirtualModelInstanceWizard.class.getPackage().getName());

	protected final A action;

	private final AbstractChooseVirtualModel<?> chooseVirtualModel;
	private final List<ConfigureModelSlot<?, ?>> modelSlotConfigurationSteps;
	private AbstractChooseAndConfigureCreationScheme<?> chooseAndConfigureCreationScheme = null;

	public AbstractCreateVirtualModelInstanceWizard(A action, FlexoController controller) {
		super(controller);
		this.action = action;
		modelSlotConfigurationSteps = new ArrayList<ConfigureModelSlot<?, ?>>();
		addStep(chooseVirtualModel = makeChooseVirtualModel());
	}

	protected abstract AbstractChooseVirtualModel<?> makeChooseVirtualModel();

	protected abstract AbstractChooseAndConfigureCreationScheme<?> makeChooseAndConfigureCreationScheme();

	private ConfigureModelSlot<?, ?> makeConfigureModelSlotStep(ModelSlot<?> ms) {
		if (ms instanceof TypeAwareModelSlot) {
			return new ConfigureTypeAwareModelSlot((TypeAwareModelSlot) ms);
		} else if (ms instanceof FreeModelSlot) {
			return new ConfigureFreeModelSlot((FreeModelSlot) ms);
		} else if (ms instanceof FMLRTModelSlot) {
			return new ConfigureVirtualModelModelSlot((FMLRTModelSlot) ms);
		} else {
			logger.warning("Could not instantiate ConfigureModelSlot for " + ms);
			return null;
		}
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	public abstract class AbstractChooseVirtualModel<VM extends AbstractVirtualModel<VM>> extends WizardStep {

		public A getAction() {
			return action;
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("choose_virtual_model");
		}

		@Override
		public boolean isValid() {
			if (getVirtualModel() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_virtual_model_type_selected"), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_virtual_model_instance_name_defined"), IssueMessageType.ERROR);
				return false;
			}

			if (StringUtils.isEmpty(getNewVirtualModelInstanceTitle())) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_virtual_model_instance_title_defined"), IssueMessageType.ERROR);
				return false;
			}
			if (!action.isValidVirtualModelInstanceName(getNewVirtualModelInstanceName())) {
				setIssueMessage(FlexoLocalization.localizedForKey("a_virtual_model_instance_with_that_name_already_exists"),
						IssueMessageType.ERROR);
				return false;
			}

			if (!getNewVirtualModelInstanceName().equals(JavaUtils.getClassName(getNewVirtualModelInstanceName()))
					&& !getNewVirtualModelInstanceName().equals(JavaUtils.getVariableName(getNewVirtualModelInstanceName()))) {
				setIssueMessage(FlexoLocalization.localizedForKey("discouraged_name_for_new_virtual_model_instance"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public String getNewVirtualModelInstanceName() {
			return action.getNewVirtualModelInstanceName();
		}

		public void setNewVirtualModelInstanceName(String newVirtualModelInstanceName) {
			if (!newVirtualModelInstanceName.equals(getNewVirtualModelInstanceName())) {
				String oldValue = getNewVirtualModelInstanceName();
				String oldTitleValue = getNewVirtualModelInstanceTitle();
				action.setNewVirtualModelInstanceName(newVirtualModelInstanceName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceName", oldValue, newVirtualModelInstanceName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceTitle", oldTitleValue,
						getNewVirtualModelInstanceTitle());
				checkValidity();
			}
		}

		public String getNewVirtualModelInstanceTitle() {
			return action.getNewVirtualModelInstanceTitle();
		}

		public void setNewVirtualModelInstanceTitle(String newVirtualModelInstanceTitle) {
			if (!newVirtualModelInstanceTitle.equals(getNewVirtualModelInstanceTitle())) {
				String oldValue = getNewVirtualModelInstanceTitle();
				String oldNameValue = getNewVirtualModelInstanceName();
				action.setNewVirtualModelInstanceTitle(newVirtualModelInstanceTitle);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceTitle", oldValue, newVirtualModelInstanceTitle);
				getPropertyChangeSupport()
						.firePropertyChange("newVirtualModelInstanceName", oldNameValue, getNewVirtualModelInstanceName());
				checkValidity();
			}
		}

		public VM getVirtualModel() {
			return (VM) action.getVirtualModel();
		}

		public void setVirtualModel(VM virtualModel) {
			if (virtualModel != getVirtualModel()) {
				AbstractVirtualModel<?> oldValue = getVirtualModel();
				((AbstractCreateVirtualModelInstance) action).setVirtualModel(virtualModel);
				getPropertyChangeSupport().firePropertyChange("virtualModel", oldValue, virtualModel);
				checkValidity();
			}
		}

		@Override
		public boolean isTransitionalStep() {
			if (getVirtualModel() != null && getVirtualModel().getModelSlots().size() == 0 && !getVirtualModel().hasCreationScheme()) {
				return false;
			}
			return true;
		}

		@Override
		public void performTransition() {
			// We have now to update all steps according to chosen VirtualModel
			// Two possibilities:
			// - either chosen VirtualModel defines some CreationScheme, and we use it
			// - otherwise, we configurate all model slots
			if (chooseVirtualModel.getVirtualModel().hasCreationScheme()) {
				chooseAndConfigureCreationScheme = makeChooseAndConfigureCreationScheme();
				addStep(chooseAndConfigureCreationScheme);
			} else {
				for (ModelSlot<?> ms : chooseVirtualModel.getVirtualModel().getModelSlots()) {
					ConfigureModelSlot<?, ?> step = makeConfigureModelSlotStep(ms);
					if (step != null) {
						modelSlotConfigurationSteps.add(step);
						addStep(step);
					}
				}
			}
		}

		@Override
		public void discardTransition() {
			for (ConfigureModelSlot<?, ?> step : modelSlotConfigurationSteps) {
				removeStep(step);
			}
			modelSlotConfigurationSteps.clear();
			if (chooseAndConfigureCreationScheme != null) {
				removeStep(chooseAndConfigureCreationScheme);
				chooseAndConfigureCreationScheme = null;
			}
		}
	}

	/**
	 * This abstract generic step is used to configure a model slot
	 * 
	 * @author sylvain
	 * 
	 */
	public abstract class ConfigureModelSlot<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<?>> extends
			WizardStep implements PropertyChangeListener {

		private final MS modelSlot;
		private final ModelSlotInstanceConfiguration<MS, RD> configuration;

		public ConfigureModelSlot(MS modelSlot) {
			this.modelSlot = modelSlot;
			configuration = (ModelSlotInstanceConfiguration<MS, RD>) getAction().getModelSlotInstanceConfiguration(getModelSlot());
			configuration.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// System.out.println("propertyChange() in " + getClass());
			checkValidity();
		}

		public A getAction() {
			return action;
		}

		public MS getModelSlot() {
			return modelSlot;
		}

		public ModelSlotInstanceConfiguration<MS, RD> getConfiguration() {
			return configuration;
		}

		@Override
		public boolean isValid() {
			boolean isValid = getConfiguration().isValidConfiguration();
			if (!isValid) {
				setIssueMessage(getConfiguration().getErrorMessage(), IssueMessageType.ERROR);
			} else {
				setIssueMessage(FlexoLocalization.localizedForKey("valid_configuration"), IssueMessageType.INFO);
			}
			return isValid;
		}

		public ImageIcon getTechnologyIcon() {
			return getController().getTechnologyAdapterController(getModelSlot().getModelSlotTechnologyAdapter()).getTechnologyBigIcon();
		}
	}

	/**
	 * This step is used to configure a type-aware model slot
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ConfigureTypeAwareModelSlotInstance.fib")
	public class ConfigureTypeAwareModelSlot<M extends FlexoModel<M, MM> & TechnologyObject<?>, MM extends FlexoMetaModel<MM> & TechnologyObject<?>>
			extends ConfigureModelSlot<TypeAwareModelSlot<M, MM>, M> {

		public ConfigureTypeAwareModelSlot(TypeAwareModelSlot<M, MM> modelSlot) {
			super(modelSlot);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_type_aware_model_slot") + " : " + getModelSlot().getName();
		}

		@Override
		public TypeAwareModelSlotInstanceConfiguration<M, MM, TypeAwareModelSlot<M, MM>> getConfiguration() {
			return (TypeAwareModelSlotInstanceConfiguration<M, MM, TypeAwareModelSlot<M, MM>>) super.getConfiguration();
		}

	}

	/**
	 * This step is used to configure a type-aware model slot
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ConfigureFreeModelSlotInstance.fib")
	public class ConfigureFreeModelSlot<RD extends ResourceData<RD> & TechnologyObject<?>> extends
			ConfigureModelSlot<FreeModelSlot<RD>, RD> {

		public ConfigureFreeModelSlot(FreeModelSlot<RD> modelSlot) {
			super(modelSlot);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_free_model_slot") + " : " + getModelSlot().getName();
		}

		@Override
		public FreeModelSlotInstanceConfiguration<RD, FreeModelSlot<RD>> getConfiguration() {
			return (FreeModelSlotInstanceConfiguration<RD, FreeModelSlot<RD>>) super.getConfiguration();
		}

	}

	/**
	 * This step is used to configure a type-aware model slot
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ConfigureVirtualModelSlotInstance.fib")
	public class ConfigureVirtualModelModelSlot<VMI extends AbstractVirtualModelInstance<VMI, VM>, VM extends AbstractVirtualModel<VM>>
			extends ConfigureModelSlot<FMLRTModelSlot<VMI, VM>, VMI> {

		public ConfigureVirtualModelModelSlot(FMLRTModelSlot modelSlot) {
			super(modelSlot);
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("configure_virtual_model_slot") + " : " + getModelSlot().getName();
		}

		@Override
		public FMLRTModelSlotInstanceConfiguration getConfiguration() {
			return (FMLRTModelSlotInstanceConfiguration) super.getConfiguration();
		}

	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ChooseAndConfigureCreationScheme.fib")
	public abstract class AbstractChooseAndConfigureCreationScheme<VM extends AbstractVirtualModel<VM>> extends WizardStep {

		public A getAction() {
			return action;
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("choose_and_configure_creation_scheme_to_use");
		}

		@Override
		public boolean isValid() {
			if (getCreationScheme() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_creation_scheme_selected"), IssueMessageType.ERROR);
				return false;
			}
			// TODO: check parameters settings ?
			return true;
		}

		public CreationScheme getCreationScheme() {
			return action.getCreationScheme();
		}

		public void setCreationScheme(CreationScheme creationScheme) {

			System.out.println("set creationScheme with " + creationScheme);

			if (creationScheme != getCreationScheme()) {
				CreationScheme oldValue = getCreationScheme();
				action.setCreationScheme(creationScheme);
				getPropertyChangeSupport().firePropertyChange("creationScheme", oldValue, creationScheme);
				checkValidity();
			}
		}

	}

}
