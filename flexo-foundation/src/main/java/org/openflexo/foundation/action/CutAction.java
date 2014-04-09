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
package org.openflexo.foundation.action;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fge.control.exceptions.CopyException;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.model.factory.ModelFactory;

public class CutAction extends AbstractCopyAction<CutAction> {

	private static final Logger logger = Logger.getLogger(CutAction.class.getPackage().getName());

	public static class CutActionType extends AbstractCopyActionType<CutAction> {

		public CutActionType(FlexoEditingContext editingContext) {
			super("cut", editingContext);
		}

		/**
		 * Factory method
		 */
		@Override
		public CutAction makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			try {
				prepareCopy(getGlobalSelectionAndFocusedObject(focusedObject, globalSelection));
			} catch (InvalidSelectionException e) {
				return null;
			}
			CutAction returned = new CutAction(this, focusedObject, globalSelection, editor);
			returned.editingContext = editingContext;
			returned.objectsToBeCopied = objectsToBeCopied;
			returned.pamelaResource = pamelaResource;
			returned.copyContext = copyContext;
			return returned;
		}
	};

	private FlexoEditingContext editingContext;
	private List<Object> objectsToBeCopied;
	private PamelaResource<?, ?> pamelaResource;
	private Object copyContext;

	private Clipboard clipboard;

	CutAction(CutActionType actionType, FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws InvalidSelectionException {
		logger.info("CUT");
		try {
			clipboard = cut();
			editingContext.setClipboard(clipboard);
		} catch (CopyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Cut current selection in the clipboard
	 * 
	 * @throws CopyException
	 * @throws InvalidSelectionException
	 */
	private Clipboard cut() throws CopyException, InvalidSelectionException {

		ModelFactory modelFactory = pamelaResource.getFactory();

		/*System.out.println("CUT");
		System.out.println("pamelaResource=" + pamelaResource);
		System.out.println("modelFactory=" + modelFactory);
		System.out.println("copyContext=" + copyContext);
		System.out.println("objectsToBeCopied=" + objectsToBeCopied);*/

		try {

			clipboard = modelFactory.cut(objectsToBeCopied.toArray(new Object[objectsToBeCopied.size()]));
			clipboard.setCopyContext(copyContext);
			// System.out.println(clipboard.debug());
			// System.out.println("copyContext=" + copyContext);
			// TODO ?
			// notifyObservers(new SelectionCopied(clipboard));
			return clipboard;
		} catch (Throwable e) {
			throw new CopyException(e, modelFactory);
		}
	}

}
