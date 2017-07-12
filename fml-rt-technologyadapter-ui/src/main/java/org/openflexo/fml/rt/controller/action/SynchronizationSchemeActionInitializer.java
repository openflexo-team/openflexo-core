/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller.action;

import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.FlexoActionFinalizer;
import org.openflexo.foundation.action.FlexoActionInitializer;
import org.openflexo.foundation.action.FlexoExceptionHandler;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.ParametersRetriever;

public class SynchronizationSchemeActionInitializer
		extends ActionInitializer<SynchronizationSchemeAction, AbstractVirtualModelInstance<?, ?>, VirtualModelInstanceObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public SynchronizationSchemeActionInitializer(ControllerActionInitializer actionInitializer) {
		super(null, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<SynchronizationSchemeAction> getDefaultInitializer() {
		return new FlexoActionInitializer<SynchronizationSchemeAction>() {
			@Override
			public boolean run(EventObject e, SynchronizationSchemeAction action) {
				getController().willExecute(action);
				ParametersRetriever<SynchronizationScheme> parameterRetriever = new ParametersRetriever<SynchronizationScheme>(action,
						getController() != null ? getController().getApplicationContext() : null);
				if (action.escapeParameterRetrievingWhenValid && parameterRetriever.isSkipable()) {
					return true;
				}
				return parameterRetriever.retrieveParameters();
			}
		};
	}

	@Override
	protected FlexoActionFinalizer<SynchronizationSchemeAction> getDefaultFinalizer() {
		return new FlexoActionFinalizer<SynchronizationSchemeAction>() {
			@Override
			public boolean run(EventObject e, SynchronizationSchemeAction action) {
				getController().hasExecuted(action);
				return true;
			}
		};
	}

	@Override
	protected FlexoExceptionHandler<SynchronizationSchemeAction> getDefaultExceptionHandler() {
		return new FlexoExceptionHandler<SynchronizationSchemeAction>() {
			@Override
			public boolean handleException(FlexoException exception, SynchronizationSchemeAction action) {
				if (exception instanceof NotImplementedException) {
					FlexoController.notify(action.getLocales().localizedForKey("not_implemented_yet"));
					return true;
				}
				return false;
			}
		};
	}

	@Override
	protected Icon getEnabledIcon() {
		return FMLIconLibrary.SYNCHRONIZATION_SCHEME_ICON;
	}

}
