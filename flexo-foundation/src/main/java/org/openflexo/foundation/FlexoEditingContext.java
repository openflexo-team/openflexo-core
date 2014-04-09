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

import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.CopyAction.CopyActionType;
import org.openflexo.foundation.action.CutAction.CutActionType;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.action.PasteAction.PasteActionType;
import org.openflexo.foundation.action.SelectAllAction.SelectAllActionType;
import org.openflexo.model.factory.Clipboard;
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
	private FlexoUndoManager undoManager;

	private CopyActionType copyActionType;
	private CutActionType cutActionType;
	private PasteActionType pasteActionType;
	private SelectAllActionType selectAllActionType;

	private Clipboard clipboard;

	public static FlexoEditingContext createInstance() {
		return new FlexoEditingContext();
	}

	private FlexoEditingContext() {
	}

	@Override
	public FlexoUndoManager getUndoManager() {
		return undoManager;
	}

	@Override
	public void initialize() {
		logger.info("Initialized FlexoEditingContext...");
		undoManager = new FlexoUndoManager();
		copyActionType = new CopyActionType(this);
		FlexoObjectImpl.addActionForClass(copyActionType, FlexoObject.class);
		cutActionType = new CutActionType(this);
		FlexoObjectImpl.addActionForClass(cutActionType, FlexoObject.class);
		pasteActionType = new PasteActionType(this);
		FlexoObjectImpl.addActionForClass(pasteActionType, FlexoObject.class);
		selectAllActionType = new SelectAllActionType(this);
		FlexoObjectImpl.addActionForClass(selectAllActionType, FlexoObject.class);
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

	public CopyActionType getCopyActionType() {
		return copyActionType;
	}

	public CutActionType getCutActionType() {
		return cutActionType;
	}

	public PasteActionType getPasteActionType() {
		return pasteActionType;
	}

	public SelectAllActionType getSelectAllActionType() {
		return selectAllActionType;
	}

	public Clipboard getClipboard() {
		return clipboard;
	}

	public void setClipboard(Clipboard clipboard) {
		this.clipboard = clipboard;
	}

}
