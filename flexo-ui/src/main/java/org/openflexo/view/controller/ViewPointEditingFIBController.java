package org.openflexo.view.controller;

import java.util.logging.Logger;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.viewpoint.ActionScheme;
import org.openflexo.foundation.viewpoint.CloningScheme;
import org.openflexo.foundation.viewpoint.CreationScheme;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourObject;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptConstraint;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.SynchronizationScheme;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.action.CreateFlexoConcept;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateFlexoBehaviour;
import org.openflexo.foundation.viewpoint.action.CreateFlexoRole;
import org.openflexo.foundation.viewpoint.action.CreateModelSlot;
import org.openflexo.foundation.viewpoint.action.DeleteFlexoConcept;
import org.openflexo.foundation.viewpoint.action.DeleteVirtualModel;
import org.openflexo.foundation.viewpoint.action.DuplicateFlexoConcept;
import org.openflexo.foundation.viewpoint.editionaction.EditionAction;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.rm.Resource;

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
		System.out.println("On tente de creer un FlexoRole");
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

	public EditionAction createEditionAction(FlexoBehaviourObject object) {
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(object, null, getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public FlexoConcept createFlexoConcept(FlexoConcept flexoConcept) {
		if (flexoConcept instanceof VirtualModel) {
			CreateFlexoConcept addFlexoConcept = CreateFlexoConcept.actionType.makeNewAction((VirtualModel) flexoConcept, null, getEditor());
			addFlexoConcept.switchNewlyCreatedFlexoConcept = false;
			addFlexoConcept.doAction();
			return addFlexoConcept.getNewFlexoConcept();
		}
		else if (flexoConcept != null) {
			CreateFlexoConcept addFlexoConcept = CreateFlexoConcept.actionType.makeNewAction(flexoConcept.getVirtualModel(), null, getEditor());
			addFlexoConcept.switchNewlyCreatedFlexoConcept = false;
			addFlexoConcept.doAction();
			addFlexoConcept.getNewFlexoConcept().addToParentFlexoConcepts(flexoConcept);
			return addFlexoConcept.getNewFlexoConcept();
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
			DeleteFlexoConcept deleteFlexoConcept = DeleteFlexoConcept.actionType.makeNewAction(flexoConcept, null, getEditor());
			deleteFlexoConcept.doAction();
		}
		return flexoConcept;
	}

	public FlexoBehaviourParameter createURIParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newURIParameter();
		newParameter.setName("uri");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("uri");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createTextFieldParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newTextFieldParameter();
		newParameter.setName("textField");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createTextAreaParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newTextAreaParameter();
		newParameter.setName("textArea");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createIntegerParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newIntegerParameter();
		newParameter.setName("integer");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createCheckBoxParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newCheckboxParameter();
		newParameter.setName("checkbox");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createDropDownParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newDropDownParameter();
		newParameter.setName("dropdown");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createIndividualParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newIndividualParameter();
		newParameter.setName("individual");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createClassParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newClassParameter();
		newParameter.setName("class");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newPropertyParameter();
		newParameter.setName("property");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createObjectPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newObjectPropertyParameter();
		newParameter.setName("property");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createDataPropertyParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newDataPropertyParameter();
		newParameter.setName("property");
		newParameter.setScheme(flexoBehaviour);
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
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createListParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newListParameter();
		newParameter.setName("list");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter createFlexoConceptInstanceParameter(FlexoBehaviour flexoBehaviour) {
		FlexoBehaviourParameter newParameter = flexoBehaviour.getVirtualModelFactory().newFlexoConceptInstanceParameter();
		newParameter.setName("flexoConceptInstance");
		newParameter.setScheme(flexoBehaviour);
		// newParameter.setLabel("label");
		flexoBehaviour.addToParameters(newParameter);
		return newParameter;
	}

	public FlexoBehaviourParameter deleteParameter(FlexoBehaviour flexoBehaviour, FlexoBehaviourParameter parameterToDelete) {
		flexoBehaviour.removeFromParameters(parameterToDelete);
		parameterToDelete.delete();
		return parameterToDelete;
	}

	public void actionFirst(EditionAction<?, ?> action) {
		if (action.getActionContainer() != null) {
			action.getActionContainer().actionFirst(action);
		}
	}

	public void actionUp(EditionAction<?, ?> action) {
		if (action != null && action.getActionContainer() != null) {
			action.getActionContainer().actionUp(action);
		}
		if (action == null) {
			logger.warning("actionUp was called with null parameter");
		}
	}

	public void actionDown(EditionAction<?, ?> action) {
		if (action.getActionContainer() != null) {
			action.getActionContainer().actionDown(action);
		}
	}

	public void actionLast(EditionAction<?, ?> action) {
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

	public Resource fibForEditionAction(EditionAction<?, ?> action) {
		if (action == null) {
			return null;
		}
		if (action.getModelSlot() == null) {
			// No specific TechnologyAdapter, lookup in generic libraries
			return getFIBPanelForObject(action);
		}
		else {
			TechnologyAdapter technologyAdapter = action.getModelSlot().getTechnologyAdapter();
			if (technologyAdapter != null) {
				TechnologyAdapterController<?> taController = getFlexoController().getTechnologyAdapterController(technologyAdapter);
				return taController.getFIBPanelForObject(action);
			}
			else
				// No specific TechnologyAdapter, lookup in generic libraries
				return getFIBPanelForObject(action);
		}

		/*System.out.println("ModelSlot=" + action.getModelSlot());
		if (action.getModelSlot() != null) {
			System.out.println("TA=" + action.getModelSlot().getTechnologyAdapter());
		}
		FileResource fibFile = new FileResource("Fib/" + action.getClass().getSimpleName() + "Panel.fib");
		System.out.println("J'essaie " + fibFile + " ca marche ? " + fibFile.exists());
		return fibFile;*/
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
