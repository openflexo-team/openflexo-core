package org.openflexo.vpm.controller.action;

import java.awt.Image;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.action.AbstractCreateFlexoConcept.ParentFlexoConceptEntry;
import org.openflexo.foundation.viewpoint.action.CreateFlexoConcept;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoConceptWizard extends AbstractCreateFlexoConceptWizard<CreateFlexoConcept> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoConceptWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("flexo_concept_must_have_an_non_empty_and_unique_name");

	private final DescribeFlexoConcept describeFlexoConcept;
	private final ConfigureAdditionalStepsForNewFlexoConcept configureAdditionalStepsForNewFlexoConcept;

	public CreateFlexoConceptWizard(CreateFlexoConcept action, FlexoController controller) {
		super(action, controller);
		addStep(describeFlexoConcept = new DescribeFlexoConcept());
		addStep(configureAdditionalStepsForNewFlexoConcept = new ConfigureAdditionalStepsForNewFlexoConcept());
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_flexo_concept");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(VPMIconLibrary.FLEXO_CONCEPT_MEDIUM_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeFlexoConcept getDescribeFlexoConcept() {
		return describeFlexoConcept;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/DescribeFlexoConcept.fib")
	public class DescribeFlexoConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateFlexoConcept getAction() {
			return CreateFlexoConceptWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_flexo_concept");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewFlexoConceptName())) {
				setIssueMessage(EMPTY_NAME, IssueMessageType.ERROR);
				return false;
			} else if (getAction().getFocusedObject() instanceof VirtualModel
					&& getAction().getFocusedObject().getFlexoConcept(getNewFlexoConceptName()) != null) {
				setIssueMessage(DUPLICATED_NAME, IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getNewFlexoConceptDescription())) {
				setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_flexo_concept"), IssueMessageType.WARNING);
			}

			return true;
		}

		public List<ParentFlexoConceptEntry> getParentFlexoConceptEntries() {
			return getAction().getParentFlexoConceptEntries();
		}

		public String getNewFlexoConceptName() {
			return getAction().getNewFlexoConceptName();
		}

		public void setNewFlexoConceptName(String newViewPointName) {
			if (!newViewPointName.equals(getNewFlexoConceptName())) {
				String oldValue = getNewFlexoConceptName();
				getAction().setNewFlexoConceptName(newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newFlexoConceptName", oldValue, newViewPointName);
				checkValidity();
			}
		}

		public String getNewFlexoConceptDescription() {
			return getAction().getNewFlexoConceptDescription();
		}

		public void setNewFlexoConceptDescription(String newFlexoConceptDescription) {
			if (!newFlexoConceptDescription.equals(getNewFlexoConceptDescription())) {
				String oldValue = getNewFlexoConceptDescription();
				getAction().setNewFlexoConceptDescription(newFlexoConceptDescription);
				getPropertyChangeSupport().firePropertyChange("newFlexoConceptDescription", oldValue, newFlexoConceptDescription);
				checkValidity();
			}
		}

	}

}
