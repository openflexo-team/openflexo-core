/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

import javax.swing.JPanel;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

public class EmptyPanel<O extends FlexoObject> extends JPanel implements ModuleView<O> {

	private final FlexoController controller;
	private final FlexoPerspective perspective;
	private final O representedObject;
	private String title = "";

	/**
	 * 
	 */
	public EmptyPanel(FlexoController controller, FlexoPerspective perspective, O representedObject) {
		this.controller = controller;
		this.perspective = perspective;
		this.representedObject = representedObject;
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
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	public FlexoController getController() {
		return controller;
	}

	/**
	 * Overrides willShow
	 * 
	 * @see org.openflexo.view.ModuleView#willShow()
	 */
	@Override
	public void willShow() {

	}

	/**
	 * Overrides willHide
	 * 
	 * @see org.openflexo.view.ModuleView#willHide()
	 */
	@Override
	public void willHide() {
		deleteModuleView();
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
		// Override when required
	}

	/**
	 * Returns flag indicating if this view is itself responsible for scroll management When not, Flexo will manage it's own scrollbar for
	 * you
	 * 
	 * @return
	 */
	@Override
	public boolean isAutoscrolled() {
		return true;
	}

	public String getTitle() {
		if (title == null) {
			return controller.getModuleLocales().localizedForKey(getRepresentedObject().getClass().getSimpleName());
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
