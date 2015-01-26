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

import org.openflexo.foundation.FlexoObject;

/**
 * Implemented by components back-synchronized with a SelectionManager This means that this component, once registered in a
 * SelectionManager, reveive synchronization request, but is not supposed to send some.
 * 
 * @author sguerin
 */
public interface SelectionListener {

	/**
	 * Notified that supplied object has been added to selection
	 * 
	 * @param object
	 *            : the object that has been added to selection
	 */
	public void fireObjectSelected(FlexoObject object);

	/**
	 * Notified that supplied object has been removed from selection
	 * 
	 * @param object
	 *            : the object that has been removed from selection
	 */
	public void fireObjectDeselected(FlexoObject object);

	/**
	 * Notified selection has been resetted
	 */
	public void fireResetSelection();

	/**
	 * Notified that the selection manager is performing a multiple selection
	 */
	public void fireBeginMultipleSelection();

	/**
	 * Notified that the selection manager has finished to perform a multiple selection
	 */
	public void fireEndMultipleSelection();

}
