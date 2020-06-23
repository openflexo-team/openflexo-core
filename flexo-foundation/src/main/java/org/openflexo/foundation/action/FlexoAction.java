/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.action.FlexoUndoManager.FlexoActionCompoundEdit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;

/**
 * Abstract representation of an action at model level (model edition primitive)
 * 
 * T2 is arbitrary and should be removed in the long run. There is absolutely no guarantee on the actual type of T2. No assertions can be
 * made. T1 can be kept if we ensure that only actions of the type FlexoAction<A extends FlexoAction<A, T1>, T1 extends FlexoModelObject>
 * are actually returned for a given object of type T1.
 * 
 * @author sylvain
 * 
 * @param <A>
 *            type of FlexoAction
 * @param <T1>
 *            type of object such {@link FlexoAction} is to be applied as focused object
 * @param <T2>
 *            type of additional object such {@link FlexoAction} is to be applied as global selection Beware that getFocusedObject cast a T2
 *            in T1 but changing T2 extends FlexoObject to T2 extends T1 seems to break a lot of things...
 *
 */
public abstract class FlexoAction<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FlexoObject>
		extends FlexoObservable {

	@SuppressWarnings("unused")
	private static final Logger logger = FlexoLogger.getLogger(FlexoAction.class.getPackage().getName());

	private FlexoActionFactory<A, T1, T2> actionFactory;
	private T1 focusedObject;
	private Vector<T2> globalSelection;
	private Object context;
	private Object invoker;
	private FlexoEditor editor;

	private FlexoActionCompoundEdit compoundEdit;

	public enum ExecutionStatus {
		NEVER_EXECUTED,
		EXECUTING_CORE,
		HAS_SUCCESSFULLY_EXECUTED,
		FAILED_EXECUTION,
		EXECUTING_UNDO_CORE,
		HAS_SUCCESSFULLY_UNDONE,
		FAILED_UNDO_EXECUTION,
		EXECUTING_REDO_CORE,
		HAS_SUCCESSFULLY_REDONE,
		FAILED_REDO_EXECUTION,
		HAS_BEEN_CANCELLED;

		public boolean hasActionExecutionSucceeded() {
			return this == ExecutionStatus.HAS_SUCCESSFULLY_EXECUTED;
		}

		public boolean hasActionUndoExecutionSucceeded() {
			return this == HAS_SUCCESSFULLY_UNDONE;
		}

		public boolean hasActionRedoExecutionSucceeded() {
			return this == HAS_SUCCESSFULLY_REDONE;
		}

	}

	/*private boolean _isExecutingInitializer = false;
	private boolean _isExecutingFinalizer = false;
	private boolean _isExecutingCore = false;
	private boolean _isExecuting = false;*/

	// private boolean actionExecutionSucceeded = false;

	protected ExecutionStatus executionStatus = ExecutionStatus.NEVER_EXECUTED;
	private FlexoException thrownException = null;

	/**
	 * Instantiate a {@link FlexoAction} with a factory, a focused object and a global selection
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	protected FlexoAction(FlexoActionFactory<A, T1, T2> actionFactory, T1 focusedObject, List<T2> globalSelection, FlexoEditor editor) {
		super();
		this.editor = editor;
		this.actionFactory = actionFactory;
		this.focusedObject = focusedObject;
		if (globalSelection != null) {
			this.globalSelection = new Vector<>();
			this.globalSelection.addAll(globalSelection);
		}
		else {
			this.globalSelection = null;
		}
	}

	/**
	 * Instantiate a {@link FlexoAction} with a focused object and a global selection<br>
	 * The factory remains null
	 * 
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	protected FlexoAction(T1 focusedObject, List<T2> globalSelection, FlexoEditor editor) {
		this(null, focusedObject, globalSelection, editor);
	}

	public boolean delete() {
		editor = null;
		invoker = null;
		context = null;
		if (globalSelection != null) {
			globalSelection.clear();
		}
		globalSelection = null;
		focusedObject = null;
		actionFactory = null;
		return true;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public FlexoActionFactory<A, T1, T2> getActionFactory() {
		return actionFactory;
	}

	public String getLocalizedName() {
		if (getActionFactory() != null) {
			return getLocales().localizedForKey(getActionFactory().getActionName());
		}
		return getClass().getSimpleName();
	}

	public String getLocalizedDescription() {
		if (getActionFactory() != null) {
			return getLocales().localizedForKey(getActionFactory().getActionName() + "_description");
		}
		return null;
	}

	/**
	 * Sets focused object
	 */
	public void setFocusedObject(T1 focusedObject) {
		this.focusedObject = focusedObject;
	}

	/**
	 * Return focused object, according to the one used in action constructor (see FlexoAction factory). If no focused object was defined,
	 * and global selection is not empty, return first object in the selection
	 * 
	 * @return a FlexoModelObject instance, representing focused object
	 */
	public T1 getFocusedObject() {
		if (focusedObject != null) {
			return focusedObject;
		}
		if (globalSelection != null && globalSelection.size() > 0) {
			return (T1) globalSelection.firstElement();
		}
		return null;
	}

	public Vector<T2> getGlobalSelection() {
		return globalSelection;
	}

	public A doAction() {
		if (editor != null) {
			editor.performAction((A) this, null);
		}
		else {
			logger.warning("No editor for action " + this);
			try {
				doActionInContext();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
		}
		return (A) this;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public boolean hasActionExecutionSucceeded() {
		return getExecutionStatus().hasActionExecutionSucceeded();
	}

	public FlexoException getThrownException() {
		return thrownException;
	}

	public void cancelExecution() {
		executionStatus = ExecutionStatus.HAS_BEEN_CANCELLED;
	}

	public boolean hasBeenCancelled() {
		return getExecutionStatus() == ExecutionStatus.HAS_BEEN_CANCELLED;
	}

	public A doActionInContext() throws FlexoException {
		// If the factory is not null, check that factory allows execution in its context
		if (getActionFactory() != null && !getActionFactory().isEnabled(getFocusedObject(), getGlobalSelection())) {
			throw new InactiveFlexoActionException(getActionFactory(), getFocusedObject(), getGlobalSelection());
		}
		try {
			executionStatus = ExecutionStatus.EXECUTING_CORE;
			doAction(getContext());
			executionStatus = ExecutionStatus.HAS_SUCCESSFULLY_EXECUTED;
		} catch (FlexoException e) {
			executionStatus = ExecutionStatus.FAILED_EXECUTION;
			thrownException = e;
			throw e;
		}
		return (A) this;
	}

	protected abstract void doAction(Object context) throws FlexoException;

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public Object getInvoker() {
		return invoker;
	}

	public void setInvoker(Object invoker) {
		this.invoker = invoker;
	}

	public Vector<FlexoObject> getGlobalSelectionAndFocusedObject() {
		return getGlobalSelectionAndFocusedObject(getFocusedObject(), getGlobalSelection());
	}

	public static <T extends FlexoObject> Vector<T> getGlobalSelectionAndFocusedObject(T focusedObject,
			Vector<? extends T> globalSelection) {
		Vector<T> v = globalSelection != null ? new Vector<>(globalSelection.size() + 1) : new Vector<>(1);
		if (globalSelection != null) {
			v.addAll(globalSelection);
		}
		if (focusedObject != null && !v.contains(focusedObject)) {
			v.add(focusedObject);
		}
		return v;
	}

	public static <T extends FlexoObject> List<T> getGlobalSelection(T focusedObject, List<T> globalSelection) {
		Vector<T> v = globalSelection != null ? new Vector<>(globalSelection.size() + 1) : new Vector<>(1);
		if (globalSelection != null) {
			v.addAll(globalSelection);
		}
		if (focusedObject != null && !v.contains(focusedObject)) {
			v.add(focusedObject);
		}
		return v;
	}

	public static boolean isHomogeneousFlexoConceptInstanceSelection(FlexoObject focusedObject, Vector<FlexoObject> selection) {
		if (focusedObject instanceof FlexoConceptInstance) {
			// Flag indicating if the whole selection is composed of FlexoConceptInstance OF SAME TYPE (ie same FlexoConcept)
			boolean isHomogeneousFlexoConceptInstanceSelection = true;
			FlexoConceptInstance fci = (FlexoConceptInstance) focusedObject;
			FlexoConcept commonConcept = fci.getFlexoConcept();
			Vector<FlexoObject> globalSelection = getGlobalSelectionAndFocusedObject(focusedObject, selection);
			for (FlexoObject o : globalSelection) {
				if (!(o instanceof FlexoConceptInstance)) {
					isHomogeneousFlexoConceptInstanceSelection = false;
					break;
				}
				if (((FlexoConceptInstance) o).getFlexoConcept() != commonConcept) {
					isHomogeneousFlexoConceptInstanceSelection = false;
					break;
				}
			}
			return isHomogeneousFlexoConceptInstanceSelection;
		}
		return false;
	}

	public FlexoEditor getEditor() {
		return editor;
	}

	private FlexoAction<?, ?, ?> ownerAction;
	private final List<FlexoAction<?, ?, ?>> embeddedActions = new ArrayList<>();

	public FlexoAction<?, ?, ?> getOwnerAction() {
		return ownerAction;
	}

	protected void setOwnerAction(FlexoAction<?, ?, ?> ownerAction) {
		this.ownerAction = ownerAction;
	}

	public List<FlexoAction<?, ?, ?>> getEmbeddedActions() {
		return embeddedActions;
	}

	public void addToEmbeddedActions(FlexoAction<?, ?, ?> embeddedAction) {
		embeddedActions.add(embeddedAction);
	}

	protected void removeFromEmbeddedActions(FlexoAction<?, ?, ?> embeddedAction) {
		embeddedActions.remove(embeddedAction);
	}

	public boolean isEmbedded() {
		return getOwnerAction() != null;
	}

	private boolean forceExecuteConfirmationPanel = false;

	public boolean getForceExecuteConfirmationPanel() {
		return forceExecuteConfirmationPanel;
	}

	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel) {
		if (forceExecuteConfirmationPanel != this.forceExecuteConfirmationPanel) {
			this.forceExecuteConfirmationPanel = forceExecuteConfirmationPanel;
			getPropertyChangeSupport().firePropertyChange("forceExecuteConfirmationPanel", !forceExecuteConfirmationPanel,
					forceExecuteConfirmationPanel);
		}
	}

	public String toSimpleString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}

	@Override
	public String toString() {
		StringBuilder returned = new StringBuilder();
		returned.append("FlexoAction: ").append(getClass().getName()).append("[");
		returned.append("]");
		return returned.toString();
	}

	/**
	 * Hook that might be overriden in sub-classes while implementing dynamic reference replacement scheme
	 * 
	 * @param propertyKey
	 * @param newValue
	 * @param oldValue
	 *            TODO
	 * @param originalValue
	 *            TODO
	 */
	protected void replacedSinglePropertyValue(String propertyKey, Object newValue, Object oldValue, Object originalValue) {
	}

	/**
	 * Hook that might be overridden in sub-classes while implementing dynamic reference replacement scheme
	 * 
	 * @param propertyKey
	 * @param index
	 * @param newValue
	 * @param oldValue
	 *            TODO
	 * @param originalValue
	 *            TODO
	 */
	protected void replacedVectorPropertyValue(String propertyKey, int index, Object newValue, Object oldValue, Object originalValue) {
	}

	// TODO: Should be refactored with injectors
	public FlexoServiceManager getServiceManager() {
		if (getEditor() != null) {
			return getEditor().getServiceManager();
		}
		else if (getFocusedObject() != null) {
			return ((FlexoObject) getFocusedObject()).getServiceManager();
		}
		return null;
	}

	/**
	 * Return flag indicating if this action is valid to be executed (true if the parameters of action are well set)
	 * 
	 * @return
	 */
	public boolean isValid() {
		return true;
	}

	public FlexoActionCompoundEdit getCompoundEdit() {
		return compoundEdit;
	}

	public void setCompoundEdit(FlexoActionCompoundEdit compoundEdit) {
		this.compoundEdit = compoundEdit;
		compoundEdit.setAction(this);
	}

	public static LocalizedDelegate getDefaultLocales(FlexoServiceManager serviceManager) {
		if (serviceManager != null) {
			return serviceManager.getLocalizationService().getFlexoLocalizer();
		}
		return FlexoLocalization.getMainLocalizer();
	}

	public LocalizedDelegate getLocales() {
		if (this instanceof TechnologySpecificFlexoAction) {
			Class<? extends TechnologyAdapter> taClass = (Class<? extends TechnologyAdapter>) TypeUtils
					.getBaseClass(TypeUtils.getTypeArgument(getClass(), TechnologySpecificFlexoAction.class, 0));
			if (taClass != null) {
				TechnologyAdapter<?> ta = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(taClass);
				return ta.getLocales();
			}
		}
		if (getFocusedObject() instanceof TechnologyObject) {
			TechnologyAdapter<?> ta = ((TechnologyObject<?>) getFocusedObject()).getTechnologyAdapter();
			if (ta != null)
				return ta.getLocales();
		}
		return getDefaultLocales(getServiceManager());
	}

	public void performPostProcessings() {
		for (PostProcessing pp : postProcessings) {
			pp.run();
		}
	}

	private List<PostProcessing> postProcessings = new ArrayList<>();

	public void addToPostProcessing(PostProcessing postProcessing) {
		postProcessings.add(postProcessing);
	}

	public void removeFromPostProcessing(PostProcessing postProcessing) {
		postProcessings.remove(postProcessing);
	}

	public static interface PostProcessing extends Runnable {
		public FlexoAction<?, ?, ?> getAction();
	}

}
