/**
 * 
 * Copyright (c) 2015, Openflexo
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class CreateEditionActionWizard extends AbstractCreateFMLElementWizard<CreateEditionAction, FMLControlGraph, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateEditionActionWizard.class.getPackage().getName());

	private final ChooseEditionActionClass chooseEditionActionClass;

	private static final Dimension DIMENSIONS = new Dimension(800, 700);

	private static final String NO_EDITION_ACTION_TYPE = "please_choose_an_edition_action_or_control_structure";
	private static final String DUPLICATED_VARIABLE_NAME = "this_variable_name_shadow_an_other_identifier";

	public CreateEditionActionWizard(CreateEditionAction action, FlexoController controller) {
		super(action, controller);
		addStep(chooseEditionActionClass = new ChooseEditionActionClass());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_edition_action");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public ChooseEditionActionClass getChooseEditionActionClass() {
		return chooseEditionActionClass;
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
	@FIBPanel("Fib/Wizard/CreateFMLElement/ChooseEditionActionClass.fib")
	public class ChooseEditionActionClass extends WizardStep implements PropertyChangeListener {

		public ChooseEditionActionClass() {
			getAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		@Override
		public void done() {
			getAction().getPropertyChangeSupport().removePropertyChangeListener(this);
			super.done();
		}

		@Override
		public void reactivate() {
			getAction().getPropertyChangeSupport().addPropertyChangeListener(this);
			super.reactivate();
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateEditionAction getAction() {
			return CreateEditionActionWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_edition_action");
		}

		@Override
		public boolean isValid() {

			if (getEditionActionClass() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_EDITION_ACTION_TYPE), IssueMessageType.ERROR);
				return false;
			}

			if (getAction().isVariableDeclaration()
					&& getFocusedObject().getInferedBindingModel().bindingVariableNamed(getAction().getDeclarationVariableName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_VARIABLE_NAME), IssueMessageType.ERROR);
				return false;
			}

			return true;
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

		public String getDeclarationVariableName() {
			return getAction().getDeclarationVariableName();
		}

		public void setDeclarationVariableName(String declarationVariableName) {
			if (!declarationVariableName.equals(getDeclarationVariableName())) {
				String oldValue = getDeclarationVariableName();
				getAction().setDeclarationVariableName(declarationVariableName);
				getPropertyChangeSupport().firePropertyChange("declarationVariableName", oldValue, declarationVariableName);
				checkValidity();
			}
		}

		public DataBinding<?> getIterationExpression() {
			return getAction().getIterationExpression();
		}

		public void setIterationExpression(DataBinding<?> expression) {
			if (!expression.equals(getIterationExpression())) {
				getAction().setIterationExpression(expression);
				getPropertyChangeSupport().firePropertyChange("iterationExpression", null, getIterationExpression());
				checkValidity();
			}
		}

		public String getIteratorName() {
			if (getAction().isIterationAction()) {
				return ((IterationAction) getAction().getBaseEditionAction()).getIteratorName();
			}
			return null;
		}

		public void setIteratorName(String iteratorName) {
			if (!iteratorName.equals(getIteratorName())) {
				String oldValue = getIteratorName();
				((IterationAction) getAction().getBaseEditionAction()).setIteratorName(iteratorName);
				getPropertyChangeSupport().firePropertyChange("iteratorName", oldValue, iteratorName);
				checkValidity();
			}
		}

		public Class<? extends FetchRequest<?, ?, ?>> getFetchRequestClass() {
			return getAction().getFetchRequestClass();
		}

		public void setFetchRequestClass(Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass) {
			if (getFetchRequestClass() != fetchRequestClass) {
				Class<? extends FetchRequest<?, ?, ?>> oldValue = getFetchRequestClass();
				getAction().setFetchRequestClass(fetchRequestClass);
				getPropertyChangeSupport().firePropertyChange("fetchRequestClass", oldValue, fetchRequestClass);
				checkValidity();
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			getPropertyChangeSupport().firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			checkValidity();
		}

	}

}
