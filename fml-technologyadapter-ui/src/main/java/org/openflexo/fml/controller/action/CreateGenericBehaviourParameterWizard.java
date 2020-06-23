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
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateGenericBehaviourParameterWizard
		extends AbstractCreateFMLElementWizard<CreateGenericBehaviourParameter, FlexoBehaviourObject, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateGenericBehaviourParameterWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = "this_name_is_already_used_please_choose_an_other_one";
	private static final String EMPTY_NAME = "edition_behaviour_must_have_an_non_empty_and_unique_name";
	private static final String NO_TYPE = "please_supply_a_type_for_the_new_parameter";

	private final DescribeGenericBehaviourParameter describeParameter;

	private static final Dimension DIMENSIONS = new Dimension(600, 500);

	public CreateGenericBehaviourParameterWizard(CreateGenericBehaviourParameter action, FlexoController controller) {
		super(action, controller);
		addStep(describeParameter = new DescribeGenericBehaviourParameter());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_behaviour_parameter");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_PARAMETER_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeGenericBehaviourParameter getDescribeFlexoBehaviourParameter() {
		return describeParameter;
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
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeGenericBehaviourParameter.fib")
	public class DescribeGenericBehaviourParameter extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateGenericBehaviourParameter getAction() {
			return CreateGenericBehaviourParameterWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return CreateGenericBehaviourParameterWizard.this.getVirtualModel();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_behaviour_parameter");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getParameterName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(EMPTY_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (getFocusedObject().getFlexoBehaviour().getParameter(getParameterName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_NAME), IssueMessageType.ERROR);
				return false;
			}
			if (getParameterType() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_TYPE), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_parameter"),
						IssueMessageType.WARNING);
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

		public Type getParameterType() {
			return getAction().getParameterType();
		}

		public void setParameterType(Type parameterType) {
			if ((parameterType == null && getParameterType() != null)
					|| (parameterType != null && !parameterType.equals(getParameterType()))) {
				String oldParameterName = getParameterName();
				Type oldValue = getParameterType();
				getAction().setParameterType(parameterType);
				getPropertyChangeSupport().firePropertyChange("parameterType", oldValue, parameterType);
				getPropertyChangeSupport().firePropertyChange("parameterName", oldParameterName, getParameterName());
				getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
				if (getAvailableWidgetTypes().size() > 0) {
					setWidgetType(getAvailableWidgetTypes().get(0));
				}
				checkValidity();
			}
		}

		@NotificationUnsafe
		public WidgetType getWidgetType() {
			return getAction().getWidgetType();
		}

		public void setWidgetType(WidgetType widgetType) {
			if (widgetType != getWidgetType()) {
				WidgetType oldValue = getWidgetType();
				getAction().setWidgetType(widgetType);
				getPropertyChangeSupport().firePropertyChange("widgetType", oldValue, widgetType);
				getPropertyChangeSupport().firePropertyChange("isList", !isList(), isList());
				getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
				checkValidity();
			}
		}

		public List<WidgetType> getAvailableWidgetTypes() {
			return FlexoBehaviourParameterImpl.getAvailableWidgetTypes(getParameterType());
		}

		public boolean isList() {
			return TypeUtils.isList(getParameterType());
		}

		public boolean getIsRequired() {
			return getAction().getIsRequired();
		}

		public void setIsRequired(boolean isRequired) {
			if (isRequired != getIsRequired()) {
				getAction().setIsRequired(isRequired);
				getPropertyChangeSupport().firePropertyChange("isRequired", !isRequired, isRequired);
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

	}

}
