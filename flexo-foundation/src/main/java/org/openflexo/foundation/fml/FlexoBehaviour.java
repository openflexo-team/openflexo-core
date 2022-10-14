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
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.Function;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.binding.FlexoBehaviourBindingModel;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.md.ListMetaData;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * An FlexoBehaviour represents a behavioural feature attached to an FlexoConcept
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoBehaviour.FlexoBehaviourImpl.class)
@Imports({ @Import(ActionScheme.class), @Import(DeletionScheme.class), @Import(NavigationScheme.class),
		@Import(SynchronizationScheme.class), @Import(CreationScheme.class), @Import(CloningScheme.class), @Import(EventListener.class) })
public interface FlexoBehaviour extends FlexoBehaviourObject, Function, FMLControlGraphOwner, FMLPrettyPrintable {

	@PropertyIdentifier(type = FlexoConcept.class)
	public static final String FLEXO_CONCEPT_KEY = "flexoConcept";
	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	// @PropertyIdentifier(type = String.class)
	// public static final String LABEL_KEY = "label";
	@PropertyIdentifier(type = Visibility.class)
	public static final String VISIBILITY_KEY = "visibility";
	@PropertyIdentifier(type = boolean.class)
	public static final String SKIP_CONFIRMATION_PANEL_KEY = "skipConfirmationPanel";
	@PropertyIdentifier(type = boolean.class)
	public static final String LONG_RUNNING_ACTION_KEY = "longRunningAction";
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
	@PropertyIdentifier(type = DataBinding.class)
	public static final String STEPS_NUMBER_KEY = "stepsNumber";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";
	@PropertyIdentifier(type = boolean.class)
	public static final String IS_ABSTRACT_KEY = "isAbstract";
	@PropertyIdentifier(type = Type.class)
	public static final String DECLARED_TYPE_KEY = "declaredType";

	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "BehaviourControlGraph_")
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	@Getter(value = IS_ABSTRACT_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean isAbstract();

	@Setter(IS_ABSTRACT_KEY)
	public void setAbstract(boolean isAbstract);

	@Override
	@Getter(value = FLEXO_CONCEPT_KEY, inverse = FlexoConcept.FLEXO_BEHAVIOURS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConcept getFlexoConcept();

	@Setter(FLEXO_CONCEPT_KEY)
	public void setFlexoConcept(FlexoConcept flexoConcept);

	@Getter(value = DECLARED_TYPE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Type getDeclaredType();

	@Setter(DECLARED_TYPE_KEY)
	public void setDeclaredType(Type type);

	/**
	 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Updater(DECLARED_TYPE_KEY)
	public void updateDeclaredType(Type type);

	@Override
	public Type getReturnType();

	public Type getAnalyzedReturnType();

	@Override
	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Override
	@Setter(NAME_KEY)
	public void setName(String name) throws InvalidNameException;

	/*@Getter(value = LABEL_KEY)
	@XMLAttribute
	public String getLabel();
	
	@Setter(LABEL_KEY)
	public void setLabel(String label);*/

	@Getter(value = VISIBILITY_KEY, defaultValue = "Default")
	@XMLAttribute
	public Visibility getVisibility();

	@Setter(VISIBILITY_KEY)
	public void setVisibility(Visibility visibility);

	@Getter(value = SKIP_CONFIRMATION_PANEL_KEY, defaultValue = "true")
	@XMLAttribute
	@FMLAttribute(SKIP_CONFIRMATION_PANEL_KEY)
	public boolean getSkipConfirmationPanel();

	@Setter(SKIP_CONFIRMATION_PANEL_KEY)
	public void setSkipConfirmationPanel(boolean skipConfirmationPanel);

	@Getter(value = LONG_RUNNING_ACTION_KEY, defaultValue = "false")
	@XMLAttribute
	@Deprecated
	public boolean getLongRunningAction();

	@Setter(LONG_RUNNING_ACTION_KEY)
	@Deprecated
	public void setLongRunningAction(boolean isLongRunningAction);

	@Getter(value = STEPS_NUMBER_KEY)
	@XMLAttribute
	@Deprecated
	public DataBinding<Integer> getStepsNumber();

	@Setter(STEPS_NUMBER_KEY)
	@Deprecated
	public void setStepsNumber(DataBinding<Integer> stepsNumber);

	@Getter(value = DEFINE_POPUP_DEFAULT_SIZE_KEY, defaultValue = "false")
	@XMLAttribute
	@Deprecated
	public boolean getDefinePopupDefaultSize();

	@Setter(DEFINE_POPUP_DEFAULT_SIZE_KEY)
	@Deprecated
	public void setDefinePopupDefaultSize(boolean definePopupDefaultSize);

	@Getter(value = WIDTH_KEY, defaultValue = "0")
	@XMLAttribute
	@Deprecated
	public int getWidth();

	@Setter(WIDTH_KEY)
	@Deprecated
	public void setWidth(int width);

	@Getter(value = HEIGHT_KEY, defaultValue = "0")
	@XMLAttribute
	@Deprecated
	public int getHeight();

	@Setter(HEIGHT_KEY)
	@Deprecated
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
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoBehaviourParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<FlexoBehaviourParameter> parameters);

	@Adder(PARAMETERS_KEY)
	@PastingPoint
	public void addToParameters(FlexoBehaviourParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(FlexoBehaviourParameter aParameter);

	@Reindexer(PARAMETERS_KEY)
	public void moveParameterToIndex(FlexoBehaviourParameter aParameter, int index);

	@Finder(collection = PARAMETERS_KEY, attribute = FlexoBehaviourParameter.NAME_KEY)
	public FlexoBehaviourParameter getParameter(String name);

	public FlexoBehaviourType getFlexoBehaviourType();

	public FlexoBehaviourActionType getFlexoBehaviourActionType();

	public FlexoBehaviourParametersType getFlexoBehaviourParametersType();

	public FlexoBehaviourParametersValuesType getFlexoBehaviourParametersValuesType();

	public String getSignature();

	public Type[] getParameterTypes();

	@Override
	public List<FlexoBehaviourParameter> getArguments();

	public String getAvailableParameterName(String baseName);

	public void parameterFirst(FlexoBehaviourParameter p);

	public void parameterUp(FlexoBehaviourParameter p);

	public void parameterDown(FlexoBehaviourParameter p);

	public void parameterLast(FlexoBehaviourParameter p);

	@Override
	public FlexoBehaviourBindingModel getBindingModel();

	/**
	 * Return flag indicating if this behaviour overrides supplied behaviour<br>
	 * Return true if and only if name and signature equals, and if both declared concepts are not the same and if there are directely
	 * connected without any intermediate implementation
	 * 
	 * @param behaviour
	 * @return
	 */
	public boolean overrides(FlexoBehaviour behaviour);

	/**
	 * Return flag indicating if this behaviour is overriden in supplied context
	 * 
	 * @param context
	 * @return
	 */
	public boolean isOverridenInContext(FlexoConcept context);

	/**
	 * Return the most specialized behaviour to execute matching supplied behaviour name and signature<br>
	 * (dynamic binding: can be only supplied behaviour or an other behaviour overriding supplied behaviour)
	 * 
	 * @param behaviour
	 * @param context
	 * @return
	 */
	public FlexoBehaviour getMostSpecializedBehaviour(FlexoConcept context);

	/**
	 * Return a flag indicating if this behaviour support custom definition of parameters<br>
	 * Default implementation is to return true
	 * 
	 * @return
	 */
	public boolean supportParameters();

	/**
	 * Return boolean indicating if this property overrides at least one property
	 * 
	 * @return
	 */
	public boolean overrides();

	public ListMetaData getUIMetaData(boolean ensureExistence);

	public MultiValuedMetaData getMetaDataForParameter(FlexoBehaviourParameter parameter, boolean ensureExistence);

	/**
	 * Return the URI of the {@link NamedFMLObject}<br>
	 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<behaviour_name> <br>
	 * eg<br>
	 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyBehaviour
	 * 
	 * @return String representing unique URI of this object
	 */
	public String getURI();

	public static abstract class FlexoBehaviourImpl extends FlexoBehaviourObjectImpl implements FlexoBehaviour {

		protected FlexoBehaviourBindingModel bindingModel;

		protected static final Logger logger = FlexoLogger.getLogger(FlexoBehaviour.class.getPackage().getName());

		private int width = 800;
		private int height = 600;

		private final FlexoBehaviourType flexoBehaviourType = new FlexoBehaviourType(this);
		private final FlexoBehaviourActionType flexoBehaviourActionType = new FlexoBehaviourActionType(this);
		private final FlexoBehaviourParametersType flexoBehaviourParametersType = new FlexoBehaviourParametersType(this);
		private final FlexoBehaviourParametersValuesType flexoBehaviourParametersValuesType = new FlexoBehaviourParametersValuesType(this);

		private DataBinding<Integer> stepsNumber;

		/**
		 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
		 * 
		 * This updater is called during updateWith() processing (generally applied during the FML parsing phases)
		 * 
		 * @param type
		 */
		@Override
		public void updateDeclaredType(Type type) {

			if (getDeclaringCompilationUnit() != null && type instanceof CustomType) {
				setDeclaredType(((CustomType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setDeclaredType(type);
			}
		}

		@Override
		public Type getDeclaredType() {
			Type returned = (Type) performSuperGetter(DECLARED_TYPE_KEY);
			if (returned == null) {
				if (getAnalyzedReturnType() instanceof Class) {
					return TypeUtils.toPrimitive((Class)getAnalyzedReturnType());
				}
				return getAnalyzedReturnType();
			}
			return returned;
		}

		@Override
		public Type getReturnType() {
			if (getDeclaredType() != null) {
				return getDeclaredType();
			}
			if (getControlGraph() != null) {
				return getAnalyzedReturnType();
			}
			return Void.TYPE;
		}

		@Override
		public Type getAnalyzedReturnType() {
			if (getControlGraph() != null) {
				return getControlGraph().getInferedType();
			}
			return null;
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

		protected String getTechnologyAdapterIdentifier() {
			return "FML";
		}

		protected String getParametersFMLRepresentation() {
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
				return getFlexoConcept().getURI() + "." + getSignature();
			}
			return "null." + getName();
		}

		@Override
		public FlexoBehaviourImpl getFlexoBehaviour() {
			return this;
		}

		@Override
		public ListMetaData getUIMetaData(boolean ensureExistence) {
			ListMetaData returned = getListMetaData("UI");
			if (returned == null && ensureExistence && getFMLModelFactory() != null) {
				returned = getFMLModelFactory().newListMetaData("UI");
				addToMetaData(returned);
			}
			return returned;
		}

		@Override
		public MultiValuedMetaData getMetaDataForParameter(FlexoBehaviourParameter parameter, boolean ensureExistence) {
			ListMetaData md = getUIMetaData(ensureExistence);
			if (md != null) {
				List<FMLMetaData> allMetaData = md.getMultipleMetaData(FlexoBehaviourParameterImpl.AVAILABLE_ANNOTATIONS);
				for (FMLMetaData metaData : allMetaData) {
					if (metaData instanceof MultiValuedMetaData) {
						if (parameter.getName().equals(((MultiValuedMetaData) metaData).getValue("value", String.class))) {
							return (MultiValuedMetaData) metaData;
						}
					}
				}
				if (ensureExistence) {
					MultiValuedMetaData returned = parameter.makeParameterMetaData();
					md.addToMetaDataList(returned);
					return returned;
				}
			}
			return null;
		}

		/*		@Override
				public String getLabel() {
					if (getUIMetaData(false) != null) {
						String returned = getUIMetaData(false).getSingleMetaData(LABEL_KEY, String.class);
						if (returned != null) {
							return returned;
						}
					}
					return getName();
				}
		
				@Override
				public void setLabel(String label) {
					if ((label == null && getLabel() != null) || (label != null && !label.equals(getLabel()))) {
						if (getUIMetaData(true) != null) {
							getUIMetaData(true).setSingleMetaData(LABEL_KEY, label, String.class);
						}
					}
				}*/

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
			notifyObservers(new DataModification<>("parameters", null, getParameters()));
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
				notifyObservers(new DataModification<>("parameters", null, getParameters()));
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
				notifyObservers(new DataModification<>("parameters", null, getParameters()));
				updateSignature(oldSignature);
			}
		}

		@Override
		public void parameterLast(FlexoBehaviourParameter p) {
			String oldSignature = getSignature();
			getParameters().remove(p);
			getParameters().add(p);
			setChanged();
			notifyObservers(new DataModification<>("parameters", null, getParameters()));
			updateSignature(oldSignature);
		}

		@Override
		public void finalizeDeserialization() {
			if (getControlGraph() != null) {
				getControlGraph().accept(new FMLControlGraphVisitor() {
					@Override
					public void visit(FMLControlGraph controlGraph) {
						controlGraph.finalizeDeserialization();
					}
				});
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
				getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, null, bindingModel);
			}
			return bindingModel;
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
		public void setName(String name) throws InvalidNameException {
			// Avoid keywords
			if ("create".equals(name)) {
				setName("_create");
				return;
			}
			if ("delete".equals(name)) {
				setName("_delete");
				return;
			}

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
				signature.append(getDisplayName());
				signature.append("(");
				signature.append(getParameterListAsString(false));
				signature.append(")");
				behaviourSignature = signature.toString();
			}
			return behaviourSignature;
		}
		
		// Name to display in signature
		protected String getDisplayName() {
			return getName();
		}

		protected String getParameterListAsString(boolean fullyQualified) {
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

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			return getControlGraph();
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			setControlGraph(controlGraph);
		}

		@Override
		public void setControlGraph(FMLControlGraph aControlGraph) {
			performSuperSetter(CONTROL_GRAPH_KEY, aControlGraph);
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(null);
			}
		}

		/**
		 * Return flag indicating if this behaviour overrides supplied behaviour<br>
		 * Return true if and only if name and signature equals, and if both declared concepts are not the same and if there are directely
		 * connected without any intermediate implementation
		 * 
		 * @param behaviour
		 * @return
		 */
		@Override
		public boolean overrides(FlexoBehaviour behaviour) {

			if (behaviour == null) {
				return false;
			}
			if (behaviour == this) {
				return false;
			}

			if (!getFlexoConcept().getAllParentFlexoConcepts().contains(behaviour.getFlexoConcept())) {
				return false;
			}

			if (behaviour.getName().equals(getName())) {
				if (behaviour.getParameters().size() == getParameters().size()) {
					boolean allParametersMatch = true;
					for (int i = 0; i < behaviour.getParameters().size(); i++) {
						if (!behaviour.getParameters().get(i).getType().equals(getParameters().get(i).getType())) {
							allParametersMatch = false;
							break;
						}
					}
					if (allParametersMatch) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * Return flag indicating if this behaviour is overriden in supplied context
		 * 
		 * @param context
		 * @return
		 */
		@Override
		public boolean isOverridenInContext(FlexoConcept context) {
			return (getMostSpecializedBehaviour(context) != this);

		}

		/**
		 * Return the most specialized behaviour to execute matching supplied behaviour name and signature<br>
		 * (dynamic binding: can be only supplied behaviour or an other behaviour overriding supplied behaviour)
		 * 
		 * @param behaviour
		 * @param context
		 * @return
		 */
		@Override
		public FlexoBehaviour getMostSpecializedBehaviour(FlexoConcept context) {
			return context.getFlexoBehaviour(getName(), getParameterTypes());

		}

		@Override
		public Type[] getParameterTypes() {
			Type[] returned = new Type[getParameters().size()];
			for (int i = 0; i < getParameters().size(); i++) {
				returned[i] = getParameters().get(i).getArgumentType();
			}
			return returned;
		}

		// TODO: perfs issues
		@Override
		public boolean overrides() {
			if (getFlexoConcept() != null) {
				if (getFlexoConcept().getParentFlexoConcepts().size() > 0) {
					for (FlexoConcept parent : getFlexoConcept().getParentFlexoConcepts()) {
						if (getMostSpecializedBehaviour(parent) != null) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * Hook called when scope of a FMLObject changed.<br>
		 * 
		 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link VirtualModel}<br>
		 * On that example {@link #getBindingFactory()} rely on {@link VirtualModel} enclosing, we must provide this hook to give a chance
		 * to objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
		 * 
		 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...<br>
		 * 
		 */
		@Override
		public void notifiedScopeChanged() {
			super.notifiedScopeChanged();
			if (getControlGraph() != null) {
				getControlGraph().accept(new FMLControlGraphVisitor() {
					@Override
					public void visit(FMLControlGraph controlGraph) {
						controlGraph.notifiedScopeChanged();
					}
				});
			}
		}

		/**
		 * Return a flag indicating if this behaviour support custom definition of parameters<br>
		 * Default implementation is to return true
		 * 
		 * @return
		 */
		@Override
		public boolean supportParameters() {
			return true;
		}

		@Override
		public DataBinding<Integer> getStepsNumber() {
			if (stepsNumber == null) {
				stepsNumber = new DataBinding<>(this, Integer.class, DataBinding.BindingDefinitionType.GET);
				stepsNumber.setBindingName("stepsNumber");
				stepsNumber.setMandatory(false);

			}
			return stepsNumber;
		}

		@Override
		public void setStepsNumber(DataBinding<Integer> stepsNumber) {
			if (stepsNumber != null) {
				this.stepsNumber = new DataBinding<>(stepsNumber.toString(), this, Integer.class, DataBinding.BindingDefinitionType.GET);
				this.stepsNumber.setBindingName("stepsNumber");
				this.stepsNumber.setMandatory(true);
			}
			notifiedBindingChanged(stepsNumber);
		}

	}

	@DefineValidationRule
	public static class DeclaredTypeShouldBeCompatibleWithAnalyzedType
			extends ValidationRule<DeclaredTypeShouldBeCompatibleWithAnalyzedType, FlexoBehaviour> {

		public DeclaredTypeShouldBeCompatibleWithAnalyzedType() {
			super(FlexoBehaviour.class, "declared_types_and_analyzed_types_must_be_compatible");
		}

		@Override
		public ValidationIssue<DeclaredTypeShouldBeCompatibleWithAnalyzedType, FlexoBehaviour> applyValidation(FlexoBehaviour behaviour) {

			if (!behaviour.isAbstract()) {
				Type expected = behaviour.getDeclaredType();
				Type analyzed = behaviour.getAnalyzedReturnType();
				// System.out.println("expected " + expected + " of " + expected.getClass());
				// System.out.println("analyzed " + analyzed + " of " + analyzed.getClass());
				if (expected != null && !TypeUtils.isTypeAssignableFrom(expected, analyzed, true)) {
					return new NotCompatibleTypesIssue(this, behaviour, expected, analyzed);
				}
			}

			return null;
		}

		public static class NotCompatibleTypesIssue
				extends ValidationError<DeclaredTypeShouldBeCompatibleWithAnalyzedType, FlexoBehaviour> {

			private Type expectedType;
			private Type analyzedType;

			public NotCompatibleTypesIssue(DeclaredTypeShouldBeCompatibleWithAnalyzedType rule, FlexoBehaviour anObject, Type expected,
					Type analyzed) {
				super(rule, anObject,
						"types_are_not_compatible_in_behaviour_($validable.signature)_:_($expectedType)_is_not_assignable_from_($analyzedType)");
				this.analyzedType = analyzed;
				this.expectedType = expected;
			}

			public String getExpectedType() {
				return TypeUtils.simpleRepresentation(expectedType);
			}

			public String getAnalyzedType() {
				return TypeUtils.simpleRepresentation(analyzedType);
			}

		}

	}

	@DefineValidationRule
	public static class AbstractBehaviourCannotHaveControlGraph
			extends ValidationRule<AbstractBehaviourCannotHaveControlGraph, FlexoBehaviour> {

		public AbstractBehaviourCannotHaveControlGraph() {
			super(FlexoBehaviour.class, "abstract_behaviour_cannot_have_control_graph");
		}

		@Override
		public ValidationIssue<AbstractBehaviourCannotHaveControlGraph, FlexoBehaviour> applyValidation(FlexoBehaviour behaviour) {
			if (behaviour.isAbstract() && behaviour.getControlGraph() != null
					&& !(behaviour.getControlGraph() instanceof EmptyControlGraph)) {
				return new ValidationError<>(this, behaviour, "control_graph_declared_for_abstract_behaviour");
			}
			return null;
		}

	}

	@DefineValidationRule
	public static class NonAbstractConceptCannotDeclareAbstractBehaviour
			extends ValidationRule<NonAbstractConceptCannotDeclareAbstractBehaviour, FlexoBehaviour> {

		public NonAbstractConceptCannotDeclareAbstractBehaviour() {
			super(FlexoBehaviour.class, "non_abstract_concept_cannot_declare_abstract_behaviour");
		}

		@Override
		public ValidationIssue<NonAbstractConceptCannotDeclareAbstractBehaviour, FlexoBehaviour> applyValidation(FlexoBehaviour behaviour) {
			if (behaviour.getFlexoConcept() != null && !behaviour.getFlexoConcept().isAbstract() && behaviour.isAbstract()) {
				return new ValidationError<>(this, behaviour, "non_abstract_concept_declares_abstract_behaviour");
			}
			return null;
		}

	}

}
