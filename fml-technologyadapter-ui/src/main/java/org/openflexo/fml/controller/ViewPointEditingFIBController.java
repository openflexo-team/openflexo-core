package org.openflexo.fml.controller;

import java.util.logging.Logger;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptConstraint;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoRole;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.DeleteFlexoConcept;
import org.openflexo.foundation.fml.action.DeleteVirtualModel;
import org.openflexo.foundation.fml.action.DuplicateFlexoConcept;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Represents a controller with basic ViewPoint edition facilities<br>
 * Extends FlexoFIBController by supporting features relative to ViewPoint edition
 * 
 * @author sylvain
 */
public class ViewPointEditingFIBController extends FlexoFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(ViewPointEditingFIBController.class.getPackage().getName());

	public ViewPointEditingFIBController(FIBComponent component) {
		super(component);
	}

	public ViewPointEditingFIBController(FIBComponent component, FlexoController controller) {
		super(component, controller);
	}

	public ModelSlot createModelSlot(VirtualModel virtualModel) {
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
	 * @return
	 */
	public FlexoConcept duplicateFlexoConcept(FlexoConcept flexoConcept, String newName) {
		DuplicateFlexoConcept duplicateAction = DuplicateFlexoConcept.actionType.makeNewAction(flexoConcept, null, getEditor());
		duplicateAction.doAction();
		return duplicateAction.getNewFlexoConcept();
	}

	public FlexoConcept addParentFlexoConcept(FlexoConcept flexoConcept) {
		logger.warning("addParentFlexoConcept not implemented yet");
		return null;
	}

	public FlexoRole createFlexoRole(FlexoConcept flexoConcept) {
		System.out.println("On tente de creer un FlexoRole dans " + Integer.toHexString(hashCode()));
		System.out.println("getFlexoController()=" + getFlexoController());
		System.out.println("getEditor()=" + getEditor());
		CreateFlexoRole createFlexoRole = CreateFlexoRole.actionType.makeNewAction(flexoConcept, null, getEditor());
		createFlexoRole.doAction();
		return createFlexoRole.getNewFlexoRole();
	}

	public FlexoRole<?> deleteFlexoRole(FlexoConcept flexoConcept, FlexoRole<?> aPatternRole) {
		flexoConcept.removeFromFlexoRoles(aPatternRole);
		aPatternRole.delete();
		return aPatternRole;
	}

	public void createConstraint(FlexoConcept flexoConcept) {
		FlexoConceptConstraint constraint = flexoConcept.getVirtualModelFactory().newFlexoConceptConstraint();
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
	 * @return
	 */
	public FlexoBehaviour duplicateFlexoBehaviour(FlexoBehaviour flexoBehaviour, String newName) {
		FlexoBehaviour newFlexoBehaviour = (FlexoBehaviour) flexoBehaviour.cloneObject();
		newFlexoBehaviour.setName(newName);
		flexoBehaviour.getFlexoConcept().addToFlexoBehaviours(newFlexoBehaviour);
		return newFlexoBehaviour;
	}

	public SynchronizationScheme createSynchronizationScheme(VirtualModel virtualModel) {
		SynchronizationScheme newEditionScheme = virtualModel.getVirtualModelFactory().newSynchronizationScheme();
		newEditionScheme.setName("synchronization");
		virtualModel.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public CreationScheme createCreationScheme(FlexoConcept flexoConcept) {
		CreationScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newCreationScheme();
		newEditionScheme.setName("creation");
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public DeletionScheme createDeletionScheme(FlexoConcept flexoConcept) {
		DeletionScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newDeletionScheme();
		newEditionScheme.setName("deletion");
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public ActionScheme createActionScheme(FlexoConcept flexoConcept) {
		ActionScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newActionScheme();
		newEditionScheme.setName("action");
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public FlexoBehaviour createAdvancedScheme(FlexoConcept flexoConcept) {
		CreateFlexoBehaviour createFlexoBehaviour = CreateFlexoBehaviour.actionType.makeNewAction(flexoConcept, null, getEditor());
		createFlexoBehaviour.doAction();
		return createFlexoBehaviour.getNewFlexoBehaviour();
	}

	public CloningScheme createCloningScheme(FlexoConcept flexoConcept) {
		CloningScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newCloningScheme();
		newEditionScheme.setName("clone");
		flexoConcept.addToFlexoBehaviours(newEditionScheme);
		return newEditionScheme;
	}

	public FlexoBehaviour deleteFlexoBehaviour(FlexoConcept flexoConcept, FlexoBehaviour flexoBehaviour) {
		flexoConcept.removeFromFlexoBehaviours(flexoBehaviour);
		flexoBehaviour.delete();
		return flexoBehaviour;
	}

	public EditionAction createEditionAction(FMLControlGraph object) {
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(object, null, getEditor());
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
		} else if (flexoConcept != null) {
			CreateFlexoConcept createFlexoConcept = CreateFlexoConcept.actionType.makeNewAction(flexoConcept.getVirtualModel(), null,
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
		} else if (flexoConcept != null) {
			DeleteFlexoConcept deleteFlexoConcept = DeleteFlexoConcept.actionType.makeNewAction(flexoConcept, null, getEditor());
			deleteFlexoConcept.doAction();
		}
		return flexoConcept;
	}

	public FlexoBehaviourParameter createURIParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newURIParameter();
		newParameter.setName("uri");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("uri");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createTextFieldParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newTextFieldParameter();
		newParameter.setName("textField");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createTextAreaParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newTextAreaParameter();
		newParameter.setName("textArea");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createIntegerParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newIntegerParameter();
		newParameter.setName("integer");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createCheckBoxParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newCheckboxParameter();
		newParameter.setName("checkbox");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createDropDownParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newDropDownParameter();
		newParameter.setName("dropdown");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createIndividualParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newIndividualParameter();
		newParameter.setName("individual");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createClassParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newClassParameter();
		newParameter.setName("class");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newPropertyParameter();
		newParameter.setName("property");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createObjectPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newObjectPropertyParameter();
		newParameter.setName("property");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createDataPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newDataPropertyParameter();
		newParameter.setName("property");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	/*public EditionSchemeImplParameter createFlexoObjectParameter() {
		FlexoBehaviourParameter newParameter = new FlexoObjectParameter(null);
		newParameter.setName("flexoObject");
		// newParameter.setLabel("label");
		addToParameters(newParameter);
		return newParameter;
	}*/

	public FlexoBehaviourParameter createTechnologyObjectParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newTechnologyObjectParameter();
		newParameter.setName("technologyObject");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createListParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newListParameter();
		newParameter.setName("list");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createFlexoConceptInstanceParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newFlexoConceptInstanceParameter();
		newParameter.setName("flexoConceptInstance");
		newParameter.setBehaviour(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter deleteParameter(FlexoBehaviour flexoBehaviour, FlexoBehaviourParameter parameterToDelete) {
		flexoBehaviour.removeFromParameters(parameterToDelete);
		parameterToDelete.delete();
		return parameterToDelete;
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
		if (action instanceof TechnologySpecificAction) {
			TechnologyAdapter technologyAdapter = ((TechnologySpecificAction<?, ?>) action).getModelSlot().getModelSlotTechnologyAdapter();
			if (technologyAdapter != null) {
				TechnologyAdapterController<?> taController = getFlexoController().getTechnologyAdapterController(technologyAdapter);
				return taController.getFIBPanelForObject(action);
			} else
				// No specific TechnologyAdapter, lookup in generic libraries
				return getFIBPanelForObject(action);
		} else {
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

		/*FileResource fibFile = new FileResource("Fib/" + editionScheme.getClass().getSimpleName() + "Panel.fib");
		System.out.println("J'essaie " + fibFile + " ca marche ? " + fibFile.exists());
		return fibFile;*/
	}
}
