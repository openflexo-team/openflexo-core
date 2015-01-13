package org.openflexo.fml.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoController;

public class CreateEditionActionWizard extends AbstractCreateFMLElementWizard<CreateEditionAction, FMLControlGraph, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateEditionActionWizard.class.getPackage().getName());

	private final ChooseEditionActionClass chooseEditionActionClass;

	private static final Dimension DIMENSIONS = new Dimension(700, 500);

	public CreateEditionActionWizard(CreateEditionAction action, FlexoController controller) {
		super(action, controller);
		addStep(chooseEditionActionClass = new ChooseEditionActionClass());
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_edition_action");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public ChooseEditionActionClass getChooseEditionActionClass() {
		return chooseEditionActionClass;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/ChooseEditionActionClass.fib")
	public class ChooseEditionActionClass extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateEditionAction getAction() {
			return CreateEditionActionWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("choose_edition_action");
		}

		@Override
		public boolean isValid() {

			/*if (StringUtils.isEmpty(getFlexoBehaviourName())) {
				setIssueMessage(EMPTY_NAME, IssueMessageType.ERROR);
				return false;
			} else if (getFlexoConcept().getFlexoBehaviour(getFlexoBehaviourName()) != null) {
				setIssueMessage(DUPLICATED_NAME, IssueMessageType.ERROR);
				return false;
			} else if (getFlexoBehaviourClass() == null) {
				setIssueMessage(NO_BEHAVIOUR_TYPE, IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(RECOMMANDED_DESCRIPTION, IssueMessageType.WARNING);
			}

			if (!getFlexoBehaviourName().substring(0, 1).toLowerCase().equals(getFlexoBehaviourName().substring(0, 1))) {
				setIssueMessage(DISCOURAGED_NAME, IssueMessageType.WARNING);
			}*/

			return false;
		}

		public Class<? extends EditionAction> getEditionActionClass() {
			return getAction().getEditionActionClass();
		}

		public void setEditionActionClass(Class<? extends EditionAction> editionActionClass) {
			if (getEditionActionClass() != editionActionClass) {
				Class<? extends EditionAction> oldValue = getEditionActionClass();
				getAction().setEditionActionClass(editionActionClass);
				getPropertyChangeSupport().firePropertyChange("editionActionClass", oldValue, editionActionClass);
				checkValidity();
			}
		}

	}

}
