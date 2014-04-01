package org.openflexo.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.openflexo.fib.view.FIBView;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

public class DefaultModuleView<O extends FlexoObject> extends JPanel implements ModuleView<O> {

	private final O representedObject;
	private final FIBView<?, ?, ?> component;
	private final FlexoPerspective perspective;
	private final FlexoController controller;

	public DefaultModuleView(FlexoController controller, O representedObject, FIBView<?, ?, ?> component, FlexoPerspective perspective) {
		super(new BorderLayout());
		this.controller = controller;
		this.representedObject = representedObject;
		this.component = component;
		this.perspective = perspective;
		add(component.getJComponent());
	}

	@Override
	public O getRepresentedObject() {
		return representedObject;
	}

	@Override
	public void deleteModuleView() {
		if (controller != null) {
			controller.removeModuleView(this);
		}
		component.delete();
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	@Override
	public void willHide() {
		// Override when required
	}

	@Override
	public void willShow() {
		// Override when required
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
		// Override when required
	}

	@Override
	public boolean isAutoscrolled() {
		return false;
	}
}
