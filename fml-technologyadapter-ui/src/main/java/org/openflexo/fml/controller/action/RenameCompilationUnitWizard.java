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
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.action.RenameCompilationUnit;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class RenameCompilationUnitWizard extends FlexoActionWizard<RenameCompilationUnit> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RenameCompilationUnitWizard.class.getPackage().getName());

	private static final String NO_NAME_DEFINED = "please_provide_a_valid_name";
	private static final String NO_URI_DEFINED = "please_provide_a_valid_URI";
	private static final String MALFORMED_URI = "malformed_URI";
	private static final String UNCHANGED_NAME_AND_URI = "unchanged_name_and_uri";
	private static final String ALREADY_EXISTING_URI = "already_existing_uri";
	private static final Dimension DIMENSIONS = new Dimension(700, 400);

	private final RenameCompilationUnitInfo renameCompilationUnitInfo;

	public RenameCompilationUnitWizard(RenameCompilationUnit action, FlexoController controller) {
		super(action, controller);
		addStep(renameCompilationUnitInfo = new RenameCompilationUnitInfo());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("rename_virtual_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return FMLIconLibrary.VIRTUAL_MODEL_BIG_ICON.getImage();
	}

	public RenameCompilationUnitInfo getMoveCompilationUnitInfo() {
		return renameCompilationUnitInfo;
	}

	@FIBPanel("Fib/Wizard/Refactor/RenameCompilationUnit.fib")
	public class RenameCompilationUnitInfo extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public RenameCompilationUnit getAction() {
			return RenameCompilationUnitWizard.this.getAction();
		}

		public FMLCompilationUnit getCompilationUnit() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_a_valid_new_name_and_or_uri");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewCompilationUnitName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_NAME_DEFINED), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getFocusedObject().getName().equals(getNewCompilationUnitName())
					&& getAction().getFocusedObject().getURI().equals(getNewCompilationUnitURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey(UNCHANGED_NAME_AND_URI), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getNewCompilationUnitURI())) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_URI_DEFINED), IssueMessageType.ERROR);
				return false;
			}
			try {
				new URL(getNewCompilationUnitURI());
			} catch (MalformedURLException e) {
				setIssueMessage(getAction().getLocales().localizedForKey(MALFORMED_URI), IssueMessageType.ERROR);
				return false;
			}
			if (getAction().getVirtualModelLibrary() == null
					|| getAction().getVirtualModelLibrary().getCompilationUnitResource(getNewCompilationUnitURI()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(ALREADY_EXISTING_URI), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public String getNewCompilationUnitName() {
			return getAction().getNewCompilationUnitName();
		}

		public void setNewCompilationUnitName(String newCompilationUnitName) {
			if ((newCompilationUnitName == null && getNewCompilationUnitName() != null)
					|| (newCompilationUnitName != null && !newCompilationUnitName.equals(getNewCompilationUnitName()))) {
				String oldValue = getNewCompilationUnitName();
				getAction().setNewCompilationUnitName(newCompilationUnitName);
				getPropertyChangeSupport().firePropertyChange("newCompilationUnitName", oldValue, newCompilationUnitName);
				getPropertyChangeSupport().firePropertyChange("newCompilationUnitURI", null, getNewCompilationUnitURI());
				checkValidity();
			}
		}

		public String getNewCompilationUnitURI() {
			return getAction().getNewCompilationUnitURI();
		}

		public void setNewCompilationUnitURI(String newCompilationUnitURI) {
			if ((newCompilationUnitURI == null && getNewCompilationUnitURI() != null)
					|| (newCompilationUnitURI != null && !newCompilationUnitURI.equals(getNewCompilationUnitURI()))) {
				String oldValue = getNewCompilationUnitURI();
				getAction().setNewCompilationUnitURI(newCompilationUnitURI);
				getPropertyChangeSupport().firePropertyChange("newCompilationUnitURI", oldValue, newCompilationUnitURI);
				checkValidity();
			}
		}

		public String getNewCompilationUnitDescription() {
			return getAction().getNewCompilationUnitDescription();
		}

		public void setNewCompilationUnitDescription(String newCompilationUnitDescription) {
			if ((newCompilationUnitDescription == null && getNewCompilationUnitDescription() != null)
					|| (newCompilationUnitDescription != null
							&& !newCompilationUnitDescription.equals(getNewCompilationUnitDescription()))) {
				String oldValue = getNewCompilationUnitDescription();
				getAction().setNewCompilationUnitDescription(newCompilationUnitDescription);
				getPropertyChangeSupport().firePropertyChange("newCompilationUnitDescription", oldValue, newCompilationUnitDescription);
				checkValidity();
			}
		}

	}

}
