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

import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.expr.VisitorException;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.foundation.fml.binding.LegacyParametersNotation;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLModelContext.FMLEntity;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.ModelSlotPathElement;
import org.openflexo.foundation.fml.md.BasicMetaData;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.md.ListMetaData;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.DeserializationInitializer;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.toolbox.StringUtils;

/**
 * Root of every object of the FML meta-model, from the {@link FMLCompilationUnit} down to the most elementary edition action.
 * <p>
 * A {@link FMLObject} is:
 * <ul>
 * <li>an {@link InnerResourceData} of the {@link FMLCompilationUnit} declaring it: {@link #getResourceData()} and
 * {@link #getDeclaringCompilationUnit()} both return that compilation unit (not the {@link VirtualModel})</li>
 * <li>a {@link Bindable}: it provides the binding model in which the Connie expressions it declares are resolved</li>
 * <li>a {@link TechnologyObject} of the {@link FMLTechnologyAdapter}</li>
 * </ul>
 * The annotations preceding a declaration in FML source ({@code @URI}, {@code @Version}, {@code @Author}, {@code @Description}...) are
 * stored as {@link FMLMetaData} (see {@link #getMetaData()}). Some properties exposed through dedicated accessors are in fact read from and
 * written to this metadata, {@link #getDescription()} for instance.
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLObject.FMLObjectImpl.class)
@Imports({ @Import(FMLPropertyValue.class), @Import(FlexoPropertyPathElement.class), @Import(ModelSlotPathElement.class),
		@Import(CreationSchemePathElement.class) })
public interface FMLObject extends FlexoObject, Bindable, InnerResourceData<FMLCompilationUnit>, TechnologyObject<FMLTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = String.class)
	String AUTHOR_KEY = "author";
	@PropertyIdentifier(type = FMLMetaData.class, cardinality = Cardinality.LIST)
	public static final String META_DATA_KEY = "metaData";
	@PropertyIdentifier(type = FMLPropertyValue.class, cardinality = Cardinality.LIST)
	public static final String FML_PROPERTY_VALUES_KEY = "FMLPropertyValues";

	@Getter(value = NAME_KEY)
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name) throws InvalidNameException;

	@Getter(value = AUTHOR_KEY)
	public String getAuthor();

	@Setter(AUTHOR_KEY)
	public void setAuthor(String author);

	/**
	 * Return the description of this object, stored as the {@code @Description("...")} annotation of its FML declaration.<br>
	 * A documentation comment preceding the declaration in FML source is ignored by the parser: it does not provide the description.
	 */
	@Getter(value = DESCRIPTION_KEY)
	public String getDescription();

	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	public boolean hasDescription();

	/**
	 * Return the metadata of this object: the annotations ({@code @Xxx}, {@code @Xxx(value)}, {@code @Xxx(key=value,...)}) preceding its
	 * declaration in FML source
	 */
	@Getter(value = META_DATA_KEY, cardinality = Cardinality.LIST, inverse = FMLMetaData.OWNER_KEY)
	public List<FMLMetaData> getMetaData();

	@Adder(META_DATA_KEY)
	public void addToMetaData(FMLMetaData metaData);

	@Remover(META_DATA_KEY)
	public void removeFromMetaData(FMLMetaData metaData);

	@Reindexer(META_DATA_KEY)
	public void moveMetaDataToIndex(FMLMetaData metaData, int index);

	@Finder(collection = META_DATA_KEY, attribute = FMLMetaData.KEY_KEY)
	public FMLMetaData getMetaData(String key);

	public boolean hasMetaData(String key);

	public BasicMetaData getBasicMetaData(String key);

	public void setBasicMetaData(String key);

	public <T> T getSingleMetaData(String key, Class<T> type);

	public <T> void setSingleMetaData(String key, T value, Class<T> type);

	public MultiValuedMetaData getMultiValuedMetaData(String key);

	public ListMetaData getListMetaData(String key);

	@Override
	public FlexoServiceManager getServiceManager();

	/**
	 * Return the {@link FMLCompilationUnit} in which this {@link FMLObject} is declared<br>
	 * 
	 */
	public FMLCompilationUnit getDeclaringCompilationUnit();

	/**
	 * Return the {@link CompilationUnitResource} in which this {@link FMLObject} is declared<br>
	 * 
	 */
	public CompilationUnitResource getDeclaringCompilationUnitResource();

	public VirtualModelLibrary getVirtualModelLibrary();

	public FMLModelFactory getFMLModelFactory();

	/**
	 * Return a string representation suitable for a common user<br>
	 * This representation will used in all GUIs
	 */
	public String getStringRepresentation();

	@DeserializationInitializer
	public void initializeDeserialization(FMLModelFactory factory);

	// DeserializationFinalizer will be called only forward, just after VirtualModel loading
	// @DeserializationFinalizer
	public void finalizeDeserialization();

	/**
	 * Hook called when the scope of this {@link FMLObject} changed, giving it a chance to update its bindings (some of them might become
	 * valid or invalid).<br>
	 * It happens for example when a {@link VirtualModel} becomes contained in another {@link VirtualModel} (its binding factory relies on
	 * the enclosing context), or when an edition action is moved from a control graph to another one.
	 */
	public void notifiedScopeChanged();

	public TechnologyAdapterService getTechnologyAdapterService();

	/**
	 * Return reflected BindingEvaluationContext, obtained at metadata conceptual level
	 * 
	 * @return
	 */
	public BindingEvaluationContext getReflectedBindingEvaluationContext();

	/**
	 * Ensure required imports are declared in CompilationUnit
	 */
	public void handleRequiredImports(FMLCompilationUnit compilationUnit);

	public Class<?> getImplementedInterface(FMLModelFactory modelFactory);

	public FMLEntity<?> getFMLEntity();

	public String getFMLKeyword(FMLModelFactory modelFactory);

	public boolean hasFMLProperties(FMLModelFactory modelFactory);

	public List<FMLProperty<?, ?>> getFMLProperties(FMLModelFactory modelFactory);

	public FMLProperty<?, ?> getFMLProperty(String propertyName, FMLModelFactory modelFactory);

	public void updateFMLPropertyValues(FMLModelFactory modelFactory);

	public List<FMLPropertyValue<?, ?>> getFMLPropertyValues(FMLModelFactory modelFactory);

	@Getter(value = FML_PROPERTY_VALUES_KEY, cardinality = Cardinality.LIST)
	@Embedded
	public List<FMLPropertyValue<?, ?>> getFMLPropertyValues();

	@Adder(FML_PROPERTY_VALUES_KEY)
	public void addToFMLPropertyValues(FMLPropertyValue<?, ?> propertyValue);

	@Remover(FML_PROPERTY_VALUES_KEY)
	public void removeFromFMLPropertyValues(FMLPropertyValue<?, ?> propertyValue);

	@Reindexer(FML_PROPERTY_VALUES_KEY)
	public void moveFMLPropertyValueToIndex(FMLPropertyValue<?, ?> propertyValue, int index);

	public FMLRTTechnologyAdapter getFMLRTTechnologyAdapter();

	/**
	 * Invoking this method causes revalidation of {@link DataBinding} declared in this {@link FMLObject}<br>
	 * This might be required to invalidate and translate into right TypingSpace all bindings declared in this object
	 */
	public void revalidateBindings();

	public static abstract class FMLObjectImpl extends FlexoObjectImpl implements FMLObject {

		private static final Logger logger = Logger.getLogger(FMLObject.class.getPackage().getName());

		private String name;

		/**
		 * Return the URI of the {@link FMLObject}<br>
		 * The convention for URI are following: <container_virtual_model_uri>/<virtual_model_name >#<flexo_concept_name>.<behaviour_name>
		 * eg<br>
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyProperty
		 * http://www.mydomain.org/MyVirtuaModel1/MyVirtualModel2#MyFlexoConcept.MyBehaviour
		 * 
		 * @return String representing unique URI of this object
		 */
		// @Override
		// public abstract String getURI();

		/**
		 * Return reflected BindingEvaluationContext, obtained at metadata conceptual level
		 * 
		 * @return
		 */
		@Override
		public BindingEvaluationContext getReflectedBindingEvaluationContext() {
			if (getResourceData() != null) {
				return getResourceData().getReflectedBindingEvaluationContext();
			}
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) throws InvalidNameException {
			if (FMLKeywords.isKeyword(name)) {
				throw new InvalidNameException(this, name);
			}
			if (requireChange(this.name, name)) {
				String oldName = this.name;
				this.name = name;
				// setChanged();
				// notifyObservers(new NameChanged(oldName, name));
				getPropertyChangeSupport().firePropertyChange(NAME_KEY, oldName, name);
			}
		}

		@Override
		public String getDescription() {
			return getSingleMetaData("Description", String.class);
		}

		@Override
		public void setDescription(String description) {
			setSingleMetaData("Description", description, String.class);
		}

		@Override
		public boolean hasDescription() {
			return StringUtils.isNotEmpty(getDescription());
		}

		@Override
		public boolean hasMetaData(String key) {
			return getMetaData(key) != null;
		}

		@Override
		public BasicMetaData getBasicMetaData(String key) {
			if (getMetaData(key) instanceof BasicMetaData) {
				return (BasicMetaData) getMetaData(key);
			}
			return null;
		}

		@Override
		public void setBasicMetaData(String key) {
			if (getMetaData(key) != null) {
				if (!(getMetaData(key) instanceof BasicMetaData)) {
					logger.warning("Unexpected meta-data: " + getMetaData(key));
				}
				return;
			}
			BasicMetaData newMD = getFMLModelFactory().newBasicMetaData(key);
			addToMetaData(newMD);
		}

		@Override
		public <T> T getSingleMetaData(String key, Class<T> type) {
			if (getMetaData(key) instanceof SingleMetaData) {
				return ((SingleMetaData<T>) getMetaData(key)).getValue(type);
			}
			return null;
		}

		@Override
		public <T> void setSingleMetaData(String key, T value, Class<T> type) {
			if (value != null) {
				if (getMetaData(key) instanceof SingleMetaData) {
					((SingleMetaData<T>) getMetaData(key)).setValue(value, type);
				}
				else {
					SingleMetaData<T> newMD = getFMLModelFactory().newSingleMetaData(key, value, type);
					addToMetaData(newMD);
				}
			}
			else {
				if (getMetaData(key) != null) {
					removeFromMetaData(getMetaData(key));
				}
			}
		}

		@Override
		public MultiValuedMetaData getMultiValuedMetaData(String key) {
			if (getMetaData(key) instanceof MultiValuedMetaData) {
				return (MultiValuedMetaData) getMetaData(key);
			}
			return null;
		}

		@Override
		public ListMetaData getListMetaData(String key) {
			if (getMetaData(key) instanceof ListMetaData) {
				return (ListMetaData) getMetaData(key);
			}
			return null;
		}

		@Override
		public final LocalizedDelegate getLocales() {
			if (getDeclaringCompilationUnit() != null) {
				return getDeclaringCompilationUnit().getLocalizedDictionary();
			}
			return super.getLocales();
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getVirtualModelLibrary() != null) {
				return getVirtualModelLibrary().getServiceManager();
			}
			if (getDeserializationFactory() != null) {
				return getDeserializationFactory().getServiceManager();
			}
			return null;
		}

		@Override
		public TechnologyAdapterService getTechnologyAdapterService() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService();
			}
			return null;
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
			}
			return null;
		}

		@Override
		public FMLRTTechnologyAdapter getFMLRTTechnologyAdapter() {
			if (getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			return null;
		}

		@Override
		public VirtualModelLibrary getVirtualModelLibrary() {
			if (getDeclaringCompilationUnit() != null) {
				return getDeclaringCompilationUnit().getVirtualModelLibrary();
			}
			return null;
		}

		/**
		 * Return the {@link ResourceData} (the "container") of this {@link FMLObject}.<br>
		 * The container is the {@link ResourceData} of this object.<br>
		 * It is an instance of {@link FMLCompilationUnit}
		 * 
		 * @return
		 */
		@Override
		public abstract FMLCompilationUnit getResourceData();

		@Override
		public final FMLCompilationUnit getDeclaringCompilationUnit() {
			return getResourceData();
		}

		@Override
		public CompilationUnitResource getDeclaringCompilationUnitResource() {
			if (getDeclaringCompilationUnit() != null) {
				return (CompilationUnitResource) getDeclaringCompilationUnit().getResource();
			}
			return null;
		}

		@Override
		public synchronized void setIsModified() {
			if (ignoreNotifications()) {
				// runWithoutMarkingModified(): linking a pretty-print node is not an edition, notify nobody (it looped with the FML
				// editor, which prints again on "FMLPrettyPrint")
				return;
			}
			super.setIsModified();
			getPropertyChangeSupport().firePropertyChange("FMLPrettyPrint", false, true);
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", false, true);
		}

		@Override
		public final synchronized void setChanged() {
			super.setChanged();
			if (!ignoreNotifications() && getResourceData() != null) {
				getResourceData().setIsModified();
			}
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			if (getPropertyChangeSupport() != null) {
				if (dataBinding != null && dataBinding.getBindingName() != null) {
					getPropertyChangeSupport().firePropertyChange(dataBinding.getBindingName(), null, dataBinding);
				}
			}
			setIsModified();
			/*if (getResourceData() != null) {
				getResourceData().setIsModified();
			}*/
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
			// logger.info("Binding decoded: " + dataBinding);
		}

		public void notifyChange(String propertyName, Object oldValue, Object newValue) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
			}
		}

		@Override
		public BindingFactory getBindingFactory() {
			if (getDeclaringCompilationUnit() != null) {
				if (getDeclaringCompilationUnit().getVirtualModel() != null) {
					return getDeclaringCompilationUnit().getVirtualModel().getBindingFactory();
				}
				return getDeclaringCompilationUnit().getBindingFactory();
			}
			return null;
		}

		/**
		 * Default implementation does nothing
		 */
		@Override
		public void notifiedScopeChanged() {
		}

		/*@Override
		public void notifyBindingModelChanged() {
			getPropertyChangeSupport().firePropertyChange(BindingModelChanged.BINDING_MODEL_CHANGED, null, null);
		}*/

		/*public LocalizedDelegate getLocalizedDictionary() {
			return getDeclaringVirtualModel().getLocalizedDictionary();
		}*/

		@Override
		public FMLModelFactory getFMLModelFactory() {
			if (getDeclaringCompilationUnit() != null && getDeclaringCompilationUnit().getFMLModelFactory() != null) {
				return getDeclaringCompilationUnit().getFMLModelFactory();
			}
			return getDeserializationFactory();
		}

		/**
		 * Default representation: the simple name of the implemented interface, followed by the name of the object when it has one. FML
		 * definitions are not serialized in XML any more (CORE-F-8), so the former XML dump would be empty.
		 */
		@Override
		public String getStringRepresentation() {
			String className = getImplementedInterface() != null ? getImplementedInterface().getSimpleName() : getClass().getSimpleName();
			return StringUtils.isNotEmpty(getName()) ? className + " " + getName() : className;
		}

		private FMLModelFactory deserializationFactory;

		@Override
		public void initializeDeserialization(FMLModelFactory factory) {
			deserializationFactory = factory;
		}

		@Override
		public void finalizeDeserialization() {
			deserializationFactory = null;
		}

		public FMLModelFactory getDeserializationFactory() {
			return deserializationFactory;
		}

		public synchronized String getFMLPrettyPrint() {
			if (this instanceof FMLPrettyPrintable) {
				FMLPrettyPrintDelegate<?> ppDelegate = ((FMLPrettyPrintable) this).getPrettyPrintDelegate();
				if (ppDelegate == null) {
					if (getDeclaringCompilationUnit() != null && getDeclaringCompilationUnit() != this) {
						// Will set all delegates
						getDeclaringCompilationUnit().getFMLPrettyPrint();
						ppDelegate = ((FMLPrettyPrintable) this).getPrettyPrintDelegate();
					}
					if (ppDelegate == null) {
						// Cannot compute FML pretty print: delegate not set
						return null;
					}
				}
				return ppDelegate.getRepresentation(ppDelegate.makePrettyPrintContext());
			}
			return "Not pretty-printable";
		}

		public String getNormalizedFML() {
			if (this instanceof FMLPrettyPrintable) {
				FMLPrettyPrintDelegate<?> ppDelegate = ((FMLPrettyPrintable) this).getPrettyPrintDelegate();
				if (ppDelegate != null) {
					return ppDelegate.getNormalizedRepresentation(ppDelegate.makePrettyPrintContext());
				}
				return null;
			}
			return "Not pretty-printable";
		}

		@Override
		public String render() {
			return getFMLPrettyPrint();
		}

		public void setPrettyPrintDelegate(FMLPrettyPrintDelegate<?> delegate) {
			// System.out.println("setPrettyPrintDelegate");
			// System.out.println("WAS: " + performSuperGetter(FMLPrettyPrintable.PRETTY_PRINT_DELEGATE_KEY));
			performSuperSetter(FMLPrettyPrintable.PRETTY_PRINT_DELEGATE_KEY, delegate);
			// System.out.println("NOW: " + delegate);
			if (delegate != null && delegate.getModelObject() != null && delegate.getModelObject() != this) {
				((FMLPrettyPrintDelegate) delegate).setModelObject(this);
			}

			/*System.out.println("Normalized:");
			System.out.println(getNormalizedFML());
			System.out.println("PP:");
			System.out.println(getFMLPrettyPrint());*/
		}

		/**
		 * Ensure required imports are declared in CompilationUnit
		 */
		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			// Default implementation does nothing
		}

		@Override
		public Class<?> getImplementedInterface(FMLModelFactory modelFactory) {
			return modelFactory.getModelEntityForInstance(this).getImplementedInterface();
		}

		@Override
		public FMLEntity<?> getFMLEntity() {
			return getFMLEntity(getFMLModelFactory());
		}

		protected FMLEntity<?> getFMLEntity(FMLModelFactory modelFactory) {
			if (modelFactory == null) {
				return null;
			}
			Class<?> implementedInterface = getImplementedInterface(modelFactory);
			if (modelFactory.getModelContext().getModelEntity(implementedInterface) == null) {
				// Supplied factory does not know this entity: retry once with our own factory, but never
				// with the same one, nor with one that does not know it either (would recurse infinitely)
				FMLModelFactory ownFactory = getFMLModelFactory();
				if (ownFactory != null && ownFactory != modelFactory
						&& ownFactory.getModelContext().getModelEntity(implementedInterface) != null) {
					return FMLModelContext.getFMLEntity((Class) implementedInterface, ownFactory);
				}
				logger.warning("Cannot find FMLEntity for " + implementedInterface
						+ ": this entity is not declared in the model context of " + modelFactory
						+ ". Check 'uses' declarations of " + getDeclaringCompilationUnit());
				return null;
			}
			return FMLModelContext.getFMLEntity((Class) implementedInterface, modelFactory);
		}

		@Override
		public final String getFMLKeyword(FMLModelFactory modelFactory) {
			if (getFMLEntity(modelFactory) != null) {
				return getFMLEntity(modelFactory).getFmlAnnotation().value();
			}
			return null;
		}

		@Override
		public final boolean hasFMLProperties(FMLModelFactory modelFactory) {
			if (getFMLEntity(modelFactory) != null) {
				return getFMLEntity(modelFactory).getProperties().size() > 0;
			}
			return false;
		}

		@Override
		public List<FMLProperty<?, ?>> getFMLProperties(FMLModelFactory modelFactory) {
			if (getFMLEntity(modelFactory) != null) {
				return (List) getFMLEntity(modelFactory).getProperties();
			}
			return null;
		}

		@Override
		public FMLProperty<?, ?> getFMLProperty(String propertyName, FMLModelFactory modelFactory) {
			List<FMLProperty<?, ?>> fmlProperties = getFMLProperties(modelFactory);
			if (fmlProperties != null) {
				for (FMLProperty fmlProperty : getFMLProperties(modelFactory)) {
					if (fmlProperty.getName().equals(propertyName)) {
						return fmlProperty;
					}
					if (fmlProperty.getLabel().equals(propertyName)) {
						return fmlProperty;
					}
				}
			}
			return null;
		}

		private <I extends FMLObject, T> FMLPropertyValue<I, T> retrieveFMLPropertyValue(FMLProperty<I, T> property) {

			for (FMLPropertyValue<?, ?> fmlPropertyValue : new ArrayList<>(getFMLPropertyValues())) {
				if (fmlPropertyValue.getProperty() == property) {
					return (FMLPropertyValue<I, T>) fmlPropertyValue;
				}
			}
			return null;
		}

		/*@Override
		public List<FMLPropertyValue<?, ?>> getFMLPropertyValues() {
			if (getFMLModelFactory() != null) {
				updateFMLPropertyValues(getFMLModelFactory());
			}
			return (List<FMLPropertyValue<?, ?>>)performSuperGetter(FML_PROPERTY_VALUES_KEY);
		}*/

		@Override
		public List<FMLPropertyValue<?, ?>> getFMLPropertyValues(FMLModelFactory modelFactory) {
			if (modelFactory != null) {
				updateFMLPropertyValues(modelFactory);
			}
			return (List<FMLPropertyValue<?, ?>>) performSuperGetter(FML_PROPERTY_VALUES_KEY);
		}

		@Override
		public void updateFMLPropertyValues(FMLModelFactory modelFactory) {
			if (getFMLEntity(modelFactory) != null) {
				for (FMLProperty fmlProperty : getFMLEntity(modelFactory).getProperties()) {
					FMLPropertyValue pValue = retrieveFMLPropertyValue(fmlProperty);
					if (pValue == null) {
						pValue = fmlProperty.makeFMLPropertyValue(this);
						if (pValue != null && pValue.isRequired(modelFactory)) {
							// The property values mirror the object, they are built when printing: not an edition (CORE-D-45)
							final FMLPropertyValue newValue = pValue;
							runWithoutMarkingModified(() -> performSuperAdder(FML_PROPERTY_VALUES_KEY, newValue));
						}
					}
					else {
						// The property value is existing, but we have no guarantee that its value is up-to-date
						// Update related object and value
						if (this instanceof WrappedFMLObject) {
							pValue.setObject(((WrappedFMLObject) this).getObject());
						}
						else {
							pValue.setObject(this);
						}
						pValue.retrievePropertyValueFromModelObject();
					}
				}
				/*List<FMLPropertyValue<?, ?>> returned = new ArrayList<>();
				for (FMLPropertyValue fmlPropertyValue : fmlPropertyValues.values()) {
					if (fmlPropertyValue != null) {
						returned.add(fmlPropertyValue);
					}
				}*/

				// Sort properties to get nice and persistent pretty print
				/*Collections.sort(returned, new Comparator<FMLPropertyValue<?, ?>>() {
					@Override
					public int compare(FMLPropertyValue<?, ?> o1, FMLPropertyValue<?, ?> o2) {
						if (o1.getProperty() == null || o2.getProperty() == null) {
							return 0;
						}
						if (o1.getProperty().getKind() != o2.getProperty().getKind()) {
							return o2.getProperty().getKind().ordinal() - o1.getProperty().getKind().ordinal();
						}
						return Collator.getInstance().compare(o1.getProperty().getName(), o2.getProperty().getName());
					}
				});*/

				// System.out.println("Returning " + returned);
				// return returned;
				// return new ArrayList<>(fmlPropertyValues.values());
			}
			// return null;
		}

		/*@Override
		public void addToFMLPropertyValues(FMLPropertyValue<?, ?> propertyValue) {
			System.out.println(">>>>>>>> addToFMLPropertyValues "+propertyValue);
			if (propertyValue.getProperty() != null) {
				fmlPropertyValues.put(propertyValue.getProperty(), propertyValue);
			}
			else {
				undefinedPropertyValues.add(propertyValue);
			}
			getPropertyChangeSupport().firePropertyChange("FMLPropertyValues",null,propertyValue);
		}*/

		/*private Map<FMLObject, WrappedFMLObject<?>> wrappedObjects = new HashMap<>();
		
		@Override
		public <O extends FMLObject> WrappedFMLObject<O> getWrappedFMLObject(O object) {
			System.out.println("On cherche dans " + this + " le wrapped de " + object);
			WrappedFMLObject<O> returned = (WrappedFMLObject<O>) wrappedObjects.get(object);
			if (returned == null) {
				returned = getFMLModelFactory().newWrappedFMLObject(object);
				wrappedObjects.put(object, returned);
			}
			return returned;
		}*/

		/*@Override
		public final String encodeFMLProperties(FMLModelFactory modelFactory) {
			List<FMLPropertyValue<?, ?>> fmlPropertyValues = getFMLPropertyValues(modelFactory);
			boolean isFirst = true;
			StringBuffer sb = new StringBuffer();
			for (FMLProperty fmlProperty : fmlPropertyValues.keySet()) {
				String encodedProperty = encodeFMLProperty(fmlProperty, fmlPropertyValues.get(fmlProperty), modelFactory);
				if (encodedProperty != null) {
					sb.append((isFirst ? "" : ",") + encodedProperty);
					isFirst = false;
				}
			}
			return sb.toString();
		}*/

		/*private <T> String encodeFMLProperty(FMLProperty<?, T> fmlProperty, T value, FMLModelFactory modelFactory) {
			if (value == null) {
				if (fmlProperty.isRequired()) {
					return fmlProperty.getName() + "=null";
				}
				return null;
			}
			else {
				if (!fmlProperty.isRequired() && value.equals(fmlProperty.getDefaultValue(modelFactory))) {
					// No need to serialize this
					return null;
				}
		
				String valueAsString = null;
				if (getDeclaringCompilationUnit() != null) {
					for (ElementImportDeclaration elementImportDeclaration : getDeclaringCompilationUnit().getElementImports()) {
						if (elementImportDeclaration.getReferencedObject() == value) {
							valueAsString = elementImportDeclaration.getAbbrev();
							break;
						}
					}
				}
		
				if (valueAsString == null) {
					try {
						valueAsString = modelFactory.getStringEncoder().toString(value);
						if (value instanceof String) {
							valueAsString = "\"" + valueAsString + "\"";
						}
					} catch (InvalidDataException e) {
						logger.warning("Don't know what to do with " + value);
						e.printStackTrace();
						return null;
					}
				}
		
				if (valueAsString != null) {
					return fmlProperty.getName() + "=" + valueAsString;
				}
				return null;
			}
		}*/

		/*@Override
		public final void decodeFMLProperties(String serializedMap) {
			StringTokenizer st = new StringTokenizer(serializedMap, ",");
			while (st.hasMoreTokens()) {
				String next = st.nextToken();
				String propertyName = next.substring(0, next.indexOf("="));
				String propertyValue = next.substring(next.indexOf("=") + 1);
				System.out.println(propertyName + " = [" + propertyValue + "]");
			}
		}*/

		/*@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			Collection returned = super.getEmbeddedValidableObjects();
			if (getFMLModelFactory() == null) {
				return returned;
			}
			else {
				List<FMLPropertyValue<?,?>> propertyValues = getFMLPropertyValues(getFMLModelFactory());
				if (propertyValues != null && propertyValues.size() > 0) {
					if (returned == null) {
						returned = new ArrayList<>();
					}
					returned.addAll(propertyValues);
				}
				if (undefinedPropertyValues != null && undefinedPropertyValues.size() > 0) {
					if (returned == null) {
						returned = new ArrayList<>();
					}
					returned.addAll(undefinedPropertyValues);
				}
				return returned;
			}
		}*/

		/*@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			Collection<? extends Validable> returned = super.getEmbeddedValidableObjects();
			if (this.getClass().getName().contains("LinkScheme")) {
				System.out.println("Pour " + this + " j'ai " + returned);
			}
			return returned;
		}*/

		@Override
		public void revalidateBindings() {
			// System.out.println("> revalidateBindings for " + this);
		}

	}

	/*@DefineValidationRule
	class AllRequiredPropertiesMustBeSet extends ValidationRule<AllRequiredPropertiesMustBeSet, FMLObject> {
		public AllRequiredPropertiesMustBeSet() {
			super(FMLObject.class, "all_required_properties_must_be_set");
		}
	
		@Override
		public ValidationIssue<AllRequiredPropertiesMustBeSet, FMLObject> applyValidation(FMLObject object) {
	
			if (this.getClass().getName().contains("LinkScheme")) {
				System.out.println("On regardes les AllRequiredPropertiesMustBeSet de " + object);
			}
	
			if (object != null && object.getFMLEntity() != null) {
				List<FMLProperty<?, ?>> missingProperties = new ArrayList<>();
				for (FMLProperty fmlProperty : object.getFMLEntity().getProperties()) {
					if (this.getClass().getName().contains("LinkScheme")) {
						System.out.println(" >>> " + fmlProperty + " required: " + fmlProperty.isRequired());
					}
					FMLPropertyValue pValue = ((FMLObjectImpl) object).retrieveFMLPropertyValue(fmlProperty);
					if (fmlProperty.isRequired() && (pValue == null || pValue.getValue() == null)) {
						missingProperties.add(fmlProperty);
					}
				}
				if (missingProperties.size() > 0) {
					return new MissingPropertiesError(this, object, missingProperties);
				}
			}
			return null;
		}
	
		public static class MissingPropertiesError extends ValidationError<AllRequiredPropertiesMustBeSet, FMLObject> {
	
			private String requiredProperties;
	
			public MissingPropertiesError(AllRequiredPropertiesMustBeSet rule, FMLObject anObject, FMLProperty<?, ?> missingProperty) {
				super(rule, anObject, "missing_required_property_($requiredProperties)_for_($typeName)");
				requiredProperties = missingProperty.getLabel();
	
			}
	
			public MissingPropertiesError(AllRequiredPropertiesMustBeSet rule, FMLObject anObject,
					List<FMLProperty<?, ?>> missingProperties) {
				super(rule, anObject, "missing_required_properties_($requiredProperties)_for_($typeName)");
				requiredProperties = null;
				for (FMLProperty<?, ?> fmlProperty : missingProperties) {
					requiredProperties = (requiredProperties == null ? fmlProperty.getLabel()
							: requiredProperties + "," + fmlProperty.getLabel());
				}
	
			}
	
			@Override
			public String getTypeName() {
				return getValidable().getImplementedInterface().getSimpleName();
			}
	
			public String getRequiredProperties() {
				return requiredProperties;
			}
	
		}
	
	}*/

	/**
	 * A variable hides a type of the same name (CORE-F-5): in <code>Foo.bar()</code>, <code>Foo</code> is the variable when one is visible,
	 * the type otherwise, as in Java. Since the case of a name carries no meaning in FML, a variable can take the name of a type: legal, but
	 * a reader is easily misled. Reported on any binding of an FML object whose path starts with a variable whose name is also a type of the
	 * typing space of its unit.
	 * 
	 * @author sylvain
	 */
	@DefineValidationRule
	public static class VariableShouldNotHideAType extends ValidationRule<VariableShouldNotHideAType, FMLObject> {

		private static final Logger logger = Logger.getLogger(VariableShouldNotHideAType.class.getPackage().getName());

		public VariableShouldNotHideAType() {
			super(FMLObject.class, "variable_should_not_hide_a_type");
		}

		@Override
		public ValidationIssue<VariableShouldNotHideAType, FMLObject> applyValidation(FMLObject object) {
			FMLCompilationUnit unit = object.getDeclaringCompilationUnit();
			if (unit == null || unit.getTypingSpace() == null) {
				return null;
			}
			Set<String> hidingNames = new TreeSet<>();
			// A name is looked up once: resolving it walks the imports and the Java packages
			Set<String> checkedNames = new HashSet<>();
			for (DataBinding<?> binding : getBindings(object)) {
				if (binding.isSet() && binding.getExpression() != null) {
					try {
						binding.getExpression().visit(e -> {
							if (e instanceof BindingPath && ((BindingPath) e).getBindingVariable() != null
									&& ((BindingPath) e).getBindingPathElementCount() > 0) {
								String name = ((BindingPath) e).getBindingVariable().getVariableName();
								if (name != null && checkedNames.add(name)) {
									Type type = unit.getTypingSpace().resolveType(name);
									if (type != null && !(type instanceof UnresolvedType)) {
										hidingNames.add(name);
									}
								}
							}
						});
					} catch (VisitorException e) {
						logger.fine("Cannot visit " + binding + ": " + e);
					}
				}
			}
			if (hidingNames.isEmpty()) {
				return null;
			}
			return new VariableHidesATypeWarning(this, object, String.join(", ", hidingNames));
		}

		/** The bindings of the supplied object, read through its PAMELA properties */
		private static List<DataBinding<?>> getBindings(FMLObject object) {
			List<DataBinding<?>> returned = new ArrayList<>();
			if (object.getFMLModelFactory() == null) {
				return returned;
			}
			org.openflexo.pamela.model.ModelEntity<?> entity = object.getFMLModelFactory().getModelEntityForInstance(object);
			if (entity == null) {
				return returned;
			}
			try {
				Iterator<? extends ModelProperty<?>> properties = entity.getProperties();
				while (properties.hasNext()) {
					ModelProperty<?> property = properties.next();
					if (DataBinding.class.equals(property.getType()) && property.getGetterMethod() != null) {
						Object value = property.getGetterMethod().invoke(object);
						if (value instanceof DataBinding) {
							returned.add((DataBinding<?>) value);
						}
					}
				}
			} catch (ModelDefinitionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.fine("Cannot read the bindings of " + object + ": " + e);
			}
			return returned;
		}

		/** The warning, which names the variables in its message */
		public static class VariableHidesATypeWarning extends ValidationWarning<VariableShouldNotHideAType, FMLObject> {

			private final String hidingNames;

			public VariableHidesATypeWarning(VariableShouldNotHideAType rule, FMLObject object, String hidingNames) {
				super(rule, object, "variable_($hidingNames)_hides_a_type_of_the_same_name");
				this.hidingNames = hidingNames;
			}

			public String getHidingNames() {
				return hidingNames;
			}
		}
	}

	/**
	 * The argument of a behaviour is designated by its name (<code>aValue</code>); <code>parameters.aValue</code> is the deprecated notation,
	 * still evaluated. Reported on any binding of any FML object that uses it for an argument it can be replaced for; the fix rewrites the
	 * binding, and is what migrates existing FML.
	 * 
	 * @author sylvain
	 */
	@DefineValidationRule
	public static class LegacyParametersNotationIsDeprecated extends ValidationRule<LegacyParametersNotationIsDeprecated, FMLObject> {

		private static final Logger ruleLogger = Logger.getLogger(LegacyParametersNotationIsDeprecated.class.getPackage().getName());

		public LegacyParametersNotationIsDeprecated() {
			super(FMLObject.class, "legacy_parameters_notation_is_deprecated");
		}

		/**
		 * Return the bindings of supplied object that use the legacy notation
		 */
		public static List<DataBinding<?>> getLegacyBindings(FMLObject object) {
			List<DataBinding<?>> returned = new ArrayList<>();
			if (object.getFMLModelFactory() == null) {
				return returned;
			}
			org.openflexo.pamela.model.ModelEntity<?> entity = object.getFMLModelFactory().getModelEntityForInstance(object);
			if (entity == null) {
				return returned;
			}
			Iterator<? extends ModelProperty<?>> properties;
			try {
				properties = entity.getProperties();
			} catch (ModelDefinitionException e) {
				ruleLogger.fine("Cannot read the properties of " + entity + ": " + e);
				return returned;
			}
			while (properties.hasNext()) {
				ModelProperty<?> property = properties.next();
				if (DataBinding.class.equals(property.getType()) && property.getGetterMethod() != null) {
					try {
						Object value = property.getGetterMethod().invoke(object);
						if (value instanceof DataBinding && LegacyParametersNotation.usesLegacyNotation((DataBinding<?>) value)) {
							returned.add((DataBinding<?>) value);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						ruleLogger.fine("Cannot read " + property + " of " + object + ": " + e);
					}
				}
			}
			return returned;
		}

		@Override
		public ValidationIssue<LegacyParametersNotationIsDeprecated, FMLObject> applyValidation(FMLObject object) {
			List<DataBinding<?>> bindings = getLegacyBindings(object);
			if (bindings.isEmpty()) {
				return null;
			}
			return new ValidationWarning<>(this, object, "parameters_notation_is_deprecated", new UseShortName(this, bindings));
		}

		protected static class UseShortName extends FixProposal<LegacyParametersNotationIsDeprecated, FMLObject> {

			private final List<DataBinding<?>> bindings;

			public UseShortName(LegacyParametersNotationIsDeprecated rule, List<DataBinding<?>> bindings) {
				super("use_the_name_of_the_argument_instead_of_parameters_dot_name");
				this.bindings = bindings;
			}

			@Override
			protected void fixAction() {
				for (DataBinding<?> binding : bindings) {
					String migrated = LegacyParametersNotation.migratedText(binding);
					if (migrated != null) {
						binding.setUnparsedBinding(migrated);
					}
				}
			}
		}

	}

}
