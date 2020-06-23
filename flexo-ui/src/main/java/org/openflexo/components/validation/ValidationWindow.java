/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.components.validation;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.openflexo.ApplicationContext;
import org.openflexo.editor.SelectAndFocusObjectTask;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.editor.validation.ComponentValidationWindow;
import org.openflexo.gina.swing.editor.validation.ValidationDialogPanel;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.selection.SelectionManager;
import org.openflexo.view.controller.FlexoController;

/**
 * Non-modal window displaying a {@link ValidationDialogPanel} for a {@link FlexoObject}<br>
 * Selection of issues is synchronized with the {@link FlexoController}'s {@link SelectionManager}
 * 
 * {@link ValidationModel} must be given to validate {@link FlexoObject}
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class ValidationWindow extends JDialog {

	private final FlexoValidationPanel validationPanel;
	private final FlexoController controller;
	private boolean isDisposed = false;

	public ValidationWindow(JFrame frame, FlexoController controller) {
		super(frame, controller.getFlexoLocales().localizedForKey("validation"), ModalityType.MODELESS);
		this.controller = controller;
		validationPanel = new FlexoValidationPanel(null, controller.getApplicationContext(), FlexoLocalization.getMainLocalizer());
		getContentPane().add(validationPanel);
		pack();
	}

	public FlexoController getController() {
		return controller;
	}

	protected void performSelect(ValidationIssue<?, ?> validationIssue) {

		if (validationIssue != null && validationIssue.getValidable() instanceof FlexoObject) {
			SelectAndFocusObjectTask task = new SelectAndFocusObjectTask(getController(), (FlexoObject) validationIssue.getValidable());
			getController().getApplicationContext().getTaskManager().scheduleExecution(task);
		}
	}

	// private ValidationProgressListener validationProgressListener;

	/**
	 * Once the {@link ComponentValidationWindow} is instantiated, this is the way to launch validation for a given {@link FlexoObject} and
	 * a supplied {@link ValidationModel}
	 * 
	 * @param object
	 * @param validationModel
	 */
	public void validateAndDisplayReportForObject(FlexoObject object, ValidationModel validationModel) {
		setVisible(true);
		validationPanel.validate(validationModel, object);
	}

	@Override
	public void dispose() {
		super.dispose();
		isDisposed = true;
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	protected class FlexoValidationPanel extends ValidationDialogPanel {

		public FlexoValidationPanel(ValidationReport validationReport, ApplicationContext applicationContext,
				LocalizedDelegate parentLocalizer) {
			super(validationReport, applicationContext.getApplicationFIBLibraryService().getApplicationFIBLibrary(), parentLocalizer);
		}

		@Override
		protected void performSelect(ValidationIssue<?, ?> validationIssue) {
			ValidationWindow.this.performSelect(validationIssue);
		}

		@Override
		protected FlexoFIBValidationController makeFIBController(FIBComponent fibComponent, LocalizedDelegate parentLocalizer) {
			FlexoFIBValidationController returned = new FlexoFIBValidationController(fibComponent) {
				@Override
				protected void performSelect(ValidationIssue<?, ?> validationIssue) {
					FlexoValidationPanel.this.performSelect(validationIssue);
				}
			};
			returned.setController(ValidationWindow.this.controller);
			return returned;
		}

		@Override
		public FlexoFIBValidationController getController() {
			return (FlexoFIBValidationController) super.getController();
		}

		/**
		 * Override parent implementation by launching validation in a dedicated task
		 */
		@Override
		public void validate(ValidationModel validationModel, Validable objectToValidate) {
			ValidationTask validationTask = new ValidationTask(validationModel, objectToValidate, getController());
			ValidationWindow.this.controller.getApplicationContext().getTaskManager().scheduleExecution(validationTask);
		}

	}
}
