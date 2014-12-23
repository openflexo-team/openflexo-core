/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.fml.controller.view;

import org.openflexo.components.widget.CommonFIB;
import org.openflexo.fib.model.listener.FIBMouseClickListener;
import org.openflexo.fib.view.FIBView;
import org.openflexo.fib.view.widget.FIBTableWidget;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.view.FIBModuleView;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This is the {@link ModuleView} representing a {@link ViewPoint}
 * 
 * @author sguerin
 * 
 */
public class ViewPointView extends FIBModuleView<ViewPoint> implements FIBMouseClickListener {

	private final FlexoPerspective perspective;

	public ViewPointView(ViewPoint viewPoint, FlexoController controller, FlexoPerspective perspective) {
		super(viewPoint, controller, CommonFIB.VIEWPOINT_VIEW_FIB);
		this.perspective = perspective;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	@Override
	public void mouseClicked(FIBView<?, ?, ?> view, int clickCount) {
		if (view instanceof FIBTableWidget && ((FIBTableWidget<?>) view).getSelected() instanceof FlexoObject && clickCount == 2) {
			FlexoObject o = (FlexoObject) ((FIBTableWidget<?>) view).getSelected();
			if (o instanceof ViewPoint || o instanceof FlexoConcept /*|| o instanceof ExampleDiagram || o instanceof DiagramPalette*/) {
				getFlexoController().selectAndFocusObject(o);
			}
		}
	}

	@Override
	public void show(final FlexoController controller, FlexoPerspective perspective) {

		controller.getControllerModel().setRightViewVisible(false);
	}

}
