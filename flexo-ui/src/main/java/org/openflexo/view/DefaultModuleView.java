/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.gina.swing.view.JFIBView;
import org.openflexo.gina.view.FIBView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

public class DefaultModuleView<O extends FlexoObject> extends JPanel implements ModuleView<O> {

	private final O representedObject;
	private final FIBView<?, ?> component;
	private final FlexoPerspective perspective;
	private final FlexoController controller;

	public DefaultModuleView(FlexoController controller, O representedObject, JFIBView<?, ?> component, FlexoPerspective perspective) {
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
