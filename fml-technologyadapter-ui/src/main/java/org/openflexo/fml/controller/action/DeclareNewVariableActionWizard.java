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

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.DeclareNewVariableAction;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class DeclareNewVariableActionWizard extends FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeclareNewVariableActionWizard.class.getPackage().getName());

	private final DescribeNewVariable describeNewVariable;
	private final DeclareNewVariableAction action;

	public DeclareNewVariableActionWizard(DeclareNewVariableAction action, FlexoController controller) {
		super(controller);
		this.action = action;
		addStep(describeNewVariable = new DescribeNewVariable());
	}

	public DeclareNewVariableAction getAction() {
		return action;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("declares_new_variable");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_BIG_ICON, IconLibrary.NEW_32_32).getImage();
	}

	public DescribeNewVariable getDescribeViewPoint() {
		return describeNewVariable;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link VirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeNewVariable.fib")
	public class DescribeNewVariable extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public DeclareNewVariableAction getAction() {
			return DeclareNewVariableActionWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_new_variable");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewVariableName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_supply_valid_variable_name"), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getFocusedObject().getBindingModel().bindingVariableNamed(getNewVariableName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("this_variable_name_shadows_an_other_variable"),
						IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public String getNewVariableName() {
			return getAction().getNewVariableName();
		}

		public void setNewVariableName(String newVariableName) {
			if (!newVariableName.equals(getNewVariableName())) {
				String oldValue = getNewVariableName();
				getAction().setNewVariableName(newVariableName);
				getPropertyChangeSupport().firePropertyChange("newVariableName", oldValue, newVariableName);
				checkValidity();
			}
		}

	}

}
