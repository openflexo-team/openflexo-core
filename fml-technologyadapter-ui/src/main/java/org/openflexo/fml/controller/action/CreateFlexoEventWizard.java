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

import java.awt.Image;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept.ParentFlexoConceptEntry;
import org.openflexo.foundation.fml.action.CreateFlexoEvent;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoEventWizard extends AbstractCreateFlexoConceptWizard<CreateFlexoEvent> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoEventWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = "this_name_is_already_used_please_choose_an_other_one";
	private static final String EMPTY_NAME = "flexo_event_must_have_an_non_empty_and_unique_name";

	private final DescribeFlexoEvent describeFlexoEvent;
	private final ConfigurePropertiesForNewFlexoConcept configurePropertiesForNewFlexoConcept;
	private final ConfigureBehavioursForNewFlexoConcept configureBehavioursForNewFlexoConcept;
	private final ConfigureInspectorForNewFlexoConcept configureInspectorForNewFlexoConcept;

	public CreateFlexoEventWizard(CreateFlexoEvent action, FlexoController controller) {
		super(action, controller);
		addStep(describeFlexoEvent = new DescribeFlexoEvent());
		addStep(configurePropertiesForNewFlexoConcept = new ConfigurePropertiesForNewFlexoConcept());
		addStep(configureBehavioursForNewFlexoConcept = new ConfigureBehavioursForNewFlexoConcept());
		addStep(configureInspectorForNewFlexoConcept = new ConfigureInspectorForNewFlexoConcept());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_flexo_event");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_EVENT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeFlexoEvent getDescribeFlexoEvent() {
		return describeFlexoEvent;
	}

	@Override
	public ConfigurePropertiesForNewFlexoConcept getConfigurePropertiesForNewFlexoConcept() {
		return configurePropertiesForNewFlexoConcept;
	}

	@Override
	public ConfigureBehavioursForNewFlexoConcept getConfigureBehavioursForNewFlexoConcept() {
		return configureBehavioursForNewFlexoConcept;
	}

	@Override
	public ConfigureInspectorForNewFlexoConcept getConfigureInspectorForNewFlexoConcept() {
		return configureInspectorForNewFlexoConcept;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeFlexoEvent.fib")
	public class DescribeFlexoEvent extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateFlexoEvent getAction() {
			return CreateFlexoEventWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return getAction().getFocusedObject().getDeclaringCompilationUnit().getVirtualModel();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_flexo_event");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewFlexoEventName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(EMPTY_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (getAction().getFocusedObject() instanceof VirtualModel
					&& getAction().getFocusedObject().getDeclaringCompilationUnit().getFlexoConcept(getNewFlexoEventName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getNewFlexoEventDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_flexo_event"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public List<ParentFlexoConceptEntry> getParentFlexoConceptEntries() {
			return getAction().getParentFlexoConceptEntries();
		}

		public String getNewFlexoEventName() {
			return getAction().getNewFlexoEventName();
		}

		public void setNewFlexoEventName(String newEventName) {
			if (!newEventName.equals(getNewFlexoEventName())) {
				String oldValue = getNewFlexoEventName();
				getAction().setNewFlexoEventName(newEventName);
				getPropertyChangeSupport().firePropertyChange("newFlexoEventName", oldValue, newEventName);
				checkValidity();
			}
		}

		public String getNewFlexoEventDescription() {
			return getAction().getNewFlexoEventDescription();
		}

		public void setNewFlexoEventDescription(String newFlexoEventDescription) {
			if (!newFlexoEventDescription.equals(getNewFlexoEventDescription())) {
				String oldValue = getNewFlexoEventDescription();
				getAction().setNewFlexoEventDescription(newFlexoEventDescription);
				getPropertyChangeSupport().firePropertyChange("newFlexoEventDescription", oldValue, newFlexoEventDescription);
				checkValidity();
			}
		}

	}

}
