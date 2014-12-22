package org.openflexo.fml.controller.action;

import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.FMLModelSlot;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.annotations.FIBPanel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.FlexoMetaModelResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateModelSlotWizard extends AbstractCreateFMLElementWizard<CreateModelSlot, VirtualModel, ViewPointObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateModelSlotWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("model_slot_must_have_an_non_empty_and_unique_name");
	private static final String NO_TECHNOLOGY_ADAPTER = FlexoLocalization.localizedForKey("please_choose_a_technology_adapter");
	private static final String NO_MODEL_SLOT_TYPE = FlexoLocalization.localizedForKey("please_choose_a_model_slot_type");
	private static final String NO_META_MODEL = FlexoLocalization.localizedForKey("please_choose_a_valid_metamodel");
	private static final String NO_VIRTUAL_MODEL = FlexoLocalization.localizedForKey("please_choose_a_valid_virtual_model");

	private final DescribeModelSlot describeModelSlot;

	private static final Dimension DIMENSIONS = new Dimension(600, 500);

	public CreateModelSlotWizard(CreateModelSlot action, FlexoController controller) {
		super(action, controller);
		addStep(describeModelSlot = new DescribeModelSlot());
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_model_slot");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(VPMIconLibrary.MODEL_SLOT_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeModelSlot getDescribeModelSlot() {
		return describeModelSlot;
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
	@FIBPanel("Fib/Wizard/CreateFlexoConcept/DescribeModelSlot.fib")
	public class DescribeModelSlot extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateModelSlot getAction() {
			return CreateModelSlotWizard.this.getAction();
		}

		public ViewPoint getViewPoint() {
			return CreateModelSlotWizard.this.getViewPoint();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_model_slot");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getModelSlotName())) {
				setIssueMessage(EMPTY_NAME, IssueMessageType.ERROR);
				return false;
			} else if (getFocusedObject() instanceof VirtualModel && getFocusedObject().getModelSlot(getModelSlotName()) != null) {
				setIssueMessage(DUPLICATED_NAME, IssueMessageType.ERROR);
				return false;
			} else if (getTechnologyAdapter() == null) {
				setIssueMessage(NO_TECHNOLOGY_ADAPTER, IssueMessageType.ERROR);
				return false;
			} else if (getModelSlotClass() == null) {
				setIssueMessage(NO_MODEL_SLOT_TYPE, IssueMessageType.ERROR);
				return false;
			} else if (getTechnologyAdapter() instanceof FMLTechnologyAdapter) {
				if (getVmRes() == null) {
					setIssueMessage(NO_VIRTUAL_MODEL, IssueMessageType.ERROR);
					return false;
				}
			} else if (TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass())) {
				if (getMmRes() == null) {
					setIssueMessage(NO_META_MODEL, IssueMessageType.ERROR);
					return false;
				}
			}

			if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_model_slot"), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getModelSlotName() {
			return getAction().getModelSlotName();
		}

		public void setModelSlotName(String modelSlotName) {
			if ((modelSlotName == null && getModelSlotName() != null)
					|| (modelSlotName != null && !modelSlotName.equals(getModelSlotName()))) {
				String oldValue = getModelSlotName();
				getAction().setModelSlotName(modelSlotName);
				getPropertyChangeSupport().firePropertyChange("modelSlotName", oldValue, modelSlotName);
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

		public TechnologyAdapter getTechnologyAdapter() {
			return getAction().getTechnologyAdapter();
		}

		public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
			if (getTechnologyAdapter() != technologyAdapter) {
				TechnologyAdapter oldValue = getTechnologyAdapter();
				getAction().setTechnologyAdapter(technologyAdapter);
				getPropertyChangeSupport().firePropertyChange("technologyAdapter", oldValue, technologyAdapter);
				getPropertyChangeSupport().firePropertyChange("modelSlotClass", null, getModelSlotClass());
				getPropertyChangeSupport().firePropertyChange("isTypeAwareModelSlot", !isTypeAwareModelSlot(), isTypeAwareModelSlot());
				getPropertyChangeSupport().firePropertyChange("isVirtualModelModelSlot", !isVirtualModelModelSlot(),
						isVirtualModelModelSlot());
				checkValidity();
			}
		}

		public FlexoMetaModelResource<?, ?, ?> getMmRes() {
			return getAction().getMmRes();
		}

		public void setMmRes(FlexoMetaModelResource<?, ?, ?> mmRes) {
			if (getMmRes() != mmRes) {
				FlexoMetaModelResource<?, ?, ?> oldValue = getMmRes();
				getAction().setMmRes(mmRes);
				getPropertyChangeSupport().firePropertyChange("mmRes", oldValue, mmRes);
				checkValidity();
			}
		}

		public VirtualModelResource getVmRes() {
			return getAction().getVmRes();
		}

		public void setVmRes(VirtualModelResource vmRes) {
			if (getVmRes() != vmRes) {
				VirtualModelResource oldValue = getVmRes();
				getAction().setVmRes(vmRes);
				getPropertyChangeSupport().firePropertyChange("vmRes", oldValue, vmRes);
				checkValidity();
			}
		}

		public boolean isRequired() {
			return getAction().isRequired();
		}

		public void setRequired(boolean required) {
			if (isRequired() != required) {
				boolean oldValue = isRequired();
				getAction().setRequired(required);
				getPropertyChangeSupport().firePropertyChange("required", oldValue, required);
				checkValidity();
			}
		}

		public boolean isReadOnly() {
			return getAction().isReadOnly();
		}

		public void setReadOnly(boolean readOnly) {
			if (isReadOnly() != readOnly) {
				boolean oldValue = isReadOnly();
				getAction().setReadOnly(readOnly);
				getPropertyChangeSupport().firePropertyChange("readOnly", oldValue, readOnly);
				checkValidity();
			}
		}

		public Class<? extends ModelSlot<?>> getModelSlotClass() {
			return getAction().getModelSlotClass();
		}

		public void setModelSlotClass(Class<? extends ModelSlot<?>> modelSlotClass) {
			if (getModelSlotClass() != modelSlotClass) {
				Class<? extends ModelSlot<?>> oldValue = getModelSlotClass();
				getAction().setModelSlotClass(modelSlotClass);
				getPropertyChangeSupport().firePropertyChange("modelSlotClass", oldValue, modelSlotClass);
				getPropertyChangeSupport().firePropertyChange("isTypeAwareModelSlot", !isTypeAwareModelSlot(), isTypeAwareModelSlot());
				getPropertyChangeSupport().firePropertyChange("isVirtualModelModelSlot", !isVirtualModelModelSlot(),
						isVirtualModelModelSlot());
				checkValidity();
			}
		}

		public boolean isTypeAwareModelSlot() {
			return getModelSlotClass() != null && !isVirtualModelModelSlot()
					&& TypeAwareModelSlot.class.isAssignableFrom(getModelSlotClass());
		}

		public boolean isVirtualModelModelSlot() {
			return getModelSlotClass() != null && getModelSlotClass().equals(FMLModelSlot.class);
		}

	}

}
