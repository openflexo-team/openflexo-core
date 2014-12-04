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
package org.openflexo.foundation.viewpoint.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.LongRunningAction;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.ViewPointObject;

/**
 * Abstract action creating a {@link FlexoConcept} or any of its subclass
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCreateFlexoConcept<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends ViewPointObject>
		extends FlexoAction<A, T1, T2> implements LongRunningAction {

	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConcept.class.getPackage().getName());

	AbstractCreateFlexoConcept(FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

}
