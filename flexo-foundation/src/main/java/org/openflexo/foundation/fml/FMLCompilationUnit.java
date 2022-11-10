/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.fml.binding.CompilationUnitBindingModel;
import org.openflexo.foundation.fml.binding.NamedImportBindingVariable;
import org.openflexo.foundation.fml.binding.NamespaceBindingVariable;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.model.PAMELAVisitor;
import org.openflexo.pamela.model.PAMELAVisitor.VisitingStrategy;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.rm.BasicResourceImpl.LocatorNotFoundException;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.JavaUtils;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(FMLCompilationUnit.FMLCompilationUnitImpl.class)
public interface FMLCompilationUnit extends FMLObject, FMLPrettyPrintable, ResourceData<FMLCompilationUnit>, BindingEvaluationContext {

	public static final String RESOURCE = "resource";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = JavaImportDeclaration.class, cardinality = Cardinality.LIST)
	public static final String JAVA_IMPORTS_KEY = "javaImports";
	@PropertyIdentifier(type = ElementImportDeclaration.class, cardinality = Cardinality.LIST)
	public static final String ELEMENT_IMPORTS_KEY = "elementImports";
	@PropertyIdentifier(type = NamespaceDeclaration.class, cardinality = Cardinality.LIST)
	public static final String NAMESPACES_KEY = "namespaces";
	@PropertyIdentifier(type = UseModelSlotDeclaration.class, cardinality = Cardinality.LIST)
	public static final String USE_DECLARATIONS_KEY = "useDeclarations";
	@PropertyIdentifier(type = VirtualModel.class)
	public static final String VIRTUAL_MODEL_KEY = "virtualModel";

	/**
	 * Return list of {@link JavaImportDeclaration} explicitely declared in this {@link FMLCompilationUnit}
	 * 
	 * @return
	 */
	@Getter(value = JAVA_IMPORTS_KEY, cardinality = Cardinality.LIST, inverse = JavaImportDeclaration.COMPILATION_UNIT_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<JavaImportDeclaration> getJavaImports();

	@Adder(JAVA_IMPORTS_KEY)
	public void addToJavaImports(JavaImportDeclaration javaImportDeclaration);

	@Remover(JAVA_IMPORTS_KEY)
	public void removeFromJavaImports(JavaImportDeclaration javaImportDeclaration);

	@Reindexer(JAVA_IMPORTS_KEY)
	public void moveJavaImportDeclarationToIndex(JavaImportDeclaration javaImportDeclaration, int index);

	/**
	 * Return list of {@link ElementImportDeclaration} explicitely declared in this {@link FMLCompilationUnit}
	 * 
	 * @return
	 */
	@Getter(value = ELEMENT_IMPORTS_KEY, cardinality = Cardinality.LIST, inverse = ElementImportDeclaration.COMPILATION_UNIT_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	public List<ElementImportDeclaration> getElementImports();

	@Adder(ELEMENT_IMPORTS_KEY)
	public void addToElementImports(ElementImportDeclaration elementImportDeclaration);

	@Remover(ELEMENT_IMPORTS_KEY)
	public void removeFromElementImports(ElementImportDeclaration elementImportDeclaration);

	@Finder(collection = ELEMENT_IMPORTS_KEY, attribute = ElementImportDeclaration.ABBREV_KEY)
	public ElementImportDeclaration getElementImport(String abbrev);

	@Reindexer(ELEMENT_IMPORTS_KEY)
	public void moveElementImportDeclarationToIndex(ElementImportDeclaration elementImportDeclaration, int index);

	/**
	 * Return the {@link VirtualModel} defined by this FMLCompilationUnit
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODEL_KEY, inverse = VirtualModel.COMPILATION_UNIT_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public VirtualModel getVirtualModel();

	@Setter(VIRTUAL_MODEL_KEY)
	public void setVirtualModel(VirtualModel virtualModel);

	/**
	 * Return list of {@link NamespaceDeclaration} explicitely declared in this {@link FMLCompilationUnit}
	 * 
	 * @return
	 */
	@Getter(value = NAMESPACES_KEY, cardinality = Cardinality.LIST, inverse = NamespaceDeclaration.COMPILATION_UNIT_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<NamespaceDeclaration> getNamespaces();

	@Adder(NAMESPACES_KEY)
	@PastingPoint
	public void addToNamespaces(NamespaceDeclaration nsDecl);

	@Remover(NAMESPACES_KEY)
	public void removeFromNamespaces(NamespaceDeclaration nsDecl);

	/**
	 * Return list of {@link UseModelSlotDeclaration} accessible from this {@link FMLCompilationUnit}<br>
	 * It includes the list of uses declarations accessible from parent and container
	 * 
	 * @return
	 */
	public List<UseModelSlotDeclaration> getAccessibleUseDeclarations();

	/**
	 * Return list of {@link UseModelSlotDeclaration} explicitely declared in this {@link VirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = USE_DECLARATIONS_KEY, cardinality = Cardinality.LIST, inverse = UseModelSlotDeclaration.COMPILATION_UNIT_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<UseModelSlotDeclaration> getUseDeclarations();

	// @Setter(USE_DECLARATIONS_KEY)
	// public void setUseDeclarations(List<UseModelSlotDeclaration> useDecls);

	@Adder(USE_DECLARATIONS_KEY)
	@PastingPoint
	public void addToUseDeclarations(UseModelSlotDeclaration useDecl);

	@Remover(USE_DECLARATIONS_KEY)
	public void removeFromUseDeclarations(UseModelSlotDeclaration useDecl);

	@Reindexer(USE_DECLARATIONS_KEY)
	public void moveUseModelSlotDeclarationToIndex(UseModelSlotDeclaration useModelSlotDeclaration, int index);

	/**
	 * Return boolean indicating if this VirtualModel uses supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Deprecated
	public <MS extends ModelSlot<?>> boolean uses(Class<MS> modelSlotClass);

	/**
	 * Declare use of supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	@Deprecated
	public <MS extends ModelSlot<?>> UseModelSlotDeclaration declareUse(Class<MS> modelSlotClass);

	/**
	 * Return resource for this virtual model
	 * 
	 * @return
	 */
	@Override
	@Getter(value = RESOURCE, ignoreType = true)
	// @CloningStrategy(value = StrategyType.FACTORY, factory = "cloneResource()")
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoResource<FMLCompilationUnit> getResource();

	/**
	 * Sets resource for this virtual model
	 * 
	 * @param aName
	 */
	@Override
	@Setter(value = RESOURCE)
	public void setResource(FlexoResource<FMLCompilationUnit> aCompilationUnitResource);

	/**
	 * Convenient method used to retrieved {@link CompilationUnitResource}
	 * 
	 * @return
	 */
	public CompilationUnitResource getVirtualModelResource();

	/**
	 * Version of encoded {@link VirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getVersion();

	@Setter(VERSION_KEY)
	public void setVersion(FlexoVersion version);

	/**
	 * Version of FML meta-model
	 * 
	 * @return
	 */
	/*@Getter(value = MODEL_VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getModelVersion();*/

	/*@Setter(MODEL_VERSION_KEY)
	public void setModelVersion(FlexoVersion modelVersion);*/

	public LocalizedDelegate getLocalizedDictionary();

	// TODO: desambiguate this method while proposing two methods: getFlexoConceptNamed() and getFlexoConceptWithURI()
	/**
	 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
	 *
	 * Look in contained VirtualModel and contained FlexoConcept, and examine dependencies (imports)<br>
	 * TODO: presents algorithm (semantics of first found concept, think of inheritance and embedding)
	 * 
	 * @param flexoConceptNameOrURI
	 * @return
	 */
	@Deprecated
	public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI);

	/**
	 * Search and return {@link FlexoConcept} with supplied local name, given the context of this {@link FMLCompilationUnit}<br>
	 * 
	 * Lookup algorithm follows:
	 * <ul>
	 * <li>First lookup in contained {@link VirtualModel}</li>
	 * <li>When not found, apply the same algorithm for each FMLCompilationUnit import of this {@link FMLCompilationUnit} (in the order they
	 * are declared : the first found is returned)</li>
	 * </ul>
	 * 
	 * @param conceptName
	 * @return
	 */
	public FlexoConcept lookupFlexoConceptWithName(String conceptName);

	/**
	 * Search and return {@link FlexoConcept} with supplied URI
	 * 
	 * @param conceptURI
	 * @return
	 */
	public FlexoConcept lookupFlexoConceptWithURI(String conceptURI);

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link VirtualModel}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter> getRequiredTechnologyAdapters();

	/**
	 * Load eventually unloaded contained VirtualModels<br>
	 * After this call return, we can safely assert that all contained {@link VirtualModel} are loaded.
	 */
	void loadContainedVirtualModelsWhenUnloaded();

	@Deprecated
	public VirtualModel getVirtualModelNamed(String virtualModelNameOrURI);

	public FMLObject getObject(String objectURI);

	/**
	 * Analyze the whole structure of the compilation unit, and declare required imports
	 */
	public void manageImports();

	/**
	 * Ensures that supplied modelSlotClass is present in use declarations
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public UseModelSlotDeclaration ensureUse(Class<? extends ModelSlot<?>> modelSlotClass);

	/**
	 * Ensure that supplied {@link FlexoResourceCenter} is refererenced in namespaces
	 * 
	 * @param rc
	 * @return
	 */
	public NamespaceDeclaration ensureNamespaceDeclaration(FlexoResourceCenter<?> rc);

	/**
	 * Ensures that the supplied RC will be referenced in element import of this {@link FMLCompilationUnit}
	 * 
	 * @param rc
	 * @return
	 */
	public ElementImportDeclaration ensureResourceCenterImport(FlexoResourceCenter<?> rc);

	public <RD extends ResourceData<RD> & FlexoObject> ElementImportDeclaration ensureResourceImport(RD resourceData);

	public <RD extends ResourceData<RD> & FlexoObject, R extends FlexoResource<RD>> ElementImportDeclaration ensureResourceImport(
			R resource) throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException;

	public <RD extends ResourceData<RD> & FlexoObject, E extends InnerResourceData<RD> & FlexoObject> ElementImportDeclaration ensureElementImport(
			E element);

	public void ensureJavaImport(Class<?> javaClass);

	public FMLTypingSpace getTypingSpace();

	public boolean isFMLPrettyPrintAvailable();

	public abstract class FMLCompilationUnitImpl extends FMLObjectImpl implements FMLCompilationUnit {

		private static final Logger logger = Logger.getLogger(FMLCompilationUnitImpl.class.getPackage().getName());

		private CompilationUnitResource resource;
		private BindingEvaluationContext reflectedBindingEvaluationContext = new ReflectedBindingEvaluationContext();
		private CompilationUnitBindingModel bindingModel;
		private final FMLTypingSpace typingSpace = new FMLTypingSpace(this);

		class ReflectedBindingEvaluationContext implements BindingEvaluationContext {

			@Override
			public ExpressionEvaluator getEvaluator() {
				return new FMLExpressionEvaluator(this);
			}

			@Override
			public Object getValue(BindingVariable bindingVariable) {
				// System.out.println("getValue() for " + bindingVariable + " of " + bindingVariable.getClass());
				if (bindingVariable instanceof NamedImportBindingVariable) {
					FlexoObject referencedObject = ((NamedImportBindingVariable) bindingVariable).getElementImportDeclaration()
							.getReferencedObject();
					// System.out.println("referencedObject=" + referencedObject);
					if (referencedObject instanceof FlexoResourceCenter<?>) {
						return ((FlexoResourceCenter<?>) referencedObject).getDefaultBaseURI();
					}
					if (referencedObject instanceof FlexoResource) {
						return ((FlexoResource) referencedObject).getURI();
					}
					if (referencedObject instanceof ResourceData) {
						return ((ResourceData) referencedObject).getResource().getURI();
					}
					if (referencedObject instanceof FlexoConcept) {
						return ((FlexoConcept) referencedObject).getURI();
					}
					if (referencedObject instanceof FlexoProperty) {
						return ((FlexoProperty) referencedObject).getURI();
					}
					if (referencedObject instanceof FlexoBehaviour) {
						return ((FlexoBehaviour) referencedObject).getURI();
					}
				}
				if (bindingVariable instanceof NamespaceBindingVariable) {
					return ((NamespaceBindingVariable) bindingVariable).getNamespaceDeclaration().getValue();
				}
				return null;
			}

		}

		/*public FMLCompilationUnitImpl() {
			super();
			System.out.println("Creating new FMLCompilationUnitImpl");
			System.out.println("hash=" + Integer.toHexString(hashCode()));
			Thread.dumpStack();
		}*/

		/**
		 * Return reflected BindingEvaluationContext, obtained at metadata conceptual level
		 * 
		 * @return
		 */
		@Override
		public BindingEvaluationContext getReflectedBindingEvaluationContext() {
			return reflectedBindingEvaluationContext;
		}

		@Override
		public FMLModelFactory getFMLModelFactory() {

			if (getResource() != null) {
				return getResource().getFactory();
			}
			else {
				return getDeserializationFactory();
			}
		}

		private BindingFactory BINDING_FACTORY;

		@Override
		public BindingFactory getBindingFactory() {
			if (BINDING_FACTORY == null) {
				BINDING_FACTORY = new CompilationUnitBindingFactory(this);
			}
			return BINDING_FACTORY;
		}

		@Override
		public FMLTypingSpace getTypingSpace() {
			return typingSpace;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			return this;
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				if (!getResource().getName().endsWith(CompilationUnitResourceFactory.FML_SUFFIX)) {
					return getResource().getName() + CompilationUnitResourceFactory.FML_SUFFIX;
				}
				return getResource().getName();
			}
			return super.getName();
		}

		@Override
		public String toString() {
			return "FMLCompilationUnit[" + getVirtualModel() + "]";
		}

		@Override
		public CompilationUnitBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new CompilationUnitBindingModel(this);
				getPropertyChangeSupport().firePropertyChange(Bindable.BINDING_MODEL_PROPERTY, null, bindingModel);
			}
			return bindingModel;
		}

		@Override
		public CompilationUnitResource getResource() {
			return resource;
		}

		@Override
		public void setResource(FlexoResource<FMLCompilationUnit> resource) {
			this.resource = (CompilationUnitResource) resource;
			if (getVirtualModel() != null) {
				getVirtualModel().getBindingModel().update();
			}
		}

		/**
		 * Convenient method used to retrieved {@link CompilationUnitResource}
		 * 
		 * @return
		 */
		@Override
		public CompilationUnitResource getVirtualModelResource() {
			return getResource();
		}

		@Override
		public void finalizeDeserialization() {
			if (getVirtualModel() != null) {
				getVirtualModel().finalizeDeserialization();
			}
			super.finalizeDeserialization();
		}

		@Override
		public FlexoVersion getVersion() {
			if (getVirtualModel() != null) {
				return getVirtualModel().getVersion();
			}
			if (getResource() != null) {
				return getResource().getVersion();
			}
			return null;
		}

		@Override
		public void setVersion(FlexoVersion aVersion) {
			if (requireChange(getVersion(), aVersion)) {
				if (getVirtualModel() != null) {
					getVirtualModel().setVersion(aVersion);
				}
				if (getResource() != null) {
					getResource().setVersion(aVersion);
				}
			}
		}

		@Override
		@Deprecated
		public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI) {
			if (getVirtualModel() != null) {
				if (getVirtualModel().getName().equals(flexoConceptNameOrURI)) {
					return getVirtualModel();
				}
				if (getVirtualModel().getURI().equals(flexoConceptNameOrURI)) {
					return getVirtualModel();
				}

				FlexoConcept returned = getVirtualModel().getFlexoConcept(flexoConceptNameOrURI);
				if (returned != null) {
					return returned;
				}
			}

			for (ElementImportDeclaration importDeclaration : getElementImports()) {
				try {
					String resourceURI = null;
					Object resourceRef = importDeclaration.getResourceReference().getBindingValue(this);
					if (resourceRef instanceof String) {
						resourceURI = (String) resourceRef;
					}
					else if (resourceRef instanceof ResourceData) {
						resourceURI = ((ResourceData) resourceRef).getResource().getURI();
					}
					else {
						logger.warning("Unexpected resourceRef: " + resourceRef + " for " + importDeclaration);
						continue;
					}
					if (getServiceManager() == null || getServiceManager().getResourceManager() == null) {
						logger.warning("Unexpected null ResourceManager");
						return null;
					}
					FlexoResource resource = getServiceManager().getResourceManager().getResource(resourceURI);
					if (resource instanceof CompilationUnitResource && resource.isLoaded()) {
						FMLCompilationUnit importedCompilationUnit = ((CompilationUnitResource) resource).getCompilationUnit();
						FlexoConcept returned = importedCompilationUnit.getFlexoConcept(flexoConceptNameOrURI);
						if (returned != null) {
							return returned;
						}
					}
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		public FlexoConcept lookupFlexoConceptWithURI(String conceptURI) {
			// TODO rewrite this
			return getFlexoConcept(conceptURI);
		}

		@Override
		public FlexoConcept lookupFlexoConceptWithName(String conceptName) {

			FlexoConcept returned = getVirtualModel().lookupFlexoConceptWithName(conceptName);
			if (returned != null) {
				return returned;
			}

			for (ElementImportDeclaration importDeclaration : getElementImports()) {

				// System.out.println(" > Import " + importDeclaration);
				try {
					String resourceURI = null;
					Object resourceRef = importDeclaration.getResourceReference().getBindingValue(this);
					// System.out.println("resourceRef=" + resourceRef);
					if (resourceRef instanceof String) {
						resourceURI = (String) resourceRef;
					}
					else if (resourceRef instanceof ResourceData) {
						resourceURI = ((ResourceData) resourceRef).getResource().getURI();
					}
					else {
						logger.warning("Unexpected resourceRef: " + resourceRef + " for " + importDeclaration);
						continue;
					}
					if (getServiceManager() == null || getServiceManager().getResourceManager() == null) {
						logger.warning("Unexpected null ResourceManager");
						return null;
					}
					FlexoResource resource = getServiceManager().getResourceManager().getResource(resourceURI);
					// System.out.println("resource: " + resource + " loaded: " + resource.isLoaded());
					if (resource instanceof CompilationUnitResource && resource.isLoaded()) {
						FMLCompilationUnit importedCompilationUnit = ((CompilationUnitResource) resource).getCompilationUnit();
						returned = importedCompilationUnit.lookupFlexoConceptWithName(conceptName);
						if (returned != null) {
							return returned;
						}
					}
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public FMLObject getObject(String objectURI) {
			return getVirtualModelLibrary().getFMLObject(objectURI, true);
		}

		@Override
		public boolean delete(Object... context) {

			// Unregister the resource from the virtual model library
			if (getResource() != null && getVirtualModelLibrary() != null) {
				getVirtualModelLibrary().unregisterCompilationUnit(getResource());
			}

			/*if (bindingModel != null) {
				bindingModel.delete();
			}*/

			boolean returned = performSuperDelete(context);

			// Delete observers
			deleteObservers();

			return returned;
		}

		@Override
		public VirtualModelLibrary getVirtualModelLibrary() {
			if (getResource() != null) {
				return getResource().getVirtualModelLibrary();
			}
			return null;
		}

		/**
		 * Return the list of {@link TechnologyAdapter} used in the context of this {@link FMLCompilationUnit}
		 * 
		 * @return
		 */
		@Override
		public List<TechnologyAdapter> getRequiredTechnologyAdapters() {

			// TODO: implement this with #use
			List<TechnologyAdapter> returned = new ArrayList<>();
			returned.add(getTechnologyAdapter());
			for (ModelSlot<?> ms : getVirtualModel().getModelSlots()) {
				if (!returned.contains(ms.getModelSlotTechnologyAdapter())) {
					returned.add(ms.getModelSlotTechnologyAdapter());
				}
			}
			loadContainedVirtualModelsWhenUnloaded();
			for (VirtualModel vm : getVirtualModel().getVirtualModels()) {
				for (TechnologyAdapter<?> ta : vm.getCompilationUnit().getRequiredTechnologyAdapters()) {
					if (!returned.contains(ta)) {
						returned.add(ta);
					}
				}
			}
			return returned;
		}

		private boolean isLoading = false;

		/**
		 * Load eventually unloaded VirtualModels<br>
		 * After this call return, we can safely assert that all {@link VirtualModel} are loaded.
		 */
		@Override
		public void loadContainedVirtualModelsWhenUnloaded() {
			if (isLoading) {
				return;
			}
			if (!isLoading) {
				isLoading = true;
				if (getResource() != null) {
					for (org.openflexo.foundation.resource.FlexoResource<?> r : getResource().getContents()) {
						if (r instanceof CompilationUnitResource) {
							((CompilationUnitResource) r).getCompilationUnit();
						}
					}
				}
			}

			isLoading = false;
		}

		/**
		 * Return {@link VirtualModel} with supplied name or URI
		 * 
		 * @return
		 */
		@Override
		@Deprecated
		public VirtualModel getVirtualModelNamed(String virtualModelNameOrURI) {

			if (getResource() != null) {
				VirtualModel returned = getContainedVirtualModelNamed(getResource(), virtualModelNameOrURI);
				if (returned != null) {
					return returned;
				}
			}

			if (getVirtualModel().getContainerVirtualModel() != null) {
				return getVirtualModel().getContainerVirtualModel().getVirtualModelNamed(virtualModelNameOrURI);
			}

			if (getVirtualModelLibrary() != null) {
				try {
					return getVirtualModelLibrary().getVirtualModel(virtualModelNameOrURI);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}
			}

			// Not found
			return null;
		}

		private VirtualModel getContainedVirtualModelNamed(CompilationUnitResource resource, String virtualModelNameOrURI) {

			if (resource != null) {
				for (CompilationUnitResource vmRes : resource.getContents(CompilationUnitResource.class)) {
					if (vmRes.getName().equals(virtualModelNameOrURI)) {
						return vmRes.getCompilationUnit().getVirtualModel();
					}
					if (vmRes.getURI().equals(virtualModelNameOrURI)) {
						return vmRes.getCompilationUnit().getVirtualModel();
					}
					VirtualModel returned = getContainedVirtualModelNamed(vmRes, virtualModelNameOrURI);
					if (returned != null) {
						return returned;
					}
				}
			}

			// Not found
			return null;
		}

		private LocalizedDelegateImpl localized;

		private Resource getLocalizedDirectoryResource() {
			Resource virtualModelDirectory = getResource().getIODelegate().getSerializationArtefactAsResource().getContainer();
			List<? extends Resource> localizedDirs = virtualModelDirectory.getContents(Pattern.compile(".*/Localized"), false);
			if (localizedDirs.size() > 0) {
				return localizedDirs.get(0);
			}
			if (virtualModelDirectory instanceof FileResourceImpl) {
				try {
					return new FileResourceImpl(virtualModelDirectory.getLocator(),
							new File(((FileResourceImpl) virtualModelDirectory).getFile(), "Localized"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LocatorNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.warning("Cannot find localized directory for " + this);
			return null;
		}

		private LocalizedDelegateImpl instantiateOrLoadLocales() {
			if (getResource() != null) {
				Resource localizedDirectoryResource = getLocalizedDirectoryResource();
				if (localizedDirectoryResource == null) {
					return null;
				}
				boolean editSupport = getResource().getIODelegate().getSerializationArtefactAsResource() instanceof FileResourceImpl;
				logger.info("Reading locales from " + localizedDirectoryResource);
				LocalizedDelegateImpl returned = new LocalizedDelegateImpl(localizedDirectoryResource,
						getVirtualModel().getContainerVirtualModel() != null ? getVirtualModel().getContainerVirtualModel().getLocales()
								: getServiceManager().getLocalizationService().getFlexoLocalizer(),
						editSupport, editSupport);
				returned.setLocalizationRetriever(new Runnable() {
					@Override
					public void run() {
						searchNewLocalizedEntries();
					}
				});
				return returned;

			}
			return null;
		}

		@Override
		public LocalizedDelegate getLocalizedDictionary() {
			if (localized == null) {
				localized = instantiateOrLoadLocales();
				if (localized == null) {
					// Cannot load locales
					if (getServiceManager() != null) {
						return getServiceManager().getLocalizationService().getFlexoLocalizer();
					}
					return null;
				}
				// Converting old dictionaries
				/*if (getDeprecatedLocalizedDictionary() != null) {
					for (FMLLocalizedEntry fmlLocalizedEntry : getDeprecatedLocalizedDictionary().getLocalizedEntries()) {
						localized.registerNewEntry(fmlLocalizedEntry.getKey(), Language.get(fmlLocalizedEntry.getLanguage()),
								fmlLocalizedEntry.getValue());
					}
				}*/
			}
			return localized;
		}

		public void createLocalizedDictionaryWhenNonExistant() {
			if (localized == null) {
				logger.fine("createLocalizedDictionary for " + this);
				localized = instantiateOrLoadLocales();
			}
		}

		/*@Override
		public FMLLocalizedDictionary getDeprecatedLocalizedDictionary() {
			if (isSerializing()) {
				return null;
			}
			return (FMLLocalizedDictionary) performSuperGetter(LOCALIZED_DICTIONARY_KEY);
		}*/

		private void searchNewEntriesForConcept(FlexoConcept concept) {
			// checkAndRegisterLocalized(concept.getName());
			for (FlexoBehaviour es : concept.getFlexoBehaviours()) {
				// checkAndRegisterLocalized(es.getName(), normalizedKey -> es.setLabel(normalizedKey));
				// checkAndRegisterLocalized(es.getDescription());
				for (FlexoBehaviourParameter p : es.getParameters()) {
					checkAndRegisterLocalized(p.getName());
				}
				for (InspectorEntry entry : concept.getInspector().getEntries()) {
					checkAndRegisterLocalized(entry.getLabel(), normalizedKey -> entry.setLabel(normalizedKey));
				}
			}
		}

		private void searchNewLocalizedEntries() {
			logger.info("Search new entries for " + this);

			CompoundEdit ce = null;
			FMLModelFactory factory = null;

			factory = getFMLModelFactory();
			if (factory != null) {
				if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					ce = factory.getEditingContext().getUndoManager().startRecording("localize_virtual_model");
				}
			}

			searchNewEntriesForConcept(getVirtualModel());

			for (FlexoConcept concept : getVirtualModel().getFlexoConcepts()) {
				searchNewEntriesForConcept(concept);
			}

			if (factory != null) {
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
				else if (factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					factory.getEditingContext().getUndoManager()
							.stopRecording(factory.getEditingContext().getUndoManager().getCurrentEdition());
				}
			}

			// getViewPoint().setChanged();
			// getViewPoint().notifyObservers();
		}

		private String checkAndRegisterLocalized(String key, Consumer<String> updateKey) {

			// System.out.println("checkAndRegisterLocalized for " + key);
			if (StringUtils.isEmpty(key)) {
				return null;
			}

			String normalizedKey = StringUtils.toLocalizedKey(key.trim());

			if (!key.equals(normalizedKey)) {
				updateKey.accept(normalizedKey);
			}

			getLocalizedDictionary().addEntry(normalizedKey);
			return normalizedKey;
		}

		private String checkAndRegisterLocalized(String key) {

			// System.out.println("checkAndRegisterLocalized for " + key);
			if (StringUtils.isEmpty(key)) {
				return null;
			}

			getLocalizedDictionary().addEntry(key);
			return key;
		}

		/**
		 * Return list of {@link UseModelSlotDeclaration} accessible from this {@link VirtualModel}<br>
		 * It includes the list of uses declarations accessible from parent and container
		 * 
		 * @return
		 */
		@Override
		public List<UseModelSlotDeclaration> getAccessibleUseDeclarations() {
			// TODO: make better implementation
			if (getVirtualModel() != null) {
				return getVirtualModel().getAccessibleUseDeclarations();
			}
			return null;
		}

		@Override
		public <MS extends ModelSlot<?>> boolean uses(Class<MS> modelSlotClass) {
			// TODO: make better implementation
			if (getVirtualModel() != null) {
				return getVirtualModel().uses(modelSlotClass);
			}
			return false;
		}

		@Override
		public <MS extends ModelSlot<?>> UseModelSlotDeclaration declareUse(Class<MS> modelSlotClass) {
			// TODO: make better implementation
			if (getVirtualModel() != null) {
				return getVirtualModel().declareUse(modelSlotClass);
			}
			return null;
		}

		@Override
		public boolean isFMLPrettyPrintAvailable() {
			if (isManagingImports) {
				return false;
			}
			return true;
		}

		@Override
		public String getFMLPrettyPrint() {
			manageImports();
			return super.getFMLPrettyPrint();
		}

		private boolean isManagingImports = false;

		/**
		 * Analyze the whole structure of the compilation unit, and declare required imports
		 */
		@Override
		public void manageImports() {

			// System.out.println("--------------> manageImports() in " + this);

			if (isManagingImports) {
				return;
			}

			isManagingImports = true;

			accept(new PAMELAVisitor() {

				@Override
				public void visit(Object object) {
					if (object instanceof FMLObject && ((FMLObject) object).getDeclaringCompilationUnit() == FMLCompilationUnitImpl.this) {
						// System.out.println("> Visiting " + object + " of " + object.getClass().getSimpleName());
						((FMLObject) object).handleRequiredImports(FMLCompilationUnitImpl.this);
					}
				}
			}, VisitingStrategy.Exhaustive);

			isManagingImports = false;

		}

		private ElementImportDeclaration retrieveImportDeclaration(FlexoObject object) {
			for (ElementImportDeclaration elementImportDeclaration : getElementImports()) {
				if (elementImportDeclaration.getReferencedObject() == object) {
					return elementImportDeclaration;
				}
				if (elementImportDeclaration.getReferencedObject() != null
						&& elementImportDeclaration.getReferencedObject().equalsObject(object)) {
					return elementImportDeclaration;
				}
				if (elementImportDeclaration.getReferencedObject() instanceof FlexoProject) {
					if (((FlexoProject) elementImportDeclaration.getReferencedObject()).getDelegateResourceCenter() == object) {
						// Special case for FlexoProject and delegate ResourceCenter
						return elementImportDeclaration;
					}
				}
				// For FMLCompilationUnit, look at URI
				if (object instanceof FMLCompilationUnit && ((FMLCompilationUnit) object).getVirtualModel() != null
						&& elementImportDeclaration.getReferencedObject() instanceof FMLCompilationUnit
						&& ((FMLCompilationUnit) elementImportDeclaration.getReferencedObject()).getVirtualModel() != null) {
					if ((((FMLCompilationUnit) object).getVirtualModel().getURI()
							.equals(((FMLCompilationUnit) elementImportDeclaration.getReferencedObject()).getVirtualModel().getURI()))) {
						return elementImportDeclaration;
					}
				}
				// For concepts, look at URI too
				if (object instanceof FlexoConcept && elementImportDeclaration.getReferencedObject() instanceof FlexoConcept) {
					if ((((FlexoConcept) object).getURI()
							.equals(((FlexoConcept) elementImportDeclaration.getReferencedObject()).getURI()))) {
						return elementImportDeclaration;
					}
				}
			}
			return null;
		}

		private String findUniqueRCAbbrev(FlexoResourceCenter<?> rc) {
			String initialName = rc.getName();
			if (initialName.contains("/")) {
				initialName = initialName.substring(initialName.lastIndexOf("/"));
			}
			if (initialName.contains("\\")) {
				initialName = initialName.substring(initialName.lastIndexOf("\\"));
			}
			String baseName = JavaUtils.getJavaName(initialName).toUpperCase();
			String returned = baseName;
			int i = 2;
			while (getElementImport(returned) != null) {
				returned = baseName + i;
				i++;
			}
			return returned;
		}

		private <RD extends ResourceData<?> & FlexoObject> String findUniqueAbbrev(RD resourceData) {
			if (resourceData instanceof FMLCompilationUnit) {
				return resourceData.getResource().getName();
			}
			String initialName = resourceData.getResource().getName();
			if (initialName.contains(".")) {
				initialName = initialName.substring(0, initialName.lastIndexOf("."));
			}
			if (initialName.contains("/")) {
				initialName = initialName.substring(initialName.lastIndexOf("/"));
			}
			if (initialName.contains("\\")) {
				initialName = initialName.substring(initialName.lastIndexOf("\\"));
			}
			String baseName = JavaUtils.getJavaName(initialName);
			baseName = JavaUtils.getConstantJavaName(baseName);
			// TODO remove this code
			// baseName = baseName.replace("_", "");
			String returned = baseName;
			int i = 2;
			while (getElementImport(returned) != null) {
				returned = baseName + i;
				i++;
			}
			return returned;
		}

		private <RD extends ResourceData<RD> & FlexoObject, E extends InnerResourceData<RD> & FlexoObject> String findUniqueAbbrev(
				E element) {
			String initialName = element.getImplementedInterface().getSimpleName();
			String baseName = JavaUtils.getJavaName(initialName);
			baseName = JavaUtils.getConstantJavaName(baseName);
			// TODO remove this code
			// baseName = baseName.replace("_", "");
			String returned = baseName;
			int i = 2;
			while (getElementImport(returned) != null) {
				returned = baseName + i;
				i++;
			}
			return returned;
		}

		@Override
		public NamespaceDeclaration ensureNamespaceDeclaration(FlexoResourceCenter<?> rc) {
			NamespaceDeclaration nsDeclaration = retrieveNamespaceDeclaration(rc.getDefaultBaseURI());
			if (nsDeclaration == null) {
				nsDeclaration = getFMLModelFactory().newNamespaceDeclaration();
				nsDeclaration.setValue(rc.getDefaultBaseURI());
				nsDeclaration.setAbbrev(findUniqueRCAbbrev(rc));
				addToNamespaces(nsDeclaration);
			}
			return nsDeclaration;
		}

		private NamespaceDeclaration retrieveNamespaceDeclaration(String value) {
			for (NamespaceDeclaration namespaceDeclaration : getNamespaces()) {
				if (value.equals(namespaceDeclaration.getValue())) {
					return namespaceDeclaration;
				}
			}
			return null;
		}

		@Override
		public ElementImportDeclaration ensureResourceCenterImport(FlexoResourceCenter<?> rc) {
			ElementImportDeclaration rcDeclaration = retrieveImportDeclaration(rc);
			if (rcDeclaration == null) {
				rcDeclaration = getFMLModelFactory().newElementImportDeclaration();
				rcDeclaration.setResourceReference(new DataBinding<>('"' + rc.getDefaultBaseURI() + '"'));
				rcDeclaration.setAbbrev(findUniqueRCAbbrev(rc));
				addToElementImports(rcDeclaration);
			}
			return rcDeclaration;
		}

		private List<ResourceData> resourcesRequestedForImport = new ArrayList<>();

		@Override
		public <RD extends ResourceData<RD> & FlexoObject> ElementImportDeclaration ensureResourceImport(RD resourceData) {

			if (resourcesRequestedForImport.contains(resourceData)) {
				return null;
			}

			resourcesRequestedForImport.add(resourceData);

			ElementImportDeclaration importDeclaration = retrieveImportDeclaration(resourceData);
			if (getFMLModelFactory() == null) {
				return importDeclaration;
			}
			if (resourceData == this) {
				return importDeclaration;
			}
			if (importDeclaration == null && resourceData.getResource() != null) {
				FlexoResourceCenter<?> resourceCenter = resourceData.getResource().getResourceCenter();
				String uri = resourceData.getResource().getURI();
				importDeclaration = getFMLModelFactory().newElementImportDeclaration();
				if (uri.startsWith(resourceCenter.getDefaultBaseURI())) {
					NamespaceDeclaration rcNSDeclaration = ensureNamespaceDeclaration(resourceCenter);
					String remainingURI = uri.substring(resourceCenter.getDefaultBaseURI().length());
					importDeclaration.setResourceReference(
							new DataBinding<>(rcNSDeclaration.getAbbrev() + "+\"" + remainingURI + "\"", importDeclaration));
					// System.out.println("---" + rcAbbrev + "+\"" + remainingURI + "\"");
				}
				else {
					importDeclaration.setResourceReference(new DataBinding<>("\"" + uri + "\"", importDeclaration));
				}
				// Don't force a deserializing now: set referenced object
				importDeclaration.setReferencedObject(resourceData);
				String abbrev = findUniqueAbbrev(resourceData);
				importDeclaration.setAbbrev(abbrev);
				addToElementImports(importDeclaration);
			}

			return importDeclaration;
		}

		@Override
		public <RD extends ResourceData<RD> & FlexoObject, E extends InnerResourceData<RD> & FlexoObject> ElementImportDeclaration ensureElementImport(
				E element) {
			if (element == null) {
				return null;
			}
			ElementImportDeclaration elementDeclaration = retrieveImportDeclaration(element);
			if (elementDeclaration == null && getFMLModelFactory() != null) {
				ElementImportDeclaration resourceImport = ensureResourceImport(element.getResourceData());
				String resourceAbbrev = resourceImport.getAbbrev();
				elementDeclaration = getFMLModelFactory().newElementImportDeclaration();
				elementDeclaration.setResourceReference(new DataBinding<>(resourceAbbrev));
				elementDeclaration
						.setObjectReference(new DataBinding<>("\"" + element.getUserIdentifier() + "-" + element.getFlexoID() + "\""));
				// Don't force a deserializing now: set referenced object
				elementDeclaration.setReferencedObject(element);
				String abbrev = findUniqueAbbrev(element);
				elementDeclaration.setAbbrev(abbrev);
				addToElementImports(elementDeclaration);

				// System.out.println("resourceAbbrev: " + resourceAbbrev);
				// System.out.println("resourceReference: " + elementDeclaration.getResourceReference());
				// System.out.println("objectReference: " + elementDeclaration.getObjectReference());
				// System.out.println("abbrev: " + abbrev);
				// System.out.println("au final: " + elementDeclaration.getReferencedObject());

			}
			return elementDeclaration;
		}

		@Override
		public <RD extends ResourceData<RD> & FlexoObject, R extends FlexoResource<RD>> ElementImportDeclaration ensureResourceImport(
				R resource) throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
			return ensureResourceImport(resource.getResourceData());
		}

		@Override
		public void ensureJavaImport(Class<?> javaClass) {
			boolean typeWasFound = false;
			for (JavaImportDeclaration importDeclaration : getJavaImports()) {
				if (importDeclaration.getFullQualifiedClassName().equals(javaClass.getName())) {
					typeWasFound = true;
					break;
				}
			}
			if (!typeWasFound && getFMLModelFactory() != null) {
				// Adding import
				JavaImportDeclaration newJavaImportDeclaration = getFMLModelFactory().newJavaImportDeclaration();
				newJavaImportDeclaration.setFullQualifiedClassName(javaClass.getName());
				addToJavaImports(newJavaImportDeclaration);
			}
		}

		/**
		 * Ensures that supplied modelSlotClass is present in use declarations
		 * 
		 * @param modelSlotClass
		 * @return
		 */
		@Override
		public UseModelSlotDeclaration ensureUse(Class<? extends ModelSlot<?>> modelSlotClass) {
			for (UseModelSlotDeclaration useModelSlotDeclaration : getUseDeclarations()) {
				if (useModelSlotDeclaration.getModelSlotClass().equals(modelSlotClass)) {
					return useModelSlotDeclaration;
				}
			}
			// Not found
			if (getFMLModelFactory() != null) {
				// Adding import
				UseModelSlotDeclaration newUseDeclaration = getFMLModelFactory().newUseModelSlotDeclaration(modelSlotClass);
				TechnologyAdapter ta = getServiceManager().getTechnologyAdapterService().getTechnologyAdapterForModelSlot(modelSlotClass);
				String identifier = JavaUtils.getConstantJavaName(ta.getIdentifier());
				newUseDeclaration.setAbbrev(identifier);
				addToUseDeclarations(newUseDeclaration);
				return newUseDeclaration;
			}
			return null;
		}

		// TODO remove this
		/*@Override
		public void addToElementImports(ElementImportDeclaration elementImportDeclaration) {
			performSuperAdder(ELEMENT_IMPORTS_KEY, elementImportDeclaration);
			System.out.println("Added import " + elementImportDeclaration);
			// Thread.dumpStack();
		}*/

		// TODO remove this
		/*@Override
		public void addToJavaImports(JavaImportDeclaration javaImportDeclaration) {
			performSuperAdder(JAVA_IMPORTS_KEY, javaImportDeclaration);
			System.out.println("Added java import " + javaImportDeclaration);
			Thread.dumpStack();
		}*/

		/**
		 * Implements evaluator in the context of URI resolving in imports
		 */
		@Override
		public ExpressionEvaluator getEvaluator() {
			return new FMLExpressionEvaluator(this);
		}

		/**
		 * Implements BindingEvaluationContext in the context of URI resolving in imports
		 */
		@Override
		public Object getValue(BindingVariable variable) {
			if (variable instanceof NamespaceBindingVariable) {
				return ((NamespaceBindingVariable) variable).getNamespaceDeclaration().getValue();
			}
			if (variable instanceof NamedImportBindingVariable) {
				FlexoObject returned = ((NamedImportBindingVariable) variable).getElementImportDeclaration().getReferencedObject();
				/*System.out.println("For variable " + variable + " returning " + returned);
				if (returned == null) {
					System.out.println("Not found variable: "+variable);
					Thread.dumpStack();
				}*/
				return returned;
			}

			logger.warning("Unexpected BindingVariable " + variable + " of " + variable.getClass());
			return null;
		}

	}

}
