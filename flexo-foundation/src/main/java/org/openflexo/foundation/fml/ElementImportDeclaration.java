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
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

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
				this.resourceReference = new DataBinding<String>(resourceReference.toString(), this, String.class,
						DataBinding.BindingDefinitionType.GET);
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
				this.objectReference = new DataBinding<String>(objectReference.toString(), this, String.class,
						DataBinding.BindingDefinitionType.GET);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FlexoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return referencedObject;
		}

		private boolean isBuildingReferencedObject = false;

		private FlexoObject buildReferencedObject() throws TypeMismatchException, NullReferenceException, InvocationTargetException,
				FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

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

						ResourceData<?> resourceData = resource.getResourceData();

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

			logger.warning("Cannot access FlexoServiceManager ");
			return null;

		}

		private FlexoObject findObjectInResource(FlexoResource<?> resource, String identifier) {
			String userIdentifier = identifier.substring(0, identifier.indexOf("-"));
			String objectIdentifier = identifier.substring(identifier.indexOf("-") + 1);
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

	}
}
