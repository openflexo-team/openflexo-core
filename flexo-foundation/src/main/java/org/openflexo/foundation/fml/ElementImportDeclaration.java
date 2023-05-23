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

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * 
 * 
 * @author sylvain
 *
 */
@ModelEntity
@XMLElement
@ImplementationClass(ElementImportDeclaration.ElementImportDeclarationImpl.class)
public interface ElementImportDeclaration extends FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLCompilationUnit.class)
	public static final String COMPILATION_UNIT_KEY = "compilationUnit";
	@PropertyIdentifier(type = String.class)
	public static final String RESOURCE_REFERENCE_KEY = "resourceReference";
	@PropertyIdentifier(type = String.class)
	public static final String OBJECT_REFERENCE_KEY = "objectReference";
	@PropertyIdentifier(type = String.class)
	public static final String ABBREV_KEY = "abbrev";

	@Getter(value = RESOURCE_REFERENCE_KEY)
	@XMLAttribute
	public DataBinding<String> getResourceReference();

	@Setter(RESOURCE_REFERENCE_KEY)
	public void setResourceReference(DataBinding<String> resourceReference);

	@Getter(value = OBJECT_REFERENCE_KEY)
	@XMLAttribute
	public DataBinding<String> getObjectReference();

	@Setter(OBJECT_REFERENCE_KEY)
	public void setObjectReference(DataBinding<String> objectReference);

	@Getter(value = COMPILATION_UNIT_KEY, inverse = FMLCompilationUnit.ELEMENT_IMPORTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLCompilationUnit getCompilationUnit();

	@Setter(COMPILATION_UNIT_KEY)
	public void setCompilationUnit(FMLCompilationUnit compilationUnit);

	@Getter(value = ABBREV_KEY)
	@XMLAttribute
	public String getAbbrev();

	@Setter(ABBREV_KEY)
	public void setAbbrev(String abbrev);

	/**
	 * Retrieve and return object referenced by this import
	 * 
	 * @return
	 */
	public FlexoObject getReferencedObject();

	/**
	 * Programmatically set referenced object
	 * 
	 * @param referencedObject
	 */
	public void setReferencedObject(FlexoObject referencedObject);

	/**
	 * Clear referenced object
	 */
	public void clearReferencedObject();

	public boolean isReferencedObjectLoaded();

	public static abstract class ElementImportDeclarationImpl extends FMLObjectImpl implements ElementImportDeclaration {

		private static final Logger logger = Logger.getLogger(ElementImportDeclarationImpl.class.getPackage().getName());

		private DataBinding<String> resourceReference;
		private DataBinding<String> objectReference;
		private FlexoObject referencedObject;

		@Override
		public FMLCompilationUnit getResourceData() {
			return getCompilationUnit();
		}

		@Override
		public String toString() {
			return "ElementImportDeclaration(" + getResourceReference() + (getObjectReference() != null ? ":" + getObjectReference() : "")
					+ ")";
		}

		@Override
		public BindingModel getBindingModel() {
			if (getCompilationUnit() != null) {
				return getCompilationUnit().getBindingModel();
			}
			return null;
		}

		@Override
		public BindingFactory getBindingFactory() {
			if (getCompilationUnit() != null) {
				return getCompilationUnit().getBindingFactory();
			}
			return null;
		}

		@Override
		public DataBinding<String> getResourceReference() {
			if (resourceReference == null) {
				resourceReference = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				resourceReference.setBindingName("resourceReference");
				resourceReference.setMandatory(true);

			}
			return resourceReference;
		}

		@Override
		public void setResourceReference(DataBinding<String> resourceReference) {
			if (resourceReference != null) {
				this.resourceReference = resourceReference;
				this.resourceReference.setOwner(this);
				this.resourceReference.setBindingName("resourceReference");
				this.resourceReference.setMandatory(true);
			}
			referencedObject = null;
			notifiedBindingChanged(resourceReference);
		}

		@Override
		public DataBinding<String> getObjectReference() {
			if (objectReference == null) {
				objectReference = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				objectReference.setBindingName("objectReference");
				objectReference.setMandatory(true);

			}
			return objectReference;
		}

		@Override
		public void setObjectReference(DataBinding<String> objectReference) {
			if (objectReference != null) {
				this.objectReference = objectReference;
				this.objectReference.setOwner(this);
				this.objectReference.setBindingName("objectReference");
				this.objectReference.setMandatory(true);
			}
			referencedObject = null;
			notifiedBindingChanged(objectReference);
		}

		@Override
		public boolean isReferencedObjectLoaded() {
			return referencedObject != null;
		}

		@Override
		public FlexoObject getReferencedObject() {
			if (referencedObject == null) {
				try {
					referencedObject = buildReferencedObject();
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return referencedObject;
		}

		@Override
		public void setReferencedObject(FlexoObject referencedObject) {
			this.referencedObject = referencedObject;
		}

		private boolean isBuildingReferencedObject = false;

		private FlexoObject buildReferencedObject() throws TypeMismatchException, NullReferenceException, FileNotFoundException,
				ResourceLoadingCancelledException, FlexoException, ReflectiveOperationException {

			if (isBuildingReferencedObject) {
				return null;
			}

			isBuildingReferencedObject = true;

			try {

				if (getResourceData() != null && getResourceData().getServiceManager() != null) {

					FlexoServiceManager serviceManager = getResourceData().getServiceManager();

					String resourceURI = null;
					if (getResourceReference().isSet() && getResourceReference().isValid()) {
						resourceURI = getResourceReference().getBindingValue(getReflectedBindingEvaluationContext());
					}
					String objectReference = null;
					if (getObjectReference().isSet() && getObjectReference().isValid()) {
						objectReference = getObjectReference().getBindingValue(getReflectedBindingEvaluationContext());
					}

					// System.out.println("On cherche le referenced object pour " + this);
					// System.out.println("ResourceReference: " + getResourceReference());
					// System.out.println("resourceURI=" + resourceURI);

					/*if (getResourceReference().isSet() && getResourceReference().isValid()) {
						System.out.println("----> On cherche l'uri pour " + getResourceReference());
						for (ElementImportDeclaration imp : getDeclaringCompilationUnit().getElementImports()) {
							System.out.println("imp: " + imp);
						}
						resourceURI = getResourceReference().getBindingValue(getReflectedBindingEvaluationContext());
						System.out.println("resourceURI=" + resourceURI);
					}
					else {
						System.out.println("----> not valid: " + getResourceReference());
						System.out.println("reason: " + getResourceReference().invalidBindingReason());
					}*/

					// System.out.println("resourceURI=" + resourceURI);
					// System.out.println("getObjectReference()=" + getObjectReference());
					// System.out.println("Et objectReference=" + objectReference);
					// System.out.println("reason: " + getObjectReference().invalidBindingReason());

					if (resourceURI == null) {
						// logger.warning("Cannot find object with null URI ");
						return null;
					}

					// Is that a ResourceCenter ?
					FlexoResourceCenter<?> rc = serviceManager.getResourceCenterService().getFlexoResourceCenter(resourceURI);
					if (rc != null) {
						return rc;
					}

					// Is that a Resource ?
					FlexoResource<?> resource = serviceManager.getResourceManager().getResource(resourceURI);
					if (resource != null) {

						// System.out.println("resource=" + resource);
						// System.out.println("resource.getLoadedResourceData()=" + resource.getLoadedResourceData());

						// We should have already loaded this resource, otherwise it means that this resource was a cross reference
						ResourceData<?> resourceData = resource.getLoadedResourceData();

						/*if (resourceData instanceof FMLCompilationUnit) {
							return ((FMLCompilationUnit) resourceData).getVirtualModel();
						}*/

						if (objectReference == null) {
							return (FlexoObject) resourceData;
						}

						// Find the right object in resource
						else {
							return findObjectInResource(resource, objectReference);
						}
					}

					logger.warning("Cannot find object with URI " + resourceURI);
					return null;
				}
			} finally {
				isBuildingReferencedObject = false;
			}

			// logger.warning("Cannot access FlexoServiceManager, resourceData=" + getResourceData());

			/*if (getResourceData() instanceof FMLCompilationUnit) {
				FMLCompilationUnitImpl cu = (FMLCompilationUnitImpl) getResourceData();
				System.out.println("vmlib=" + cu.getVirtualModelLibrary());
				System.out.println("dfact=" + cu.getDeserializationFactory());
				System.out.println("sm=" + cu.getServiceManager());
			}
			
			Thread.dumpStack();
			System.exit(-1);*/
			return null;

		}

		private FlexoObject findObjectInResource(FlexoResource<?> resource, String identifier) {

			String userIdentifier = null;
			String objectIdentifier;

			if (identifier.contains("-")) {
				userIdentifier = identifier.substring(0, identifier.indexOf("-"));
				objectIdentifier = identifier.substring(identifier.indexOf("-") + 1);
			}
			else {
				objectIdentifier = identifier;
			}
			try {
				// Ensure the resource is loaded
				resource.getResourceData();
				// System.out.println("On retourne " + resource.findObject(objectIdentifier, userIdentifier));
				return (FlexoObject) resource.findObject(objectIdentifier, userIdentifier);
			} catch (RuntimeException | FileNotFoundException | ResourceLoadingCancelledException | FlexoException e) {
				logger.log(Level.SEVERE, "Error while finding object in resource '" + resource.getURI() + "'", e);
			}
			logger.warning("Cannot find object " + userIdentifier + "_" + objectIdentifier + " in resource " + resource);
			return null;
		}

		/**
		 * Clear referenced object
		 */
		@Override
		public void clearReferencedObject() {
			referencedObject = null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getObjectReference().rebuild();
			getResourceReference().rebuild();
		}

	}

	@DefineValidationRule
	public static class ResourceReferenceBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ElementImportDeclaration> {
		public ResourceReferenceBindingIsRequiredAndMustBeValid() {
			super("'resource_reference'_binding_is_not_valid", ElementImportDeclaration.class);
		}

		@Override
		public DataBinding<String> getBinding(ElementImportDeclaration object) {
			return object.getResourceReference();
		}
	}

	@DefineValidationRule
	public static class ObjectReferenceBindingMustBeValid extends BindingMustBeValid<ElementImportDeclaration> {
		public ObjectReferenceBindingMustBeValid() {
			super("'object_reference'_binding_is_not_valid", ElementImportDeclaration.class);
		}

		@Override
		public DataBinding<String> getBinding(ElementImportDeclaration object) {
			return object.getObjectReference();
		}
	}

	@DefineValidationRule
	public static class ImportDeclarationMustAddressValidReferencedObject
			extends ValidationRule<ImportDeclarationMustAddressValidReferencedObject, ElementImportDeclaration> {
		public ImportDeclarationMustAddressValidReferencedObject() {
			super(ElementImportDeclaration.class, "import_declaration_must_reference_a_valid_object");
		}

		@Override
		public ValidationIssue<ImportDeclarationMustAddressValidReferencedObject, ElementImportDeclaration> applyValidation(
				ElementImportDeclaration importDeclaration) {
			if (importDeclaration.getReferencedObject() == null) {
				return new ValidationError<>(this, importDeclaration, "import_declaration_must_reference_a_valid_object");
			}
			return null;
		}

	}

}
