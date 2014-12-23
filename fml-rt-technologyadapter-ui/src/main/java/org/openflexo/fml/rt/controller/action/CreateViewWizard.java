package org.openflexo.fml.rt.controller.action;

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateView;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateViewWizard extends FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateViewWizard.class.getPackage().getName());

	private final CreateView action;

	private final ChooseViewPoint chooseViewPoint;

	public CreateViewWizard(CreateView action, FlexoController controller) {
		super(controller);
		this.action = action;
		addStep(chooseViewPoint = new ChooseViewPoint());
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_view");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLRTIconLibrary.VIEW_MEDIUM_ICON, IconLibrary.NEW_32_32).getImage();
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateView/ChooseViewPoint.fib")
	public class ChooseViewPoint extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateView getAction() {
			return action;
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("choose_view_point");
		}

		@Override
		public boolean isValid() {

			if (getFolder() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_folder_defined"), IssueMessageType.ERROR);
				return false;
			} else if (getViewpointResource() == null) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_view_point_selected"), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getNewViewTitle())) {
				setIssueMessage(FlexoLocalization.localizedForKey("no_view_title_defined"), IssueMessageType.ERROR);
				return false;
			}
			if (getFolder().getResourceWithName(getNewViewName()) != null) {
				setIssueMessage(FlexoLocalization.localizedForKey("a_view_with_that_name_already_exists"), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		public RepositoryFolder<ViewResource> getFolder() {
			return action.getFolder();
		}

		public String getNewViewName() {
			return action.getNewViewName();
		}

		public void setNewViewName(String newViewName) {
			if (!newViewName.equals(getNewViewName())) {
				String oldValue = getNewViewName();
				action.setNewViewName(newViewName);
				getPropertyChangeSupport().firePropertyChange("newViewName", oldValue, newViewName);
				getPropertyChangeSupport().firePropertyChange("newViewTitle", oldValue, newViewName);
				checkValidity();
			}
		}

		public String getNewViewTitle() {
			return action.getNewViewTitle();
		}

		public void setNewViewTitle(String newViewTitle) {
			if (!newViewTitle.equals(getNewViewTitle())) {
				String oldValue = getNewViewTitle();
				action.setNewViewTitle(newViewTitle);
				getPropertyChangeSupport().firePropertyChange("newViewTitle", oldValue, newViewTitle);
				checkValidity();
			}
		}

		public ViewPointResource getViewpointResource() {
			return action.getViewpointResource();
		}

		public void setViewpointResource(ViewPointResource viewpointResource) {
			if (viewpointResource != getViewpointResource()) {
				ViewPointResource oldValue = getViewpointResource();
				action.setViewpointResource(viewpointResource);
				getPropertyChangeSupport().firePropertyChange("viewpointResource", oldValue, viewpointResource);
				checkValidity();
			}
		}
	}

}
