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
package org.openflexo.foundation;

import java.util.logging.Logger;

import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.EditingContextImpl;

/**
 * The {@link FlexoEditingContext} represents the {@link EditingContext} for the whole application<br>
 * 
 * This {@link EditingContext} is a {@link FlexoService} instanciated in Openflexo service architecture
 * 
 * @author sylvain
 * 
 */
public class FlexoEditingContext extends EditingContextImpl implements FlexoService {

	protected static final Logger logger = Logger.getLogger(FlexoEditingContext.class.getPackage().getName());

	private FlexoServiceManager serviceManager;
	private final FlexoUndoManager undoManager;

	public static FlexoEditingContext createInstance() {
		return new FlexoEditingContext();
	}

	private FlexoEditingContext() {
		undoManager = new FlexoUndoManager();
	}

	@Override
	public FlexoUndoManager getUndoManager() {
		return undoManager;
	}

	@Override
	public void initialize() {
		logger.info("Initialized FlexoEditingContext...");
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		logger.fine(getClass().getSimpleName() + " service received notification " + notification + " from " + caller);
	}

	@Override
	public void register(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

}
