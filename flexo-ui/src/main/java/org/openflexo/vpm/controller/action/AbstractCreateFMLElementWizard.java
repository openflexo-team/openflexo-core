package org.openflexo.vpm.controller.action;

import java.util.logging.Logger;

import org.openflexo.components.wizard.FlexoWizard;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptObject;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModel;
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
public abstract class AbstractCreateFMLElementWizard<A extends FlexoAction<A, T1, T2>, T1 extends FlexoConceptObject, T2 extends ViewPointObject>
		extends FlexoWizard {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFMLElementWizard.class.getPackage().getName());

	private final A action;

	public AbstractCreateFMLElementWizard(A action, FlexoController controller) {
		super(controller);
		this.action = action;
	}

	public A getAction() {
		return action;
	}

	public T1 getFocusedObject() {
		return action.getFocusedObject();
	}

	public ViewPoint getViewPoint() {
		return getFocusedObject().getViewPoint();
	}

	public VirtualModel getVirtualModel() {
		return getFocusedObject().getVirtualModel();
	}

	public FlexoConcept getFlexoConcept() {
		return getFocusedObject().getFlexoConcept();
	}

}
