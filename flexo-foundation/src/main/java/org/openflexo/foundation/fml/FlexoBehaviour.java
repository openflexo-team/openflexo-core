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

package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.FlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.binding.FlexoBehaviourBindingModel;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphConverter;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Finder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.ChainedCollection;
import org.openflexo.toolbox.StringUtils;

/**
 * An FlexoBehaviour represents a behavioural feature attached to an FlexoConcept
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoBehaviour.FlexoBehaviourImpl.class)
@Imports({ @Import(ActionScheme.class), @Import(DeletionScheme.class), @Import(NavigationScheme.class),
		@Import(SynchronizationScheme.class), @Import(CreationScheme.class), @Import(CloningScheme.class) })
public interface FlexoBehaviour extends FlexoBehaviourObject, ActionContainer, Function, FMLControlGraphOwner {

	// public static final String FLEXO_BEHAVIOUR_INSTANCE = "flexoBehaviourInstance";
	// public static final String VIRTUAL_MODEL_INSTANCE = "virtualModelInstance";

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	public static final String LABEL_KEY = "label";
	@PropertyIdentifier(type = boolean.class)
	public static final String SKIP_CONFIRMATION_PANEL_KEY = "skipConfirmationPanel";
	@PropertyIdentifier(type = boolean.class)
	public static final String DEFINE_POPUP_DEFAULT_SIZE_KEY = "definePopupDefaultSize";
	@PropertyIdentifier(type = int.class)
	public static final String WIDTH_KEY = "width";
	@PropertyIdentifier(type = int.class)
	public static final String HEIGHT_KEY = "height";
	@PropertyIdentifier(type = String.class)
	public static final String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = Vector.class)
	public static final String PARAMETERS_KEY = "parameters";

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";

	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	@XMLElement(context = "BehaviourControlGraph_")
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY, inverse = FlexoConcept.FLEXO_BEHAVIOURS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Override
	public Type getReturnType();

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name);

	@Getter(value = LABEL_KEY)
	@XMLAttribute
	public String getLabel();

	@Setter(LABEL_KEY)
	public void setLabel(String label);

	@Getter(value = SKIP_CONFIRMATION_PANEL_KEY, defaultValue = "true")
	@XMLAttribute
	public boolean getSkipConfirmationPanel();

	@Setter(SKIP_CONFIRMATION_PANEL_KEY)
	public void setSkipConfirmationPanel(boolean skipConfirmationPanel);

	@Getter(value = DEFINE_POPUP_DEFAULT_SIZE_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getDefinePopupDefaultSize();

	@Setter(DEFINE_POPUP_DEFAULT_SIZE_KEY)
	public void setDefinePopupDefaultSize(boolean definePopupDefaultSize);

	@Getter(value = WIDTH_KEY, defaultValue = "0")
	@XMLAttribute
	public int getWidth();

	@Setter(WIDTH_KEY)
	public void setWidth(int width);

	@Getter(value = HEIGHT_KEY, defaultValue = "0")
	@XMLAttribute
	public int getHeight();

	@Setter(HEIGHT_KEY)
	public void setHeight(int height);

	/*@Override
	@Getter(value = DESCRIPTION_KEY)
	@XMLElement
	public String getDescription();
	
	@Override
	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);*/

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = FlexoBehaviourParameter.FLEXO_BEHAVIOUR_KEY)
	@Embedded
	@XMLElement
	public List<FlexoBehaviourParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<FlexoBehaviourParameter> parameters);

	@Adder(PARAMETERS_KEY)
	@PastingPoint
	public void addToParameters(FlexoBehaviourParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(FlexoBehaviourParameter aParameter);

	@Finder(collection = PARAMETERS_KEY, attribute = FlexoBehaviourParameter.NAME_KEY)
	public FlexoBehaviourParameter getParameter(String name);

	public FlexoBehaviourType getFlexoBehaviourType();

	public FlexoBehaviourActionType getFlexoBehaviourActionType();

	public FlexoBehaviourParametersType getFlexoBehaviourParametersType();

	public FlexoBehaviourParametersValuesType getFlexoBehaviourParametersValuesType();

	public String getSignature();

	@Override
	public List<FlexoBehaviourParameter> getArguments();

	public String getAvailableParameterName(String baseName);

	public void parameterFirst(FlexoBehaviourParameter p);

	public void parameterUp(FlexoBehaviourParameter p);

	public void parameterDown(FlexoBehaviourParameter p);

	public void parameterLast(FlexoBehaviourParameter p);

	@Override
	public FlexoBehaviourBindingModel getBindingModel();

	public static abstract class FlexoBehaviourImpl extends FlexoBehaviourObjectImpl implements FlexoBehaviour {

		protected FlexoBehaviourBindingModel bindingModel;

		// protected BindingModel _bindingModel;

		protected static final Logger logger = FlexoLogger.getLogger(FlexoBehaviour.class.getPackage().getName());

		private String name;
		private String label;

		// private boolean definePopupDefaultSize = false;
		private int width = 800;
		private int height = 600;

		private final FlexoBehaviourType flexoBehaviourType = new FlexoBehaviourType(this);
		private final FlexoBehaviourActionType flexoBehaviourActionType = new FlexoBehaviourActionType(this);
		private final FlexoBehaviourParametersType flexoBehaviourParametersType = new FlexoBehaviourParametersType(this);
		private final FlexoBehaviourParametersValuesType flexoBehaviourParametersValuesType = new FlexoBehaviourParametersValuesType(this);

		/**
		 * Stores a chained collections of objects which are involved in validation
		 */
		private final ChainedCollection<FMLObject> validableObjects = null;

		@Override
		public Type getReturnType() {
			if (getControlGraph() != null) {
				return getControlGraph().getInferedType();
			}
			return Void.TYPE;
		}

		@Override
		public FlexoBehaviourType getFlexoBehaviourType() {
			return flexoBehaviourType;
		}

		@Override
		public FlexoBehaviourActionType getFlexoBehaviourActionType() {
			return flexoBehaviourActionType;
		}

		@Override
		public FlexoBehaviourParametersType getFlexoBehaviourParametersType() {
			return flexoBehaviourParametersType;
		}

		@Override
		public FlexoBehaviourParametersValuesType getFlexoBehaviourParametersValuesType() {
			return flexoBehaviourParametersValuesType;
		}

		@Override
		public String getStringRepresentation() {
			return (getFlexoConcept() != null ? getFlexoConcept().getStringRepresentation() : "null") + "." + getName();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName() + " " + getName() + "("
					+ getParametersFMLRepresentation(context) + ") {", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			if (getControlGraph() != null) {
				out.append(getControlGraph().getFMLRepresentation(context), context, 1);
			}
			out.append(StringUtils.LINE_SEPARATOR, context);
			out.append("}", context);
			out.append(StringUtils.LINE_SEPARATOR, context);
			return out.toString();
		}

		protected String getTechnologyAdapterIdentifier() {
			return "FML";
		}

		protected String getParametersFMLRepresentation(FMLRepresentationContext context) {
			if (getParameters().size() > 0) {
				StringBuffer sb = new StringBuffer();
				boolean isFirst = true;
				for (FlexoBehaviourParameter p : getParameters()) {
					sb.append((isFirst ? "" : ", ") + TypeUtils.simpleRepresentation(p.getType()) + " " + p.getName());
					isFirst = false;
				}
				return sb.toString();
			}
			return "";
		}

		/**
		 * Return the URI of the {@link NamedFMLObject}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getURI() + "." + getName();
			}
			return "null." + getName();
		}

		@Override
		public FlexoBehaviourImpl getFlexoBehaviour() {
			return this;
		}

		@Override
		public String getLabel() {
			if (label == null || StringUtils.isEmpty(label) || label.equals(name)) {
				return getName();
			}
			return label;
		}

		@Override
		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public void actionFirst(EditionAction a) {
			getActions().remove(a);
			getActions().add(0, a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void actionUp(EditionAction a) {
			int index = getActions().indexOf(a);
			if (index > 0) {
				getActions().remove(a);
				getActions().add(index - 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionDown(EditionAction a) {
			int index = getActions().indexOf(a);
			if (index > -1) {
				getActions().remove(a);
				getActions().add(index + 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionLast(EditionAction a) {
			getActions().remove(a);
			getActions().add(a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void setParameters(List<FlexoBehaviourParameter> someParameters) {
			String oldSignature = getSignature();
			performSuperSetter(PARAMETERS_KEY, someParameters);
			updateSignature(oldSignature);
		}

		@Override
		public void addToParameters(FlexoBehaviourParameter parameter) {
			String oldSignature = getSignature();
			performSuperAdder(PARAMETERS_KEY, parameter);
			updateSignature(oldSignature);
		}

		@Override
		public void removeFromParameters(FlexoBehaviourParameter parameter) {
			String oldSignature = getSignature();
			performSuperRemover(PARAMETERS_KEY, parameter);
			updateSignature(oldSignature);
		}

		@Override
		public void parameterFirst(FlexoBehaviourParameter p) {
			String oldSignature = getSignature();
			getParameters().remove(p);
			getParameters().add(0, p);
			setChanged();
			notifyObservers(new DataModification("parameters", null, getParameters()));
			updateSignature(oldSignature);
		}

		@Override
		public void parameterUp(FlexoBehaviourParameter p) {
			int index = getParameters().indexOf(p);
			if (index > 0) {
				String oldSignature = getSignature();
				getParameters().remove(p);
				getParameters().add(index - 1, p);
				setChanged();
				notifyObservers(new DataModification("parameters", null, getParameters()));
				updateSignature(oldSignature);
			}
		}

		@Override
		public void parameterDown(FlexoBehaviourParameter p) {
			int index = getParameters().indexOf(p);
			if (index > -1) {
				String oldSignature = getSignature();
				getParameters().remove(p);
				getParameters().add(index + 1, p);
				setChanged();
				notifyObservers(new DataModification("parameters", null, getParameters()));
				updateSignature(oldSignature);
			}
		}

		@Override
		public void parameterLast(FlexoBehaviourParameter p) {
			String oldSignature = getSignature();
			getParameters().remove(p);
			getParameters().add(p);
			setChanged();
			notifyObservers(new DataModification("parameters", null, getParameters()));
			updateSignature(oldSignature);
		}

		/*public FlexoBehaviourParameter getParameter(String name) {
			if (name == null) {
				return null;
			}
			for (FlexoBehaviourParameter p : parameters) {
				if (name.equals(p.getName())) {
					return p;
				}
			}
			return null;
		}*/

		/*@Override
		public VirtualModel getVirtualModel() {
			if (getFlexoConcept() != null && getFlexoConcept().getVirtualModel() != null) {
				return getFlexoConcept().getVirtualModel();
			}
			if (getFlexoConcept() instanceof VirtualModel) {
				return (VirtualModel) getFlexoConcept();
			}
			return null;
		}*/

		/**
		 * Creates a new {@link EditionAction} of supplied class, and add it at the end of action list<br>
		 * Delegates creation to model slot
		 * 
		 * @return newly created {@link EditionAction}
		 */
		@Override
		public <A extends TechnologySpecificAction<?, ?>> A createAction(Class<A> actionClass, ModelSlot<?> modelSlot) {
			A newAction = modelSlot.createAction(actionClass);
			addToActions(newAction);
			return newAction;
		}

		@Override
		public EditionAction deleteAction(EditionAction anAction) {
			removeFromActions(anAction);
			anAction.delete();
			return anAction;
		}

		@Override
		public void finalizeDeserialization() {
			// Convert all references to former FlexoBehaviourParameter model to generic FlexoBehaviourParameters
			// TODO: remove this code once all deprecated FlexoBehaviourParameter subinterfaces will be removed
			if (getDeserializationFactory() != null) {
				for (FlexoBehaviourParameter p : new ArrayList<>(getParameters())) {
					if (!(p instanceof GenericBehaviourParameter)) {
						FMLModelFactory factory = getDeserializationFactory();
						GenericBehaviourParameter newParameter = factory.newParameter(getFlexoBehaviour());
						newParameter.setName(p.getName());
						newParameter.setType(p.getType());
						if (p instanceof FlexoConceptInstanceParameter) {
							FMLTechnologyAdapter ta = factory.getServiceManager().getTechnologyAdapterService()
									.getTechnologyAdapter(FMLTechnologyAdapter.class);
							FlexoConceptInstanceTypeFactory fciFactory = ta.getFlexoConceptInstanceTypeFactory();
							newParameter.setType(fciFactory.makeCustomType(((FlexoConceptInstanceParameter) p)._getFlexoConceptTypeURI()));
						}
						newParameter.setWidget(p.getWidget());
						newParameter.setContainer(p.getContainer());
						newParameter.setDefaultValue(p.getDefaultValue());
						newParameter.setList(p.getList());
						newParameter.setIsRequired(p.getIsRequired());
						newParameter.setDescription(p.getDescription());
						logger.info("Converted former parameter " + p + " into " + newParameter + " with " + newParameter.getType());
						removeFromParameters(p);
						addToParameters(newParameter);
					}
				}
			}

			super.finalizeDeserialization();

		}

		/**
		 * Return the FlexoBehaviour's specific {@link BindingModel}.<br>
		 * This method might be overriden for specific BindinModel management.
		 */
		protected FlexoBehaviourBindingModel makeBindingModel() {
			return new FlexoBehaviourBindingModel(this);
		}

		/**
		 * Return the FlexoBehaviour's specific {@link BindingModel}. Creates it when required.
		 */
		@Override
		public final FlexoBehaviourBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = makeBindingModel();
				// appendContextualBindingVariables(bindingModel);
			}
			return bindingModel;
			/*if (isRebuildingBindingModel) {
				return _bindingModel;
			}
			if (_bindingModel == null) {
				createBindingModel();
			}
			return _bindingModel;*/
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			if (controlGraph == getControlGraph()) {
				return getBindingModel();
			}
			return null;
		}

		@Override
		public void reduce() {
			if (getControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph()).reduce();
			}
		}

		/*@Override
		public BindingModel getInferedBindingModel() {
			return getBindingModel();
		}*/

		/*@Override
		public void updateBindingModels() {
			if (isDeserializing()) {
				return;
			}
			logger.fine("updateBindingModels()");
			_bindingModel = null;
			createBindingModel();
			getPropertyChangeSupport().firePropertyChange(BindingModelChanged.BINDING_MODEL_CHANGED, null, _bindingModel);
			rebuildActionsBindingModel();
			recursivelyUpdateInferedBindingModels(this);
		}*/

		/*private void recursivelyUpdateInferedBindingModels(ActionContainer container) {
			for (EditionAction action : container.getActions()) {
				action.rebuildInferedBindingModel();
				if (action instanceof ActionContainer) {
					recursivelyUpdateInferedBindingModels((ActionContainer) action);
				}
			}
		}*/

		/*protected void rebuildActionsBindingModel() {
			for (EditionAction action : getActions()) {
				action.rebuildInferedBindingModel();
			}
		}*/

		// private boolean isRebuildingBindingModel = false;

		/*private final void createBindingModel() {
			_bindingModel = new BindingModel();
			isRebuildingBindingModel = true;
			_bindingModel.addToBindingVariables(new BindingVariable("parameters", getFlexoBehaviourParametersValuesType()));
			_bindingModel.addToBindingVariables(new BindingVariable("parametersDefinitions", getFlexoBehaviourParametersType()));
			// _bindingModel.addToBindingVariables(new EditionSchemeParametersBindingVariable(this));
			// _bindingModel.addToBindingVariables(new EditionSchemeParameterListPathElement(this, null));
			appendContextualBindingVariables(_bindingModel);
			if (getFlexoConcept() != null) {
				for (final FlexoRole property : getFlexoConcept().getFlexoRoles()) {
					_bindingModel.addToBindingVariables(new FlexoPropertyBindingVariable(property));
				}
			}
			for (final EditionAction a : getActions()) {
				if (a instanceof AssignableAction && ((AssignableAction) a).getIsVariableDeclaration()) {
					_bindingModel.addToBindingVariables(new BindingVariable(((AssignableAction) a).getVariableName(),
							((AssignableAction) a).getAssignableType(), true) {
						@Override
						public Type getType() {
							return ((AssignableAction) a).getAssignableType();
						}
					});
				}
			}
			// notifyBindingModelChanged();
			isRebuildingBindingModel = false;
		}*/

		/*protected void appendContextualBindingVariables(BindingModel bindingModel) {
			// Si flexo concept est un diagram spec alors rajouter la varialble diagram
			// AprÃ¨s faudra voir au runtime;
			if (getFlexoConcept() != null) {
				if (getFlexoConcept() instanceof VirtualModel) {
					bindingModel.addToBindingVariables(new BindingVariable(FlexoBehaviour.VIRTUAL_MODEL_INSTANCE, FlexoConceptInstanceType
							.getFlexoConceptInstanceType(getFlexoConcept())));
				} else {
					bindingModel.addToBindingVariables(new BindingVariable(FlexoBehaviour.FLEXO_BEHAVIOUR_INSTANCE,
							FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConcept())));
					bindingModel.addToBindingVariables(new BindingVariable(FlexoBehaviour.VIRTUAL_MODEL_INSTANCE, FlexoConceptInstanceType
							.getFlexoConceptInstanceType(getFlexoConcept().getVirtualModel())));
				}
		
			if (getFlexoConcept().getVirtualModel() instanceof DiagramSpecification) {
				bindingModel.addToBindingVariables(new BindingVariable(DiagramEditionScheme.DIAGRAM, FlexoConceptInstanceType
						.getFlexoConceptInstanceType(getFlexoConcept().getVirtualModel())));
			} 
			if(getFlexoConcept() instanceof DiagramSpecification){
				bindingModel.addToBindingVariables(new BindingVariable(DiagramEditionScheme.DIAGRAM, FlexoConceptInstanceType
						.getFlexoConceptInstanceType(getFlexoConcept())));
				bindingModel.addToBindingVariables(new BindingVariable(DiagramEditionScheme.TOP_LEVEL, DiagramRootPane.class));
			}
			else {
				bindingModel.addToBindingVariables(new BindingVariable(FlexoBehaviour.VIRTUAL_MODEL_INSTANCE, FlexoConceptInstanceType
						.getFlexoConceptInstanceType(getFlexoConcept().getVirtualModel())));
			}
			// }
			// if (this instanceof DiagramEditionScheme) {
			if (getFlexoConcept() != null && getFlexoConcept().getVirtualModel() instanceof DiagramSpecification) {
				bindingModel.addToBindingVariables(new BindingVariable(DiagramEditionScheme.TOP_LEVEL, DiagramRootPane.class));
			}
		}*/

		@Override
		public void variableAdded(AssignableAction action) {
			// updateBindingModels();
		}

		/*@Override
		public boolean getDefinePopupDefaultSize() {
			return definePopupDefaultSize;
		}
		
		@Override
		public void setDefinePopupDefaultSize(boolean definePopupDefaultSize) {
			this.definePopupDefaultSize = definePopupDefaultSize;
		}*/

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public void setWidth(int width) {
			this.width = width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public void setHeight(int height) {
			this.height = height;
		}

		@Override
		public List<FlexoBehaviourParameter> getArguments() {
			return getParameters();
		}

		@Override
		public void setName(String name) {
			String oldSignature = getSignature();
			super.setName(name);
			updateSignature(oldSignature);
		}

		protected void updateSignature(String oldSignature) {
			behaviourSignature = null;
			getPropertyChangeSupport().firePropertyChange("signature", oldSignature, getSignature());
		}

		private String behaviourSignature = null;

		@Override
		public String getSignature() {
			if (behaviourSignature == null) {
				StringBuffer signature = new StringBuffer();
				signature.append(getName());
				signature.append("(");
				signature.append(getParameterListAsString(false));
				signature.append(")");
				behaviourSignature = signature.toString();
			}
			return behaviourSignature;
		}

		private String getParameterListAsString(boolean fullyQualified) {
			StringBuffer returned = new StringBuffer();
			boolean isFirst = true;
			for (FlexoBehaviourParameter param : getParameters()) {
				// returned.append((isFirst ? "" : ",") + (fullyQualified ? TypeUtils.fullQualifiedRepresentation(param.getType())
				// : TypeUtils.simpleRepresentation(param.getType())));
				returned.append((isFirst ? "" : ",") + param.getName());
				isFirst = false;
			}
			return returned.toString();
		}

		@Override
		public String getAvailableParameterName(String baseName) {
			String testName = baseName;
			int index = 2;
			while (getParameter(testName) != null) {
				testName = baseName + index;
				index++;
			}
			return testName;
		}

		@Deprecated
		@Override
		public void addToActions(EditionAction anAction) {
			/*FMLControlGraph controlGraph = getControlGraph();
			if (controlGraph == null) {
				// If control graph is null, action will be new new control graph
				setControlGraph(anAction);
			} else {
				// Otherwise, sequentially append action
				controlGraph.sequentiallyAppend(anAction);
			}
			// performSuperAdder(ACTIONS_KEY, anAction);*/
			FMLControlGraphConverter.addToActions(this, null, anAction);
		}

		@Deprecated
		@Override
		public void removeFromActions(EditionAction anAction) {
			FMLControlGraphConverter.removeFromActions(this, null, anAction);
			// anAction.delete();
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			return getControlGraph();
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {
			setControlGraph(controlGraph);
		}

	}
}
