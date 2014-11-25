package org.openflexo.components.validation;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.swing.validation.FIBValidationController;
import org.openflexo.view.controller.FlexoController;

public class FlexoFIBValidationController extends FIBValidationController {

	private FlexoController controller;

	public FlexoFIBValidationController(FIBComponent rootComponent) {
		super(rootComponent);
	}

	public FlexoController getController() {
		return controller;
	}

	public void setController(FlexoController controller) {
		this.controller = controller;
		getPropertyChangeSupport().firePropertyChange("controller", null, controller);
	}

	@Override
	public void checkAgain() {
		if (getValidationModel() != null && getValidatedObject() != null) {
			ValidationTask validationTask = new ValidationTask(getValidationModel(), getValidatedObject(), this);
			controller.getApplicationContext().getTaskManager().scheduleExecution(validationTask);
		}
	}
}
