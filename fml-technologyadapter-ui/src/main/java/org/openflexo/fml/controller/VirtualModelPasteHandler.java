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
import org.openflexo.foundation.action.copypaste.DefaultPastingContext;
import org.openflexo.foundation.action.copypaste.FlexoClipboard;
import org.openflexo.foundation.action.copypaste.FlexoPasteHandler;
import org.openflexo.foundation.action.copypaste.PastingContext;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.DuplicateVirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.Clipboard;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * Paste Handler suitable for pasting something into a {@link VirtualModel}<br>
 * 
 * Handled objects are those:
 * <ul>
 * <li>Pasting of a {@link FlexoConcept} as a concept of VirtualModel</li>
 * <li>Pasting of a {@link VirtualModel} as a child virtual model of this virtual model</li>
 * </ul>
 * 
 * Other kind of objects not specific for {@link VirtualModel} will be handled in {@link FlexoConceptPasteHandler}
 * 
 * 
 * @author sylvain
 * 
 */
public class VirtualModelPasteHandler extends FlexoPasteHandler<VirtualModel> {

	private static final Logger logger = Logger.getLogger(VirtualModelPasteHandler.class.getPackage().getName());

	@Override
	public Class<VirtualModel> getPastingPointHolderType() {
		return VirtualModel.class;
	}

