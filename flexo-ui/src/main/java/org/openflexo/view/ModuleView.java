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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This interface is implemented by all views that will be displayed as a top-level view of a module. This abstract representation is used
 * by a general scheme implemented in module controller to manage the navigation and includes a control panel.
 * 
 * @author sguerin
 */
public interface ModuleView<O extends FlexoObject> {

	public O getRepresentedObject();

	/**
	 * Delete the ModuleView
	 * 
	 * VERY IMPORTANT: in all implementations, DO NOT FORGET TO CALL {@link FlexoController.removeModuleView(this)}
	 */
	public void deleteModuleView();

	/**
	 * This method should return the perspective in which this view is supposed to be seen. DO NOT return null!!!
	 * 
	 * @return
	 */
	public FlexoPerspective getPerspective();

	/**
	 * This method is called before the module view is about to be shown
	 * 
	 */
	public void willShow();

	/**
	 * This method is called before the module view is about to be hidden
	 * 
	 */
	public void willHide();

	/**
	 * This method is called when the module view is shown with a controller and perspective
	 * 
	 */
	public void show(FlexoController controller, FlexoPerspective perspective);

	/**
	 * Returns flag indicating if this view is itself responsible for scroll management When not, Flexo will manage it's own scrollbar for
	 * you
	 * 
	 * @return
	 */
	public boolean isAutoscrolled();

}
