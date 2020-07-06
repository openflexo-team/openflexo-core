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
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

/**
 * Paste Handler suitable for pasting something into a {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
public class FlexoConceptPasteHandler extends FlexoPasteHandler<FlexoConcept> {

	private static final Logger logger = Logger.getLogger(FlexoConceptPasteHandler.class.getPackage().getName());

	@Override
	public Class<FlexoConcept> getPastingPointHolderType() {
		return FlexoConcept.class;
	}

	@Override
	public PastingContext<FlexoConcept> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard) {

		if (focusedObject instanceof FlexoConceptObject) {
			// In this case, FlexoConcept will be contained in another FlexoConcept
			return new DefaultPastingContext<>(((FlexoConceptObject) focusedObject).getFlexoConcept());
		}

		return null;
	}

	/*@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<FlexoConcept> pastingContext) {
	
		Clipboard leaderClipboard = clipboard.getLeaderClipboard();
	
		// Translating names
		if (leaderClipboard.isSingleObject()) {
			if (leaderClipboard.getSingleContents() instanceof FlexoConceptObject) {
				translateName((FlexoConceptObject) leaderClipboard.getSingleContents());
			}
		}
		else {
			for (Object o : leaderClipboard.getMultipleContents()) {
				if (o instanceof FlexoConceptObject) {
					translateName((FlexoConceptObject) o);
				}
			}
		}
	}*/

	/*@Override
	public Object paste(FlexoClipboard clipboard, PastingContext<FlexoConcept> pastingContext) {
	
		if (pastingContext.getPastingPointHolder() instanceof VirtualModel) {
			// In this case, FlexoConcept will be pasted as a FlexoConcept in a VirtualModel
	
			try {
	
				ModelEntity<VirtualModel> vmEntity = clipboard.getLeaderClipboard().getModelFactory().getModelContext()
						.getModelEntity(VirtualModel.class);
				ModelProperty<? super VirtualModel> conceptProperty = vmEntity
						.getModelProperty(VirtualModel.FLEXO_CONCEPTS_KEY);
	
				System.out.println("OK, je copie le concept dans le VM " + pastingContext.getPastingPointHolder());
	
				System.out.println("vmEntity=" + vmEntity);
				System.out.println("conceptProperty=" + conceptProperty);
	
				System.out.println(((FlexoConcept) clipboard.getLeaderClipboard().getSingleContents()).getFMLRepresentation());
	
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
	
		return super.paste(clipboard, pastingContext);
	}*/

	@Override
	public Object paste(FlexoClipboard clipboard, PastingContext<FlexoConcept> pastingContext) {
		if (pastingContext.getPastingPointHolder() == null) {
			return null;
		}

		FlexoConcept container = pastingContext.getPastingPointHolder();

		if (clipboard.getLeaderClipboard().isSingleObject()) {

			Object copiedObject = clipboard.getLeaderClipboard().getSingleContents();

			if (copiedObject instanceof VirtualModel) {
				// cannot copy
				return null;
			}

			if (copiedObject instanceof FlexoConcept) {
				System.out.println("Pasting a FlexoConcept");

				if (container instanceof VirtualModel) {
					// We copy a FlexoConcept in a VirtualModel
					System.out.println("In a VirtualModel");
					try {
						ModelEntity<VirtualModel> vmEntity = clipboard.getLeaderClipboard().getModelFactory().getModelContext()
								.getModelEntity(VirtualModel.class);
						ModelProperty<? super VirtualModel> conceptProperty = vmEntity.getModelProperty(VirtualModel.FLEXO_CONCEPTS_KEY);
						translateNameWhenRequired((FlexoConcept) copiedObject, (VirtualModel) container);
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

				else {
					// We copy a FlexoConcept in a FlexoConcept
					System.out.println("In a FlexoConcept");
					try {
						ModelEntity<FlexoConcept> conceptEntity = clipboard.getLeaderClipboard().getModelFactory().getModelContext()
								.getModelEntity(FlexoConcept.class);
						ModelProperty<? super FlexoConcept> conceptProperty = conceptEntity
								.getModelProperty(FlexoConcept.EMBEDDED_FLEXO_CONCEPT_KEY);
						translateNameWhenRequired((FlexoConcept) copiedObject, container);
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

			}

			else if (copiedObject instanceof FlexoBehaviour) {
				System.out.println("Pasting a FlexoBehaviour");
				translateNameWhenRequired((FlexoBehaviour) copiedObject, container);
				return super.paste(clipboard, pastingContext);
			}

			else if (copiedObject instanceof FlexoProperty) {
				System.out.println("Pasting a FlexoProperty");
				translateNameWhenRequired((FlexoProperty) copiedObject, container);
				return super.paste(clipboard, pastingContext);
			}

			return super.paste(clipboard, pastingContext);
		}
		else { // Multiple objects in clipboard, not implemented yet

			/*System.out.println("MultipleContents= " + clipboard.getLeaderClipboard().getMultipleContents());
			
			for (int i = 0; i < clipboard.getLeaderClipboard().getMultipleContents().size(); i++) {
				Object originalContent = clipboard.getLeaderClipboard().getOriginalContents()[i];
				Object copiedContent = clipboard.getLeaderClipboard().getMultipleContents().get(i);
				System.out.println("**** originalContent=" + originalContent);
				System.out.println("     copiedContent=" + copiedContent);
			}*/

			System.out.println("Multiple objects in clipboard, not implemented yet");
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

	private static void translateNameWhenRequired(FlexoConcept copiedConcept, FlexoConcept container) {

		String baseName = copiedConcept.getName();

		if (container.getEmbeddedFlexoConcept(baseName) == null) {
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
		while (container.getEmbeddedFlexoConcept(testedName) != null && index < 1000) {
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

	private static void translateNameWhenRequired(FlexoBehaviour copiedBehaviour, FlexoConcept container) {

		String baseName = copiedBehaviour.getName();

		if (container.getFlexoBehaviour(baseName, copiedBehaviour.getParameterTypes()) == null) {
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
		while (container.getFlexoBehaviour(testedName, copiedBehaviour.getParameterTypes()) != null && index < 1000) {
			index++;
			testedName = baseName + index;
		}

		try {
			copiedBehaviour.setName(testedName);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void translateNameWhenRequired(FlexoProperty<?> copiedProperty, FlexoConcept container) {

		String baseName = copiedProperty.getName();

		if (container.getAccessibleProperty(baseName) == null) {
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
		while (container.getAccessibleProperty(testedName) != null && index < 1000) {
			index++;
			testedName = baseName + index;
		}

		try {
			copiedProperty.setName(testedName);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
