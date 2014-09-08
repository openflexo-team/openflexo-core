package org.openflexo.components.wizard;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.view.controller.FlexoFIBController;

public class WizardPanelController extends FlexoFIBController {

	public WizardPanelController(FIBComponent component) {
		super(component);
	}

	@Override
	public FlexoWizard getDataObject() {
		return (FlexoWizard) super.getDataObject();
	}

}
