package org.openflexo.vpm.controller.action;

import java.util.logging.Logger;

import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractCreateFlexoConceptWizard<A extends FlexoAction<?, ?, ?>> extends FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConceptWizard.class.getPackage().getName());

	private final A action;

	public AbstractCreateFlexoConceptWizard(A action, FlexoController controller) {
		super(controller);
		this.action = action;
	}

	public A getAction() {
		return action;
	}

}
