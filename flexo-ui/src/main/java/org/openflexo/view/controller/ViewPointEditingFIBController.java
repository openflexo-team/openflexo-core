package org.openflexo.view.controller;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.viewpoint.ActionScheme;
import org.openflexo.foundation.viewpoint.CloningScheme;
import org.openflexo.foundation.viewpoint.CreationScheme;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptConstraint;
import org.openflexo.foundation.viewpoint.EditionScheme;
import org.openflexo.foundation.viewpoint.EditionSchemeObject;
import org.openflexo.foundation.viewpoint.EditionSchemeParameter;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.foundation.viewpoint.SynchronizationScheme;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.action.AddEditionPattern;
import org.openflexo.foundation.viewpoint.action.CreateEditionAction;
import org.openflexo.foundation.viewpoint.action.CreateModelSlot;
import org.openflexo.foundation.viewpoint.action.CreatePatternRole;
import org.openflexo.foundation.viewpoint.action.DuplicateEditionPattern;
import org.openflexo.foundation.viewpoint.editionaction.EditionAction;
import org.openflexo.logging.FlexoLogger;

/**
 * Represents a controller with basic ViewPoint edition facilities Extends FlexoFIBController by supporting features relative to ViewPoint
 * edirion
 * 
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
		DuplicateEditionPattern duplicateAction = DuplicateEditionPattern.actionType.makeNewAction(flexoConcept, null, getEditor());
		duplicateAction.doAction();
		return duplicateAction.getNewFlexoConcept();
	}

	public FlexoConcept addParentFlexoConcept(FlexoConcept flexoConcept) {
		logger.warning("addParentEditionPattern not implemented yet");
		return null;
	}

	public PatternRole createPatternRole(FlexoConcept flexoConcept) {
		CreatePatternRole createPatternRole = CreatePatternRole.actionType.makeNewAction(flexoConcept, null, getEditor());
		createPatternRole.doAction();
		return createPatternRole.getNewPatternRole();
	}

	public PatternRole<?> deletePatternRole(FlexoConcept flexoConcept, PatternRole<?> aPatternRole) {
		flexoConcept.removeFromPatternRoles(aPatternRole);
		aPatternRole.delete();
		return aPatternRole;
	}

	public void createConstraint(FlexoConcept flexoConcept) {
		FlexoConceptConstraint constraint = flexoConcept.getVirtualModelFactory().newEditionPatternConstraint();
		flexoConcept.addToEditionPatternConstraints(constraint);
	}

	public FlexoConceptConstraint deleteConstraint(FlexoConcept flexoConcept, FlexoConceptConstraint constraint) {
		flexoConcept.removeFromEditionPatternConstraints(constraint);
		constraint.delete();
		return constraint;
	}

	/**
	 * Duplicates this EditionScheme, given a new name<br>
	 * Newly created EditionScheme is added to parent FlexoConcept
	 * 
	 * @param newName
	 * @return
	 */
	public EditionScheme duplicateEditionScheme(EditionScheme editionScheme, String newName) {
		EditionScheme newEditionScheme = (EditionScheme) editionScheme.cloneObject();
		newEditionScheme.setName(newName);
		editionScheme.getFlexoConcept().addToEditionSchemes(newEditionScheme);
		return newEditionScheme;
	}

	public SynchronizationScheme createSynchronizationScheme(VirtualModel virtualModel) {
		SynchronizationScheme newEditionScheme = virtualModel.getVirtualModelFactory().newSynchronizationScheme();
		newEditionScheme.setName("synchronization");
		virtualModel.addToEditionSchemes(newEditionScheme);
		return newEditionScheme;
	}

	public CreationScheme createCreationScheme(FlexoConcept flexoConcept) {
		CreationScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newCreationScheme();
		newEditionScheme.setName("creation");
		flexoConcept.addToEditionSchemes(newEditionScheme);
		return newEditionScheme;
	}

	public DeletionScheme createDeletionScheme(FlexoConcept flexoConcept) {
		DeletionScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newDeletionScheme();
		newEditionScheme.setName("deletion");
		flexoConcept.addToEditionSchemes(newEditionScheme);
		return newEditionScheme;
	}

	public ActionScheme createActionScheme(FlexoConcept flexoConcept) {
		ActionScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newActionScheme();
		newEditionScheme.setName("action");
		flexoConcept.addToEditionSchemes(newEditionScheme);
		return newEditionScheme;
	}

	public CloningScheme createCloningScheme(FlexoConcept flexoConcept) {
		CloningScheme newEditionScheme = flexoConcept.getVirtualModelFactory().newCloningScheme();
		newEditionScheme.setName("clone");
		flexoConcept.addToEditionSchemes(newEditionScheme);
		return newEditionScheme;
	}

	public EditionScheme deleteEditionScheme(FlexoConcept flexoConcept, EditionScheme editionScheme) {
		flexoConcept.removeFromEditionSchemes(editionScheme);
		editionScheme.delete();
		return editionScheme;
	}

	public EditionAction createEditionAction(EditionSchemeObject object) {
		CreateEditionAction createEditionAction = CreateEditionAction.actionType.makeNewAction(object, null, getEditor());
		createEditionAction.doAction();
		return createEditionAction.getNewEditionAction();
	}

	public FlexoConcept createFlexoConcept(FlexoConcept flexoConcept) {
		if (flexoConcept instanceof VirtualModel) {
			AddEditionPattern addEditionPattern = AddEditionPattern.actionType.makeNewAction((VirtualModel) flexoConcept, null,
					getEditor());
			addEditionPattern.switchNewlyCreatedEditionPattern = false;
			addEditionPattern.doAction();
			return addEditionPattern.getNewFlexoConcept();
		} else if (flexoConcept != null) {
			AddEditionPattern addEditionPattern = AddEditionPattern.actionType.makeNewAction(flexoConcept.getVirtualModel(), null,
					getEditor());
			addEditionPattern.switchNewlyCreatedEditionPattern = false;
			addEditionPattern.doAction();
			addEditionPattern.getNewFlexoConcept().addToParentFlexoConcepts(flexoConcept);
			return addEditionPattern.getNewFlexoConcept();
		}
		logger.warning("Unexpected null flexo concept");
		return null;
	}

	public EditionSchemeParameter createURIParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newURIParameter();
		newParameter.setName("uri");
		// newParameter.setLabel("uri");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createTextFieldParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newTextFieldParameter();
		newParameter.setName("textField");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createTextAreaParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newTextAreaParameter();
		newParameter.setName("textArea");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createIntegerParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newIntegerParameter();
		newParameter.setName("integer");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createCheckBoxParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newCheckboxParameter();
		newParameter.setName("checkbox");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createDropDownParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newDropDownParameter();
		newParameter.setName("dropdown");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createIndividualParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newIndividualParameter();
		newParameter.setName("individual");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createClassParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newClassParameter();
		newParameter.setName("class");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createPropertyParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newPropertyParameter();
		newParameter.setName("property");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createObjectPropertyParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newObjectPropertyParameter();
		newParameter.setName("property");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createDataPropertyParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newDataPropertyParameter();
		newParameter.setName("property");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	/*public EditionSchemeImplParameter createFlexoObjectParameter() {
		EditionSchemeParameter newParameter = new FlexoObjectParameter(null);
		newParameter.setName("flexoObject");
		// newParameter.setLabel("label");
		addToParameters(newParameter);
		return newParameter;
	}*/

	public EditionSchemeParameter createTechnologyObjectParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newTechnologyObjectParameter();
		newParameter.setName("technologyObject");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createListParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newListParameter();
		newParameter.setName("list");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter createEditionPatternInstanceParameter(EditionScheme editionScheme) {
		EditionSchemeParameter newParameter = editionScheme.getVirtualModelFactory().newEditionPatternInstanceParameter();
		newParameter.setName("flexoConceptInstance");
		// newParameter.setLabel("label");
		editionScheme.addToParameters(newParameter);
		return newParameter;
	}

	public EditionSchemeParameter deleteParameter(EditionScheme editionScheme, EditionSchemeParameter parameterToDelete) {
		editionScheme.removeFromParameters(parameterToDelete);
		parameterToDelete.delete();
		return parameterToDelete;
	}

	public boolean isEditionScheme(Object selectedObject, EditionScheme context) {
		return selectedObject == null || selectedObject == context;
	}

	public boolean isEditionAction(Object selectedObject) {
		return selectedObject instanceof EditionAction;
	}

	public File fibForEditionAction(EditionAction<?, ?> action) {
		if (action == null) {
			return null;
		}
		if (action.getModelSlot() == null) {
			// No specific TechnologyAdapter, lookup in generic libraries
			return getFIBPanelForObject(action);
		} else {
			TechnologyAdapter technologyAdapter = action.getModelSlot().getTechnologyAdapter();
			TechnologyAdapterController<?> taController = getFlexoController().getTechnologyAdapterController(technologyAdapter);
			return taController.getFIBPanelForObject(action);
		}

		/*System.out.println("ModelSlot=" + action.getModelSlot());
		if (action.getModelSlot() != null) {
			System.out.println("TA=" + action.getModelSlot().getTechnologyAdapter());
		}
		FileResource fibFile = new FileResource("Fib/" + action.getClass().getSimpleName() + "Panel.fib");
		System.out.println("J'essaie " + fibFile + " ca marche ? " + fibFile.exists());
		return fibFile;*/
	}

	public File fibForEditionScheme(EditionScheme editionScheme) {
		if (editionScheme == null) {
			return null;
		}
		// No specific TechnologyAdapter, lookup in generic libraries
		return getFIBPanelForObject(editionScheme);

		/*FileResource fibFile = new FileResource("Fib/" + editionScheme.getClass().getSimpleName() + "Panel.fib");
		System.out.println("J'essaie " + fibFile + " ca marche ? " + fibFile.exists());
		return fibFile;*/
	}
}
