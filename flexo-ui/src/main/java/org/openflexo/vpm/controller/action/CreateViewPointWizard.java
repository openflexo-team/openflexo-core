package org.openflexo.vpm.controller.action;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.action.CreateViewPoint;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.foundation.viewpoint.rm.ViewPointResource;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateViewPointWizard extends AbstractCreateFlexoConceptWizard<CreateViewPoint> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateViewPointWizard.class.getPackage().getName());

	private final DescribeViewPoint describeViewPoint;

	public CreateViewPointWizard(CreateViewPoint action, FlexoController controller) {
		super(action, controller);
		addStep(describeViewPoint = new DescribeViewPoint());
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_view_point");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(VPMIconLibrary.VIEWPOINT_MEDIUM_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeViewPoint getDescribeViewPoint() {
		return describeViewPoint;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/DescribeViewPoint.fib")
	public class DescribeViewPoint extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateViewPoint getAction() {
			return CreateViewPointWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_view_point");
		}

		@Override
		public boolean isValid() {

			if (getViewPointFolder() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_folder_defined"), IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getNewViewPointName())) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_supply_valid_view_point_name"), IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getNewViewPointURI())) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_supply_uri"), IssueMessageType.ERROR);
				return false;
			} else if (!isValidURI()) {
				setIssueMessage(FlexoLocalization.localizedForKey("please_supply_valid_uri"), IssueMessageType.ERROR);
				return false;
			} else if (getAction().getViewPointLibrary() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("could_not_access_viewpoint_library"), IssueMessageType.ERROR);
				return false;
			} else if (getAction().getViewPointLibrary().getViewPointResource(getNewViewPointURI()) != null) {
				setIssueMessage(FlexoLocalization.localizedForKey("already_existing_viewpoint_uri"), IssueMessageType.ERROR);
				return false;
			} else if (getViewPointFolder().getResourceWithName(getNewViewPointName()) != null) {
				setIssueMessage(FlexoLocalization.localizedForKey("already_existing_viewpoint_name"), IssueMessageType.ERROR);
				return false;
			} else if (StringUtils.isEmpty(getNewViewPointDescription())) {
				setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_view_point"), IssueMessageType.WARNING);
			}

			return true;
		}

		@SuppressWarnings("unchecked")
		public RepositoryFolder<ViewPointResource> getViewPointFolder() {
			return getAction().getViewPointFolder();
		}

		public String getNewViewPointName() {
			return getAction().getNewViewPointName();
		}

		public void setNewViewPointName(String newViewPointName) {
			if (!newViewPointName.equals(getNewViewPointName())) {
				String oldValue = getNewViewPointName();
				getAction().setNewViewPointName(newViewPointName);
				getPropertyChangeSupport().firePropertyChange("newViewPointName", oldValue, newViewPointName);
				checkValidity();
			}
		}

		public String getNewViewPointURI() {
			return getAction().getNewViewPointURI();
		}

		public void setNewViewPointURI(String newViewPointURI) {
			if (!newViewPointURI.equals(getNewViewPointURI())) {
				String oldValue = getNewViewPointURI();
				getAction().setNewViewPointURI(newViewPointURI);
				getPropertyChangeSupport().firePropertyChange("newViewPointURI", oldValue, newViewPointURI);
				checkValidity();
			}
		}

		private boolean isValidURI() {
			try {
				new URL(getNewViewPointURI());
			} catch (MalformedURLException e) {
				return false;
			}
			return true;
		}

		public String getNewViewPointDescription() {
			return getAction().getNewViewPointDescription();
		}

		public void setNewViewPointDescription(String newViewPointDescription) {
			if (!newViewPointDescription.equals(getNewViewPointDescription())) {
				String oldValue = getNewViewPointDescription();
				getAction().setNewViewPointDescription(newViewPointDescription);
				getPropertyChangeSupport().firePropertyChange("newViewPointDescription", oldValue, newViewPointDescription);
				checkValidity();
			}
		}

	}

}
