/*
 * (c) Copyright 2013-2014 Openflexo
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
package org.openflexo.vpm.controller;

import java.awt.Event;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoClipboard;
import org.openflexo.foundation.action.PasteAction.DefaultPastingContext;
import org.openflexo.foundation.action.PasteAction.PasteHandler;
import org.openflexo.foundation.action.PasteAction.PastingContext;
import org.openflexo.foundation.fml.ActionContainer;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.toolbox.StringUtils;

/**
 * Paste Handler suitable for pasting something into a FlexoAction
 * 
 * @author sylvain
 * 
 */
public class ActionContainerPasteHandler implements PasteHandler<FlexoBehaviourObject> {

	private static final Logger logger = Logger.getLogger(ActionContainerPasteHandler.class.getPackage().getName());

	public static final String COPY_SUFFIX = "-copy";

	@Override
	public Class<FlexoBehaviourObject> getPastingPointHolderType() {
		return FlexoBehaviourObject.class;
	}

	@Override
	public PastingContext<FlexoBehaviourObject> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard, Event event) {

		// Wrong focused type
		if (!(focusedObject instanceof FlexoBehaviourObject)) {
			return null;
		}
		// Paste a Flexo Action in a Flexo Action Container
		if ((focusedObject instanceof ActionContainer)) {
			return new DefaultPastingContext<FlexoBehaviourObject>((FlexoBehaviourObject) focusedObject, event);
		}
		// Paste a Flexo Action from a Flexo Action
		if ((focusedObject instanceof EditionAction<?, ?>)) {
			return new DefaultPastingContext<FlexoBehaviourObject>(
					(FlexoBehaviourObject) ((EditionAction<?, ?>) focusedObject).getActionContainer(), event);
		}

		return null;
	}

	@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<FlexoBehaviourObject> pastingContext) {

		Clipboard leaderClipboard = clipboard.getLeaderClipboard();

		// Translating names
		if (leaderClipboard.isSingleObject()) {
			if (leaderClipboard.getSingleContents() instanceof FlexoBehaviourObject) {
				translateName((FlexoBehaviourObject) leaderClipboard.getSingleContents());
			}
		} else {
			for (Object o : leaderClipboard.getMultipleContents()) {
				if (o instanceof FlexoBehaviour) {
					translateName((FlexoBehaviourObject) o);
				}
			}
		}
	}

	private String translateName(FlexoBehaviourObject object) {
		String oldName = object.getName();
		if (StringUtils.isEmpty(oldName)) {
			return null;
		}
		String newName;
		if (oldName.endsWith(COPY_SUFFIX)) {
			newName = oldName + "2";
		} else if (oldName.contains(COPY_SUFFIX)) {
			try {
				int currentIndex = Integer.parseInt(oldName.substring(oldName.lastIndexOf(COPY_SUFFIX) + COPY_SUFFIX.length()));
				newName = oldName.substring(0, oldName.lastIndexOf(COPY_SUFFIX)) + COPY_SUFFIX + (currentIndex + 1);
			} catch (NumberFormatException e) {
				logger.warning("Could not parse as int " + oldName.substring(oldName.lastIndexOf(COPY_SUFFIX)));
				newName = oldName + COPY_SUFFIX;
			}
		} else {
			newName = oldName + COPY_SUFFIX;
		}
		System.out.println("translating name from " + oldName + " to " + newName);
		object.setName(newName);
		return newName;
	}

	@Override
	public void finalizePasting(FlexoClipboard clipboard, PastingContext<FlexoBehaviourObject> pastingContext) {
		// TODO Auto-generated method stub

	}

}
