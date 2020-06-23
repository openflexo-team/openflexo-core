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
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoProperty;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoRole;
import org.openflexo.foundation.fml.action.CreateAbstractProperty;
import org.openflexo.foundation.fml.action.CreateExpressionProperty;
import org.openflexo.foundation.fml.action.CreateGetSetProperty;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

/**
 * Common stuff for wizards of {@link AbstractCreateFlexoProperty} action
 * 
 * @author sylvain
 * 
 * @param <A>
 * @see CreateAbstractProperty
 * @see AbstractCreateFlexoRole
 * @see CreateExpressionProperty
 * @see CreateGetSetProperty
 */
public abstract class AbstractCreateFlexoPropertyWizard<A extends AbstractCreateFlexoProperty<A>>
		extends AbstractCreateFMLElementWizard<A, FlexoConceptObject, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoPropertyWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = "this_name_is_already_used_please_choose_an_other_one";
	private static final String EMPTY_NAME = "flexo_property_must_have_an_non_empty_and_unique_name";
	private static final String RECOMMANDED_DESCRIPTION = "it_is_recommanded_to_describe_flexo_property";
	private static final String DISCOURAGED_NAME = "name_is_discouraged_by_convention_property_name_usually_start_with_a_lowercase_letter";

	private final DescribeProperty describeProperty;

	private static final Dimension DIMENSIONS = new Dimension(700, 500);

	public AbstractCreateFlexoPropertyWizard(A action, FlexoController controller) {
		super(action, controller);
		addStep(describeProperty = makeDescriptionStep());
	}

	protected abstract DescribeProperty makeDescriptionStep();

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_ROLE_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeProperty getDescribeProperty() {
		return describeProperty;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to set new property parameters
	 * 
	 * @author sylvain
	 * 
	 */
	public abstract class DescribeProperty extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractCreateFlexoPropertyWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return AbstractCreateFlexoPropertyWizard.this.getVirtualModel();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_property");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getPropertyName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(EMPTY_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (getFlexoConcept().getDeclaredProperty(getPropertyName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey(RECOMMANDED_DESCRIPTION), IssueMessageType.WARNING);
			}

			if (!getPropertyName().substring(0, 1).toLowerCase().equals(getPropertyName().substring(0, 1))) {
				setIssueMessage(getAction().getLocales().localizedForKey(DISCOURAGED_NAME), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getPropertyName() {
			return getAction().getPropertyName();
		}

		public void setPropertyName(String propertyName) {
			if ((propertyName == null && getPropertyName() != null) || (propertyName != null && !propertyName.equals(getPropertyName()))) {
				String oldValue = getPropertyName();
				getAction().setPropertyName(propertyName);
				getPropertyChangeSupport().firePropertyChange("propertyName", oldValue, propertyName);
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
