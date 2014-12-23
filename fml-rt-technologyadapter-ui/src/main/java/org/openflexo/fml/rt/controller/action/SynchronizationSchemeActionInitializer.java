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
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.ParametersRetriever;

public class SynchronizationSchemeActionInitializer extends
		ActionInitializer<SynchronizationSchemeAction, VirtualModelInstance, VirtualModelInstanceObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public SynchronizationSchemeActionInitializer(ControllerActionInitializer actionInitializer) {
		super(null, actionInitializer);
	}

	@Override
	protected FlexoActionInitializer<SynchronizationSchemeAction> getDefaultInitializer() {
		return new FlexoActionInitializer<SynchronizationSchemeAction>() {
			@Override
			public boolean run(EventObject e, SynchronizationSchemeAction action) {
				ParametersRetriever<SynchronizationScheme> parameterRetriever = new ParametersRetriever<SynchronizationScheme>(action);
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
					FlexoController.notify(FlexoLocalization.localizedForKey("not_implemented_yet"));
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
