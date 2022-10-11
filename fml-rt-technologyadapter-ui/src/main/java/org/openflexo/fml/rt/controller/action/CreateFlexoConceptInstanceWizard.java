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

package org.openflexo.fml.rt.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoObserver;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoConceptInstanceWizard extends FlexoActionWizard<CreateFlexoConceptInstance> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoConceptInstanceWizard.class.getPackage().getName());

	private static final String NO_CONTAINER = "please_supply_a_valid_container";
	private static final String NO_CONCEPT = "please_supply_a_valid_type_for_instance_beeing_created";
	private static final String SELECT_A_CREATION_SCHEME = "select_a_creation_scheme";
	private static final String CANNOT_INSTANTIATE_ENUM = "cannot_instantiate_enum";

	private DescribeFlexoConceptInstance describeFlexoConceptInstance;

	private static final Dimension DIMENSIONS = new Dimension(600, 400);

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	public CreateFlexoConceptInstanceWizard(CreateFlexoConceptInstance action, FlexoController controller) {
		super(action, controller);
		addStep(describeFlexoConceptInstance = new DescribeFlexoConceptInstance());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("define_new_concept_instance");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeFlexoConceptInstance getDescribeNewParentConcepts() {
		return describeFlexoConceptInstance;
	}

	/**
	 * This step is used to describe new {@link FlexoConceptInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConceptInstance/DescribeFlexoConceptInstance.fib")
	public class DescribeFlexoConceptInstance extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateFlexoConceptInstance getAction() {
			return CreateFlexoConceptInstanceWizard.this.getAction();
		}

		public FlexoConceptInstance getContainer() {
			return getAction().getContainer();
		}

		public void setContainer(FlexoConceptInstance container) {

			if ((container == null && getContainer() != null) || (container != null && !container.equals(getContainer()))) {
				FlexoConceptInstance oldValue = getContainer();
				getAction().setContainer(container);
				getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
				getPropertyChangeSupport().firePropertyChange("availableFlexoConcepts", null, getAvailableFlexoConcepts());
				if (getAvailableFlexoConcepts().size() > 0) {
					setFlexoConcept(getAvailableFlexoConcepts().get(0));
				}
				checkValidity();
			}
		}

		public FlexoConcept getFlexoConcept() {
			return getAction().getFlexoConcept();
		}

		public void setFlexoConcept(FlexoConcept flexoConcept) {
			if ((flexoConcept == null && getFlexoConcept() != null) || (flexoConcept != null && !flexoConcept.equals(getFlexoConcept()))) {
				FlexoConcept oldValue = getFlexoConcept();
				getAction().setFlexoConcept(flexoConcept);
				getPropertyChangeSupport().firePropertyChange("flexoConcept", oldValue, flexoConcept);
				if (flexoConcept != null && flexoConcept.getCreationSchemes().size() > 0) {
					setCreationScheme(flexoConcept.getCreationSchemes().get(0));
				}
				else {
					setCreationScheme(null);
				}
				checkValidity();
			}
		}

		public List<FlexoConcept> getAvailableFlexoConcepts() {
			Stream<FlexoConcept> concepts;
			if (getContainer() instanceof FMLRTVirtualModelInstance) {
				concepts = ((FMLRTVirtualModelInstance) getContainer()).getVirtualModel().getAllRootFlexoConcepts(true, false).stream();
			}
			else {
				concepts = getContainer().getFlexoConcept().getAccessibleEmbeddedFlexoConcepts().stream();
			}
			return concepts.filter((flexoConcept) -> !flexoConcept.isAbstract()).collect(Collectors.toList());
		}

		public CreationScheme getCreationScheme() {
			return getAction().getCreationScheme();
		}

		public void setCreationScheme(CreationScheme creationScheme) {
			if ((creationScheme == null && getCreationScheme() != null)
					|| (creationScheme != null && !creationScheme.equals(getCreationScheme()))) {
				CreationScheme oldValue = getCreationScheme();
				getAction().setCreationScheme(creationScheme);
				getPropertyChangeSupport().firePropertyChange("creationScheme", oldValue, creationScheme);
				checkValidity();
			}
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_flexo_concept_instance");
		}

		@Override
		public boolean isValid() {

			if (getContainer() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_CONTAINER), IssueMessageType.ERROR);
				return false;
			}
			if (getFlexoConcept() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_CONCEPT), IssueMessageType.ERROR);
				return false;
			}
			if (getFlexoConcept() instanceof FlexoEnum) {
				setIssueMessage(getAction().getLocales().localizedForKey(CANNOT_INSTANTIATE_ENUM), IssueMessageType.ERROR);
				return false;
			}
			if (getFlexoConcept().hasCreationScheme()) {
				if (getAction().getCreationScheme() == null) {
					setIssueMessage(getAction().getLocales().localizedForKey(SELECT_A_CREATION_SCHEME), IssueMessageType.ERROR);
					return false;
				}
				if (getAction().getCreationSchemeAction() == null) {
					setIssueMessage(getAction().getLocales().localizedForKey(SELECT_A_CREATION_SCHEME), IssueMessageType.ERROR);
					return false;
				}
				// Fixed issue with 'next' button not available when selecting a creation scheme with parameters
				/*if (!getAction().getCreationSchemeAction().areRequiredParametersSetAndValid()) {
					setIssueMessage(getAction().getLocales().localizedForKey(INVALID_PARAMETERS), IssueMessageType.ERROR);
					return false;
				}*/
			}
			return true;

		}

		@Override
		public boolean isTransitionalStep() {
			return (getCreationScheme() != null && getCreationScheme().getParameters().size() > 0);
		}

		@Override
		public void performTransition() {
			// We have now to update all steps according to chosen VirtualModel
			// Two possibilities:
			// - either chosen VirtualModel defines some CreationScheme, and we use it
			// - otherwise, we configurate all model slots
			if (getCreationScheme() != null && getCreationScheme().getParameters().size() > 0) {
				configureCreationScheme = makeChooseAndConfigureCreationScheme();
				addStep(configureCreationScheme);
			}
		}

		@Override
		public void discardTransition() {
			if (configureCreationScheme != null) {
				removeStep(configureCreationScheme);
				configureCreationScheme = null;
			}
		}
	}

	private ConfigureCreationScheme configureCreationScheme;

	public ConfigureCreationScheme makeChooseAndConfigureCreationScheme() {
		return new ConfigureCreationScheme(getAction().getCreationSchemeAction());
	}

	@FIBPanel("Fib/Wizard/CreateFlexoConceptInstance/ConfigureCreationScheme.fib")
	public class ConfigureCreationScheme extends WizardStep implements FlexoObserver {

		private CreationSchemeAction creationSchemeAction;

		public ConfigureCreationScheme(CreationSchemeAction creationSchemeAction) {

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

		public CreateFlexoConceptInstance getAction() {
			return CreateFlexoConceptInstanceWizard.this.getAction();
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
					// "Invalid parameter: " + parameter + " value=" + getAction().getCreationSchemeAction().getParameterValue(parameter));
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
