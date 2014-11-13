package org.openflexo.components.validation;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.openflexo.editor.SelectAndFocusObjectTask;
import org.openflexo.fib.editor.ComponentValidationWindow;
import org.openflexo.fib.swing.validation.ValidationPanel;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationModel;
import org.openflexo.selection.SelectionManager;
import org.openflexo.view.controller.FlexoController;

/**
 * Non-modal window displaying a {@link ValidationPanel} for a {@link FlexoObject}<br>
 * Selection of issues is synchronized with the {@link FlexoController}'s {@link SelectionManager}
 * 
 * {@link ValidationModel} must be given to validate {@link FlexoObject}
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class ValidationWindow extends JDialog {

	private final ValidationPanel validationPanel;
	private final FlexoController controller;
	private boolean isDisposed = false;

	public ValidationWindow(JFrame frame, FlexoController controller) {
		super(frame, FlexoLocalization.localizedForKey(FlexoLocalization.getMainLocalizer(), "validation"), ModalityType.MODELESS);
		this.controller = controller;
		validationPanel = new ValidationPanel(null, FlexoLocalization.getMainLocalizer()) {
			@Override
			protected void performSelect(ValidationIssue<?, ?> validationIssue) {
				ValidationWindow.this.performSelect(validationIssue);
			}

			@Override
			public void startValidation(ValidationModel validationModel) {
				super.startValidation(validationModel);
				ValidationWindow.this.startValidation(validationModel);
			}

			@Override
			public void stopValidation(ValidationModel validationModel) {
				ValidationWindow.this.stopValidation(validationModel);
				super.stopValidation(validationModel);
			}
		};
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

	private ValidationProgressListener validationProgressListener;

	/**
	 * Once the {@link ComponentValidationWindow} is instantiated, this is the way to launch validation for a given {@link FlexoObject} and
	 * a supplied {@link ValidationModel}
	 * 
	 * @param object
	 * @param validationModel
	 */
	public void validateAndDisplayReportForObject(FlexoObject object, ValidationModel validationModel) {
		startValidation(validationModel);
		validationPanel.validate(validationModel, object);
		stopValidation(validationModel);
		setVisible(true);
	}

	public void startValidation(ValidationModel validationModel) {
		validationProgressListener = new ValidationProgressListener();
		validationModel.getPropertyChangeSupport().addPropertyChangeListener(validationProgressListener);
	}

	public void stopValidation(ValidationModel validationModel) {
		validationModel.getPropertyChangeSupport().removePropertyChangeListener(validationProgressListener);
	}

	@Override
	public void dispose() {
		super.dispose();
		isDisposed = true;
	}

	public boolean isDisposed() {
		return isDisposed;
	}
}