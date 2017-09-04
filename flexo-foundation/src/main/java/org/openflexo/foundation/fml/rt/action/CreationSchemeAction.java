/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.rt.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

/**
 * Provides execution environment of a {@link CreationScheme} on a given {@link VirtualModelInstance} as a {@link FlexoAction}<br>
 * The {@link VirtualModelInstance} is the instance where new {@link FlexoConceptInstance} will be instantiated
 *
 * An {@link CreationSchemeAction} represents the execution (in the "instances" world) of an {@link CreationScheme} in a given
 * {@link VirtualModelInstance}.<br>
 * To be used and executed on Openflexo platform, it is wrapped in a {@link FlexoAction}.<br>
 * 
 * @author sylvain
 *
 */
public class CreationSchemeAction extends AbstractCreationSchemeAction<CreationSchemeAction, CreationScheme, VirtualModelInstance<?, ?>> {

	private static final Logger logger = Logger.getLogger(CreationSchemeAction.class.getPackage().getName());

	/**
	 * Constructor to be used with a factory
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public CreationSchemeAction(CreationSchemeActionFactory actionFactory, VirtualModelInstance<?, ?> focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action without factory
	 * 
	 * @param creationScheme
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public CreationSchemeAction(CreationScheme creationScheme, VirtualModelInstance<?, ?> focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(creationScheme, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action as an action embedded in another one
	 * 
	 * @param creationScheme
	 * @param focusedObject
	 * @param globalSelection
	 * @param ownerAction
	 */
	public CreationSchemeAction(CreationScheme creationScheme, VirtualModelInstance<?, ?> focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoAction<?, ?, ?> ownerAction) {
		super(creationScheme, focusedObject, globalSelection, ownerAction);
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		return true;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParametersException, FlexoException {
		super.doAction(context);
	}

}
