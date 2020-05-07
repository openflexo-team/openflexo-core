/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.copypaste.FlexoClipboard;
import org.openflexo.foundation.action.copypaste.FlexoPasteHandler;
import org.openflexo.foundation.action.copypaste.PastingContext;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.pamela.factory.Clipboard;
import org.openflexo.toolbox.StringUtils;

/**
 * Paste Handler suitable for pasting something into a FlexoProperty
 * 
 * @author sylvain
 * 
 */
public class FlexoPropertyPasteHandler extends FlexoPasteHandler<FlexoProperty> {

	private static final Logger logger = Logger.getLogger(FlexoPropertyPasteHandler.class.getPackage().getName());

	@Override
	public Class<FlexoProperty> getPastingPointHolderType() {
		return FlexoProperty.class;
	}

	@Override
	public PastingContext<FlexoProperty> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard) {

		// Wrong focused type
		if (!(focusedObject instanceof FlexoProperty)) {
			return null;
		}
		// Paste a FlexoBehaviourParameter from a FlexoBehaviourParameter
		/*if (focusedObject instanceof FlexoBehaviourParameter) {
			return new DefaultPastingContext<FlexoBehaviour>(((FlexoBehaviourParameter) focusedObject).getFlexoBehaviour(), event);
		}*/

		return null;
	}

	@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<FlexoProperty> pastingContext) {

		Clipboard leaderClipboard = clipboard.getLeaderClipboard();

		// Translating names
		if (leaderClipboard.isSingleObject()) {
			if (leaderClipboard.getSingleContents() instanceof FlexoProperty) {
				translateName((FlexoProperty) leaderClipboard.getSingleContents());
			}
		}
		else {
			for (Object o : leaderClipboard.getMultipleContents()) {
				if (o instanceof FlexoProperty) {
					translateName((FlexoProperty) o);
				}
			}
		}
	}

	private static String translateName(FlexoProperty<?> object) {
		String oldName = object.getName();
		if (StringUtils.isEmpty(oldName)) {
			return null;
		}
		String newName;
		if (oldName.endsWith(COPY_SUFFIX)) {
			newName = oldName + "2";
		}
		else if (oldName.contains(COPY_SUFFIX)) {
			try {
				int currentIndex = Integer.parseInt(oldName.substring(oldName.lastIndexOf(COPY_SUFFIX) + COPY_SUFFIX.length()));
				newName = oldName.substring(0, oldName.lastIndexOf(COPY_SUFFIX)) + COPY_SUFFIX + (currentIndex + 1);
			} catch (NumberFormatException e) {
				logger.warning("Could not parse as int " + oldName.substring(oldName.lastIndexOf(COPY_SUFFIX)));
				newName = oldName + COPY_SUFFIX;
			}
		}
		else {
			newName = oldName + COPY_SUFFIX;
		}
		System.out.println("translating name from " + oldName + " to " + newName);
		try {
			object.setName(newName);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newName;
	}

}
