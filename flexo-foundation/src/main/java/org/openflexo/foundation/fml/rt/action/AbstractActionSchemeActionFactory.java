/**
 * 
 * Copyright (c) 2014, Openflexo
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

import org.openflexo.foundation.OpenflexoRuntimeException;
import org.openflexo.foundation.action.ActionGroup;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.AbstractActionScheme;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;

/**
 * Factory for {@link AbstractActionSchemeAction} (an execution environment of a {@link AbstractActionScheme} on a given
 * {@link FlexoConceptInstance} as a {@link FlexoAction})
 * 
 * @author sylvain
 *
 * @param <A>
 *            type of FlexoAction
 * @param <FB>
 *            type of {@link AbstractActionScheme}
 * @param <O>
 *            type of {@link FlexoConceptInstance} on which this action applies
 */
public abstract class AbstractActionSchemeActionFactory<A extends AbstractActionSchemeAction<A, FB, O>, FB extends AbstractActionScheme, O extends FlexoConceptInstance>
		extends FlexoBehaviourActionFactory<A, FB, O> {

	private static final Logger logger = Logger.getLogger(AbstractActionSchemeActionFactory.class.getPackage().getName());

	public AbstractActionSchemeActionFactory(FB actionScheme, O flexoConceptInstance, ActionGroup actionGroup, int actionCategory) {
		super(actionScheme, flexoConceptInstance, actionGroup, actionCategory);
	}

	/**
	 * A behaviour whose {@code @Enable} expression fails is shown disabled (CORE-F-11, decided with the user): running it anyway raises the
	 * failure
	 */
	@Override
	public boolean isEnabledForSelection(O object, Vector<VirtualModelInstanceObject> globalSelection) {
		try {
			return getActionScheme().evaluateCondition(target(object));
		} catch (FMLExecutionException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("@Enable of " + getActionScheme() + " fails on " + object + ": " + e.getMessage());
			}
			return false;
		}
	}

	/**
	 * When the behaviour is about to run, its {@code @Enable} expression is evaluated strictly: its failure fails the action (CORE-F-11)
	 */
	@Override
	public boolean isEnabledForExecution(O object, Vector<VirtualModelInstanceObject> globalSelection) throws OpenflexoRuntimeException {
		// As isEnabled() for a behaviour, no registration on the object type is required
		return getActionScheme().evaluateCondition(target(object));
	}

	/**
	 * A behaviour whose {@code @Visible} expression fails is shown (and disabled, see
	 * {@link #isEnabledForSelection(FlexoConceptInstance, Vector)}), so that the user sees it (CORE-F-11, decided with the user)
	 */
	@Override
	public boolean isVisibleForSelection(O object, Vector<VirtualModelInstanceObject> globalSelection) {
		try {
			return getActionScheme().evaluateVisible(target(object));
		} catch (FMLExecutionException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("@Visible of " + getActionScheme() + " fails on " + object + ": " + e.getMessage());
			}
			return true;
		}
	}

	/**
	 * The instance on which the availability of the behaviour is evaluated: the one this factory was made for (the instance the behaviour is
	 * called on), the focused object of the action otherwise. A behaviour of a concept called as {@code x.doIt()} focuses the virtual model
	 * instance, but its {@code @Enable} speaks of {@code x} (CORE-F-11)
	 */
	private FlexoConceptInstance target(O object) {
		return getFlexoConceptInstance() != null ? getFlexoConceptInstance() : object;
	}

	public AbstractActionScheme getActionScheme() {
		return getBehaviour();
	}

}
