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
import org.openflexo.foundation.action.copypaste.DefaultPastingContext;
import org.openflexo.foundation.action.copypaste.FlexoClipboard;
import org.openflexo.foundation.action.copypaste.FlexoPasteHandler;
import org.openflexo.foundation.action.copypaste.PastingContext;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.DuplicateVirtualModel;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.pamela.factory.Clipboard;

/**
 * Paste Handler suitable for pasting something into a {@link RepositoryFolder} in {@link FMLTechnologyAdapter}<br>
 * 
 * Handled objects are those:
 * <ul>
 * <li>Pasting of a {@link VirtualModel}</li>
 * </ul>
 * 
 * Other kind of objects not specific for {@link VirtualModel} will be handled in {@link FlexoConceptPasteHandler}
 * 
 * 
 * @author sylvain
 * 
 */
public class RepositoryFolderPasteHandler extends FlexoPasteHandler<RepositoryFolder> {

	private static final Logger logger = Logger.getLogger(RepositoryFolderPasteHandler.class.getPackage().getName());

	@Override
	public Class<RepositoryFolder> getPastingPointHolderType() {
		return RepositoryFolder.class;
	}

	@Override
	public PastingContext<RepositoryFolder> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard) {

		if (clipboard.getLeaderClipboard().isSingleObject()
				&& (clipboard.getLeaderClipboard().getSingleContents() instanceof VirtualModel)) {

			/*if (focusedObject instanceof VirtualModelResource) {
				// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
				return new DefaultPastingContext<>(((VirtualModelResource) focusedObject).getVirtualModel());
			}
			
			if (focusedObject instanceof FlexoConceptObject) {
				// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
				return new DefaultPastingContext<>(((FlexoConceptObject) focusedObject).getOwningVirtualModel());
			}*/

			if (focusedObject instanceof RepositoryFolder) {
				// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
				return new DefaultPastingContext<>((RepositoryFolder) focusedObject);
			}
		}

		return null;
	}

	@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<RepositoryFolder> pastingContext) {

		Clipboard leaderClipboard = clipboard.getLeaderClipboard();

		// Translating names
		if (leaderClipboard.isSingleObject()) {

			if (clipboard.getLeaderClipboard().getSingleContents() instanceof VirtualModel
					&& pastingContext.getPastingPointHolder() != null) {
				System.out.println("OK on paste un VM dans un folder");
				System.out.println("Copying " + clipboard.getLeaderClipboard().getSingleContents());
				System.out.println("In " + pastingContext);
				System.out.println("Holder " + pastingContext.getPastingPointHolder());

				/*VirtualModel originalVM = (VirtualModel) clipboard.getLeaderClipboard().getOriginalContents()[0];
				VirtualModel copy = (VirtualModel) clipboard.getLeaderClipboard().getSingleContents();
				
				VirtualModelResourceFactory vmResFactory = originalVM.getTechnologyAdapter().getVirtualModelResourceFactory();
				
				System.out.println("On doit cloner la resource " + originalVM.getResource());
				System.out.println("vmResFactory=" + vmResFactory);
				
				VirtualModelResource newResource;
				try {
					newResource = vmResFactory.makeContainedVirtualModelResource(originalVM.getResource().getName() + "-Copy",
							pastingContext.getPastingPointHolder().getVirtualModelResource(), false);
					System.out.println("On vient de creer " + newResource);
					copy.setResource(newResource);
					newResource.setResourceData(copy);
					newResource.save();
				} catch (SaveResourceException e) {
					e.printStackTrace();
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				}*/
			}

		}
		else {
			/*for (Object o : leaderClipboard.getMultipleContents()) {
				if (o instanceof FlexoConceptObject) {
					translateName((FlexoConceptObject) o);
				}
			}*/
		}
	}

	@Override
	public boolean isPastable(FlexoClipboard clipboard, PastingContext<RepositoryFolder> pastingContext) {

		// System.out.println("Je me demande si c'est pastable dans " + focusedObject);
		// System.out.println("Moi j'ai ca:" + clipboard.debug());

		if (clipboard.getLeaderClipboard().isSingleObject() && clipboard.getLeaderClipboard().getSingleContents() instanceof FlexoConcept
				&& pastingContext != null && pastingContext.getPastingPointHolder() != null) {
			return true;
		}

		return super.isPastable(clipboard, pastingContext);
	}

	@Override
	public Object paste(FlexoClipboard clipboard, PastingContext<RepositoryFolder> pastingContext) {

		if (clipboard.getLeaderClipboard().isSingleObject() && clipboard.getLeaderClipboard().getSingleContents() instanceof VirtualModel
				&& pastingContext.getPastingPointHolder() != null) {
			System.out.println("Pasting VirtualModel in a folder");
			// System.out.println("Copying " + clipboard.getLeaderClipboard().getSingleContents());
			// System.out.println("In " + pastingContext);
			// System.out.println("Holder " + pastingContext.getPastingPointHolder());

			VirtualModel originalVirtualModel = (VirtualModel) clipboard.getLeaderClipboard().getOriginalContents()[0];
			DuplicateVirtualModel action = DuplicateVirtualModel.actionType.makeNewAction(originalVirtualModel, null,
					clipboard.getEditor());
			action.setTargetFolder(pastingContext.getPastingPointHolder());

			String baseName = originalVirtualModel.getName();
			int index = 2;
			while (!action.isValid()) {
				action.setNewVirtualModelName(baseName + index);
				index++;
			}
			action.doAction();
			return action.getDuplicate();
		}

		return null;
	}

}
