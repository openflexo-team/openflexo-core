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

import java.util.Enumeration;
import java.util.Vector;

import org.openflexo.foundation.FlexoObject;

/**
 * Default implementation for a SelectionSynchronizedComponent
 * 
 * @author sguerin
 */
public abstract class DefaultSelectionSynchronizedComponent implements SelectionSynchronizedComponent {

	private SelectionManager _selectionManager;

	public DefaultSelectionSynchronizedComponent(SelectionManager selectionManager) {
		super();
		_selectionManager = selectionManager;
	}

	@Override
	public SelectionManager getSelectionManager() {
		return _selectionManager;
	}

	@Override
	public Vector<FlexoObject> getSelection() {
		if (getSelectionManager() != null) {
			return getSelectionManager().getSelection();
		}
		return null;
	}

	@Override
	public void resetSelection() {
		if (getSelectionManager() != null) {
			getSelectionManager().resetSelection();
		}
		else {
			fireResetSelection();
		}
	}

	@Override
	public void addToSelected(FlexoObject object) {
		if (mayRepresents(object)) {
			if (getSelectionManager() != null) {
				getSelectionManager().addToSelected(object);
			}
			else {
				fireObjectSelected(object);
			}
		}
	}

	@Override
	public void removeFromSelected(FlexoObject object) {
		if (mayRepresents(object)) {
			if (getSelectionManager() != null) {
				getSelectionManager().removeFromSelected(object);
			}
			else {
				fireObjectDeselected(object);
			}
		}
	}

	@Override
	public void addToSelected(Vector<? extends FlexoObject> objects) {
		if (getSelectionManager() != null) {
			getSelectionManager().addToSelected(objects);
		}
		else {
			fireBeginMultipleSelection();
			for (Enumeration<?> en = objects.elements(); en.hasMoreElements();) {
				FlexoObject next = (FlexoObject) en.nextElement();
				fireObjectSelected(next);
			}
			fireEndMultipleSelection();
		}
	}

	@Override
	public void removeFromSelected(Vector<? extends FlexoObject> objects) {
		if (getSelectionManager() != null) {
			getSelectionManager().removeFromSelected(objects);
		}
		else {
			fireBeginMultipleSelection();
			for (Enumeration<?> en = objects.elements(); en.hasMoreElements();) {
				FlexoObject next = (FlexoObject) en.nextElement();
				fireObjectDeselected(next);
			}
			fireEndMultipleSelection();
		}
	}

	@Override
	public void setSelectedObjects(Vector<? extends FlexoObject> objects) {
		if (getSelectionManager() != null) {
			getSelectionManager().setSelectedObjects(objects);
		}
		else {
			resetSelection();
			addToSelected(objects);
		}
	}

	@Override
	public FlexoObject getFocusedObject() {
		if (getSelectionManager() != null) {
			return getSelectionManager().getFocusedObject();
		}
		return null;
	}

	@Override
	public boolean mayRepresents(FlexoObject anObject) {
		return true;
	}

	@Override
	public abstract void fireObjectSelected(FlexoObject object);

	@Override
	public abstract void fireObjectDeselected(FlexoObject object);

	@Override
	public abstract void fireResetSelection();

	@Override
	public abstract void fireBeginMultipleSelection();

	@Override
	public abstract void fireEndMultipleSelection();

}
