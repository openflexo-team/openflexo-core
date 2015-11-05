/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.view;

import org.openflexo.fml.controller.CommonFIB;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.gina.model.listener.FIBMouseClickListener;
import org.openflexo.gina.swing.view.widget.JFIBTableWidget;
import org.openflexo.gina.view.FIBView;
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
public class ViewPointView extends FIBModuleView<ViewPoint>implements FIBMouseClickListener {

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
	public void mouseClicked(FIBView<?, ?> view, int clickCount) {
		if (view instanceof JFIBTableWidget && ((JFIBTableWidget<?>) view).getSelected() instanceof FlexoObject && clickCount == 2) {
			FlexoObject o = (FlexoObject) ((JFIBTableWidget<?>) view).getSelected();
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
