/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.DuplicateVirtualModel;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class DuplicateVirtualModelWizard extends FlexoActionWizard<DuplicateVirtualModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DuplicateVirtualModelWizard.class.getPackage().getName());

	private static final String NO_NAME_DEFINED = "please_provide_a_valid_name";
	private static final String NO_URI_DEFINED = "please_provide_a_valid_URI";
	private static final String MALFORMED_URI = "malformed_URI";
	private static final String UNCHANGED_NAME = "unchanged_name";
	private static final String UNCHANGED_URI = "unchanged_uri";
	private static final String ALREADY_EXISTING_URI = "already_existing_uri";
	private static final Dimension DIMENSIONS = new Dimension(700, 400);

	private final DuplicateVirtualModelInfo duplicateVirtualModelInfo;

	public DuplicateVirtualModelWizard(DuplicateVirtualModel action, FlexoController controller) {
		super(action, controller);
		addStep(duplicateVirtualModelInfo = new DuplicateVirtualModelInfo());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("duplicate_virtual_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return FMLIconLibrary.VIRTUAL_MODEL_BIG_ICON.getImage();
	}

	public DuplicateVirtualModelInfo getDuplicateVirtualModelInfo() {
		return duplicateVirtualModelInfo;
	}

	@FIBPanel("Fib/Wizard/Refactor/DuplicateVirtualModel.fib")
	public class DuplicateVirtualModelInfo extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public DuplicateVirtualModel getAction() {
			return DuplicateVirtualModelWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_a_valid_new_name_and_or_uri");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewVirtualModelName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_NAME_DEFINED), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getFocusedObject().getName().equals(getNewVirtualModelName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(UNCHANGED_NAME), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getFocusedObject().getURI().equals(getNewVirtualModelURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey(UNCHANGED_URI), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getNewVirtualModelURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_URI_DEFINED), IssueMessageType.ERROR);
				return false;
			}
			try {
				new URL(getNewVirtualModelURI());
			} catch (MalformedURLException e) {
				setIssueMessage(getAction().getLocales().localizedForKey(MALFORMED_URI), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getVirtualModelLibrary() == null
					|| getAction().getVirtualModelLibrary().getCompilationUnitResource(getNewVirtualModelURI()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(ALREADY_EXISTING_URI), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public String getNewVirtualModelName() {
			return getAction().getNewVirtualModelName();
		}

		public void setNewVirtualModelName(String newVirtualModelName) {
			if ((newVirtualModelName == null && getNewVirtualModelName() != null)
					|| (newVirtualModelName != null && !newVirtualModelName.equals(getNewVirtualModelName()))) {
				String oldValue = getNewVirtualModelName();
				getAction().setNewVirtualModelName(newVirtualModelName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelName", oldValue, newVirtualModelName);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", null, getNewVirtualModelURI());
				checkValidity();
			}
		}

		public String getNewVirtualModelURI() {
			return getAction().getNewVirtualModelURI();
		}

		public void setNewVirtualModelURI(String newVirtualModelURI) {
			if ((newVirtualModelURI == null && getNewVirtualModelURI() != null)
					|| (newVirtualModelURI != null && !newVirtualModelURI.equals(getNewVirtualModelURI()))) {
				String oldValue = getNewVirtualModelURI();
				getAction().setNewVirtualModelURI(newVirtualModelURI);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelURI", oldValue, newVirtualModelURI);
				checkValidity();
			}
		}

		public String getNewVirtualModelDescription() {
			return getAction().getNewVirtualModelDescription();
		}

		public void setNewVirtualModelDescription(String newVirtualModelDescription) {
			if ((newVirtualModelDescription == null && getNewVirtualModelDescription() != null)
					|| (newVirtualModelDescription != null && !newVirtualModelDescription.equals(getNewVirtualModelDescription()))) {
				String oldValue = getNewVirtualModelDescription();
				getAction().setNewVirtualModelDescription(newVirtualModelDescription);
				getPropertyChangeSupport().firePropertyChange("newVirtualModelDescription", oldValue, newVirtualModelDescription);
				checkValidity();
			}
		}

	}

}
