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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

/**
 * Provides execution environment of a {@link DeletionScheme} on a given {@link FlexoConceptInstance} as a {@link FlexoAction}
 *
 * An {@link DeletionSchemeAction} represents the execution (in the "instances" world) of a {@link DeletionScheme}.<br>
 * To be used and executed on Openflexo platform, it is wrapped in a {@link FlexoAction}.<br>
 * 
 * @author sylvain
 */
public class DeletionSchemeAction extends AbstractActionSchemeAction<DeletionSchemeAction, DeletionScheme, FlexoConceptInstance> {

	private static final Logger logger = Logger.getLogger(DeletionSchemeAction.class.getPackage().getName());

	/**
	 * Constructor to be used with a factory
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public DeletionSchemeAction(DeletionSchemeActionFactory actionType, FlexoConceptInstance focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action without factory
	 * 
	 * @param flexoBehaviour
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public DeletionSchemeAction(DeletionScheme deletionScheme, FlexoConceptInstance focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(deletionScheme, focusedObject, globalSelection, editor);
	}

	/**
	 * Constructor to be used for creating a new action as an action embedded in another one
	 * 
	 * @param flexoBehaviour
	 * @param focusedObject
	 * @param globalSelection
	 * @param ownerAction
	 *            Action in which action to be created will be embedded
	 */
	public DeletionSchemeAction(DeletionScheme deletionScheme, FlexoConceptInstance focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoAction<?, ?, ?> ownerAction) {
		super(deletionScheme, focusedObject, globalSelection, ownerAction);
	}

	public DeletionScheme getDeletionScheme() {
		return getFlexoBehaviour();
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParametersException, FlexoException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Delete FlexoConceptInstance using DeletionScheme");
			logger.fine("getDeletionScheme()=" + getDeletionScheme());
			logger.fine("getFlexoConceptInstance()=" + getFlexoConceptInstance());
		}

		// System.out.println("Delete " + getFlexoConceptInstance());
		// System.out.println("Concept: " + getFlexoConceptInstance().getFlexoConcept());
		// System.out.println("DeletionScheme: " + getDeletionScheme().getFMLRepresentation());
		// System.out.println("Defined in " + getDeletionScheme().getFlexoConcept());

		if (getFlexoConceptInstance() == null) {
			throw new InvalidParametersException("Cannot delete a null FlexoConceptInstance");
		}
		if (getFlexoConceptInstance().getFlexoConcept() == null) {
			throw new InvalidParametersException("Cannot delete a FlexoConceptInstance with null concept: " + getFlexoConceptInstance());
		}

		if (getDeletionScheme() != null) {
			if (getDeletionScheme().getFlexoConcept() == null) {
				throw new InvalidParametersException(
						"Inconsistent data: DeletionScheme is not defined in any FlexoConcept: " + getDeletionScheme());
			}
			if (getDeletionScheme().getFlexoConcept().isAssignableFrom(getFlexoConceptInstance().getFlexoConcept())) {
				getFlexoConceptInstance().deleteWithScheme(getDeletionScheme(), this);
			}
			else {
				throw new InvalidParametersException("DeletionScheme " + getDeletionScheme() + " is not a behaviour defined for "
						+ getFlexoConceptInstance().getFlexoConcept());
			}
		}
		else {
			getFlexoConceptInstance().delete(context);
		}
	}

}
