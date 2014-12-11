package org.openflexo.components.wizard;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.view.controller.FlexoFIBController;

public class WizardPanelController extends FlexoFIBController {

	public WizardPanelController(FIBComponent component) {
		super(component);
	}

	@Override
	public Wizard getDataObject() {
		return (Wizard) super.getDataObject();
	}

	public void finish() {
		getDataObject().finish();
		super.validateAndDispose();
	}
}
