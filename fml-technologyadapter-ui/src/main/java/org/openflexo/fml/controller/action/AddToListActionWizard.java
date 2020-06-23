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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.action.AddToAction;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class AddToListActionWizard extends FlexoActionWizard<AddToAction> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddToListActionWizard.class.getPackage().getName());

	private final DescribeListWhereToAdd describeListWhereToAdd;

	public AddToListActionWizard(AddToAction action, FlexoController controller) {
		super(action, controller);
		addStep(describeListWhereToAdd = new DescribeListWhereToAdd());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("add_to_list");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeListWhereToAdd getDescribeListWhereToAdd() {
		return describeListWhereToAdd;
	}

	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeListWhereToAdd.fib")
	public class DescribeListWhereToAdd extends WizardStep implements PropertyChangeListener {

		public DescribeListWhereToAdd() {
			getAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AddToAction getAction() {
			return AddToListActionWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_list_where_to_add");
		}

		@Override
		public boolean isValid() {

			if (!getList().isValid()) {
				setIssueMessage(getAction().getLocales().localizedForKey("list_is_not_valid"), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		public DataBinding<?> getList() {
			return getAction().getList();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == getAction()) {
				if (evt.getPropertyName().equals("list")) {
					checkValidity();
				}
			}
		}
	}

}
