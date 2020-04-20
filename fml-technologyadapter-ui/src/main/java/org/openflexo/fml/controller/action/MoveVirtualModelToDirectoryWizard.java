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
import java.io.File;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.MoveVirtualModelToDirectory;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.view.controller.FlexoController;

public class MoveVirtualModelToDirectoryWizard extends FlexoActionWizard<MoveVirtualModelToDirectory> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MoveVirtualModelToDirectoryWizard.class.getPackage().getName());

	private static final String INVALID_FOLDER = "cannot_move_virtual_model_in_that_folder";
	private static final String NO_FOLDER_DEFINED = "you_must_define_target_folder";
	private static final String CANNOT_MOVE = "cannot_move_such_virtual_model";
	private static final String POSSIBLE_INCONSISTENCY = "you_are_about_to_move_embedded_virtual_model_in_directory_causing_possible_inconsistencies";
	private static final Dimension DIMENSIONS = new Dimension(700, 300);

	private final MoveVirtualModelInfo moveVirtualModelInfo;

	public MoveVirtualModelToDirectoryWizard(MoveVirtualModelToDirectory action, FlexoController controller) {
		super(action, controller);
		addStep(moveVirtualModelInfo = new MoveVirtualModelInfo());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("move_virtual_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.VIRTUAL_MODEL_BIG_ICON, IconLibrary.BIG_GENERATE_MARKER).getImage();
	}

	public MoveVirtualModelInfo getMoveVirtualModelInfo() {
		return moveVirtualModelInfo;
	}

	@FIBPanel("Fib/Wizard/Refactor/MoveVirtualModelToDirectory.fib")
	public class MoveVirtualModelInfo extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public MoveVirtualModelToDirectory getAction() {
			return MoveVirtualModelToDirectoryWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_target_folder");
		}

		@Override
		public boolean isValid() {

			if (getVirtualModel().getResource().getIODelegate() instanceof DirectoryBasedIODelegate) {
				File virtualModelDirectory = ((DirectoryBasedIODelegate) getVirtualModel().getResource().getIODelegate()).getDirectory();
				if (getNewFolder() == null) {
					setIssueMessage(getAction().getLocales().localizedForKey(NO_FOLDER_DEFINED), IssueMessageType.ERROR);
					return false;
				}
				else if (getNewFolder().getSerializationArtefact() instanceof File) {
					if (FileUtils.isFileContainedIn((File) getNewFolder().getSerializationArtefact(), virtualModelDirectory)) {
						setIssueMessage(getAction().getLocales().localizedForKey(INVALID_FOLDER), IssueMessageType.ERROR);
						return false;
					}
					if (getVirtualModel().getOwningVirtualModel() != null) {
						setIssueMessage(getAction().getLocales().localizedForKey(POSSIBLE_INCONSISTENCY), IssueMessageType.WARNING);
					}
					return true;
				}
				else {
					setIssueMessage(getAction().getLocales().localizedForKey(INVALID_FOLDER), IssueMessageType.ERROR);
					return false;
				}

			}
			else {
				setIssueMessage(getAction().getLocales().localizedForKey(CANNOT_MOVE), IssueMessageType.ERROR);
				return false;
			}
		}

		public RepositoryFolder<CompilationUnitResource, ?> getNewFolder() {
			return getAction().getNewFolder();
		}

		public void setNewFolder(RepositoryFolder<CompilationUnitResource, ?> newFolder) {
			if ((newFolder == null && getNewFolder() != null) || (newFolder != null && !newFolder.equals(getNewFolder()))) {
				RepositoryFolder<CompilationUnitResource, ?> oldValue = getNewFolder();
				getAction().setNewFolder(newFolder);
				getPropertyChangeSupport().firePropertyChange("newFolder", oldValue, newFolder);
				checkValidity();
			}
		}
	}

}
