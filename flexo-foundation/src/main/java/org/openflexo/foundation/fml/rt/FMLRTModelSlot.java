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

package org.openflexo.foundation.fml.rt;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomType;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType.DefaultVirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.VirtualModelInstanceType.VirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;
import org.openflexo.toolbox.StringUtils;

/**
 * A {@link ModelSlot} allowing to access an {@link VirtualModelInstance}<br>
 * 
 * Such {@link ModelSlot} is defining a general contract modellized by an abstract {@link VirtualModel}<br>
 * 
 * There are two different implementations of a {@link FMLRTModelSlot}:
 * <ul>
 * <li>Native implementation (see {@link FMLRTVirtualModelInstanceModelSlot}) provided by {@link FMLRTTechnologyAdapter}</li>
 * <li>Alternative implementation provided by some {@link TechnologyAdapter} which present data as instances of {@link FlexoConcept} (see
 * )</li>
 * </ul>
 * 
 * @author sylvain
 *
 * @param <VMI>
 *            type of {@link VirtualModelInstance} presented by this model slot
 * @param <TA>
 *            technology providing this model slot
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FMLRTVirtualModelInstanceModelSlot.class) })
@ImplementationClass(FMLRTModelSlot.FMLRTModelSlotImpl.class)
public interface FMLRTModelSlot<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>> extends ModelSlot<VMI> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";
	@PropertyIdentifier(type = CompilationUnitResource.class)
	public static final String ACCESSED_VIRTUAL_MODEL_RESOURCE_KEY = "accessedVirtualModelResource";
	@PropertyIdentifier(type = VirtualModel.class)
	public static final String ACCESSED_VIRTUAL_MODEL_KEY = "accessedVirtualModel";

	// This property remains the persistent way to store the accessed VirtualModel
	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute(xmlTag = "virtualModelURI")
	public String getAccessedVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setAccessedVirtualModelURI(String virtualModelURI);

	@Getter(value = ACCESSED_VIRTUAL_MODEL_RESOURCE_KEY, ignoreType = true)
	public CompilationUnitResource getAccessedVirtualModelResource();

	@Setter(ACCESSED_VIRTUAL_MODEL_RESOURCE_KEY)
	public void setAccessedVirtualModelResource(CompilationUnitResource virtualModelResource);

	@Getter(ACCESSED_VIRTUAL_MODEL_KEY)
	public VirtualModel getAccessedVirtualModel();

	@Setter(ACCESSED_VIRTUAL_MODEL_KEY)
	public void setAccessedVirtualModel(VirtualModel aVirtualModel);

	public FlexoConceptInstanceRole makeFlexoConceptInstanceRole(FlexoConcept flexoConcept);

	public Class<TA> getTechnologyAdapterClass();

	public static abstract class FMLRTModelSlotImpl<VMI extends VirtualModelInstance<VMI, TA>, TA extends TechnologyAdapter<TA>>
			extends ModelSlotImpl<VMI> implements FMLRTModelSlot<VMI, TA> {

		private static final Logger logger = Logger.getLogger(FMLRTModelSlot.class.getPackage().getName());

		@Override
		public FlexoConceptInstanceRole makeFlexoConceptInstanceRole(FlexoConcept flexoConcept) {
			FlexoConceptInstanceRole returned = makeFlexoRole(FlexoConceptInstanceRole.class);
			returned.setFlexoConceptType(flexoConcept);
			returned.setModelSlot(this);
			return returned;
		}

		@Override
		public <PR extends FlexoRole<?>> String defaultFlexoRoleName(Class<PR> flexoRoleClass) {
			if (FlexoConceptInstanceRole.class.isAssignableFrom(flexoRoleClass)) {
				return "flexoConceptInstance";
			}
			else if (PrimitiveRole.class.isAssignableFrom(flexoRoleClass)) {
				return "primitive";
			}
			logger.warning("Unexpected role: " + flexoRoleClass.getName());
			return null;
		}

		protected CompilationUnitResource virtualModelResource;

		private String virtualModelURI;

		private boolean isNotifying = false;

		@Override
		public CompilationUnitResource getAccessedVirtualModelResource() {

			if (virtualModelResource == null && StringUtils.isNotEmpty(getAccessedVirtualModelURI()) && getVirtualModelLibrary() != null) {
				virtualModelResource = getVirtualModelLibrary().getCompilationUnitResource(getAccessedVirtualModelURI());
				if (virtualModelResource != null) {
					// logger.info("Looked-up " + virtualModelResource);
					if (!isNotifying) {
						try {
							isNotifying = true;
							getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_KEY, null, getAccessedVirtualModel());
							getPropertyChangeSupport().firePropertyChange(TYPE_KEY, null, getType());
							getPropertyChangeSupport().firePropertyChange("resultingType", null, getResultingType());
						} finally {
							isNotifying = false;
						}
					}
				}
			}
			if (type != null && type.isResolved() && type.getVirtualModel() != null) {
				virtualModelResource = type.getVirtualModel().getCompilationUnitResource();
			}
			return virtualModelResource;
		}

		@Override
		public void setAccessedVirtualModelResource(CompilationUnitResource virtualModelResource) {
			if ((virtualModelResource == null && this.virtualModelResource != null)
					|| (virtualModelResource != null && !virtualModelResource.equals(this.virtualModelResource))) {
				CompilationUnitResource oldResource = getAccessedVirtualModelResource();
				VirtualModel oldVirtualModel = getAccessedVirtualModel();
				Type oldType = getType();
				String oldURI = getAccessedVirtualModelURI();
				this.virtualModelResource = virtualModelResource;
				if (virtualModelResource != null) {
					this.virtualModelURI = virtualModelResource.getURI();
					this.type = virtualModelResource.getCompilationUnit().getVirtualModel().getVirtualModelInstanceType();
				}
				else {
					this.virtualModelURI = null;
					this.type = null;
				}
				getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODEL_URI_KEY, oldURI, getAccessedVirtualModelURI());
				getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_RESOURCE_KEY, oldResource,
						getAccessedVirtualModelResource());
				getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_KEY, oldVirtualModel, getAccessedVirtualModel());
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, oldType, getType());
				notifyResultingTypeChanged();
			}
		}

		@Override
		public String getAccessedVirtualModelURI() {
			if (virtualModelResource != null) {
				return virtualModelResource.getURI();
			}
			if (type != null) {
				if (type.isResolved() && type.getVirtualModel() != null) {
					return type.getVirtualModel().getURI();
				}
				else {
					return type.getConceptURI();
				}
			}
			return virtualModelURI;
		}

		@Override
		public void setAccessedVirtualModelURI(String virtualModelURI) {
			if ((virtualModelURI == null && getAccessedVirtualModelURI() != null)
					|| (virtualModelURI != null && !virtualModelURI.equals(getAccessedVirtualModelURI()))) {
				String oldValue = getAccessedVirtualModelURI();
				CompilationUnitResource oldResource = getAccessedVirtualModelResource();
				VirtualModel oldVirtualModel = getAccessedVirtualModel();
				Type oldType = getType();
				this.virtualModelURI = virtualModelURI;
				this.virtualModelResource = null;
				this.type = null;
				getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODEL_URI_KEY, oldValue, getAccessedVirtualModelURI());
				getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_RESOURCE_KEY, oldResource,
						getAccessedVirtualModelResource());
				getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_KEY, oldVirtualModel, getAccessedVirtualModel());
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, oldType, getType());
				notifyResultingTypeChanged();
			}
		}

		/**
		 * Return adressed virtual model (the virtual model this model slot specifically adresses, not the one in which it is defined)
		 * 
		 * @return
		 */
		@Override
		public final VirtualModel getAccessedVirtualModel() {
			if (getAccessedVirtualModelResource() != null /*&& !getAccessedVirtualModelResource().isLoading()*/) {
				if (getAccessedVirtualModelResource().isLoaded()) {
					return getAccessedVirtualModelResource().getLoadedResourceData().getVirtualModel();
				}
				else if (!getAccessedVirtualModelResource().isLoading()) {
					try {
						return getAccessedVirtualModelResource().getResourceData().getVirtualModel();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (ResourceLoadingCancelledException e) {
						e.printStackTrace();
					} catch (FlexoException e) {
						e.printStackTrace();
					}
				}
				return null;

				// Do not load virtual model when unloaded
				// return getAccessedVirtualModelResource().getLoadedResourceData();
				/*try {
					return getAccessedVirtualModelResource().getResourceData().getVirtualModel();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}*/
			}
			if (type != null && type.isResolved()) {
				return type.getVirtualModel();
			}
			return null;
		}

		@Override
		public void setAccessedVirtualModel(VirtualModel aVirtualModel) {
			if (aVirtualModel == null) {
				setAccessedVirtualModelResource(null);
			}
			else {
				type = aVirtualModel.getVirtualModelInstanceType();
				setAccessedVirtualModelResource(aVirtualModel.getCompilationUnitResource());
			}
		}

		/**
		 * 
		 * @param msInstance
		 * @param o
		 * @return URI as String
		 */
		@Override
		public String getURIForObject(VMI resourceData, Object o) {
			logger.warning("This method should be refined by child classes");
			return null;
		}

		/**
		 * @param msInstance
		 * @param objectURI
		 * @return the Object
		 */
		@Override
		public Object retrieveObjectWithURI(VMI resourceData, String objectURI) {
			logger.warning("This method should be refined by child classes");
			return null;
		}

		@Override
		public String getModelSlotDescription() {
			return "Virtual Model conform to " + getAccessedVirtualModelURI() /*+ (isReflexiveModelSlot() ? " [reflexive]" : "")*/;
		}

		@Override
		protected String getFMLRepresentationForConformToStatement() {
			return "conformTo " + getAccessedVirtualModelURI() + " ";
		}

		@SuppressWarnings("unchecked")
		@Override
		public VirtualModelModelSlotInstance<VMI, TA> makeActorReference(VMI object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			VirtualModelModelSlotInstance<VMI, TA> returned = factory.newInstance(VirtualModelModelSlotInstance.class);
			returned.setModelSlot(this);
			returned.setFlexoConceptInstance(fci);
			returned.setVirtualModelInstanceURI(object.getURI());
			return returned;

		}

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			super.handleRequiredImports(compilationUnit);
			if (compilationUnit != null) {
				if (getAccessedVirtualModel() != null && getAccessedVirtualModel().getCompilationUnit() != null) {
					compilationUnit.ensureResourceImport(getAccessedVirtualModel().getCompilationUnit());
				}
			}
		}

		/**
		 * Build {@link CustomType} represented by supplied serialized version, asserting this type is the accessed type through this role
		 * 
		 * @param serializedType
		 * @return
		 */
		@Override
		public FlexoConceptInstanceType buildType(String serializedType) {
			return new VirtualModelInstanceType(serializedType, getVirtualModelInstanceTypeFactory());
		}

		private VirtualModelInstanceType type = VirtualModelInstanceType.UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE;

		@Override
		public Type getType() {
			if (type != null) {
				return type;
			}
			if (getAccessedVirtualModel() == null) {
				if (StringUtils.isNotEmpty(getAccessedVirtualModelURI()) && getTechnologyAdapter() != null
						&& getTechnologyAdapter().getVirtualModelInstanceTypeFactory() != null) {
					type = getTechnologyAdapter().getVirtualModelInstanceTypeFactory().makeCustomType(getAccessedVirtualModelURI());
				}
				return type;
			}
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getAccessedVirtualModel());
		}

		@Override
		public String getTypeDescription() {
			if (getAccessedVirtualModel() != null) {
				return getAccessedVirtualModel().getName();
			}
			return "VirtualModel";
		}

		/**
		 * Declare supplied type as the the accessed type through this role
		 * 
		 * @param type
		 */
		@Override
		public void setType(Type type) {
			if (type instanceof VirtualModelInstanceType) {
				CompilationUnitResource oldResource = getAccessedVirtualModelResource();
				VirtualModel oldVirtualModel = getAccessedVirtualModel();
				Type oldType = getType();
				String oldURI = getAccessedVirtualModelURI();
				virtualModelResource = null;
				virtualModelURI = null;
				this.type = (VirtualModelInstanceType) type;

				getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODEL_URI_KEY, oldURI, getAccessedVirtualModelURI());
				getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_RESOURCE_KEY, oldResource,
						getAccessedVirtualModelResource());
				getPropertyChangeSupport().firePropertyChange(ACCESSED_VIRTUAL_MODEL_KEY, oldVirtualModel, getAccessedVirtualModel());
				getPropertyChangeSupport().firePropertyChange(TYPE_KEY, oldType, getType());
				notifyResultingTypeChanged();

			}
			else {
				logger.warning("Unexpected type: " + type);
			}
		}

		private VirtualModelInstanceTypeFactory customTypeFactory;

		private VirtualModelInstanceTypeFactory getVirtualModelInstanceTypeFactory() {
			if (customTypeFactory == null) {
				customTypeFactory = new DefaultVirtualModelInstanceTypeFactory(getTechnologyAdapter()) {

					@Override
					public VirtualModel resolveVirtualModel(VirtualModelInstanceType typeToResolve) {
						// System.out.println("Resolving VirtualModel " + typeToResolve);
						if (getDeclaringCompilationUnit() != null) {
							for (ElementImportDeclaration elementImportDeclaration : getDeclaringCompilationUnit().getElementImports()) {
								if (elementImportDeclaration.isReferencedObjectLoaded()) {
									if (elementImportDeclaration.getReferencedObject() instanceof FMLCompilationUnit) {
										FMLCompilationUnit referencedCompilationUnit = (FMLCompilationUnit) elementImportDeclaration
												.getReferencedObject();
										if (referencedCompilationUnit.getVirtualModel() != null && referencedCompilationUnit
												.getVirtualModel().getURI().equals(typeToResolve.getConceptURI())) {
											if (typeToResolve == type) {
												setAccessedVirtualModel(referencedCompilationUnit.getVirtualModel());
											}
											// System.out.println("Found: " + referencedCompilationUnit.getVirtualModel());
											return referencedCompilationUnit.getVirtualModel();
										}
									}
								}
							}
						}
						// System.out.println("Not found " + typeToResolve);
						return null;

					}
				};
			}
			return customTypeFactory;
		}

	}

	@DefineValidationRule
	public static class VirtualModelIsRequired extends ValidationRule<VirtualModelIsRequired, FMLRTModelSlot<?, ?>> {
		public VirtualModelIsRequired() {
			super(FMLRTModelSlot.class, "virtual_model_is_required");
		}

		@Override
		public ValidationIssue<VirtualModelIsRequired, FMLRTModelSlot<?, ?>> applyValidation(FMLRTModelSlot<?, ?> modelSlot) {

			if (modelSlot.getAccessedVirtualModel() == null) {
				return new ValidationError<>(this, modelSlot, "fml_rt_model_slot_does_not_define_a_valid_virtual_model");
			}
			return null;
		}

	}

}
