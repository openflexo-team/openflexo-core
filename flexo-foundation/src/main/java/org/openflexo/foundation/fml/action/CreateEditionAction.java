/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.DefaultFMLControlGraphOwner;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IncrementalIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.editionaction.NotifyPropertyChangedAction;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.editionaction.RoleSpecificAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.FireEventAction;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;

public class CreateEditionAction extends FlexoAction<CreateEditionAction, FMLControlGraph, FMLObject>
		implements Bindable, PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(CreateEditionAction.class.getPackage().getName());

	public static FlexoActionFactory<CreateEditionAction, FMLControlGraph, FMLObject> actionType = new FlexoActionFactory<CreateEditionAction, FMLControlGraph, FMLObject>(
			"create_edition_action", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateEditionAction makeNewAction(FMLControlGraph focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateEditionAction(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLControlGraph object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FMLControlGraph object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateEditionAction.actionType, FMLControlGraph.class);
	}

	private ModelSlot<?> modelSlot;
	private FlexoRole<?> flexoRole;
	private Class<? extends EditionAction> editionActionClass;
	private Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass;

	private EditionAction newEditionAction;

	private final List<Class<? extends EditionAction>> availableActions;
	private final List<Class<? extends FetchRequest<?, ?, ?>>> availableFetchRequests;

	private final HashMap<Class<? extends EditionAction>, TechnologyAdapter> editionActionForTechnologyAdapterMap;
	private final HashMap<Class<? extends EditionAction>, EditionAction> editionActionMap;

	private boolean isVariableDeclaration = false;
	private boolean isAssignation = false;
	private boolean isReturnStatement = false;
	private boolean isAddToListAction = false;
	private IterationType iterationType = IterationType.Expression;

	private void addToAvailableActions(Class<? extends EditionAction> availableActionClass, TechnologyAdapter ta) {
		if (!availableActions.contains(availableActionClass)) {
			availableActions.add(availableActionClass);
			editionActionForTechnologyAdapterMap.put(availableActionClass, ta);
			if (FetchRequest.class.isAssignableFrom(availableActionClass)) {
				availableFetchRequests.add((Class<FetchRequest<?, ?, ?>>) availableActionClass);
			}
		}
	}

	CreateEditionAction(FMLControlGraph focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

		availableActions = new ArrayList<>();
		availableFetchRequests = new ArrayList<>();
		editionActionForTechnologyAdapterMap = new HashMap<>();
		editionActionMap = new HashMap<>();

		// availableActions.add(AssignationAction.class);
		// availableActions.add(DeclarationAction.class);

		FMLTechnologyAdapter fmlTA = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		addToAvailableActions(ExpressionAction.class, fmlTA);
		addToAvailableActions(AddClassInstance.class, fmlTA);
		addToAvailableActions(LogAction.class, fmlTA);
		addToAvailableActions(ConditionalAction.class, fmlTA);
		addToAvailableActions(IterationAction.class, fmlTA);
		addToAvailableActions(WhileAction.class, fmlTA);
		addToAvailableActions(IncrementalIterationAction.class, fmlTA);
		addToAvailableActions(RemoveFromListAction.class, fmlTA);
		addToAvailableActions(AddFlexoConceptInstance.class, fmlTA);
		addToAvailableActions(AddVirtualModelInstance.class, fmlTA);
		addToAvailableActions(InitiateMatching.class, fmlTA);
		addToAvailableActions(MatchFlexoConceptInstance.class, fmlTA);
		addToAvailableActions(FinalizeMatching.class, fmlTA);
		addToAvailableActions(SelectFlexoConceptInstance.class, fmlTA);
		addToAvailableActions(SelectVirtualModelInstance.class, fmlTA);
		addToAvailableActions(DeleteAction.class, fmlTA);
		addToAvailableActions(DeleteFlexoConceptInstance.class, fmlTA);
		addToAvailableActions(FireEventAction.class, fmlTA);
		addToAvailableActions(NotifyPropertyChangedAction.class, fmlTA);

		for (UseModelSlotDeclaration useDecl : getVirtualModel().getAccessibleUseDeclarations()) {
			Class<? extends ModelSlot<?>> modelSlotClass = useDecl.getModelSlotClass();
			TechnologyAdapter modelSlotTA = getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapterForModelSlot(modelSlotClass);
			for (Class<? extends TechnologySpecificAction<?, ?, ?>> eaClass : getServiceManager().getTechnologyAdapterService()
					.getAvailableEditionActionTypes(modelSlotClass)) {
				addToAvailableActions(eaClass, modelSlotTA);
			}
			for (Class<? extends FetchRequest<?, ?, ?>> frClass : getServiceManager().getTechnologyAdapterService()
					.getAvailableFetchRequestActionTypes(modelSlotClass)) {
				addToAvailableActions(frClass, modelSlotTA);
			}
		}

	}

	public List<Class<? extends EditionAction>> getAvailableActionClasses() {
		return availableActions;
	}

	public List<Class<? extends FetchRequest<?, ?, ?>>> getAvailableFetchRequestClasses() {
		return availableFetchRequests;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add edition action, modelSlot=" + modelSlot + " editionActionClass=" + editionActionClass);

		newEditionAction = null;
		EditionAction baseEditionAction = getBaseEditionAction();

		if (baseEditionAction instanceof AssignableAction) {
			if (isAssignation()) {
				AssignationAction<?> newAssignationAction = getFocusedObject().getFMLModelFactory().newAssignationAction();
				newAssignationAction.setAssignableAction((AssignableAction) baseEditionAction);
				newAssignationAction.setAssignation((DataBinding) getAssignation());
				newEditionAction = newAssignationAction;
			}
			else if (isVariableDeclaration()) {
				DeclarationAction<?> newDeclarationAction = getFocusedObject().getFMLModelFactory().newDeclarationAction();
				newDeclarationAction.setAssignableAction((AssignableAction) baseEditionAction);
				newDeclarationAction.setVariableName(getDeclarationVariableName());
				newEditionAction = newDeclarationAction;
			}
			else if (isReturnStatement()) {
				ReturnStatement<?> newReturnStatement = getFocusedObject().getFMLModelFactory().newReturnStatement();
				newReturnStatement.setAssignableAction((AssignableAction) baseEditionAction);
				newEditionAction = newReturnStatement;
			}
			else if (isAddToListAction()) {
				AddToListAction<?> newAddToListAction = getFocusedObject().getFMLModelFactory().newAddToListAction();
				newAddToListAction.setAssignableAction((AssignableAction) baseEditionAction);
				newAddToListAction.setList((DataBinding) getListExpression());
				newEditionAction = newAddToListAction;
			}
		}

		if (newEditionAction == null) {
			newEditionAction = baseEditionAction;
		}

		if (newEditionAction != null) {
			getFocusedObject().sequentiallyAppend(newEditionAction);
		}

		else {
			throw new InvalidParameterException("cannot build EditionAction for " + editionActionClass);
		}

	}

	public TechnologyAdapter getTechnologyAdapter(Class<? extends EditionAction> editionActionClass) {
		TechnologyAdapter returned = editionActionForTechnologyAdapterMap.get(editionActionClass);
		if (returned != null) {
			return returned;
		}
		FMLTechnologyAdapter fmlTA = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		return fmlTA;
	}

	public Class<? extends EditionAction> getEditionActionClass() {
		if (editionActionClass == null) {
			setEditionActionClass(ExpressionAction.class);
		}
		return editionActionClass;
	}

	public void setEditionActionClass(Class<? extends EditionAction> editionActionClass) {
		if ((editionActionClass == null && this.editionActionClass != null)
				|| (editionActionClass != null && !editionActionClass.equals(this.editionActionClass))) {
			Class<? extends EditionAction> oldValue = this.editionActionClass;
			this.editionActionClass = editionActionClass;
			getPropertyChangeSupport().firePropertyChange("editionActionClass", oldValue, editionActionClass);
			// baseEditionAction = makeEditionAction();
			getPropertyChangeSupport().firePropertyChange("baseEditionAction", oldValue, editionActionClass);
			getPropertyChangeSupport().firePropertyChange("isAssignableAction", !isAssignableAction(), isAssignableAction());
			getPropertyChangeSupport().firePropertyChange("isIterationAction", !isIterationAction(), isIterationAction());
			getPropertyChangeSupport().firePropertyChange("modelSlot", getModelSlot() != null ? null : true, getModelSlot());
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
		}
	}

	public Class<? extends FetchRequest<?, ?, ?>> getFetchRequestClass() {
		if (fetchRequestClass == null && availableFetchRequests != null && !availableFetchRequests.isEmpty()) {
			setFetchRequestClass(availableFetchRequests.get(0));
		}
		return fetchRequestClass;
	}

	public void setFetchRequestClass(Class<? extends FetchRequest<?, ?, ?>> fetchRequestClass) {
		if ((fetchRequestClass == null && this.fetchRequestClass != null)
				|| (fetchRequestClass != null && !fetchRequestClass.equals(this.fetchRequestClass))) {
			Class<? extends FetchRequest<?, ?, ?>> oldValue = this.fetchRequestClass;
			this.fetchRequestClass = fetchRequestClass;
			updateIteration();
			getPropertyChangeSupport().firePropertyChange("fetchRequestClass", oldValue, fetchRequestClass);
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
			getPropertyChangeSupport().firePropertyChange("fetchRequestAction", oldValue, fetchRequestClass);
		}
	}

	public EditionAction getBaseEditionAction() {
		EditionAction returned = editionActionMap.get(getEditionActionClass());
		if (returned == null) {
			returned = makeEditionAction();
			if (returned != null) {
				editionActionMap.put(editionActionClass, returned);
				returned.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
		}
		return returned;
	}

	public FetchRequest<?, ?, ?> getFetchRequestAction() {
		if (isIterationAction()) {
			if (getIterationType() == IterationType.FetchRequest
					&& ((IterationAction) getBaseEditionAction()).getIterationAction() instanceof FetchRequest) {
				return (FetchRequest<?, ?, ?>) ((IterationAction) getBaseEditionAction()).getIterationAction();
			}
		}
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof EditionAction) {
			getPropertyChangeSupport().firePropertyChange("declarationVariableName", null, getDeclarationVariableName());
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getDeclarationVariableName());
		}
	}

	public String getStringRepresentation() {
		EditionAction baseEditionAction = getBaseEditionAction();

		if (baseEditionAction instanceof AssignableAction) {
			if (isAssignation()) {
				return getAssignation() + " = " + baseEditionAction.getStringRepresentation();
			}
			else if (isVariableDeclaration()) {
				return TypeUtils.simpleRepresentation(((AssignableAction) baseEditionAction).getAssignableType()) + " "
						+ getDeclarationVariableName() + " = " + baseEditionAction.getStringRepresentation();
			}
			else if (isAddToListAction()) {
				return getListExpression() + ".FML::AddToList(" + baseEditionAction.getStringRepresentation() + ")";
			}
			else {
				return baseEditionAction.getStringRepresentation();
			}
		}

		if (baseEditionAction != null) {
			return baseEditionAction.getStringRepresentation();
		}

		return "null";
	}

	@Override
	public boolean delete() {
		for (EditionAction ea : editionActionMap.values()) {
			ea.getPropertyChangeSupport().removePropertyChangeListener(this);
			ea.delete();
		}
		editionActionMap.clear();
		editionActionForTechnologyAdapterMap.clear();
		availableActions.clear();
		return super.delete();
	}

	public EditionAction getNewEditionAction() {
		return newEditionAction;
	}

	@Override
	public boolean isValid() {
		if (getEditionActionClass() == null) {
			return false;
		}
		return true;

		/*switch (actionChoice) {
		case BuiltInAction:
			if (builtInActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		case ControlAction:
			if (controlActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		case ModelSlotSpecificAction:
			if (modelSlot == null) {
				validityMessage = NO_MODEL_SLOT;
				return false;
			}
			if (modelSlotSpecificActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		case RequestAction:
			if (modelSlot == null) {
				validityMessage = NO_MODEL_SLOT;
				return false;
			}
			if (requestActionClass == null) {
				validityMessage = NO_ACTION_TYPE_SELECTED;
				return false;
			}
			return true;
		
		default:
			return false;
		}*/

	}

	private DefaultFMLControlGraphOwner owner;

	private EditionAction makeEditionAction() {
		EditionAction returned = null;
		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();

		if (editionActionClass == null) {
			logger.warning("Unexpected " + editionActionClass);
			return null;
		}

		if (factory == null) {
			logger.warning("Unexpected null factory for " + getFocusedObject());
			return null;
		}

		if (org.openflexo.foundation.fml.editionaction.AssignationAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newAssignationAction();
		}
		else if (org.openflexo.foundation.fml.editionaction.ExpressionAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newExpressionAction();
		}
		else if (org.openflexo.foundation.fml.editionaction.LogAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newLogAction();
		}
		else if (org.openflexo.foundation.fml.editionaction.NotifyPropertyChangedAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newNotifyPropertyChangedAction();
		}
		else if (org.openflexo.foundation.fml.rt.editionaction.FireEventAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newFireEventAction();
		}
		else if (org.openflexo.foundation.fml.editionaction.AddToListAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newAddToListAction();
		}
		else if (org.openflexo.foundation.fml.editionaction.RemoveFromListAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newRemoveFromListAction();
		}
		else if (AddFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newAddFlexoConceptInstance();
		}
		else if (AddClassInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newAddClassInstance();
		}
		else if (AddVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newAddVirtualModelInstance();
		}
		else if (InitiateMatching.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newInitiateMatching();
		}
		else if (MatchFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newMatchFlexoConceptInstance();
		}
		else if (FinalizeMatching.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newFinalizeMatching();
		}
		else if (SelectFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newSelectFlexoConceptInstance();
		}
		else if (SelectVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newSelectVirtualModelInstance();
		}
		else if (DeleteFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newDeleteFlexoConceptInstanceAction();
		}
		else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newDeleteAction();
		}
		else if (ConditionalAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newConditionalAction();
		}
		else if (WhileAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newWhileAction();
		}
		else if (IncrementalIterationAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newIncrementalIterationAction();
		}
		else if (IterationAction.class.isAssignableFrom(editionActionClass)) {
			returned = factory.newIterationAction();
			updateIteration((IterationAction) returned);
		}
		else if (FetchRequest.class.isAssignableFrom(editionActionClass) && getModelSlot() != null) {
			returned = getModelSlot().makeFetchRequest((Class<FetchRequest<?, ?, ?>>) editionActionClass);
		}
		else if (TechnologySpecificAction.class.isAssignableFrom(editionActionClass) && getModelSlot() != null) {
			returned = getModelSlot().makeEditionAction((Class<TechnologySpecificAction<?, ?, ?>>) editionActionClass);
		}

		// Special case for technoly specific action whose model slot cannot be looked-up
		if (returned == null && getModelSlot() == null) {
			returned = factory.newInstance(editionActionClass);
		}

		// System.out.println("editionActionClass=" + editionActionClass);
		// System.out.println("getFlexoRole()=" + getFlexoRole());
		// System.out.println("getModelSlot()=" + getModelSlot());

		if (RoleSpecificAction.class.isAssignableFrom(editionActionClass) && getFlexoRole() != null) {
			((RoleSpecificAction) returned).setFlexoRole(getFlexoRole());
		}
		else if (TechnologySpecificAction.class.isAssignableFrom(editionActionClass) && getModelSlot() != null) {
			// ((TechnologySpecificAction) returned).setModelSlot(getModelSlot());
			((TechnologySpecificAction) returned).getReceiver().setUnparsedBinding(getModelSlot().getName());
		}

		if (returned != null) {

			owner = factory.newInstance(DefaultFMLControlGraphOwner.class);
			owner.setConceptObject(getFocusedObject());
			returned.setOwner(owner);

			return returned;
		}

		logger.warning("Cannot build EditionAction " + editionActionClass);
		return null;

	}

	public List<ModelSlot<?>> getAvailableModelSlotsForAction(Class<? extends EditionAction> actionType) {
		List<ModelSlot<?>> returned = new ArrayList<>();
		// if (getFocusedObject().getOwner().getOwningVirtualModel() != null) {
		for (ModelSlot<?> ms : getModelSlotsAccessibleFromFocusedObject()) {
			if (ms.getAvailableEditionActionTypes().contains(actionType)) {
				returned.add(ms);
			}
			if (ms.getAvailableFetchRequestActionTypes().contains(actionType)) {
				returned.add(ms);
			}
		}
		// }
		return returned;
	}

	public ModelSlot<?> getModelSlot() {
		if (getFlexoRole() != null) {
			return getFlexoRole().getModelSlot();
		}
		List<ModelSlot<?>> availableMS = getAvailableModelSlotsForAction(getEditionActionClass());
		if (modelSlot == null) {
			if (availableMS.size() > 0) {
				// Force the model slot not to be null;
				modelSlot = getAvailableModelSlotsForAction(getEditionActionClass()).get(0);
				return getAvailableModelSlotsForAction(getEditionActionClass()).get(0);
			}
		}
		if (modelSlot != null && !availableMS.contains(modelSlot)) {
			if (availableMS.size() > 0) {
				modelSlot = getAvailableModelSlotsForAction(getEditionActionClass()).get(0);
			}
			else {
				modelSlot = null;
			}
		}
		return modelSlot;
	}

	public void setModelSlot(ModelSlot<?> modelSlot) {
		if ((modelSlot == null && this.modelSlot != null) || (modelSlot != null && !modelSlot.equals(this.modelSlot))) {
			ModelSlot<?> oldValue = this.modelSlot;
			this.modelSlot = modelSlot;
			getPropertyChangeSupport().firePropertyChange("modelSlot", oldValue, modelSlot);
		}
	}

	public FlexoRole<?> getFlexoRole() {
		return flexoRole;
	}

	public void setFlexoRole(FlexoRole<?> flexoRole) {
		if ((flexoRole == null && this.flexoRole != null) || (flexoRole != null && !flexoRole.equals(this.flexoRole))) {
			FlexoRole<?> oldValue = this.flexoRole;
			this.flexoRole = flexoRole;
			getPropertyChangeSupport().firePropertyChange("flexoRole", oldValue, flexoRole);
		}
	}

	/*public Class<? extends EditionAction> getBuiltInActionClass() {
		return builtInActionClass;
	}
	
	public void setBuiltInActionClass(Class<? extends EditionAction> builtInActionClass) {
		this.builtInActionClass = builtInActionClass;
	}
	
	public Class<? extends EditionAction> getControlActionClass() {
		return controlActionClass;
	}
	
	public void setControlActionClass(Class<? extends ControlStructureAction> controlActionClass) {
		this.controlActionClass = controlActionClass;
	}
	
	public Class<? extends EditionAction> getModelSlotSpecificActionClass() {
		return modelSlotSpecificActionClass;
	}
	
	public void setModelSlotSpecificActionClass(Class<? extends TechnologySpecificAction<?, ?, ?>> modelSlotSpecificActionClass) {
		this.modelSlotSpecificActionClass = modelSlotSpecificActionClass;
	}
	
	public Class<? extends FetchRequest<?, ?, ?>> getRequestActionClass() {
		return requestActionClass;
	}
	
	public void setRequestActionClass(Class<? extends FetchRequest<?, ?, ?>> requestActionClass) {
		this.requestActionClass = requestActionClass;
	}*/

	private DataBinding<?> assignation = null;
	private DataBinding<?> iterationExpression = null;
	private DataBinding<?> listExpression = null;
	private String declarationVariableName = null;

	/**
	 * Return VirtualModel encoding EditionAction to be created
	 * 
	 * @return
	 */
	public VirtualModel getVirtualModel() {
		if (getFocusedObject().getOwner().getFlexoConcept() instanceof VirtualModel) {
			return (VirtualModel) getFocusedObject().getOwner().getFlexoConcept();
		}
		else if (getFocusedObject().getOwner().getOwningVirtualModel() != null) {
			return getFocusedObject().getOwner().getOwningVirtualModel();
		}
		return null;
	}

	/**
	 * Return a list of accessible model slots from this focused object. If this object is part of a virtual model(an action in a behavior
	 * of a VirtualModel or Viewpoint) then return virtual model model slots<br>
	 * Otherwise (if it is part of a FlexoConcept for instance) then return the models slots of its owned virtual model.
	 * 
	 * @return
	 */
	private List<ModelSlot<?>> getModelSlotsAccessibleFromFocusedObject() {
		if (getVirtualModel() != null) {
			return getVirtualModel().getModelSlots();
		}
		else {
			return null;
		}
	}

	private static Type getAssignableType() {
		return Object.class;
	}

	public DataBinding<?> getAssignation() {
		if (assignation == null) {

			assignation = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET_SET) {
				@Override
				public Type getDeclaredType() {
					return getAssignableType();
				}
			};
			assignation.setDeclaredType(getAssignableType());
			assignation.setBindingName("assignation");
			assignation.setMandatory(true);

		}
		assignation.setDeclaredType(getAssignableType());
		return assignation;
	}

	public void setAssignation(DataBinding<?> assignation) {
		setAssignation(true);
		if (assignation != null) {
			this.assignation = new DataBinding<Object>(assignation.toString(), this, Object.class,
					DataBinding.BindingDefinitionType.GET_SET) {
				@Override
				public Type getDeclaredType() {
					return getAssignableType();
				}
			};
			assignation.setDeclaredType(getAssignableType());
			assignation.setBindingName("assignation");
			assignation.setMandatory(true);
		}
		notifiedBindingChanged(this.assignation);
	}

	private String getDefaultVariableName() {

		String baseName = getBaseVariableName();
		String current = baseName;
		int i = 2;

		while (getFocusedObject().getInferedBindingModel().bindingVariableNamed(current) != null) {
			current = baseName + i;
			i++;
		}

		return current;
	}

	private String getBaseVariableName() {

		if (getBaseEditionAction() instanceof AssignableAction) {
			Type assignableType = ((AssignableAction) getBaseEditionAction()).getAssignableType();
			String typeAsString = TypeUtils.simpleRepresentation(assignableType);
			if (assignableType instanceof Class) {
				if (typeAsString.startsWith("a") || typeAsString.startsWith("e") || typeAsString.startsWith("i")
						|| typeAsString.startsWith("o") || typeAsString.startsWith("u")) {
					return "an" + typeAsString.substring(0, 1).toUpperCase() + typeAsString.substring(1);
				}
				else {
					return "a" + typeAsString.substring(0, 1).toUpperCase() + typeAsString.substring(1);
				}
			}
		}

		return "variable";
	}

	public String getDeclarationVariableName() {
		if (declarationVariableName == null) {
			return getDefaultVariableName();
		}
		return declarationVariableName;
	}

	public void setDeclarationVariableName(String declarationVariableName) {
		setVariableDeclaration(true);
		if ((declarationVariableName == null && this.declarationVariableName != null)
				|| (declarationVariableName != null && !declarationVariableName.equals(this.declarationVariableName))) {
			String oldValue = this.declarationVariableName;
			this.declarationVariableName = declarationVariableName;
			getPropertyChangeSupport().firePropertyChange("declarationVariableName", oldValue, declarationVariableName);
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
		}
	}

	@Override
	public BindingFactory getBindingFactory() {
		return getFocusedObject().getBindingFactory();
	}

	@Override
	public BindingModel getBindingModel() {
		return getFocusedObject().getInferedBindingModel();
	}

	@Override
	public void notifiedBindingChanged(org.openflexo.connie.DataBinding<?> dataBinding) {
		if (dataBinding == getIterationExpression()) {
			updateIteration();
		}
		getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
	}

	@Override
	public void notifiedBindingDecoded(org.openflexo.connie.DataBinding<?> dataBinding) {
		// TODO
	}

	public boolean isAssignableAction() {
		return getBaseEditionAction() instanceof AssignableAction;
	}

	public boolean isIterationAction() {
		return getBaseEditionAction() instanceof IterationAction;
	}

	public static enum IterationType {
		Expression, FetchRequest;
	}

	public IterationType getIterationType() {
		return iterationType;
	}

	public void setIterationType(IterationType iterationType) {
		if (iterationType != this.iterationType) {
			IterationType oldValue = this.iterationType;
			this.iterationType = iterationType;
			updateIteration();
			getPropertyChangeSupport().firePropertyChange("iterationType", oldValue, iterationType);
		}
	}

	private void updateIteration() {
		IterationAction iterationAction = (IterationAction) editionActionMap.get(IterationAction.class);
		if (iterationAction != null) {
			updateIteration(iterationAction);
		}
	}

	private void updateIteration(IterationAction iterationAction) {
		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
		if (iterationAction != null) {
			switch (getIterationType()) {
				case Expression:
					ExpressionAction exp = factory.newExpressionAction(getIterationExpression());
					iterationAction.setIterationAction(exp);
					break;
				case FetchRequest:
					FetchRequest<?, ?, ?> fetchRequest = factory.newInstance(getFetchRequestClass());
					iterationAction.setIterationAction(fetchRequest);
					// Unused List<ModelSlot<?>> availableMS =
					getAvailableModelSlotsForAction(getFetchRequestClass());
					break;
				default:
					break;
			}
		}
	}

	public boolean isVariableDeclaration() {
		return isVariableDeclaration;
	}

	public void setVariableDeclaration(boolean isVariableDeclaration) {
		if (isVariableDeclaration != this.isVariableDeclaration) {
			boolean oldValue = this.isVariableDeclaration;
			this.isVariableDeclaration = isVariableDeclaration;
			getPropertyChangeSupport().firePropertyChange("isVariableDeclaration", oldValue, isVariableDeclaration);
			if (isVariableDeclaration) {
				setReturnStatement(false);
				setAssignation(false);
				setAddToListAction(false);
			}
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
		}
	}

	public boolean isReturnStatement() {
		return isReturnStatement;
	}

	public void setReturnStatement(boolean isReturnStatement) {
		if (isReturnStatement != this.isReturnStatement) {
			boolean oldValue = this.isReturnStatement;
			this.isReturnStatement = isReturnStatement;
			getPropertyChangeSupport().firePropertyChange("isReturnStatement", oldValue, isReturnStatement);
			if (isReturnStatement) {
				setVariableDeclaration(false);
				setAssignation(false);
				setAddToListAction(false);
			}
		}
	}

	public boolean isAddToListAction() {
		return isAddToListAction;
	}

	public void setAddToListAction(boolean isAddToListAction) {
		if (isAddToListAction != this.isAddToListAction) {
			boolean oldValue = this.isAddToListAction;
			this.isAddToListAction = isAddToListAction;
			getPropertyChangeSupport().firePropertyChange("isAddToListAction", oldValue, isAddToListAction);
			if (isAddToListAction) {
				setReturnStatement(false);
				setVariableDeclaration(false);
				setAssignation(false);
			}
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
		}
	}

	public boolean isAssignation() {
		return isAssignation;
	}

	public void setAssignation(boolean isAssignation) {
		if (isAssignation != this.isAssignation) {
			boolean oldValue = this.isAssignation;
			this.isAssignation = isAssignation;
			getPropertyChangeSupport().firePropertyChange("isAssignation", oldValue, isAssignation);
			if (isAssignation) {
				setReturnStatement(false);
				setVariableDeclaration(false);
				setAddToListAction(false);
			}
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
		}
	}

	public DataBinding<?> getIterationExpression() {
		if (iterationExpression == null) {

			iterationExpression = new DataBinding<>(this, List.class, DataBinding.BindingDefinitionType.GET);
			iterationExpression.setBindingName("iterationExpression");
			iterationExpression.setMandatory(true);

		}
		// iterationExpression.setDeclaredType(getAssignableType());
		return iterationExpression;
	}

	public void setIterationExpression(DataBinding<?> iterationExpression) {
		if (iterationExpression != null) {
			this.iterationExpression = new DataBinding<>(iterationExpression.toString(), this, List.class,
					DataBinding.BindingDefinitionType.GET);
			iterationExpression.setBindingName("iterationExpression");
			iterationExpression.setMandatory(true);
		}
		updateIteration();
		notifiedBindingChanged(this.assignation);
		getPropertyChangeSupport().firePropertyChange("iterationExpression", null, iterationExpression);
		getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
	}

	public DataBinding<?> getListExpression() {
		if (listExpression == null) {
			listExpression = new DataBinding<>(this, List.class, DataBinding.BindingDefinitionType.GET);
			listExpression.setBindingName("listExpression");
			listExpression.setMandatory(true);

		}
		// listExpression.setDeclaredType(getAssignableType());
		return listExpression;
	}

	public void setListExpression(DataBinding<?> listExpression) {
		if (listExpression != null) {
			this.listExpression = new DataBinding<>(listExpression.toString(), this, List.class, DataBinding.BindingDefinitionType.GET);
			listExpression.setBindingName("listExpression");
			listExpression.setMandatory(true);
		}
		notifiedBindingChanged(this.listExpression);
		getPropertyChangeSupport().firePropertyChange("listExpression", null, listExpression);
		getPropertyChangeSupport().firePropertyChange("stringRepresentation", null, getStringRepresentation());
	}

}
