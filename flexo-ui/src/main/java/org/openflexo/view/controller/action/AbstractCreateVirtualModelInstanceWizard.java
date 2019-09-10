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

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.AbstractCreateVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractCreateVirtualModelInstanceWizard<A extends AbstractCreateVirtualModelInstance<?, ?, ?, ?>>
		extends FlexoActionWizard<A> {

	private static final Logger logger = Logger.getLogger(AbstractCreateVirtualModelInstanceWizard.class.getPackage().getName());

	private final AbstractChooseVirtualModel chooseVirtualModel;
	// private final List<ConfigureModelSlot<?, ?>> modelSlotConfigurationSteps;
	private AbstractChooseAndConfigureCreationScheme chooseAndConfigureCreationScheme = null;

	public AbstractCreateVirtualModelInstanceWizard(A action, FlexoController controller) {
		super(action, controller);
		addStep(chooseVirtualModel = makeChooseVirtualModel());
	}

	protected abstract AbstractChooseVirtualModel makeChooseVirtualModel();

	protected abstract AbstractChooseAndConfigureCreationScheme makeChooseAndConfigureCreationScheme();

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	public abstract class AbstractChooseVirtualModel extends WizardStep {

		public A getAction() {
			return AbstractCreateVirtualModelInstanceWizard.this.getAction();
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_virtual_model");
		}

		@Override
		public boolean isValid() {
			if (getVirtualModel() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_virtual_model_type_selected"), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getNewVirtualModelInstanceName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_virtual_model_instance_name_defined"), IssueMessageType.ERROR);
				return false;
			}

			if (StringUtils.isEmpty(getNewVirtualModelInstanceTitle())) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_virtual_model_instance_title_defined"),
						IssueMessageType.ERROR);
				return false;
			}
			if (!getAction().isValidVirtualModelInstanceName(getNewVirtualModelInstanceName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("a_virtual_model_instance_with_that_name_already_exists"),
						IssueMessageType.ERROR);
				return false;
			}

			if (getVirtualModel().getCreationSchemes().size() > 0 && getCreationScheme() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_creation_scheme_selected"), IssueMessageType.ERROR);
				return false;
			}

			if (!getNewVirtualModelInstanceName().equals(JavaUtils.getClassName(getNewVirtualModelInstanceName()))
					&& !getNewVirtualModelInstanceName().equals(JavaUtils.getVariableName(getNewVirtualModelInstanceName()))) {
				setIssueMessage(getAction().getLocales().localizedForKey("discouraged_name_for_new_virtual_model_instance"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public String getNewVirtualModelInstanceName() {
			return getAction().getNewVirtualModelInstanceName();
		}

		public void setNewVirtualModelInstanceName(String newVirtualModelInstanceName) {
			if (!newVirtualModelInstanceName.equals(getNewVirtualModelInstanceName())) {
				String oldValue = getNewVirtualModelInstanceName();
				String oldTitleValue = getNewVirtualModelInstanceTitle();
				getAction().setNewVirtualModelInstanceName(newVirtualModelInstanceName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceName", oldValue, newVirtualModelInstanceName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceTitle", oldTitleValue,
						getNewVirtualModelInstanceTitle());
				checkValidity();
			}
		}

		public String getNewVirtualModelInstanceTitle() {
			return getAction().getNewVirtualModelInstanceTitle();
		}

		public void setNewVirtualModelInstanceTitle(String newVirtualModelInstanceTitle) {
			if (!newVirtualModelInstanceTitle.equals(getNewVirtualModelInstanceTitle())) {
				String oldValue = getNewVirtualModelInstanceTitle();
				String oldNameValue = getNewVirtualModelInstanceName();
				getAction().setNewVirtualModelInstanceTitle(newVirtualModelInstanceTitle);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceTitle", oldValue, newVirtualModelInstanceTitle);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelInstanceName", oldNameValue,
						getNewVirtualModelInstanceName());
				checkValidity();
			}
		}

		public VirtualModel getVirtualModel() {
			return getAction().getVirtualModel();
		}

		public void setVirtualModel(VirtualModel virtualModel) {
			if (virtualModel != getVirtualModel()) {
				VirtualModel oldValue = getVirtualModel();
				((AbstractCreateVirtualModelInstance) getAction()).setVirtualModel(virtualModel);
				getPropertyChangeSupport().firePropertyChange("virtualModel", oldValue, virtualModel);
				getPropertyChangeSupport().firePropertyChange("creationScheme", null, getCreationScheme());
				checkValidity();
			}
		}

		public CompilationUnitResource getVirtualModelResource() {
			if (getAction().getVirtualModel() != null) {
				return (CompilationUnitResource) getAction().getVirtualModel().getResource();
			}
			return null;
		}

		public void setVirtualModelResource(CompilationUnitResource virtualModelResource) {
			if (getVirtualModelResource() != virtualModelResource) {
				CompilationUnitResource oldValue = getVirtualModelResource();
				if (virtualModelResource != null) {
					((AbstractCreateVirtualModelInstance) getAction()).setVirtualModel(virtualModelResource.getCompilationUnit().getVirtualModel());
				}
				else {
					getAction().setVirtualModel(null);
				}
				getPropertyChangeSupport().firePropertyChange("virtualModelResource", oldValue, virtualModelResource);
				checkValidity();
			}
		}

		public CreationScheme getCreationScheme() {
			return getAction().getCreationScheme();
		}

		public void setCreationScheme(CreationScheme creationScheme) {

			if (creationScheme != getCreationScheme()) {
				CreationScheme oldValue = getCreationScheme();
				getAction().setCreationScheme(creationScheme);
				getPropertyChangeSupport().firePropertyChange("creationScheme", oldValue, creationScheme);
				checkValidity();
			}
		}

		@Override
		public boolean isTransitionalStep() {
			if (getVirtualModel() == null) {
				return false;
			}
			if (getCreationScheme() != null && getCreationScheme().getParameters().size() > 0) {
				return true;
			}
			return false;
		}

		@Override
		public void performTransition() {
			// We have now to update all steps according to chosen VirtualModel
			// Two possibilities:
			// - either chosen VirtualModel defines some CreationScheme, and we use it
			// - otherwise, we configurate all model slots
			if (getCreationScheme() != null && getCreationScheme().getParameters().size() > 0) {
				chooseAndConfigureCreationScheme = makeChooseAndConfigureCreationScheme();
				addStep(chooseAndConfigureCreationScheme);
			}
		}

		@Override
		public void discardTransition() {
			/*for (ConfigureModelSlot<?, ?> step : modelSlotConfigurationSteps) {
				removeStep(step);
			}
			modelSlotConfigurationSteps.clear();*/
			if (chooseAndConfigureCreationScheme != null) {
				removeStep(chooseAndConfigureCreationScheme);
				chooseAndConfigureCreationScheme = null;
			}
		}

		public Class<CreationScheme> getCreationSchemeType() {
			return CreationScheme.class;
		}

		// private String initializationOption;

		/*public String getInitializationOption() {
			return initializationOption;
		}
		
		public void setInitializationOption(String initializationOption) {
			if ((initializationOption == null && this.initializationOption != null)
					|| (initializationOption != null && !initializationOption.equals(this.initializationOption))) {
				String oldValue = this.initializationOption;
				this.initializationOption = initializationOption;
				getPropertyChangeSupport().firePropertyChange("initializationOption", oldValue, initializationOption);
				checkValidity();
			}
		}
		
		public boolean chooseCreationScheme() {
			return getInitializationOption() != null && getInitializationOption().equals("choose_a_creation_scheme");
		}*/

	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateVirtualModelInstance/ChooseAndConfigureCreationScheme.fib")
	public abstract class AbstractChooseAndConfigureCreationScheme extends WizardStep implements FlexoObserver {

		private CreationSchemeAction creationSchemeAction;

		public AbstractChooseAndConfigureCreationScheme(CreationSchemeAction creationSchemeAction) {

			this.creationSchemeAction = creationSchemeAction;
			if (creationSchemeAction != null) {
				creationSchemeAction.addObserver(this);
			}
		}

		@Override
		public void delete() {
			if (creationSchemeAction != null) {
				creationSchemeAction.deleteObserver(this);
			}
			super.delete();
		}

		@Override
		public void update(FlexoObservable observable, DataModification dataModification) {
			if (dataModification.propertyName().equals(FlexoBehaviourAction.PARAMETER_VALUE_CHANGED)) {
				checkValidity();
			}
		}

		public A getAction() {
			return AbstractCreateVirtualModelInstanceWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_creation_scheme_to_use");
		}

		@Override
		public boolean isValid() {
			if (getCreationScheme() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_creation_scheme_selected"), IssueMessageType.ERROR);
				return false;
			}

			for (FlexoBehaviourParameter parameter : getAction().getCreationScheme().getParameters()) {

				if (!parameter.isValid(getAction().getCreationSchemeAction(),
						getAction().getCreationSchemeAction().getParameterValue(parameter))) {
					// System.out.println(
					// "Invalid parameter: " + parameter + " value=" + action.getCreationSchemeAction().getParameterValue(parameter));
					setIssueMessage(getAction().getLocales().localizedForKey("invalid_parameter") + " : " + parameter.getName(),
							IssueMessageType.ERROR);
					return false;
				}
			}

			return true;
		}

		public CreationScheme getCreationScheme() {
			return getAction().getCreationScheme();
		}

		public void setCreationScheme(CreationScheme creationScheme) {

			if (creationScheme != getCreationScheme()) {
				CreationScheme oldValue = getCreationScheme();
				getAction().setCreationScheme(creationScheme);
				getPropertyChangeSupport().firePropertyChange("creationScheme", oldValue, creationScheme);
				checkValidity();
			}
		}

	}

}
