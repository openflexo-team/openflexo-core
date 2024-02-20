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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.components.validation.RevalidationTask;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.fml.controller.validation.FixIssueDialog;
import org.openflexo.fml.controller.validation.IssueFixing;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.DeleteRepositoryFolder;
import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AddParentFlexoConcept;
import org.openflexo.foundation.fml.action.AddUseDeclaration;
import org.openflexo.foundation.fml.action.CreateAbstractProperty;
import org.openflexo.foundation.fml.action.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateExpressionProperty;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreateFlexoEnumValue;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateGetSetProperty;
import org.openflexo.foundation.fml.action.CreateInspectorEntry;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.action.CreateTechnologyRole;
import org.openflexo.foundation.fml.action.CreateTopLevelVirtualModel;
import org.openflexo.foundation.fml.action.DeleteCompilationUnit;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.action.RenameCompilationUnit;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.ExpressionIterationAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IncrementalIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.validation.ProblemIssue;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.rm.Resource;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Represents a controller with basic FML edition facilities<br>
 * Extends FlexoFIBController by supporting features relative to FML edition
 * 
 * @author sylvain
 */
public class FMLFIBController extends FlexoFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(FMLFIBController.class.getPackage().getName());

	public FMLFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
	}

	public FMLFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory, controller);
	}

	public void changeURI(FMLCompilationUnit compilationUnit) {
		RenameCompilationUnit renameAction = RenameCompilationUnit.actionType.makeNewAction(compilationUnit, null, getEditor());
		renameAction.doAction();
	}

	public void deleteFolder(RepositoryFolder<?, ?> folder) {
		DeleteRepositoryFolder deleteRepositoryFolder = DeleteRepositoryFolder.actionType.makeNewAction(folder, null, getEditor());
		deleteRepositoryFolder.doAction();
	}

	public ModelSlot<?> createModelSlot(FlexoConcept concept) {
		CreateModelSlot createModelSlot = CreateModelSlot.actionType.makeNewAction(concept, null, getEditor());
		createModelSlot.doAction();
		return createModelSlot.getNewModelSlot();
	}

	public void deleteModelSlot(FlexoConcept concept, ModelSlot<?> modelSlot) {
		concept.removeFromModelSlots(modelSlot);
		modelSlot.delete();
	}

	public UseModelSlotDeclaration addToUseDeclarations(VirtualModel virtualModel) {
		AddUseDeclaration addToUseDeclarations = AddUseDeclaration.actionType.makeNewAction(virtualModel, null, getEditor());
		addToUseDeclarations.doAction();
		return addToUseDeclarations.getNewUseDeclaration();
	}

	public FlexoConcept addParentFlexoConcept(FlexoConcept flexoConcept) {
		AddParentFlexoConcept addParentFlexoConcept = AddParentFlexoConcept.actionType.makeNewAction(flexoConcept, null, getEditor());
		addParentFlexoConcept.doAction();
		return flexoConcept;
	}

	public AbstractProperty<?> createAbstractProperty(FlexoConcept flexoConcept) {
		CreateAbstractProperty createAbstractProperty = CreateAbstractProperty.actionType.makeNewAction(flexoConcept, null, getEditor());
		createAbstractProperty.doAction();
		return createAbstractProperty.getNewFlexoProperty();
	}

	public FlexoRole<?> createTechnologyRole(FlexoConcept flexoConcept) {
		CreateTechnologyRole createFlexoRole = CreateTechnologyRole.actionType.makeNewAction(flexoConcept, null, getEditor());
		createFlexoRole.doAction();
		return createFlexoRole.getNewFlexoRole();
	}

	public FlexoConceptInstanceRole createFlexoConceptInstanceRole(FlexoConcept flexoConcept) {
		CreateFlexoConceptInstanceRole createFlexoRole = CreateFlexoConceptInstanceRole.actionType.makeNewAction(flexoConcept, null,
				getEditor());
		createFlexoRole.doAction();
		return createFlexoRole.getNewFlexoRole();
	}

	public PrimitiveRole<?> createPrimitiveRole(FlexoConcept flexoConcept) {
		CreatePrimitiveRole createFlexoRole = CreatePrimitiveRole.actionType.makeNewAction(flexoConcept, null, getEditor());
		createFlexoRole.doAction();
		return createFlexoRole.getNewFlexoRole();
	}

	public ExpressionProperty<?> createExpressionProperty(FlexoConcept flexoConcept) {
		CreateExpressionProperty createExpressionProperty = CreateExpressionProperty.actionType.makeNewAction(flexoConcept, null,
				getEditor());
		createExpressionProperty.doAction();
		return createExpressionProperty.getNewFlexoProperty();
	}

	public GetSetProperty<?> createGetSetProperty(FlexoConcept flexoConcept) {
		CreateGetSetProperty createGetSetProperty = CreateGetSetProperty.actionType.makeNewAction(flexoConcept, null, getEditor());
		createGetSetProperty.doAction();
		return createGetSetProperty.getNewFlexoProperty();
	}

	public FlexoProperty<?> deleteFlexoProperty(FlexoConcept flexoConcept, FlexoProperty<?> aProperty) {
		flexoConcept.removeFromFlexoProperties(aProperty);
		aProperty.delete();
		return aProperty;
	}

	public void createSimpleInvariant(FlexoConcept flexoConcept) throws InvalidNameException {
		AbstractInvariant invariant = flexoConcept.getFMLModelFactory().newSimpleInvariant();
		invariant.setName("New invariant");
		flexoConcept.addToInvariants(invariant);
	}

	public AbstractInvariant deleteInvariant(FlexoConcept flexoConcept, AbstractInvariant invariant) {
		flexoConcept.removeFromInvariants(invariant);
		invariant.delete();
		return invariant;
	}

	public FlexoEnumValue createFlexoEnumValue(FlexoEnum flexoEnum) {
		CreateFlexoEnumValue createFlexoBehaviourParameter = CreateFlexoEnumValue.actionType.makeNewAction(flexoEnum, null, getEditor());
		createFlexoBehaviourParameter.doAction();
		return createFlexoBehaviourParameter.getNewValue();
	}

	public FlexoEnumValue deleteFlexoEnumValue(FlexoEnumValue enumValue) {
		if (enumValue.getFlexoEnum() != null) {
			enumValue.getFlexoEnum().removeFromValues(enumValue);
		}
		enumValue.delete();
		return enumValue;
	}

	public FlexoEnumValue deleteFlexoEnumValue(FlexoEnum flexoEnum, FlexoEnumValue enumValue) {
		flexoEnum.removeFromValues(enumValue);
		enumValue.delete();
		return enumValue;
	}

	/**
	 * Duplicates this FlexoBehaviour, given a new name<br>
	 * Newly created FlexoBehaviour is added to parent FlexoConcept
	 * 
	 * @param newName
	 * @return the duplicated Flexo Behaviour
	 */
	public FlexoBehaviour duplicateFlexoBehaviour(FlexoBehaviour flexoBehaviour, String newName) {
		FlexoBehaviour newFlexoBehaviour = (FlexoBehaviour) flexoBehaviour.cloneObject();
		try {
			newFlexoBehaviour.setName(newName);
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flexoBehaviour.getFlexoConcept().addToFlexoBehaviours(newFlexoBehaviour);
		return newFlexoBehaviour;
	}

	public SynchronizationScheme createSynchronizationScheme(VirtualModel virtualModel) {
		SynchronizationScheme newEditionScheme = virtualModel.getFMLModelFactory().newSynchronizationScheme();
		try {
			newEditionScheme.setName("synchronization");
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newEditionScheme.setControlGraph(virtualModel.getFMLModelFactory().newEmptyControlGraph());
		virtualModel.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public CreationScheme createCreationScheme(FlexoConcept flexoConcept) {
		CreationScheme newEditionScheme = flexoConcept.getFMLModelFactory().newCreationScheme();
		try {
			newEditionScheme.setName("creation");
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newEditionScheme.setControlGraph(flexoConcept.getFMLModelFactory().newEmptyControlGraph());
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public DeletionScheme createDeletionScheme(FlexoConcept flexoConcept) {
		DeletionScheme newEditionScheme = flexoConcept.getFMLModelFactory().newDeletionScheme();
		try {
			newEditionScheme.setName("deletion");
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newEditionScheme.setControlGraph(flexoConcept.getFMLModelFactory().newEmptyControlGraph());
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public ActionScheme createActionScheme(FlexoConcept flexoConcept) {
		ActionScheme newEditionScheme = flexoConcept.getFMLModelFactory().newActionScheme();
		try {
			newEditionScheme.setName("newAction");
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newEditionScheme.setControlGraph(flexoConcept.getFMLModelFactory().newEmptyControlGraph());
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public FlexoBehaviour createAdvancedScheme(FlexoConcept flexoConcept) {
		CreateFlexoBehaviour createFlexoBehaviour = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null, getEditor());
		createFlexoBehaviour.doAction();
		return createFlexoBehaviour.getNewFlexoBehaviour();
	}

	public CloningScheme createCloningScheme(FlexoConcept flexoConcept) {
		CloningScheme newEditionScheme = flexoConcept.getFMLModelFactory().newCloningScheme();
		try {
			newEditionScheme.setName("newClone");
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newEditionScheme.setControlGraph(flexoConcept.getFMLModelFactory().newEmptyControlGraph());
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public FlexoBehaviour deleteFlexoBehaviour(FlexoConcept flexoConcept, FlexoBehaviour flexoBehaviour) {
		flexoConcept.removeFromFlexoBehaviours(flexoBehaviour);
		flexoBehaviour.delete();
		return flexoBehaviour;
	}

	public FlexoBehaviourParameter createFlexoBehaviourParameter(FlexoBehaviour flexoBehaviour) {
		CreateGenericBehaviourParameter createFlexoBehaviourParameter = CreateGenericBehaviourParameter.actionType
				.makeNewAction(flexoBehaviour, null, getEditor());
		createFlexoBehaviourParameter.doAction();
		return createFlexoBehaviourParameter.getNewParameter();
	}

	public EditionAction createEditionAction(FMLControlGraph object) {
		if (object != null) {
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(object, null, getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInThenControlGraph(ConditionalAction conditional) {
		if (conditional != null) {
			if (conditional.getThenControlGraph() == null) {
				EmptyControlGraph cg = conditional.getFMLModelFactory().newEmptyControlGraph();
				conditional.setThenControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(conditional.getThenControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInElseControlGraph(ConditionalAction conditional) {
		if (conditional != null) {
			if (conditional.getElseControlGraph() == null) {
				EmptyControlGraph cg = conditional.getFMLModelFactory().newEmptyControlGraph();
				conditional.setElseControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(conditional.getElseControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInIteration(IterationAction iteration) {
		if (iteration != null) {
			if (iteration.getControlGraph() == null) {
				EmptyControlGraph cg = iteration.getFMLModelFactory().newEmptyControlGraph();
				iteration.setControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInWhileAction(WhileAction iteration) {
		if (iteration != null) {
			if (iteration.getControlGraph() == null) {
				EmptyControlGraph cg = iteration.getFMLModelFactory().newEmptyControlGraph();
				iteration.setControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInIncrementalIterationAction(IncrementalIterationAction iteration) {
		if (iteration != null) {
			if (iteration.getControlGraph() == null) {
				EmptyControlGraph cg = iteration.getFMLModelFactory().newEmptyControlGraph();
				iteration.setControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInExpressionIterationAction(ExpressionIterationAction iteration) {
		if (iteration != null) {
			if (iteration.getControlGraph() == null) {
				EmptyControlGraph cg = iteration.getFMLModelFactory().newEmptyControlGraph();
				iteration.setControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInGetControlGraph(GetProperty<?> property) {
		if (property != null) {
			if (property.getGetControlGraph() == null) {
				EmptyControlGraph cg = property.getFMLModelFactory().newEmptyControlGraph();
				property.setGetControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(property.getGetControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public EditionAction createEditionActionInSetControlGraph(GetSetProperty<?> property) {
		if (property != null) {
			if (property.getSetControlGraph() == null) {
				EmptyControlGraph cg = property.getFMLModelFactory().newEmptyControlGraph();
				property.setSetControlGraph(cg);
			}
			CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(property.getSetControlGraph(), null,
					getEditor());
			createEditionAction.doAction();
			return createEditionAction.getNewEditionAction();
		}
		return null;
	}

	public FlexoConceptObject deleteFlexoConceptObject(FlexoConceptObject flexoConceptObject) {
		if (flexoConceptObject != null) {
			DeleteFlexoConceptObjects deleteFlexoConcept = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConceptObject, null,
					getEditor());
			deleteFlexoConcept.doAction();
		}
		return flexoConceptObject;
	}

	/*public FlexoBehaviourParameter createURIParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newURIParameter();
		newParameter.setName("uri");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("uri");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createTextFieldParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newTextFieldParameter();
		newParameter.setName("textField");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createTextAreaParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newTextAreaParameter();
		newParameter.setName("textArea");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createIntegerParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newIntegerParameter();
		newParameter.setName("integer");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createCheckBoxParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newCheckboxParameter();
		newParameter.setName("checkbox");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createDropDownParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newDropDownParameter();
		newParameter.setName("dropdown");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createIndividualParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newInstance(IndividualParameter.class);
		newParameter.setName("individual");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createClassParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newInstance(ClassParameter.class);
		newParameter.setName("class");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newInstance(PropertyParameter.class);
		newParameter.setName("property");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createObjectPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newInstance(ObjectPropertyParameter.class);
		newParameter.setName("property");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createDataPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newInstance(DataPropertyParameter.class);
		newParameter.setName("property");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}*/

	/*public EditionSchemeImplParameter createFlexoObjectParameter() {
		FlexoBehaviourParameter newParameter = new FlexoObjectParameter(null);
		newParameter.setName("flexoObject");
		// newParameter.setLabel("label");
		addToParameters(newParameter);
		return newParameter;
	}*/

	/*public FlexoBehaviourParameter createTechnologyObjectParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newTechnologyObjectParameter();
		newParameter.setName("technologyObject");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createListParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newListParameter();
		newParameter.setName("list");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}
	
	public FlexoBehaviourParameter createFlexoConceptInstanceParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getFMLModelFactory().newFlexoConceptInstanceParameter();
		newParameter.setName("flexoConceptInstance");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}*/

	public FlexoBehaviourParameter deleteParameter(FlexoBehaviour flexoBehaviour, FlexoBehaviourParameter parameterToDelete) {
		flexoBehaviour.removeFromParameters(parameterToDelete);
		parameterToDelete.delete();
		return parameterToDelete;
	}

	public InspectorEntry createInspectorEntry(FlexoConceptInspector inspector) {
		CreateInspectorEntry createInspectorEntry = CreateInspectorEntry.actionType.makeNewAction(inspector, null, getEditor());
		createInspectorEntry.doAction();
		return createInspectorEntry.getNewEntry();
	}

	public boolean isFlexoBehaviour(Object selectedObject, FlexoBehaviour context) {
		return selectedObject == null || selectedObject == context;
	}

	public boolean isEditionAction(Object selectedObject) {
		return selectedObject instanceof EditionAction;
	}

	public Resource fibForEditionAction(EditionAction action) {
		if (action == null) {
			return null;
		}
		if (action instanceof TechnologySpecificAction) {
			TechnologyAdapter technologyAdapter = ((TechnologySpecificAction<?, ?>) action).getModelSlotTechnologyAdapter();
			if (technologyAdapter != null) {
				TechnologyAdapterController<?> taController = FlexoController.getTechnologyAdapterController(technologyAdapter);
				return taController.getFIBPanelForObject(action);
			}
			// No specific TechnologyAdapter, lookup in generic libraries
			return getFIBPanelForObject(action);
		}
		// No specific TechnologyAdapter, lookup in generic libraries
		return getFIBPanelForObject(action);
	}

	public Resource fibForFlexoBehaviour(FlexoBehaviour flexoBehaviour) {
		if (flexoBehaviour == null) {
			return null;
		}
		// No specific TechnologyAdapter, lookup in generic libraries
		return getFIBPanelForObject(flexoBehaviour);

	}

	public FIBInspector inspectorForObject(FMLObject object) {
		if (object == null) {
			return null;
		}
		if (getFlexoController() != null) {
			return getFlexoController().getModuleInspectorController().inspectorForObject(object);
		}
		if (defaultInspectorGroup != null) {
			return defaultInspectorGroup.inspectorForClass(object.getClass());
		}
		return null;
	}

	// We store here a map of cloned basic tabs associated to their original FIBInspector
	// for performances reasons: we don't want to clone multiple times
	private Map<FIBInspector, FIBTab> basicInspectorTabs = new HashMap<>();

	public FIBTab basicInspectorTabForObject(FMLObject object) {
		// return inspectorForObject(object);

		FIBInspector inspector = inspectorForObject(object);
		if (inspector != null && inspector.getTabPanel() != null) {
			FIBTab returned = basicInspectorTabs.get(inspector);
			if (returned == null) {
				FIBTab originalBasicTab = (FIBTab) inspector.getTabPanel().getSubComponentNamed("BasicTab");
				if (originalBasicTab != null) {
					// We have here to clone the component, because original component refers to a root container
					// that we don't want to be displayed. So we clone the component, and define a clean API on it using setDataClass()
					returned = (FIBTab) originalBasicTab.cloneObject();
					returned.setControllerClass(getInspectorControllerClass());
					returned.setDataType(originalBasicTab.getRootComponent().getVariable(FIBComponent.DEFAULT_DATA_VARIABLE).getType());
					basicInspectorTabs.put(inspector, returned);
				}
			}
			return returned;
		}
		return null;
	}

	public FIBComponent inspectorForFlexoConceptInstance(FlexoConceptInstance fci) {
		if (getFlexoController() != null && getFlexoController().getModuleInspectorController() != null && fci != null) {
			return getFlexoController().getModuleInspectorController().getFIBInspectorPanel(fci.getFlexoConcept(),
					getInspectorControllerClass());
		}
		return null;
	}

	public Class<? extends FlexoFIBController> getInspectorControllerClass() {
		return FMLFIBInspectorController.class;
	}

	// Debug
	private InspectorGroup defaultInspectorGroup;

	// Debug
	public InspectorGroup getDefaultInspectorGroup() {
		return defaultInspectorGroup;
	}

	// Debug
	public void setDefaultInspectorGroup(InspectorGroup defaultInspectorGroup) {
		this.defaultInspectorGroup = defaultInspectorGroup;
	}

	public void moveControlGraph(FMLControlGraph controlGraph, FMLControlGraph receiver) {

		if (controlGraph == receiver) {
			return;
		}

		/*System.out.println("On veut bouger le graphe de controle");
		System.out.println(controlGraph.getFMLRepresentation());
		System.out.println("Juste apres:");
		System.out.println(receiver.getFMLRepresentation());*/

		controlGraph.moveWhileSequentiallyAppendingTo(receiver);
	}

	public boolean canMoveControlGraph(FMLControlGraph controlGraph, FMLControlGraph receiver) {
		return controlGraph != null && controlGraph.getOwner() != receiver;
	}

	private boolean showErrorsWarnings = true;

	public boolean showErrorsWarnings() {
		return showErrorsWarnings;
	}

	public void setShowErrorsWarnings(boolean showErrorsWarnings) {
		// System.out.println("setShowErrorsWarnings with " + showErrorsWarnings);
		if (this.showErrorsWarnings != showErrorsWarnings) {
			this.showErrorsWarnings = showErrorsWarnings;
			getPropertyChangeSupport().firePropertyChange("showErrorsWarnings", !showErrorsWarnings, showErrorsWarnings);
		}
	}

	public void showIssue(ValidationIssue<?, ?> issue) {
		if (issue != null) {
			Validable objectToSelect = issue.getValidable();
			if (getFlexoController() != null) {
				System.out.println("Select and focus object " + objectToSelect);
				getFlexoController().selectAndFocusObject((FlexoObject) objectToSelect);
			}
		}
	}

	public void fixIssue(ValidationIssue<?, ?> issue) {
		if (issue instanceof ProblemIssue) {
			FMLCompilationUnit compilationUnitToRevalidate = null;
			if (issue.getValidationReport().getRootObject() instanceof FMLCompilationUnit) {
				compilationUnitToRevalidate = (FMLCompilationUnit) issue.getValidationReport().getRootObject();
			}
			IssueFixing<?, ?> fixing = new IssueFixing<>((ProblemIssue<?, ?>) issue, getFlexoController());
			FixIssueDialog dialog = new FixIssueDialog(fixing, getFlexoController());
			dialog.showDialog();
			if (dialog.getStatus() == Status.VALIDATED) {
				fixing.fix();
				if (compilationUnitToRevalidate != null) {
					revalidate(compilationUnitToRevalidate);
				}
			}
			else if (dialog.getStatus() == Status.NO) {
				fixing.ignore();
			}
		}
	}

	public void revalidate(FMLCompilationUnit compilationUnit) {
		if (getServiceManager() != null) {
			FMLTechnologyAdapterController tac = getServiceManager().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(FMLTechnologyAdapterController.class);
			FMLValidationReport virtualModelReport = (FMLValidationReport) tac.getValidationReport(compilationUnit);
			RevalidationTask validationTask = new RevalidationTask(virtualModelReport);
			getServiceManager().getTaskManager().scheduleExecution(validationTask);
		}
	}

	public FlexoConcept createFlexoConcept(FlexoConcept flexoConcept) {
		if (flexoConcept instanceof VirtualModel) {
			return createFlexoConceptInVirtualModel((VirtualModel) flexoConcept);
		}
		else if (flexoConcept != null) {
			return createFlexoConceptInContainer(flexoConcept);
		}
		logger.warning("Unexpected null flexo concept");
		return null;
	}

	public FlexoConcept createFlexoConceptInVirtualModel(VirtualModel virtualModel) {
		CreateFlexoConcept createFlexoConcept = CreateFlexoConcept.actionType.makeNewAction(virtualModel, null, getEditor());
		createFlexoConcept.switchNewlyCreatedFlexoConcept = false;
		createFlexoConcept.doAction();
		return createFlexoConcept.getNewFlexoConcept();
	}

	public FlexoConcept createFlexoConceptInContainer(FlexoConcept containerConcept) {
		CreateFlexoConcept createFlexoConcept = CreateFlexoConcept.actionType.makeNewAction(containerConcept.getOwningVirtualModel(), null,
				getEditor());
		createFlexoConcept.setContainerFlexoConcept(containerConcept);
		createFlexoConcept.switchNewlyCreatedFlexoConcept = false;
		createFlexoConcept.doAction();
		return createFlexoConcept.getNewFlexoConcept();
	}

	public FlexoConcept createFlexoConceptChildOf(FlexoConcept parentConcept) {
		CreateFlexoConcept createFlexoConcept = CreateFlexoConcept.actionType.makeNewAction(parentConcept.getOwningVirtualModel(), null,
				getEditor());
		createFlexoConcept.addToParentConcepts(parentConcept);
		createFlexoConcept.switchNewlyCreatedFlexoConcept = false;
		createFlexoConcept.doAction();
		return createFlexoConcept.getNewFlexoConcept();
	}

	public FlexoConcept deleteFlexoConcept(FlexoConcept flexoConcept) {
		if (flexoConcept instanceof VirtualModel) {
			DeleteCompilationUnit deleteVirtualModel = DeleteCompilationUnit.actionType
					.makeNewAction(((VirtualModel) flexoConcept).getDeclaringCompilationUnit(), null, getEditor());
			deleteVirtualModel.doAction();
		}
		else if (flexoConcept != null) {
			DeleteFlexoConceptObjects deleteFlexoConcept = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConcept, null,
					getEditor());
			deleteFlexoConcept.doAction();
		}
		return flexoConcept;
	}

	// Should be above to be available from everywhere
	// TODO: refactor this (generic perspective should be adapted to FML in OpenflexoModeller)
	public VirtualModel createTopLevelVirtualModel(RepositoryFolder<CompilationUnitResource, ?> folder) {
		CreateTopLevelVirtualModel createTopLevelVirtualModel = CreateTopLevelVirtualModel.actionType.makeNewAction(folder, null,
				getEditor());
		createTopLevelVirtualModel.doAction();
		return createTopLevelVirtualModel.getNewVirtualModel();
	}

	// Should be above to be available from everywhere
	// TODO: refactor this (generic perspective should be adapted to FML in OpenflexoModeller)
	public VirtualModel createContainedVirtualModel(FlexoResource<FMLCompilationUnit> containerVirtualModelResource)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		CreateContainedVirtualModel createContainedVirtualModel = CreateContainedVirtualModel.actionType
				.makeNewAction(containerVirtualModelResource.getResourceData(), null, getEditor());
		createContainedVirtualModel.doAction();
		return createContainedVirtualModel.getNewVirtualModel();
	}

	// Should be above to be available from everywhere
	// TODO: refactor this (generic perspective should be adapted to FML in OpenflexoModeller)
	public void deleteCompilationUnit(FlexoResource<FMLCompilationUnit> compilationUnitResource)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		DeleteCompilationUnit deleteCompilationUnit = DeleteCompilationUnit.actionType
				.makeNewAction(compilationUnitResource.getResourceData(), null, getEditor());
		deleteCompilationUnit.doAction();
	}

	@NotificationUnsafe
	public void moveFlexoConcept(FlexoConcept concept, FlexoConcept container) {
		logger.info("Moving concept " + concept + " into " + container);
		// Disconnect from former container concept (if any)
		if (concept.getApplicableContainerFlexoConcept() != null) {
			concept.getApplicableContainerFlexoConcept().removeFromEmbeddedFlexoConcepts(concept);
		}
		// Disconnect from owner VirtualModel
		if (concept.getOwner() != null) {
			concept.getOwner().removeFromFlexoConcepts(concept);
		}
		if (container instanceof VirtualModel) {
			// Normal case
			((VirtualModel) container).addToFlexoConcepts(concept);
		}
		else {
			// Move in a container FlexoConcept
			container.getOwner().addToFlexoConcepts(concept);
			container.addToEmbeddedFlexoConcepts(concept);
		}
		revalidate(concept.getDeclaringCompilationUnit());
	}

	@NotificationUnsafe
	public boolean canMoveFlexoConcept(FlexoConcept concept, FlexoConcept container) {
		// System.out.println("on peut bouger " + concept + " dans " + container + " ?");
		// System.out.println("alors: " + (concept != null && concept != container && concept.getContainerFlexoConcept() != container
		// && concept.getDeclaringVirtualModel() != container));
		return concept != null && concept != container && concept.getApplicableContainerFlexoConcept() != container
				&& concept.getDeclaringCompilationUnit() != container;
	}

	@Override
	public void moveVirtualModelInFolder(CompilationUnitResource vmResource, RepositoryFolder receiver) {
		super.moveVirtualModelInFolder(vmResource, receiver);
		revalidate(vmResource.getCompilationUnit());
	}

	@Override
	public void moveVirtualModelInVirtualModel(CompilationUnitResource vmResource, CompilationUnitResource container) {
		super.moveVirtualModelInVirtualModel(vmResource, container);
		revalidate(vmResource.getCompilationUnit());
	}

}