	@Override
	public PastingContext<VirtualModel> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard) {

		if (clipboard.getLeaderClipboard().isSingleObject()
				&& (clipboard.getLeaderClipboard().getSingleContents() instanceof FlexoConcept)) {

			if (focusedObject instanceof CompilationUnitResource) {
				// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
				return new DefaultPastingContext<>(((CompilationUnitResource) focusedObject).getCompilationUnit().getVirtualModel());
			}

			if (focusedObject instanceof VirtualModel) {
				// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
				return new DefaultPastingContext<>((VirtualModel) focusedObject);
			}

			if (focusedObject instanceof FlexoConceptObject) {
				// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
				FlexoConcept relatedConcept = ((FlexoConceptObject) focusedObject).getFlexoConcept();
				if (relatedConcept instanceof VirtualModel) {
					return new DefaultPastingContext<>((VirtualModel) relatedConcept);
				}
			}
		}

		return null;
	}

	@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<VirtualModel> pastingContext) {

		Clipboard leaderClipboard = clipboard.getLeaderClipboard();

		// Translating names
		if (leaderClipboard.isSingleObject()) {

			if (clipboard.getLeaderClipboard().getSingleContents() instanceof VirtualModel
					&& pastingContext.getPastingPointHolder() != null) {

				/*System.out.println("OK on paste un VM dans un autre VM");
				System.out.println("Copying " + clipboard.getLeaderClipboard().getSingleContents());
				System.out.println("In " + pastingContext);
				System.out.println("Holder " + pastingContext.getPastingPointHolder());*/

				/*VirtualModel originalVM = (VirtualModel) clipboard.getLeaderClipboard().getOriginalContents()[0];
				VirtualModel copy = (VirtualModel) clipboard.getLeaderClipboard().getSingleContents();
				
				CompilationUnitResourceFactory vmResFactory = originalVM.getTechnologyAdapter().getCompilationUnitResourceFactory();
				
				System.out.println("On doit cloner la resource " + originalVM.getResource());
				System.out.println("vmResFactory=" + vmResFactory);
				
				CompilationUnitResource newResource;
				try {
					newResource = vmResFactory.makeContainedCompilationUnitResource(originalVM.getResource().getName() + "-Copy",
							(CompilationUnitResource)pastingContext.getPastingPointHolder().getCompilationUnit().getResource(), false);
					System.out.println("On vient de creer " + newResource);
					copy.getCompilationUnit().setResource(newResource);
					newResource.setResourceData(copy.getCompilationUnit());
					newResource.save();
				} catch (SaveResourceException e) {
					e.printStackTrace();
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				}*/
			}

			/*else if (leaderClipboard.getSingleContents() instanceof FlexoConceptObject) {
				translateName((FlexoConceptObject) leaderClipboard.getSingleContents());
			}*/
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
	public boolean isPastable(FlexoClipboard clipboard, PastingContext<VirtualModel> pastingContext) {

		// System.out.println("Je me demande si c'est pastable dans " + focusedObject);
		// System.out.println("Moi j'ai ca:" + clipboard.debug());

		if (clipboard.getLeaderClipboard().isSingleObject() && clipboard.getLeaderClipboard().getSingleContents() instanceof FlexoConcept
				&& pastingContext != null && pastingContext.getPastingPointHolder() != null) {
			return true;
		}

		return super.isPastable(clipboard, pastingContext);
	}

	private VirtualModel pasteVirtualModel(VirtualModel originalVirtualModel, VirtualModel targetVirtualModel, FlexoClipboard clipboard) {

		targetVirtualModel.getCompilationUnit().loadContainedVirtualModelsWhenUnloaded();

		DuplicateVirtualModel action = DuplicateVirtualModel.actionType.makeNewAction(originalVirtualModel, null, clipboard.getEditor());
		action.setTargetContainer(targetVirtualModel.getResource());
		action.setNewVirtualModelURI(null);

		String baseName = originalVirtualModel.getName();

		char charAt = baseName.charAt(baseName.length() - 1);
		int index;
		try {
			index = Integer.parseInt("" + charAt) + 1;
			baseName = baseName.substring(0, baseName.length() - 1);
		} catch (NumberFormatException e) {
			index = 2;
		}

		while (!action.isValid() && index < 1000) {
			action.setNewVirtualModelName(baseName + index);
			index++;
		}
		action.doAction();
		return action.getDuplicate();

	}

	@Override
	public Object paste(FlexoClipboard clipboard, PastingContext<VirtualModel> pastingContext) {

		if (pastingContext.getPastingPointHolder() == null) {
			return null;
		}

		if (clipboard.getLeaderClipboard().isSingleObject()) {

			if (clipboard.getLeaderClipboard().getSingleContents() instanceof VirtualModel) {
				System.out.println("Pasting a VirtualModel in another VirtualModel");
				// System.out.println("Copying " + clipboard.getLeaderClipboard().getSingleContents());
				// System.out.println("In " + pastingContext);
				// System.out.println("Holder " + pastingContext.getPastingPointHolder());

				VirtualModel originalVirtualModel = (VirtualModel) clipboard.getLeaderClipboard().getOriginalContents()[0];
				VirtualModel targetVirtualModel = pastingContext.getPastingPointHolder();
				return pasteVirtualModel(originalVirtualModel, targetVirtualModel, clipboard);

			}

			else if (clipboard.getLeaderClipboard().getSingleContents() instanceof FlexoConcept) {
				System.out.println("Pasting a FlexoConcept in a VirtualModel");

				try {

					ModelEntity<VirtualModel> vmEntity = clipboard.getLeaderClipboard().getModelFactory().getModelContext()
							.getModelEntity(VirtualModel.class);
					ModelProperty<? super VirtualModel> conceptProperty = vmEntity.getModelProperty(VirtualModel.FLEXO_CONCEPTS_KEY);

					// System.out.println("OK, je copie le concept dans le VM " + pastingContext.getPastingPointHolder());
					// System.out.println("vmEntity=" + vmEntity);
					// System.out.println("conceptProperty=" + conceptProperty);

					FlexoConcept copiedConcept = (FlexoConcept) clipboard.getLeaderClipboard().getSingleContents();
					translateNameWhenRequired(copiedConcept, pastingContext.getPastingPointHolder());

					// System.out.println(copiedConcept.getFMLRepresentation());

					return clipboard.getLeaderClipboard().getModelFactory().paste(clipboard.getLeaderClipboard(), conceptProperty,
							pastingContext.getPastingPointHolder());

				} catch (ModelExecutionException e) {
					e.printStackTrace();
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}

			}
			return null;
		}
		else { // Multiple objects in clipboard, not implemented yet

			/*System.out.println("MultipleContents= " + clipboard.getLeaderClipboard().getMultipleContents());
			
			for (int i = 0; i < clipboard.getLeaderClipboard().getMultipleContents().size(); i++) {
				Object originalContent = clipboard.getLeaderClipboard().getOriginalContents()[i];
				Object copiedContent = clipboard.getLeaderClipboard().getMultipleContents().get(i);
				System.out.println("**** originalContent=" + originalContent);
				System.out.println("     copiedContent=" + copiedContent);
			}*/

			return null;
		}

	}

	private static void translateNameWhenRequired(FlexoConcept copiedConcept, VirtualModel virtualModel) {

		String baseName = copiedConcept.getName();

		if (virtualModel.getFlexoConcept(baseName) == null) {
			return;
		}

		char charAt = baseName.charAt(baseName.length() - 1);
		int index;
		try {
			index = Integer.parseInt("" + charAt) + 1;
			baseName = baseName.substring(0, baseName.length() - 1);
		} catch (NumberFormatException e) {
			index = 2;
		}

		String testedName = baseName + index;
		while (virtualModel.getFlexoConcept(testedName) != null && index < 1000) {
			index++;
			testedName = baseName + index;
		}

		try {
			copiedConcept.setName(testedName);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
