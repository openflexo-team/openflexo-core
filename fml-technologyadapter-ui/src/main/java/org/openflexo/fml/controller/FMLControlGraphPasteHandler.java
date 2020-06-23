/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openflexo.foundation.action.copypaste.FlexoClipboard;
import org.openflexo.foundation.action.copypaste.FlexoPasteHandler;
import org.openflexo.foundation.action.copypaste.PastingContext;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;

/**
 * Paste Handler suitable for pasting a FMLControlGraph
 * 
 * @author sylvain
 * 
 */
public class FMLControlGraphPasteHandler extends FlexoPasteHandler<FMLControlGraph> {

	private static final Logger logger = Logger.getLogger(FMLControlGraphPasteHandler.class.getPackage().getName());

	@Override
	public Class<FMLControlGraph> getPastingPointHolderType() {
		return FMLControlGraph.class;
	}

	@Override
	public boolean isPastable(FlexoClipboard clipboard, PastingContext<FMLControlGraph> pastingContext) {
		return	clipboard.getLeaderClipboard().isSingleObject() &&
				clipboard.getLeaderClipboard().getSingleContents() instanceof FMLControlGraph
				&& pastingContext.getPastingPointHolder() != null;
	}

	@Override
	public Object paste(FlexoClipboard clipboard, PastingContext<FMLControlGraph> pastingContext) {

		FMLControlGraph cgBeeingPasted = (FMLControlGraph) clipboard.getLeaderClipboard().getSingleContents();
		FMLControlGraph cgWhereToPaste = pastingContext.getPastingPointHolder();
		cgWhereToPaste.sequentiallyAppend(cgBeeingPasted);

		try {
			clipboard.getLeaderClipboard().consume();
		} catch (ModelExecutionException | ModelDefinitionException | CloneNotSupportedException e) {
			logger.log(Level.WARNING, "Can't paste", e);
		}

		// return super.paste(clipboard, pastingContext);
		return null;
	}

}
