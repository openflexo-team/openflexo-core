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

package org.openflexo.foundation.fml.rt.action;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.binding.DeclarationBindingVariable;
import org.openflexo.foundation.fml.binding.FlexoBehaviourBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRunTimeEngine;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ReturnException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.logging.FMLConsole.LogLevel;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.localization.LocalizedDelegate;

/**
 * Provides execution environment of a {@link FlexoBehaviour} on a given {@link FlexoConceptInstance} as a {@link FlexoAction}
 *
 * Abstract base implementation for a {@link FlexoAction} which aims at executing a {@link FlexoBehaviour}
 * 
 * An {@link FlexoBehaviourAction} represents the execution (in the "instances" world) of an {@link FlexoBehaviour}.<br>
 * To be used and executed on Openflexo platform, it is wrapped in a {@link FlexoAction}.<br>
 * 
 * @author sylvain
 * 
 * @param <A>
 *            type of {@link FlexoBehaviourAction} beeing executed
 * @param <FB>
 *            type of {@link FlexoBehaviour}
 * @param <O>
 *            type of {@link FlexoConceptInstance} on which this action applies
 */
public abstract class FlexoBehaviourAction<A extends FlexoBehaviourAction<A, FB, O>, FB extends FlexoBehaviour, O extends FlexoConceptInstance>
		extends FlexoAction<A, O, VirtualModelInstanceObject> implements RunTimeEvaluationContext {

	private static final Logger logger = Logger.getLogger(FlexoBehaviourAction.class.getPackage().getName());

	public static final String PARAMETER_VALUE_CHANGED = "parameterValueChanged";

	private FB flexoBehaviour;

	protected Hashtable<String, Object> variables;
	protected ParameterValues parameterValues;

	private MatchingSet defaultMatchingSet = null;

	protected Object returnedValue = null;

	public boolean escapeParameterRetrievingWhenValid = true;

	/**
	 * Constructor to be used with a factory
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	protected FlexoBehaviourAction(FlexoBehaviourActionFactory<A, FB, O> actionFactory, O focusedObject,
			List<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
		variables = new Hashtable<>();
		parameterValues = new ParameterValues();
	}

	/**
	 * Constructor to be used for creating a new action without factory
	 * 
	 * @param flexoBehaviour
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public FlexoBehaviourAction(FB flexoBehaviour, O focusedObject, List<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		this((FlexoBehaviourActionFactory<A, FB, O>) null, focusedObject, globalSelection, editor);
		this.flexoBehaviour = flexoBehaviour;
	}

	/**
	 * Constructor to be used for creating a new action as an action embedded in another one
	 * 
	 * @param flexoBehaviour
	 * @param focusedObject
	 * @param globalSelection
	 * @param ownerAction
	 *            Action in which action to be created will be embedded
	 */
	public FlexoBehaviourAction(FB flexoBehaviour, O focusedObject, List<VirtualModelInstanceObject> globalSelection,
			FlexoAction<?, ?, ?> ownerAction) {
		this(flexoBehaviour, focusedObject, globalSelection, ownerAction.getEditor());
		setOwnerAction(ownerAction);
		ownerAction.addToEmbeddedActions(this);
	}

	@Override
	public final ExpressionEvaluator getEvaluator() {
		return new FMLExpressionEvaluator(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public FlexoBehaviourActionFactory<A, FB, O> getActionFactory() {
		return (FlexoBehaviourActionFactory<A, FB, O>) super.getActionFactory();
	}

	@Override
	public final LocalizedDelegate getLocales() {
		if (getFlexoBehaviour() != null) {
			return getFlexoBehaviour().getLocales();
		}
		return super.getLocales();
	}

	@Override
	public String getLocalizedName() {
		if (getLocales() != null) {
			return getLocales().localizedForKey(getFlexoBehaviour().getName());
		}
		return super.getLocalizedName();
	}

	@Override
	public String getLocalizedDescription() {
		if (getLocales() != null) {
			return getLocales().localizedForKey(getFlexoBehaviour().getDescription());
		}
		return super.getLocalizedName();
	}

	public final FB getFlexoBehaviour() {
		if (getActionFactory() != null) {
			return getActionFactory().getBehaviour();
		}
		return flexoBehaviour;
	}

	/**
	 * Return the {@link FlexoConceptInstance} on which this {@link FlexoBehaviour} is applied.<br>
	 * 
	 * @return
	 */
	@Override
	public FlexoConceptInstance getFlexoConceptInstance() {
		if (getActionFactory() != null) {
			return getActionFactory().getFlexoConceptInstance();
		}
		return getFocusedObject();
	}

	/**
	 * Return the {@link VirtualModelInstance} on which we work.<br>
	 * If {@link FlexoConceptInstance} on which this {@link FlexoBehaviour} is applied (see {@link #getFlexoConceptInstance()} is a
	 * {@link VirtualModelInstance}, return it, otherwise return the owner {@link VirtualModelInstance}
	 */
	@Override
	public VirtualModelInstance<?, ?> getVirtualModelInstance() {
		return retrieveVirtualModelInstance();
	}

	private VirtualModelInstance<?, ?> retrieveVirtualModelInstance() {
		if (getFlexoConceptInstance() instanceof VirtualModelInstance) {
			return (VirtualModelInstance<?, ?>) getFlexoConceptInstance();
		}
		if (getFlexoConceptInstance() != null) {
			return getFlexoConceptInstance().getVirtualModelInstance();
		}
		return null;
	}

	@Override
	public FMLRunTimeEngine getFMLRunTimeEngine() {
		if (getEditor() != null) {
			return getEditor().getFMLRunTimeEngine();
		}
		return null;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (getFlexoBehaviour() == null) {
			return false;
		}
		return true;
	}

	/**
	 * Compute and store default parameters, and return a flag indicating if all parameters declared as "mandatory" could be successfully
	 * filled
	 * 
	 * @return
	 */
	// TODO: we must order this if dependancies are not resolved using basic sequence
	public boolean retrieveDefaultParameters() {
		boolean returned = true;
		FlexoBehaviour flexoBehaviour = getFlexoBehaviour();
		// logger.info("BEGIN retrieveDefaultParameters() for " + flexoBehaviour);
		for (final FlexoBehaviourParameter parameter : flexoBehaviour.getParameters()) {
			Object value = parameterValues.get(parameter.getArgumentName());
			if (value == null) {
				value = parameter.getDefaultValue(this);
				// logger.info("Parameter " + parameter.getName() + " default value = " + defaultValue);
				if (value != null) {
					parameterValues.put(parameter.getArgumentName(), value);
				}
			}
			/*if (parameter instanceof ListParameter) {
				List list = (List) ((ListParameter) parameter).getList(this);
				parameterListValues.put((ListParameter) parameter, list);
			}*/
			// logger.info("Parameter " + parameter.getName() + " valid=" + parameter.isValid(this, defaultValue));
			if (!parameter.isValid(this, value)) {
				// logger.info("Parameter " + parameter + " is not valid for value " + defaultValue);
				returned = false;
			}
		}
		// logger.info("END retrieveDefaultParameters() for " + flexoBehaviour);
		return returned;
	}

	/**
	 * Return a flag indicating if all parameters declared as "mandatory" have been set
	 * 
	 * @return
	 */
	public boolean areRequiredParametersSetAndValid() {
		FlexoBehaviour flexoBehaviour = getFlexoBehaviour();
		for (final FlexoBehaviourParameter parameter : flexoBehaviour.getParameters()) {
			if (!parameter.isValid(this, parameterValues.get(parameter.getName()))) {
				return false;
			}
		}
		return true;
	}

	public FlexoResourceCenter<?> getResourceCenter() {
		if (getFocusedObject() != null) {
			return ((VirtualModelInstanceObject) getFocusedObject()).getResourceCenter();
		}
		return null;
	}

	public FlexoConcept getFlexoConcept() {
		if (getFlexoBehaviour() != null) {
			return getFlexoBehaviour().getFlexoConcept();
		}
		return null;
	}

	/**
	 * Calling this method will register a new variable in the run-time context provided by this {@link FlexoBehaviourAction} instance in
	 * the context of its implementation of {@link RunTimeEvaluationContext}.<br>
	 * Variable is initialized with supplied name and value
	 * 
	 * @param variableName
	 * @param value
	 */
	@Override
	public void declareVariable(String variableName, Object value) {
		if (value != null) {
			variables.put(variableName, value);
		}
		else {
			variables.remove(variableName);
		}
	}

	/**
	 * Calling this method will dereference variable identified by supplied name
	 * 
	 * @param variableName
	 */
	@Override
	public void dereferenceVariable(String variableName) {
		variables.remove(variableName);
	}

	public Object getParameterValue(FlexoBehaviourParameter parameter) {
		/*System.out.println("On me demande la valeur du parametre " + parameter.getName() + " a priori c'est "
				+ parameterValues.get(parameter));*/
		/*if (parameter instanceof URIParameter) {
			if (parameterValues.get(parameter) == null
					|| parameterValues.get(parameter) instanceof String && StringUtils.isEmpty((String) parameterValues.get(parameter))) {
				return ((URIParameter) parameter).getDefaultValue(this);
			}
		}*/
		return parameterValues.get(parameter.getArgumentName());
	}

	public void setParameterValue(FlexoBehaviourParameter parameter, Object value) {
		// System.out.println("setParameterValue " + value + " for parameter " + parameter.getName());
		parameterValues.put(parameter.getArgumentName(), value);
		/*for (FlexoBehaviourParameter p : getEditionScheme().getParameters()) {
			if (p instanceof URIParameter) {
				// System.out.println("Hop, je recalcule l'uri, ici");
			}
		}*/
	}

	/**
	 * This is the internal code performing execution of the control graph of {@link FlexoBehaviour}
	 */
	protected void executeControlGraph() throws OperationCancelledException, FMLExecutionException {

		if (getApplicableFlexoBehaviour() != null && getApplicableFlexoBehaviour().getControlGraph() != null) {
			try {
				getApplicableFlexoBehaviour().getControlGraph().execute(this);
			} catch (ReturnException e) {
				returnedValue = e.getReturnedValue();
			} catch (FMLExecutionException e) {
				logger.warning("Unexpected exception while executing FML control graph: " + e);
				System.err.println(getApplicableFlexoBehaviour().getFMLPrettyPrint());
				e.printStackTrace();
				throw e;
			}
			if (defaultMatchingSet != null) {
				finalizeDefaultMatchingSet();
			}
		}

	}

	protected void compensateCancelledExecution() {
		logger.info("compensateCancelledExecution hook");
		Thread.dumpStack();
	}

	public Object getReturnedValue() {
		return returnedValue;
	}

	@Override
	public void logOut(String message, LogLevel logLevel) {
		if (getEditor() != null && getEditor().getFMLConsole() != null) {
			getEditor().getFMLConsole().log(message, logLevel, getFlexoConceptInstance(), getFlexoBehaviour());
		}
		else {
			System.out.println(message);
		}
	}

	@Override
	public void logErr(String message, LogLevel logLevel) {
		if (getEditor() != null && getEditor().getFMLConsole() != null) {
			getEditor().getFMLConsole().log(message, logLevel, getFlexoConceptInstance(), getFlexoBehaviour());
		}
		else {
			System.err.println(message);
		}
	}

	public MatchingSet initiateDefaultMatchingSet(MatchFlexoConceptInstance action) {
		if (defaultMatchingSet == null) {
			defaultMatchingSet = new MatchingSet(action, this);
		}
		return defaultMatchingSet;
	}

	public void finalizeDefaultMatchingSet() {
		if (defaultMatchingSet != null) {
			for (FlexoConceptInstance fci : new ArrayList<>(defaultMatchingSet.getUnmatchedInstances())) {
				fci.delete();
			}
		}
		defaultMatchingSet = null;
	}

	/**
	 * Override when required
	 * 
	 * @param action
	 */
	public <T> void hasPerformedAction(EditionAction action, T object) {
	}

	private FlexoConcept declaredConceptualLevel;

	/**
	 * Return declared conceptual level, if any
	 * 
	 * This is the {@link FlexoConcept} which should be considered for {@link FlexoBehaviour} lookup resolution.<br>
	 * When null (undefined), choose the declared {@link FlexoConcept} of considered {@link FlexoConceptInstance}
	 * 
	 * @return
	 */
	public FlexoConcept getDeclaredConceptualLevel() {
		return declaredConceptualLevel;
	}

	/**
	 * Sets declared conceptual level, if any
	 * 
	 * This is the {@link FlexoConcept} which should be considered for {@link FlexoBehaviour} lookup resolution.<br>
	 * When null (undefined), choose the declared {@link FlexoConcept} of considered {@link FlexoConceptInstance}
	 * 
	 * @param declaredConceptualLevel
	 */
	public void setDeclaredConceptualLevel(FlexoConcept declaredConceptualLevel) {
		this.declaredConceptualLevel = declaredConceptualLevel;
	}

	/**
	 * Implements FML dynamic binding
	 * 
	 * Return applicable FlexoBehaviour to execute according to the one beeing identified at compile-time.
	 * 
	 * We manage here containment by looking up container
	 * 
	 * @return
	 */
	public final FB getApplicableFlexoBehaviour() {
		return getApplicableFlexoBehaviour(
				getDeclaredConceptualLevel() == null ? getFlexoConceptInstance().getFlexoConcept() : getDeclaredConceptualLevel());
	}

	/**
	 * Internally used for {@link #getApplicableFlexoBehaviour()}
	 * 
	 * @param concept
	 * @return
	 */
	private FB getApplicableFlexoBehaviour(FlexoConcept concept) {
		// System.out.println("Looking " + getFlexoBehaviour().getSignature() + " in " + concept);
		FB returned = (FB) getFlexoBehaviour().getMostSpecializedBehaviour(concept);
		if (returned == null) {
			if (concept.getContainerFlexoConcept() != null) {
				return getApplicableFlexoBehaviour(concept.getContainerFlexoConcept());
			}
			else if (concept instanceof VirtualModel && ((VirtualModel) concept).getContainerVirtualModel() != null) {
				return getApplicableFlexoBehaviour(((VirtualModel) concept).getContainerVirtualModel());
			}
			else {
				return getApplicableFlexoBehaviour(concept.getOwner());
			}
		}
		else {
			// System.out.println("Found " + getFlexoBehaviour().getSignature() + " in " + concept);
			return returned;
		}
	}

	/**
	 * Implements FML dynamic binding
	 * 
	 * Return applicable FlexoConceptInstance to consider when executing applicable FlexoBehaviour
	 * 
	 * We manage here containment by looking up container
	 * 
	 * @return
	 */
	protected FlexoConceptInstance getApplicableFlexoConceptInstance() {
		return getApplicableFlexoConceptInstance(getFlexoConceptInstance());
	}

	/**
	 * Internally used for {@link #getApplicableFlexoConceptInstance()}
	 * 
	 * @param fci
	 * @return
	 */
	private FlexoConceptInstance getApplicableFlexoConceptInstance(FlexoConceptInstance fci) {
		if (fci == null) {
			return null;
		}
		FB applicablebehaviour = getApplicableFlexoBehaviour();
		if (applicablebehaviour.getFlexoConcept().isAssignableFrom(fci.getFlexoConcept())) {
			return fci;
		}
		else {
			if (fci.getFlexoConcept().getContainerFlexoConcept() != null) {
				return getApplicableFlexoConceptInstance(fci.getContainerFlexoConceptInstance());
			}
			else if (fci.getFlexoConcept() instanceof VirtualModel
					&& ((VirtualModel) fci.getFlexoConcept()).getContainerVirtualModel() != null) {
				return getApplicableFlexoConceptInstance(fci.getVirtualModelInstance().getContainerVirtualModelInstance());
			}
			else {
				return getApplicableFlexoConceptInstance(fci.getVirtualModelInstance());
			}
		}

	}

	@Override
	public Object getValue(BindingVariable variable) {

		if (variables.get(variable.getVariableName()) != null) {
			return variables.get(variable.getVariableName());
		}
		// TODO: I think code above should be replaced by that:
		/*if (variable instanceof DeclarationBindingVariable) {
			return variables.get(variable.getVariableName());
		}*/

		if (variable.getVariableName().equals(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY)) {
			return getParametersValues();
		}
		else if (variable.getVariableName().equals(FMLBindingFactory.FLEXO_CONCEPT_INSTANCE)) {
			return getFlexoConceptInstance();
		}
		else if (variable.getVariableName().equals(FMLBindingFactory.VIRTUAL_MODEL_INSTANCE)) {
			if (getFlexoConceptInstance() instanceof FMLRTVirtualModelInstance) {
				return getFlexoConceptInstance();
			}
			return getVirtualModelInstance();
		}

		// Not found at this level, delegate it to the FlexoConceptInstance
		if (getApplicableFlexoConceptInstance() != null) {
			return getApplicableFlexoConceptInstance().getValue(variable);
		}

		// Maybe to the FMLRTVirtualModelInstance ?
		if (getVirtualModelInstance() != null) {
			return getVirtualModelInstance().getValue(variable);
		}

		logger.warning("Unexpected variable requested in FlexoBehaviourAction: " + variable + " of " + variable.getClass());
		return null;

	}

	@Override
	public void setValue(Object value, BindingVariable variable) {

		if (variable instanceof DeclarationBindingVariable) {
			if (value != null) {
				variables.put(variable.getVariableName(), value);
			}
			else {
				variables.remove(variable.getVariableName());
			}
			return;
		}

		/*if (variables.get(variable.getVariableName()) != null) {
			variables.put(variable.getVariableName(), value);
			return;
		}*/
		else if (variable.getVariableName().equals(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY)) {
			logger.warning(
					"Forbidden write access " + FlexoBehaviourBindingModel.PARAMETERS_PROPERTY + " in " + this + " of " + getClass());
			return;
		}
		/*else if (variable.getVariableName().equals(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY)) {
			logger.warning("Forbidden write access " + FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY + " in " + this + " of "
					+ getClass());
			return;
		}*/

		if (getFlexoConceptInstance() != null) {
			getFlexoConceptInstance().setValue(value, variable);
			return;
		}

		if (getVirtualModelInstance() != null) {
			getVirtualModelInstance().setValue(value, variable);
			return;
		}

		logger.warning(
				"Unexpected variable requested in settable context in FlexoBehaviourAction: " + variable + " of " + variable.getClass());
		/*logger.warning("getFlexoConceptInstance()=" + getFlexoConceptInstance());
		logger.warning("getVirtualModelInstance()=" + getVirtualModelInstance());
		
		if (variable instanceof FlexoPropertyBindingVariable) {
			logger.warning("coucou la property " + ((FlexoPropertyBindingVariable) variable).getFlexoProperty() + " dans "
					+ ((FlexoPropertyBindingVariable) variable).getFlexoProperty().getFlexoConcept());
		}*/
	}

	public ParameterValues getParametersValues() {
		return parameterValues;
	}

	public boolean parameterValueChanged() {
		setChanged();
		notifyObservers(new DataModification<>(PARAMETER_VALUE_CHANGED, null, getParametersValues()));
		return true;
	}

	public class ParameterValues extends Hashtable<String, Object> {

		@Override
		public synchronized Object get(Object key) {
			if (!(key instanceof String)) {
				System.out.println("Unexpected key : " + key);
				Thread.dumpStack();
			}
			return super.get(key);
		}

		@Override
		public synchronized Object put(String name, Object value) {
			if (value == null) {
				return null;
			}

			Object returned = super.put(name, value);
			/*for (FlexoBehaviourParameter p : parameter.getFlexoBehaviour().getParameters()) {
				if (p != parameter && p instanceof URIParameter && ((URIParameter) p).getModelSlot() instanceof TypeAwareModelSlot) {
					URIParameter uriParam = (URIParameter) p;
					TypeAwareModelSlot modelSlot = uriParam.getModelSlot();
					String newURI;
					try {
						newURI = uriParam.getBaseURI().getBindingValue(FlexoBehaviourAction.this);
			
						newURI = modelSlot.generateUniqueURIName(
								(TypeAwareModelSlotInstance) getVirtualModelInstance().getModelSlotInstance(modelSlot), newURI);
						logger.info("Generated new URI " + newURI + " for " + getVirtualModelInstance().getModelSlotInstance(modelSlot));
						// NPE Protection
						if (newURI != null) {
							super.put(uriParam, newURI);
						}
					} catch (TypeMismatchException e) {
						e.printStackTrace();
					} catch (NullReferenceException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}*/
			parameterValueChanged();
			return returned;
		}
	}

}
