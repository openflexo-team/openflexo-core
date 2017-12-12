/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Freemodellingeditor, a component of the software infrastructure 
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

package org.openflexo.foundation.nature;

import java.util.Vector;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.resource.SaveResourceException;

/**
 * Base implementation for a {@link FlexoAction} given a {@link ProjectNature} to a {@link FlexoProject}
 * 
 * @author sylvain
 */
public abstract class GivesNatureAction<A extends GivesNatureAction<A, N>, N extends ProjectNature<N>>
		extends FlexoAction<A, FlexoProject<?>, FlexoObject> {

	private N newNature;

	protected GivesNatureAction(FlexoActionFactory<A, FlexoProject<?>, FlexoObject> actionFactory, FlexoProject<?> focusedObject,
			Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	public abstract N makeNewNature();

	public N getNewNature() {
		return newNature;
	}

	@Override
	protected void doAction(Object context) throws SaveResourceException, InvalidArgumentException {

		newNature = makeNewNature();
		getFocusedObject().addToProjectNatures(newNature);

		// We have now to notify project of nature modifications
		getFocusedObject().getPropertyChangeSupport().firePropertyChange("hasNature(String)", false, true);
		getFocusedObject().getPropertyChangeSupport().firePropertyChange("hasNature(Class)", false, true);
		getFocusedObject().getPropertyChangeSupport().firePropertyChange("getNature(String)", false, true);
		getFocusedObject().getPropertyChangeSupport().firePropertyChange("getNature(Class)", false, true);
	}

}
