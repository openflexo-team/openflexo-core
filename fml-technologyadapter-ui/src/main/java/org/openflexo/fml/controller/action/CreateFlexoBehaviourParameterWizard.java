/**
 * 
 * Copyright (c) 2014, Openflexo
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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateFlexoBehaviourParameterWizard extends
		AbstractCreateFMLElementWizard<CreateFlexoBehaviourParameter, FlexoBehaviourObject, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFlexoBehaviourParameterWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("edition_behaviour_must_have_an_non_empty_and_unique_name");

	private final DescribeFlexoBehaviourParameter describeParameter;

	private static final Dimension DIMENSIONS = new Dimension(600, 500);

	public CreateFlexoBehaviourParameterWizard(CreateFlexoBehaviourParameter action, FlexoController controller) {
		super(action, controller);
		addStep(describeParameter = new DescribeFlexoBehaviourParameter());
	}

	@Override
	public String getWizardTitle() {
		return FlexoLocalization.localizedForKey("create_behaviour_parameter");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_PARAMETER_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeFlexoBehaviourParameter getDescribeFlexoBehaviourParameter() {
		return describeParameter;
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
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeFlexoBehaviourParameter.fib")
	public class DescribeFlexoBehaviourParameter extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateFlexoBehaviourParameter getAction() {
			return CreateFlexoBehaviourParameterWizard.this.getAction();
		}

		public ViewPoint getViewPoint() {
			return CreateFlexoBehaviourParameterWizard.this.getViewPoint();
		}

		@Override
		public String getTitle() {
			return FlexoLocalization.localizedForKey("describe_behaviour_parameter");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getParameterName())) {
				setIssueMessage(EMPTY_NAME, IssueMessageType.ERROR);
				return false;
			} else if (getFocusedObject().getFlexoBehaviour().getParameter(getParameterName()) != null) {
				setIssueMessage(DUPLICATED_NAME, IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(FlexoLocalization.localizedForKey("it_is_recommanded_to_describe_parameter"), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getParameterName() {
			return getAction().getParameterName();
		}

		public void setParameterName(String parameterName) {
			if ((parameterName == null && getParameterName() != null)
					|| (parameterName != null && !parameterName.equals(getParameterName()))) {
				String oldValue = getParameterName();
				getAction().setParameterName(parameterName);
				getPropertyChangeSupport().firePropertyChange("parameterName", oldValue, parameterName);
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

		public Class<? extends FlexoBehaviourParameter> getFlexoBehaviourParameterClass() {
			return getAction().getFlexoBehaviourParameterClass();
		}

		public void setFlexoBehaviourParameterClass(Class<? extends FlexoBehaviourParameter> parameterClass) {
			if (getFlexoBehaviourParameterClass() != parameterClass) {
				Class<? extends FlexoBehaviourParameter> oldValue = getFlexoBehaviourParameterClass();
				getAction().setFlexoBehaviourParameterClass(parameterClass);
				getPropertyChangeSupport().firePropertyChange("modelSlotClass", oldValue, parameterClass);
				checkValidity();
			}
		}

		public List<Class<? extends FlexoBehaviourParameter>> getAvailableParameterTypes() {
			return getAction().getAvailableParameterTypes();
		}

	}

}
