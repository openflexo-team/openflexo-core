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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.FMLModelContext.FMLEntity;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.md.BasicMetaData;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.md.ListMetaData;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.DeserializationInitializer;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.toolbox.StringUtils;

/**
 * This is the root class for all objects involved in an {@link VirtualModel} (a FML "program").<br>
 * A {@link FMLObject} has a name, a description and can be identified by an URI
 * 
 * It represents an object which is part of a FML model.<br>
 * As such, you securely access to the {@link VirtualModel} in which this object "lives" using {@link #getResourceData()}<br>
 * 
 * A {@link FMLObject} is a {@link Bindable} as conforming to CONNIE binding scheme<br>
 * A {@link FMLObject} is a {@link InnerResourceData} (in a VirtualModel)<br>
 * A {@link FMLObject} is a {@link TechnologyObject} (powered with {@link FMLTechnologyAdapter})
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLObject.FMLObjectImpl.class)
@XMLElement(idFactory = "userIdentifier+'-'+flexoID")
@Imports({ @Import(FMLPropertyValue.class) })
public interface FMLObject extends FlexoObject, Bindable, InnerResourceData<FMLCompilationUnit>, TechnologyObject<FMLTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	public static final String NAME_KEY = "name";
	@PropertyIdentifier(type = String.class)
	String DESCRIPTION_KEY = "description";
	@PropertyIdentifier(type = String.class)
	String AUTHOR_KEY = "author";
	@PropertyIdentifier(type = FMLMetaData.class, cardinality = Cardinality.LIST)
	public static final String META_DATA_KEY = "metaData";

	@Getter(value = NAME_KEY)
	@XMLAttribute
	public String getName();

	@Setter(NAME_KEY)
	public void setName(String name) throws InvalidNameException;

	@Getter(value = AUTHOR_KEY)
	@XMLAttribute
	public String getAuthor();

	@Setter(AUTHOR_KEY)
	public void setAuthor(String author);

	@Getter(value = DESCRIPTION_KEY)
	@XMLAttribute
	public String getDescription();

	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	public boolean hasDescription();

	/**
	 * Return list of meta-data declared for this object
	 * 
	 * @return
	 */
	// TODO: ignoreForEquality to be removed once conversion from XML to FML is done
	@Getter(value = META_DATA_KEY, cardinality = Cardinality.LIST, inverse = FMLMetaData.OWNER_KEY, ignoreForEquality = true)
	public List<FMLMetaData> getMetaData();

	@Adder(META_DATA_KEY)
	public void addToMetaData(FMLMetaData metaData);

	@Remover(META_DATA_KEY)
	public void removeFromMetaData(FMLMetaData metaData);

	@Finder(collection = META_DATA_KEY, attribute = FMLMetaData.KEY_KEY)
	public FMLMetaData getMetaData(String key);

	public boolean hasMetaData(String key);

	public BasicMetaData getBasicMetaData(String key);

	public void setBasicMetaData(String key);

	public <T> T getSingleMetaData(String key, Class<T> type);

	public <T> void setSingleMetaData(String key, T value, Class<T> type);

	public MultiValuedMetaData getMultiValuedMetaData(String key);

	public ListMetaData getListMetaData(String key);

	/**
	 * Return the URI of the {@link NamedFMLObject}<br>
	 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<behaviour_name> <br>
	 * eg<br>
	 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyBehaviour
	 * 
	 * @return String representing unique URI of this object
	 */
	// public String getURI();

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

	// public void notifyBindingModelChanged();

	// public FMLLocalizedDictionary getLocalizedDictionary();

	@DeserializationInitializer
	public void initializeDeserialization(FMLModelFactory factory);

	// DeserializationFinalizer will be called only forward, just after VirtualModel loading
	// @DeserializationFinalizer
	public void finalizeDeserialization();

	/**
	 * Return the {@link ResourceData} (the "container") of this {@link FMLObject}.<br>
	 * The container is the {@link ResourceData} of this object.<br>
	 * It is an instance of {@link VirtualModel} (a {@link VirtualModel} or a {@link VirtualModel})
	 * 
	 * @return
	 */
	// @Override
	// public VirtualModel getResourceData();

	/**
	 * Hook called when scope of a FMLObject changed.<br>
	 * 
	 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link VirtualModel}<br>
	 * On that example {@link #getBindingFactory()} rely on {@link VirtualModel} enclosing, we must provide this hook to give a chance to
	 * objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
	 * 
	 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...
	 * 
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

	public String getFMLKeyword(FMLModelFactory modelFactory);

	public boolean hasFMLProperties(FMLModelFactory modelFactory);

	public Set<FMLProperty> getFMLProperties(FMLModelFactory modelFactory);

	public FMLProperty getFMLProperty(String propertyName, FMLModelFactory modelFactory);

	public List<FMLPropertyValue<?, ?>> getFMLPropertyValues(FMLModelFactory modelFactory);

	public void addToFMLPropertyValues(FMLPropertyValue<?, ?> propertyValue);

	// public String encodeFMLProperties(FMLModelFactory modelFactory);

	// public void decodeFMLProperties(String serializedMap);

	public <O extends FMLObject> WrappedFMLObject<O> getWrappedFMLObject(O object);

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
			return getSingleMetaData(DESCRIPTION_KEY, String.class);
		}

		@Override
		public void setDescription(String description) {
			setSingleMetaData(DESCRIPTION_KEY, description, String.class);
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
				return getDeclaringCompilationUnit().getVirtualModelLibrary().getServiceManager();
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

		/**
		 * Return the ViewPoint in which this {@link FMLObject} is defined<br>
		 * If container of this object is a {@link VirtualModel}, return this ViewPoint<br>
		 * Otherwise, container of this object is a {@link VirtualModel}, return ViewPoint of VirtualModel
		 * 
		 */
		/*@Override
		public VirtualModel getDeclaringCompilationUnit() {
			return getResourceData();
		}*/

		@Override
		public CompilationUnitResource getDeclaringCompilationUnitResource() {
			if (getDeclaringCompilationUnit() != null) {
				return (CompilationUnitResource) getDeclaringCompilationUnit().getResource();
			}
			return null;
		}

		@Override
		public synchronized void setIsModified() {
			super.setIsModified();
			getPropertyChangeSupport().firePropertyChange("FMLPrettyPrint", false, true);
			getPropertyChangeSupport().firePropertyChange("stringRepresentation", false, true);
		}

		@Override
		public final synchronized void setChanged() {
			super.setChanged();
			if (getResourceData() != null) {
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
		 * Hook called when scope of a FMLObject changed.<br>
		 * 
		 * It happens for example when a {@link VirtualModel} is declared to be contained in a {@link VirtualModel}<br>
		 * On that example {@link #getBindingFactory()} rely on {@link VirtualModel} enclosing, we must provide this hook to give a chance
		 * to objects that rely on ViewPoint instanciation context to update their bindings (some bindings might becomes valid)<br>
		 * 
		 * It may also happen if an EditionAction is moved from a control graph to another control graph, etc...<br>
		 * 
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

		@Override
		public String getStringRepresentation() {
			return getFMLModelFactory().stringRepresentation(this);
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

		public String getFMLPrettyPrint() {
			if (this instanceof FMLPrettyPrintable) {
				FMLPrettyPrintDelegate<?> ppDelegate = ((FMLPrettyPrintable) this).getPrettyPrintDelegate();
				if (ppDelegate == null) {
					if (getDeclaringCompilationUnit() != null) {
						// Will set all delegates
						getDeclaringCompilationUnit().getFMLPrettyPrint();
						ppDelegate = ((FMLPrettyPrintable) this).getPrettyPrintDelegate();
					}
					if (ppDelegate == null) {
						return "Cannot compute FML pretty print: delegate not set";
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

		protected FMLEntity<?> getFMLEntity(FMLModelFactory modelFactory) {
			return FMLModelContext.getFMLEntity((Class) getImplementedInterface(modelFactory), modelFactory);
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
		public Set<FMLProperty> getFMLProperties(FMLModelFactory modelFactory) {
			if (getFMLEntity(modelFactory) != null) {
				return (Set) getFMLEntity(modelFactory).getProperties();
			}
			return null;
		}

		@Override
		public FMLProperty getFMLProperty(String propertyName, FMLModelFactory modelFactory) {
			Set<FMLProperty> fmlProperties = getFMLProperties(modelFactory);
			if (fmlProperties != null) {
				for (FMLProperty fmlProperty : getFMLProperties(modelFactory)) {
					if (fmlProperty.getName().equals(propertyName)) {
						return fmlProperty;
					}
				}
			}
			return null;
		}

		private Map<FMLProperty, FMLPropertyValue> fmlPropertyValues = new HashMap<>();

		@Override
		public List<FMLPropertyValue<?, ?>> getFMLPropertyValues(FMLModelFactory modelFactory) {
			if (getFMLEntity(modelFactory) != null) {
				for (FMLProperty fmlProperty : getFMLEntity(modelFactory).getProperties()) {
					FMLPropertyValue pValue = fmlPropertyValues.get(fmlProperty);
					if (pValue == null) {
						pValue = fmlProperty.makeFMLPropertyValue(this);
						if (pValue != null && pValue.isRequired(modelFactory)) {
							fmlPropertyValues.put(fmlProperty, pValue);
						}
					}
				}
				List<FMLPropertyValue<?, ?>> returned = new ArrayList<>();
				for (FMLPropertyValue fmlPropertyValue : fmlPropertyValues.values()) {
					if (fmlPropertyValue != null) {
						returned.add(fmlPropertyValue);
					}
				}

				// Sort properties to get nice and persistent pretty print
				Collections.sort(returned, new Comparator<FMLPropertyValue<?, ?>>() {
					@Override
					public int compare(FMLPropertyValue<?, ?> o1, FMLPropertyValue<?, ?> o2) {
						if (o1.getProperty().getKind() != o2.getProperty().getKind()) {
							return o2.getProperty().getKind().ordinal() - o1.getProperty().getKind().ordinal();
						}
						return Collator.getInstance().compare(o1.getProperty().getName(), o2.getProperty().getName());
					}
				});

				System.out.println("Returning " + returned);
				return returned;
				// return new ArrayList<>(fmlPropertyValues.values());
			}
			return null;
		}

		@Override
		public void addToFMLPropertyValues(FMLPropertyValue<?, ?> propertyValue) {
			fmlPropertyValues.put(propertyValue.getProperty(), propertyValue);
		}

		private Map<FMLObject, WrappedFMLObject<?>> wrappedObjects = new HashMap<>();

		@Override
		public <O extends FMLObject> WrappedFMLObject<O> getWrappedFMLObject(O object) {
			WrappedFMLObject<O> returned = (WrappedFMLObject<O>) wrappedObjects.get(object);
			if (returned == null) {
				returned = getFMLModelFactory().newWrappedFMLObject(object);
				wrappedObjects.put(object, returned);
			}
			return returned;
		}

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
				System.exit(-1);
			}
		}*/

	}

	public static abstract class BindingIsRecommandedAndShouldBeValid<C extends FMLObject>
			extends ValidationRule<BindingIsRecommandedAndShouldBeValid<C>, C> {
		public BindingIsRecommandedAndShouldBeValid(String ruleName, Class<C> clazz) {
			super(clazz, ruleName);
		}

		public abstract DataBinding<?> getBinding(C object);

		@Override
		public ValidationIssue<BindingIsRecommandedAndShouldBeValid<C>, C> applyValidation(C object) {
			if (getBinding(object) != null && getBinding(object).isSet()) {
				// We force revalidate the binding to be sure that the binding is valid
				if (!getBinding(object).forceRevalidate()) {
					FMLObjectImpl.logger.info("Binding NOT valid: " + getBinding(object) + " for " + object.getStringRepresentation()
							+ ". Reason: " + getBinding(object).invalidBindingReason());
					DeleteBinding<C> deleteBinding = new DeleteBinding<>(this);
					// return new ValidationError<BindingMustBeValid<C>, C>(this, object, BindingMustBeValid.this.getRuleName(), "Binding: "
					// + getBinding(object) + " reason: " + getBinding(object).invalidBindingReason(), deleteBinding);
					return new InvalidBindingIssue<>(this, object, deleteBinding);
				}
			}
			else {
				return new RecommandedBindingWarning<>(this, object);
			}
			return null;
		}

		public static class RecommandedBindingWarning<C extends FMLObject>
				extends ValidationWarning<BindingIsRecommandedAndShouldBeValid<C>, C> {

			public RecommandedBindingWarning(BindingIsRecommandedAndShouldBeValid<C> rule, C anObject) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_recommanded_here");
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

		}

		public static class InvalidBindingIssue<C extends FMLObject> extends ValidationError<BindingIsRecommandedAndShouldBeValid<C>, C> {

			@SafeVarargs
			public InvalidBindingIssue(BindingIsRecommandedAndShouldBeValid<C> rule, C anObject,
					FixProposal<BindingIsRecommandedAndShouldBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_not_valid: ($binding)", fixProposals);
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

		protected static class DeleteBinding<C extends FMLObject> extends FixProposal<BindingIsRecommandedAndShouldBeValid<C>, C> {

			private final BindingIsRecommandedAndShouldBeValid<C> rule;

			public DeleteBinding(BindingIsRecommandedAndShouldBeValid<C> rule) {
				super("delete_this_binding");
				this.rule = rule;
			}

			@Override
			protected void fixAction() {
				rule.getBinding(getValidable()).reset();
			}

		}
	}

	public static abstract class BindingMustBeValid<C extends FMLObject> extends ValidationRule<BindingMustBeValid<C>, C> {
		public BindingMustBeValid(String ruleName, Class<C> clazz) {
			super(clazz, ruleName);
		}

		public abstract DataBinding<?> getBinding(C object);

		@Override
		public ValidationIssue<BindingMustBeValid<C>, C> applyValidation(C object) {
			if (getBinding(object) != null && getBinding(object).isSet()) {
				// We force revalidate the binding to be sure that the binding is valid
				if (!getBinding(object).forceRevalidate()) {
					FMLObjectImpl.logger.info("Binding NOT valid: " + getBinding(object) + " for " + object.getStringRepresentation()
							+ ". Reason: " + getBinding(object).invalidBindingReason());
					DeleteBinding<C> deleteBinding = new DeleteBinding<>(this);
					// return new ValidationError<BindingMustBeValid<C>, C>(this, object, BindingMustBeValid.this.getRuleName(), "Binding: "
					// + getBinding(object) + " reason: " + getBinding(object).invalidBindingReason(), deleteBinding);
					return new InvalidBindingIssue<>(this, object, deleteBinding);
				}
			}
			return null;
		}

		public static class InvalidBindingIssue<C extends FMLObject> extends ValidationError<BindingMustBeValid<C>, C> {

			@SafeVarargs
			public InvalidBindingIssue(BindingMustBeValid<C> rule, C anObject, FixProposal<BindingMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_not_valid: ($binding)", fixProposals);
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

		protected static class DeleteBinding<C extends FMLObject> extends FixProposal<BindingMustBeValid<C>, C> {

			private final BindingMustBeValid<C> rule;

			public DeleteBinding(BindingMustBeValid<C> rule) {
				super("delete_this_binding");
				this.rule = rule;
			}

			@Override
			protected void fixAction() {
				rule.getBinding(getValidable()).reset();
			}

		}
	}

	public static abstract class BindingIsRequiredAndMustBeValid<C extends FMLObject>
			extends ValidationRule<BindingIsRequiredAndMustBeValid<C>, C> {
		public BindingIsRequiredAndMustBeValid(String ruleName, Class<C> clazz) {
			super(clazz, ruleName);
		}

		public abstract DataBinding<?> getBinding(C object);

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<C>, C> applyValidation(C object) {
			DataBinding<?> b = getBinding(object);
			if (b == null || !b.isSet()) {
				return new UndefinedRequiredBindingIssue<>(this, object);
			}
			// We force revalidate the binding to be sure that the binding is valid
			else if (!b.forceRevalidate()) {
				// FMLObjectImpl.logger.info(getClass().getName() + ": Binding NOT valid: " + b + " for " + object.getStringRepresentation()
				// + ". Reason: " + b.invalidBindingReason());
				// Thread.dumpStack();

				/*if (b.toString().equals("donneesReferentiel.tiers")) {
					System.out.println(
							"C'est la que j'ai mon probleme, avec " + b + " pour " + object + " of " + object.getImplementedInterface());
					System.out.println("reason: " + b.invalidBindingReason());
					Thread.dumpStack();
				}*/

				InvalidRequiredBindingIssue<C> returned = new InvalidRequiredBindingIssue<>(this, object);

				if (object instanceof FlexoConceptObject) {
					String proposal = b.toString();
					if (((FlexoConceptObject) object).getFlexoConcept() instanceof VirtualModel) {
						FMLObjectImpl.logger
								.info("Not valid for VirtualModel " + ((FlexoConceptObject) object).getFlexoConcept() + " " + b);
						proposal = proposal.replace("virtualModelInstance.virtualModelDefinition", "this.virtualModel");
						proposal = proposal.replace("virtualModelInstance", "this");
					}
					else {
						FMLObjectImpl.logger.info("Not valid for Concept " + ((FlexoConceptObject) object).getFlexoConcept() + " " + b);
						proposal = proposal.replace("virtualModelInstance", "container");
						proposal = proposal.replace("flexoConceptInstance", "this");
					}
					if (!proposal.equals(b.toString())) {
						FMLObjectImpl.logger.info("DataBinding validation: providing proposal " + proposal + " instead of " + b.toString());
						returned.addToFixProposals(new UseProposedBinding<>(b, proposal));
					}
					else {
						FMLObjectImpl.logger
								.info("DataBinding validation: cannot find any proposal " + proposal + " instead of " + b.toString());
					}
				}

				return returned;
				// return new ValidationError<BindingIsRequiredAndMustBeValid<C>, C>(this, object,
				// BindingIsRequiredAndMustBeValid.this.getRuleName(), "Binding: " + getBinding(object) + " reason: "
				// + getBinding(object).invalidBindingReason());
			}
			return null;
		}

		protected static class UseProposedBinding<C extends FMLObject> extends FixProposal<BindingIsRequiredAndMustBeValid<C>, C> {

			private DataBinding<?> binding;
			private String proposedValue;

			public UseProposedBinding(DataBinding<?> binding, String proposedValue) {
				super("sets_value_to_($proposedValue)");
				this.binding = binding;
				this.proposedValue = proposedValue;
			}

			public DataBinding<?> getBinding() {
				return binding;
			}

			public String getProposedValue() {
				return proposedValue;
			}

			@Override
			protected void fixAction() {
				binding.setUnparsedBinding(proposedValue);
				// binding.markedAsToBeReanalized();
			}
		}

		public static class UndefinedRequiredBindingIssue<C extends FMLObject>
				extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

			@SafeVarargs
			public UndefinedRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
					FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_was_not_set", fixProposals);
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

		public static class InvalidRequiredBindingIssue<C extends FMLObject>
				extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

			@SafeVarargs
			public InvalidRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
					FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
				super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_value_is_invalid: ($binding)", fixProposals);

				/*System.out.println("InvalidRequiredBindingIssue:");
				System.out.println("object: " + anObject);
				System.out.println(anObject.getFMLRepresentation());
				System.out.println("binding=" + rule.getBinding(anObject));
				System.out.println("reason=" + rule.getBinding(anObject).invalidBindingReason());*/
			}

			public DataBinding<?> getBinding() {
				return getCause().getBinding(getValidable());
			}

			public String getReason() {
				return getBinding().invalidBindingReason();
			}

			@Override
			public String getDetailedInformations() {
				return "($reason)";
			}
		}

	}

}
