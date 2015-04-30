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

package org.openflexo.foundation.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoProperty;

public class SortFlexoProperties extends FlexoAction<SortFlexoProperties, FlexoObject, FlexoObject> {

	public static final FlexoActionType<SortFlexoProperties, FlexoObject, FlexoObject> actionType = new FlexoActionType<SortFlexoProperties, FlexoObject, FlexoObject>(
			"sort_flexo_properties") {

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return false;
		}

		@Override
		public SortFlexoProperties makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new SortFlexoProperties(focusedObject, globalSelection, editor);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(actionType, FlexoObject.class);
	}

	private String name;
	private String value;

	private FlexoProperty createdProperty;

	public SortFlexoProperties(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		Vector<FlexoObject> v = getGlobalSelectionAndFocusedObject();
		for (FlexoObject object : v) {
			sortPropertiesForObject(object);
		}
	}

	private void sortPropertiesForObject(FlexoObject object) {
		Collections.sort(object.getCustomProperties(), new Comparator<FlexoProperty>() {

			@Override
			public int compare(FlexoProperty o1, FlexoProperty o2) {
				if (o1.getName() == null) {
					if (o2.getName() == null) {
						return 0;
					} else {
						return -1;
					}
				} else {
					if (o2.getName() == null) {
						return 1;
					}
					return o1.getName().compareTo(o2.getName());
				}
			}

		});
	}

}
