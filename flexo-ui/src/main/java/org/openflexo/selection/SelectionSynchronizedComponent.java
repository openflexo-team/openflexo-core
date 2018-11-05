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

package org.openflexo.selection;

import java.util.Vector;

import org.openflexo.foundation.FlexoObject;

/**
 * Implemented by components fully-synchronized with a SelectionManager This means that this component, once registered in a
 * SelectionManager, reveive synchronization request, and can send it's own selection requests, so that selection and the representation
 * given by current component are synchronized.
 * 
 * @author sguerin
 */
public interface SelectionSynchronizedComponent extends SelectionListener {

	/**
	 * Returns the SelectionManager whose this component is connected to
	 * 
	 * @return the SelectionManager whose this component is connected to
	 */
	public SelectionManager getSelectionManager();

	/**
	 * Return the current selection, as a Vector of FlexoObject
	 * 
	 * @return a Vector of FlexoObject
	 */
	public Vector<FlexoObject> getSelection();

	/**
	 * Reset selection
	 */
	public void resetSelection();

	/**
	 * Add supplied object to current selection
	 * 
	 * @param object
	 *            : the object to add to selection
	 */
	public void addToSelected(FlexoObject object);

	/**
	 * Remove supplied object from current selection
	 * 
	 * @param object
	 *            : the object to remove from selection
	 */
	public void removeFromSelected(FlexoObject object);

	/**
	 * Add supplied objects to current selection
	 * 
	 * @param objects
	 *            : objects to add to selection, as a Vector of FlexoObject
	 */
	public void addToSelected(Vector<? extends FlexoObject> objects);

	/**
	 * Remove supplied objects from current selection
	 * 
	 * @param objects
	 *            : objects to remove from selection, as a Vector of FlexoObject
	 */
	public void removeFromSelected(Vector<? extends FlexoObject> objects);

	/**
	 * Sets supplied vector of FlexoObjects to be the current Selection
	 * 
	 * @param objects
	 *            : the object to set for current selection, as a Vector of FlexoObject
	 */
	public void setSelectedObjects(Vector<? extends FlexoObject> objects);

	/**
	 * Return currently focused object
	 */
	public FlexoObject getFocusedObject();

	/**
	 * Return boolean indicating if supplied object could be represented in current component
	 * 
	 * @param anObject
	 * @return a boolean
	 */
	public boolean mayRepresents(FlexoObject anObject);

}
