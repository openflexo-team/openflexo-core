package org.openflexo.vpm.controller.action;

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateVirtualModelWizard extends AbstractCreateVirtualModelWizard<CreateVirtualModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateVirtualModelWizard.class.getPackage().getName());

	private final DescribeVirtualModel describeVirtualModel;

	public CreateVirtualModelWizard(CreateVirtualModel action, FlexoController controller) {
		super(action, controller);
		addStep(describeVirtualModel = new DescribeVirtualModel());
		appendConfigureModelSlots();
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_virtual_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(VPMIconLibrary.VIRTUAL_MODEL_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeVirtualModel getDescribeVirtualModel() {
		return describeVirtualModel;
	}

	@Override
	public ViewPoint getViewPoint() {
		return getAction().getFocusedObject();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/DescribeVirtualModel.fib")
	public class DescribeVirtualModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateVirtualModel getAction() {
			return CreateVirtualModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_virtual_model");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewVirtualModelName())) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_supply_valid_virtual_model_name"), IssueMessageType.ERROR);
				return false;
			} else if (getAction().getFocusedObject().getVirtualModelNamed(getNewVirtualModelName()) != null) {
				setIssueMessage(FlexoLocalization.localizedForKey("duplicated_virtual_model_name"), IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getNewVirtualModelDescription())) {
				setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_virtual_model"), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getNewVirtualModelName() {
			return getAction().getNewVirtualModelName();
		}

		public void setNewVirtualModelName(String newViewPointName) {
			if (!newViewPointName.equals(getNewVirtualModelName())) {
				String oldValue = getNewVirtualModelName();
				getAction().setNewVirtualModelName(newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelName", oldValue, newViewPointName);
				checkValidity();
			}
		}

		public String getNewVirtualModelDescription() {
			return getAction().getNewVirtualModelDescription();
		}

		public void setNewVirtualModelDescription(String newViewPointDescription) {
			if (!newViewPointDescription.equals(getNewVirtualModelDescription())) {
				String oldValue = getNewVirtualModelDescription();
				getAction().setNewVirtualModelDescription(newViewPointDescription);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", oldValue, newViewPointDescription);
				checkValidity();
			}
		}

	}

}
