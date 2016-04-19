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
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.DeleteRepositoryFolder;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptConstraint;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AddParentFlexoConcept;
import org.openflexo.foundation.fml.action.CreateAbstractProperty;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateExpressionProperty;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreateGetSetProperty;
import org.openflexo.foundation.fml.action.CreateInspectorEntry;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.action.CreateTechnologyRole;
import org.openflexo.foundation.fml.action.CreateViewPoint;
import org.openflexo.foundation.fml.action.CreateVirtualModel;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.action.DeleteViewpoint;
import org.openflexo.foundation.fml.action.DeleteVirtualModel;
import org.openflexo.foundation.fml.action.DuplicateFlexoConcept;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.widget.FIBTextField;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
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

	public void deleteFolder(RepositoryFolder<?> folder) {
		DeleteRepositoryFolder deleteRepositoryFolder = DeleteRepositoryFolder.actionType.makeNewAction(folder, null, getEditor());
		deleteRepositoryFolder.doAction();
	}

	public ViewPoint createViewPoint(RepositoryFolder<ViewPointResource> folder) {
		CreateViewPoint createViewPoint = CreateViewPoint.actionType.makeNewAction(folder, null, getEditor());
		createViewPoint.doAction();
		return createViewPoint.getNewViewPoint();
	}

	public void deleteViewPoint(FlexoResource<ViewPoint> viewPointResource)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		DeleteViewpoint deleteViewPoint = DeleteViewpoint.actionType.makeNewAction(viewPointResource.getResourceData(null), null,
				getEditor());
		deleteViewPoint.doAction();
	}

	public VirtualModel createVirtualModel(FlexoResource<ViewPoint> viewPointResource)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		CreateVirtualModel createVirtualModel = CreateVirtualModel.actionType.makeNewAction(viewPointResource.getResourceData(null), null,
				getEditor());
		createVirtualModel.doAction();
		return createVirtualModel.getNewVirtualModel();
	}

	public void deleteVirtualModel(FlexoResource<VirtualModel> virtualModelResource)
			throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		DeleteVirtualModel deleteVirtualModel = DeleteVirtualModel.actionType.makeNewAction(virtualModelResource.getResourceData(null),
				null, getEditor());
		deleteVirtualModel.doAction();
	}

	public ModelSlot<?> createModelSlot(AbstractVirtualModel<?> virtualModel) {
		CreateModelSlot createModelSlot = CreateModelSlot.actionType.makeNewAction(virtualModel, null, getEditor());
		createModelSlot.doAction();
		return createModelSlot.getNewModelSlot();
	}

	public void deleteModelSlot(VirtualModel virtualModel, ModelSlot<?> modelSlot) {
		virtualModel.removeFromModelSlots(modelSlot);
		modelSlot.delete();
	}

	/**
	 * Duplicates supplied FlexoConcept, given a new name<br>
	 * Newly created FlexoConcept is added to ViewPoint
	 * 
	 * @param newName
	 * @return the duplicated FlexoConcept
	 */
	public FlexoConcept duplicateFlexoConcept(FlexoConcept flexoConcept, String newName) {
		DuplicateFlexoConcept duplicateAction = DuplicateFlexoConcept.actionType.makeNewAction(flexoConcept, null, getEditor());
		duplicateAction.doAction();
		return duplicateAction.getNewFlexoConcept();
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

	public void createConstraint(FlexoConcept flexoConcept) {
		FlexoConceptConstraint constraint = flexoConcept.getFMLModelFactory().newFlexoConceptConstraint();
		flexoConcept.addToFlexoConceptConstraints(constraint);
	}

	public FlexoConceptConstraint deleteConstraint(FlexoConcept flexoConcept, FlexoConceptConstraint constraint) {
		flexoConcept.removeFromFlexoConceptConstraints(constraint);
		constraint.delete();
		return constraint;
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
		newFlexoBehaviour.setName(newName);
		flexoBehaviour.getFlexoConcept().addToFlexoBehaviours(newFlexoBehaviour);
		return newFlexoBehaviour;
	}

	public SynchronizationScheme createSynchronizationScheme(VirtualModel virtualModel) {
		SynchronizationScheme newEditionScheme = virtualModel.getFMLModelFactory().newSynchronizationScheme();
		newEditionScheme.setName("synchronization");
		newEditionScheme.setControlGraph(virtualModel.getFMLModelFactory().newEmptyControlGraph());
		virtualModel.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public CreationScheme createCreationScheme(FlexoConcept flexoConcept) {
		CreationScheme newEditionScheme = flexoConcept.getFMLModelFactory().newCreationScheme();
		newEditionScheme.setName("creation");
		newEditionScheme.setControlGraph(flexoConcept.getFMLModelFactory().newEmptyControlGraph());
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public DeletionScheme createDeletionScheme(FlexoConcept flexoConcept) {
		DeletionScheme newEditionScheme = flexoConcept.getFMLModelFactory().newDeletionScheme();
		newEditionScheme.setName("deletion");
		newEditionScheme.setControlGraph(flexoConcept.getFMLModelFactory().newEmptyControlGraph());
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public ActionScheme createActionScheme(FlexoConcept flexoConcept) {
		ActionScheme newEditionScheme = flexoConcept.getFMLModelFactory().newActionScheme();
		newEditionScheme.setName("action");
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
		newEditionScheme.setName("clone");
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
		CreateFlexoBehaviourParameter createFlexoBehaviourParameter = CreateFlexoBehaviourParameter.actionType.makeNewAction(flexoBehaviour,
				null, getEditor());
		createFlexoBehaviourParameter.doAction();
		return createFlexoBehaviourParameter.getNewParameter();
	}

	public EditionAction createEditionAction(FMLControlGraph object) {
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(object, null, getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public EditionAction createEditionActionInThenControlGraph(ConditionalAction conditional) {
		if (conditional.getThenControlGraph() == null) {
			EmptyControlGraph cg = conditional.getFMLModelFactory().newEmptyControlGraph();
			conditional.setThenControlGraph(cg);
		}
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(conditional.getThenControlGraph(), null,
				getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public EditionAction createEditionActionInElseControlGraph(ConditionalAction conditional) {
		if (conditional.getElseControlGraph() == null) {
			EmptyControlGraph cg = conditional.getFMLModelFactory().newEmptyControlGraph();
			conditional.setElseControlGraph(cg);
		}
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(conditional.getElseControlGraph(), null,
				getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public EditionAction createEditionActionInIteration(IterationAction iteration) {
		if (iteration.getControlGraph() == null) {
			EmptyControlGraph cg = iteration.getFMLModelFactory().newEmptyControlGraph();
			iteration.setControlGraph(cg);
		}
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(iteration.getControlGraph(), null,
				getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public EditionAction createEditionActionInGetControlGraph(GetSetProperty<?> property) {
		if (property.getGetControlGraph() == null) {
			EmptyControlGraph cg = property.getFMLModelFactory().newEmptyControlGraph();
			property.setGetControlGraph(cg);
		}
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(property.getGetControlGraph(), null,
				getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public EditionAction createEditionActionInSetControlGraph(GetSetProperty<?> property) {
		if (property.getSetControlGraph() == null) {
			EmptyControlGraph cg = property.getFMLModelFactory().newEmptyControlGraph();
			property.setSetControlGraph(cg);
		}
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(property.getSetControlGraph(), null,
				getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public FlexoConcept createFlexoConcept(FlexoConcept flexoConcept) {
		if (flexoConcept instanceof VirtualModel) {
			CreateFlexoConcept createFlexoConcept = CreateFlexoConcept.actionType.makeNewAction((VirtualModel) flexoConcept, null,
					getEditor());
			createFlexoConcept.switchNewlyCreatedFlexoConcept = false;
			createFlexoConcept.doAction();
			return createFlexoConcept.getNewFlexoConcept();
		}
		else if (flexoConcept != null) {
			CreateFlexoConcept createFlexoConcept = CreateFlexoConcept.actionType.makeNewAction(flexoConcept.getOwningVirtualModel(), null,
					getEditor());
			createFlexoConcept.addToParentConcepts(flexoConcept);
			createFlexoConcept.switchNewlyCreatedFlexoConcept = false;
			createFlexoConcept.doAction();
			/*if (addFlexoConcept.getNewFlexoConcept() != null) {
				addFlexoConcept.getNewFlexoConcept().addToParentFlexoConcepts(flexoConcept);
			}*/
			return createFlexoConcept.getNewFlexoConcept();
		}
		logger.warning("Unexpected null flexo concept");
		return null;
	}

	public FlexoConcept deleteFlexoConcept(FlexoConcept flexoConcept) {
		if (flexoConcept instanceof VirtualModel) {
			DeleteVirtualModel deleteVirtualModel = DeleteVirtualModel.actionType.makeNewAction((VirtualModel) flexoConcept, null,
					getEditor());
			deleteVirtualModel.doAction();
		}
		else if (flexoConcept != null) {
			DeleteFlexoConceptObjects deleteFlexoConcept = DeleteFlexoConceptObjects.actionType.makeNewAction(flexoConcept, null,
					getEditor());
			deleteFlexoConcept.doAction();
		}
		return flexoConcept;
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

	public void actionFirst(EditionAction action) {
		if (action.getActionContainer() != null) {
			action.getActionContainer().actionFirst(action);
		}
	}

	public void actionUp(EditionAction action) {
		if (action != null && action.getActionContainer() != null) {
			action.getActionContainer().actionUp(action);
		}
		if (action == null) {
			logger.warning("actionUp was called with null parameter");
		}
	}

	public void actionDown(EditionAction action) {
		if (action.getActionContainer() != null) {
			action.getActionContainer().actionDown(action);
		}
	}

	public void actionLast(EditionAction action) {
		if (action.getActionContainer() != null) {
			action.getActionContainer().actionLast(action);
		}
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
		if (action instanceof TechnologySpecificAction && ((TechnologySpecificAction<?, ?>) action).getModelSlot() != null) {
			TechnologyAdapter technologyAdapter = ((TechnologySpecificAction<?, ?>) action).getModelSlot().getModelSlotTechnologyAdapter();
			if (technologyAdapter != null) {
				TechnologyAdapterController<?> taController = getFlexoController().getTechnologyAdapterController(technologyAdapter);
				return taController.getFIBPanelForObject(action);
			}
			else
				// No specific TechnologyAdapter, lookup in generic libraries
				return getFIBPanelForObject(action);
		}
		else {
			// No specific TechnologyAdapter, lookup in generic libraries
			return getFIBPanelForObject(action);
		}

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

			FIBInspector returned = defaultInspectorGroup.inspectorForClass(object.getClass());

			System.out.println("################ Pour " + object + " l'inspecteur c'est " + returned);

			// System.out.println(returned.getFIBLibrary().getFIBModelFactory().stringRepresentation(returned));

			FIBTab advancedTab = (FIBTab) returned.getTabPanel().getSubComponentNamed("AdvancedTab");

			FIBTextField tf = (FIBTextField) advancedTab.getSubComponentNamed("FlexoConceptTextField");

			System.out.println("J'ai mon TF=" + tf);

			DataBinding<?> dataBinding = tf.getData();
			System.out.println("data=" + dataBinding);

			BindingValue bv = (BindingValue) dataBinding.getExpression();

			System.out.println("bv=" + bv.getBindingVariable() + " of " + bv.getBindingVariable().getClass());
			System.out.println("path[0]=" + bv.getBindingPath().get(0));

			System.out.println("BM=" + tf.getData().getOwner().getBindingModel());

			if (tf.getData().getOwner() instanceof FIBComponent) {
				FIBComponent fib = (FIBComponent) tf.getData().getOwner();
				System.out.println(fib.getFIBLibrary().stringRepresentation(fib));
				System.out.println("Root component:");
				System.out.println(fib.getFIBLibrary().stringRepresentation(fib.getRootComponent()));
			}

			return returned;
		}
		return null;
	}

	public FIBTab basicInspectorTabForObject(FMLObject object) {
		// return inspectorForObject(object);

		FIBInspector inspector = inspectorForObject(object);
		if (inspector != null && inspector.getTabPanel() != null) {
			FIBTab returned = (FIBTab) inspector.getTabPanel().getSubComponentNamed("BasicTab");
			if (returned != null) {
				returned.setControllerClass(FMLFIBInspectorController.class);
			}
			return returned;
		}
		return null;
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

}
