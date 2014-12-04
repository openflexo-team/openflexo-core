package org.openflexo.vpm.controller.action;

import java.awt.Dimension;
import java.util.logging.Logger;

import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.foundation.viewpoint.action.AbstractCreateFlexoConcept;
import org.openflexo.foundation.viewpoint.action.CreateFlexoConcept;
import org.openflexo.foundation.viewpoint.action.CreateViewPoint;
import org.openflexo.foundation.viewpoint.action.CreateVirtualModel;
import org.openflexo.view.controller.FlexoController;

/**
 * Common stuff for wizards of {@link AbstractCreateFlexoConcept} action
 * 
 * @author sylvain
 *
 * @param <A>
 * @see CreateFlexoConcept
 * @see CreateVirtualModel
 * @see CreateViewPoint
 */
public abstract class AbstractCreateFlexoConceptWizard<A extends AbstractCreateFlexoConcept<?, ?, ?>> extends FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConceptWizard.class.getPackage().getName());

	private final A action;

	private static final Dimension DIMENSIONS = new Dimension(900, 600);

	public AbstractCreateFlexoConceptWizard(A action, FlexoController controller) {
		super(controller);
		this.action = action;
	}

	public A getAction() {
		return action;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}
}
