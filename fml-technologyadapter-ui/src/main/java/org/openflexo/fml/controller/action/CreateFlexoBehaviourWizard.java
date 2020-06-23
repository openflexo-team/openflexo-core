/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoBehaviourWizard extends AbstractCreateFMLElementWizard<CreateFlexoBehaviour, FlexoConceptObject, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoBehaviourWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = "this_name_is_already_used_please_choose_an_other_one";
	private static final String EMPTY_NAME = "flexo_behaviour_must_have_an_non_empty_and_unique_name";
	private static final String NO_BEHAVIOUR_TYPE = "please_choose_a_behaviour_type";
	private static final String RECOMMANDED_DESCRIPTION = "it_is_recommanded_to_describe_flexo_behaviour";
	private static final String DISCOURAGED_NAME = "name_is_discouraged_by_convention_behaviour_name_usually_start_with_a_lowercase_letter";

	private final DescribeFlexoBehaviour describeFlexoBehaviour;
	private final ConfigureFlexoBehaviourParameters configureParameters;

	private static final Dimension DIMENSIONS = new Dimension(700, 500);

	public CreateFlexoBehaviourWizard(CreateFlexoBehaviour action, FlexoController controller) {
		super(action, controller);
		addStep(describeFlexoBehaviour = new DescribeFlexoBehaviour());
		addStep(configureParameters = new ConfigureFlexoBehaviourParameters());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_flexo_behaviour");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeFlexoBehaviour getDescribeFlexoBehaviour() {
		return describeFlexoBehaviour;
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
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeFlexoBehaviour.fib")
	public class DescribeFlexoBehaviour extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateFlexoBehaviour getAction() {
			return CreateFlexoBehaviourWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return CreateFlexoBehaviourWizard.this.getVirtualModel();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_flexo_behaviour");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getFlexoBehaviourName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(EMPTY_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (getFlexoConcept().getFlexoBehaviour(getFlexoBehaviourName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (getFlexoBehaviourClass() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_BEHAVIOUR_TYPE), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey(RECOMMANDED_DESCRIPTION), IssueMessageType.WARNING);
			}

			if (!getFlexoBehaviourName().substring(0, 1).toLowerCase().equals(getFlexoBehaviourName().substring(0, 1))) {
				setIssueMessage(getAction().getLocales().localizedForKey(DISCOURAGED_NAME), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getFlexoBehaviourName() {
			return getAction().getFlexoBehaviourName();
		}

		public void setFlexoBehaviourName(String roleName) {
			if ((roleName == null && getFlexoBehaviourName() != null) || (roleName != null && !roleName.equals(getFlexoBehaviourName()))) {
				String oldValue = getFlexoBehaviourName();
				getAction().setFlexoBehaviourName(roleName);
				getPropertyChangeSupport().firePropertyChange("flexoBehaviourName", oldValue, roleName);
				checkValidity();
			}
		}

		public String getDescription() {
			return getAction().getDescription();
		}

		public void setDescription(String description) {
			if ((description == null && getDescription() != null) || (description != null && !description.equals(getDescription()))) {
				String oldValue = getDescription();
				getAction().setDescription(description);
				getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
				checkValidity();
			}
		}

		public boolean getIsAbstract() {
			return getAction().getIsAbstract();
		}

		public void setIsAbstract(boolean isAbstract) {
			if (isAbstract != getIsAbstract()) {
				getAction().setIsAbstract(isAbstract);
				getPropertyChangeSupport().firePropertyChange("isAbstract", !isAbstract, isAbstract);
				checkValidity();
			}
		}

		public Visibility getVisibility() {
			return getAction().getVisibility();
		}

		public void setVisibility(Visibility visibility) {
			if ((visibility == null && getVisibility() != null) || (visibility != null && !visibility.equals(getVisibility()))) {
				Visibility oldValue = getVisibility();
				getAction().setVisibility(visibility);
				getPropertyChangeSupport().firePropertyChange("visibility", oldValue, visibility);
				checkValidity();
			}
		}

		public Class<? extends FlexoBehaviour> getFlexoBehaviourClass() {
			return getAction().getFlexoBehaviourClass();
		}

		public void setFlexoBehaviourClass(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
			if (getFlexoBehaviourClass() != flexoBehaviourClass) {
				Class<? extends FlexoBehaviour> oldValue = getFlexoBehaviourClass();
				getAction().setFlexoBehaviourClass(flexoBehaviourClass);
				getPropertyChangeSupport().firePropertyChange("flexoBehaviourClass", oldValue, flexoBehaviourClass);
				getPropertyChangeSupport().firePropertyChange("flexoBehaviourName", null, getFlexoBehaviourName());
				checkValidity();
			}
		}

	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/ConfigureFlexoBehaviourParameters.fib")
	public class ConfigureFlexoBehaviourParameters extends WizardStep implements PropertyChangeListener {

		public ConfigureFlexoBehaviourParameters() {
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateFlexoBehaviour getAction() {
			return CreateFlexoBehaviourWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_behaviour_parameters");
		}

		public List<BehaviourParameterEntry> getParameterEntries() {
			return getAction().getParameterEntries();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getParameterEntries().contains(evt.getSource())) {
				checkValidity();
			}
		}

		public BehaviourParameterEntry newParameterEntry() {
			BehaviourParameterEntry newEntry = getAction().newParameterEntry();
			newEntry.getPropertyChangeSupport().addPropertyChangeListener(this);
			checkValidity();
			return newEntry;
		}

		public void deleteParameterEntry(BehaviourParameterEntry parameterEntryToDelete) {
			parameterEntryToDelete.getPropertyChangeSupport().removePropertyChangeListener(this);
			getAction().deleteParameterEntry(parameterEntryToDelete);
			checkValidity();
		}

		@Override
		public boolean isValid() {

			if (getParameterEntries().size() == 0) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_parameters_defined"), IssueMessageType.WARNING);
				return true;
			}
			else {

				// We try to detect duplicated names
				for (BehaviourParameterEntry entry : getParameterEntries()) {
					String paramName = entry.getParameterName();
					for (BehaviourParameterEntry entry2 : getParameterEntries()) {
						if ((entry != entry2) && (entry.getParameterName().equals(entry2.getParameterName()))) {
							setIssueMessage(getAction().getLocales().localizedForKey("duplicated_parameter_name") + " : " + paramName,
									IssueMessageType.ERROR);
							return false;
						}
					}
				}

				// Then, we valid each BehaviourParameterEntry
				boolean hasWarnings = false;
				for (BehaviourParameterEntry entry : getParameterEntries()) {
					String errorMessage = entry.getConfigurationErrorMessage();
					String warningMessage = entry.getConfigurationWarningMessage();
					if (StringUtils.isNotEmpty(errorMessage)) {
						setIssueMessage(errorMessage, IssueMessageType.ERROR);
						return false;
					}
					if (StringUtils.isNotEmpty(warningMessage)) {
						setIssueMessage(warningMessage, IssueMessageType.WARNING);
						hasWarnings = true;
					}
				}
				if (!hasWarnings) {
					setIssueMessage(getAction().getLocales().localizedForKey("all_behaviour_parameters_are_valid"), IssueMessageType.INFO);
				}
				return true;
			}

		}

	}

}
