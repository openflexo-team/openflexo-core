/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation;

/*
 * DataModification.java
 * Project WorkflowEditor
 * 
 * Created by benoit on Mar 5, 2004
 */

/**
 * A DataModification encapsulates a modification that has been done on the datastructure. This object is sent to the datastructure
 * observers.
 * 
 * @author benoit
 */
public class DataModification {

	private String _propertyName;

	private final Object _newValue;

	private final Object _oldValue;

	// ==========================================================================
	// ============================= Constructor
	// ================================
	// ==========================================================================

	/**
	 * @param modifcationType
	 *            : one of the static int declared in this class.
	 * @param oldValue
	 * @param newValue
	 */
	public DataModification(Object oldValue, Object newValue) {
		super();
		_oldValue = oldValue;
		_newValue = newValue;
	}

	public DataModification(String propertyName, Object oldValue, Object newValue) {
		super();
		_oldValue = oldValue;
		_newValue = newValue;
		_propertyName = propertyName;
	}

	public Object oldValue() {
		return _oldValue;
	}

	public Object newValue() {
		return _newValue;
	}

	public String propertyName() {
		return _propertyName;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "/" + _propertyName + "\nold Value: " + (_oldValue != null ? _oldValue : "null")
				+ "\nnew Value: " + (_newValue != null ? _newValue : "null");
	}

	private boolean _isReentrant = false;

	public boolean isReentrant() {
		return _isReentrant;
	}

	public void setReentrant(boolean isReentrant) {
		_isReentrant = isReentrant;
	}
}
